package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.data.HostErrors
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

class EditFragment : Fragment(R.layout.fragment_edit) {
    private val tag = "EditFragment"

    private lateinit var nameTIL: TextInputLayout
    private lateinit var hostnameTIL: TextInputLayout
    private lateinit var ipv4TIL: TextInputLayout
    private lateinit var cidrTIL: TextInputLayout
    private lateinit var macTIL: TextInputLayout
    private lateinit var notesTIL: TextInputLayout
    val viewModel: HostViewModel by activityViewModels()
    val args: EditFragmentArgs by navArgs()

    private var loadingDialog: AlertDialog? = null
    private var submitting: Boolean = false
    private var nameOfAddedHost: String = ""
    private var hostToAdd: Host? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTIL = view.findViewById(R.id.txtedit_name)
        hostnameTIL = view.findViewById(R.id.txtedit_hostname)
        ipv4TIL = view.findViewById(R.id.txtedit_ipv4)
        cidrTIL = view.findViewById(R.id.txtedit_cidr)
        macTIL = view.findViewById(R.id.txtedit_mac)
        notesTIL = view.findViewById(R.id.txtedit_notes)

        // Pre-fill fields with existing host data
        nameTIL.editText?.setText(args.host.name)
        hostnameTIL.editText?.setText(args.host.hostname)
        ipv4TIL.editText?.setText(args.host.ipv4?.toString())
        cidrTIL.editText?.setText(args.host.cidr?.toString())
        macTIL.editText?.setText(args.host.mac)
        notesTIL.editText?.setText(args.host.notes)

        // Intercept the back button and display a confirmation prompt
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmDiscardChanges()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_fragment_edit, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.edit_action_save -> {
                            hostToAdd = validateInputs()
                            if (hostToAdd != null) {
                                Log.d(tag, "Valid inputs")
                                showLoadingDialog()
                                submitting = true
                                nameOfAddedHost = hostToAdd!!.name
                                viewModel.update(hostToAdd!!)
                            }
                            true
                        }
                        // Intercept the Toolbar Up button
                        android.R.id.home -> {
                            confirmDiscardChanges()
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.STARTED)

        // Consolidated observer for all submission states
        viewModel.loadingStatus.observe(viewLifecycleOwner)
        {
                status ->
            if (submitting) {
                when (status) {
                    LoadingStatus.LOADING -> showLoadingDialog()
                    LoadingStatus.SUCCESS -> {
                        hideLoadingDialog()
                        viewModel.replaceOrAppendHost(hostToAdd!!)
                        submitting = false
                        Snackbar.make(
                            requireView(),
                            getString(R.string.added_host, nameOfAddedHost),
                            Snackbar.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }

                    LoadingStatus.ERROR -> {
                        hideLoadingDialog()
                        submitting = false
                        viewModel.errorMessage.value?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun confirmDiscardChanges() {
        if (submitting) return

        val initialHost = args.host
        val hasChanges = nameTIL.editText?.text?.toString() != initialHost.name ||
                hostnameTIL.editText?.text?.toString() != (initialHost.hostname ?: "") ||
                ipv4TIL.editText?.text?.toString() != (initialHost.ipv4?.toString() ?: "") ||
                cidrTIL.editText?.text?.toString() != (initialHost.cidr?.toString() ?: "") ||
                macTIL.editText?.text?.toString() != (initialHost.mac ?: "") ||
                notesTIL.editText?.text?.toString() != (initialHost.notes ?: "")

        if (hasChanges) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Unsaved changes")
                .setMessage("Discard changes?")
                .setPositiveButton("Discard") { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton("Keep editing", null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            val padding = resources.getDimensionPixelSize(R.dimen.view_host_entry_margin)
            val progressBar = ProgressBar(requireContext()).apply {
                setPadding(padding, padding, padding, padding)
            }
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.loading_dialog_title)
                .setView(progressBar)
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onDestroyView() {
        hideLoadingDialog() // Hide the dialog before returning. Stops it from living forever onscreen.
        super.onDestroyView()
    }

    private fun validateInputs(): Host? {
        // Clear previous errors
        listOf(nameTIL, hostnameTIL, ipv4TIL, cidrTIL, macTIL).forEach { it.error = null }

        val name = nameTIL.editText?.text.toString()
        val hostname = hostnameTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val ipv4String = ipv4TIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val cidr = cidrTIL.editText?.text.toString().toIntOrNull()
        val mac = macTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val notes = notesTIL.editText?.text.toString().takeIf { it.isNotBlank() }

        val host = Host(
            name = name,
            hostname = hostname,
            cidr = cidr,
            mac = mac,
            notes = notes,
            id = args.host.id // Preserve ID during edit
        )

        val errors = host.validate(ipv4String)

        // Map HostErrors to UI
        errors.forEach { error ->
            when (error) {
                HostErrors.NO_NAME -> nameTIL.error = getString(R.string.err_no_name)
                HostErrors.NAME_LONG -> nameTIL.error = getString(R.string.err_name_long)
                HostErrors.HOSTNAME_INVALID -> hostnameTIL.error =
                    getString(R.string.err_hostname_invalid)

                HostErrors.IPv4_INVALID -> ipv4TIL.error = getString(R.string.err_ipv4_invalid)
                HostErrors.CIDR_INVALID -> cidrTIL.error = getString(R.string.err_cidr_invalid)
                HostErrors.MAC_INVALID -> macTIL.error = getString(R.string.err_mac_invalid)
                HostErrors.NEEDS_ADDRESS -> cidrTIL.error =
                    getString(R.string.err_needs_address)

                HostErrors.NO_IDENTIFIER -> {
                    val msg = getString(R.string.err_no_identifier)
                    hostnameTIL.error = msg
                    ipv4TIL.error = msg
                    macTIL.error = msg
                }
            }
        }
        return if (errors.isEmpty()) host else null
    }
}
