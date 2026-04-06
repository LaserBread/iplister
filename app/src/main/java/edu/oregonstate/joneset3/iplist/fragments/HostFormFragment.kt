package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.data.HostErrors
import edu.oregonstate.joneset3.iplist.util.Guides
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

/**
 * Serves as a reusable, polymorphic base for both [AddFragment] and [EditFragment] since the two reuse
 * so much code.
 *
 * @param loadingSuccess Contains the string resource of what message should be shown when the
 * database is successfully modified.
 *
 * @param loadingHeader Contains the string resource of what message should be shown while the app
 * awaits the database's response.
 */
abstract class HostFormFragment(
    @StringRes private val loadingSuccess: Int,
    @StringRes private val loadingHeader: Int
) : Fragment(R.layout.fragment_edit) {
    /**
     * The tag used for logging.
     */
    abstract val logTag: String

    protected lateinit var nameTIL: TextInputLayout
    protected lateinit var hostnameTIL: TextInputLayout
    protected lateinit var ipv4TIL: TextInputLayout
    protected lateinit var cidrTIL: TextInputLayout
    protected lateinit var macTIL: TextInputLayout
    protected lateinit var ipv6TIL: TextInputLayout
    protected lateinit var notesTIL: TextInputLayout

    protected val viewModel: HostViewModel by activityViewModels()

    private var loadingDialog: AlertDialog? = null
    private var submitting: Boolean = false
    private var nameOfModifiedHost: String = ""

    // The lazy keyword allows us to wait for the Views to be initialized before creating the list.
    protected val TILs by lazy {
        listOf(
            nameTIL,
            hostnameTIL,
            ipv4TIL,
            cidrTIL,
            macTIL,
            ipv6TIL,
            notesTIL
        )
    }

    // Overrides the base Fragment class
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initTILs()

        // Intercept the back button and display a confirmation prompt
        onBackPressed()

        // Originally, this one was going to be hellish and not very reusable, but there is only
        // one part of the code that actually changes between AddFragment and EditFragment.
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_fragment_edit, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.edit_action_save -> {
                            val hostToModify = validateInputs()
                            if (hostToModify != null) {
                                Log.d(logTag, "Valid inputs")
                                showLoadingDialog()
                                submitting = true
                                nameOfModifiedHost = hostToModify.name

                                // The only part that changes is right here. So, I decided to make
                                // this an abstract function that the children could implement.
                                dbInteractionFun(hostToModify)
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
            }, viewLifecycleOwner, Lifecycle.State.STARTED
        )

        // Consolidated observer for all submission states
        viewModel.loadingStatus.observe(viewLifecycleOwner)
        { status ->
            if (submitting) {
                when (status) {
                    LoadingStatus.LOADING -> showLoadingDialog()
                    LoadingStatus.SUCCESS -> {
                        hideLoadingDialog()
                        submitting = false
                        Snackbar.make(
                            requireView(),
                            getString(loadingSuccess, nameOfModifiedHost),
                            Snackbar.LENGTH_LONG
                        ).show()
                        onSuccessfulDatabaseTransaction()
                        Log.d(logTag,"Submitted")
                        findNavController().navigateUp()
                    }

                    LoadingStatus.ERROR -> {
                        hideLoadingDialog()
                        submitting = false

                        viewModel.errorMessage.value?.let {
                            Log.e(logTag, "Server Exception Occurred:\n${it}")
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles what happens when the OS' back button (ie, the physical or virtual one), is pressed.
     * Namely, it asks the user if they want to discard any changes they have made.
     */
    private fun onBackPressed() {
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (hasChanges()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Unsaved changes")
                        .setMessage("Discard this host entry?")
                        .setPositiveButton("Discard") { _, _ ->
                            isEnabled = false // Disable callback so next back press works
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        // Just carry on
                        .setNegativeButton("Keep editing", null)
                        .show()
                } else {
                    // If no changes, just go back normally
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    /**
     * Specifies what action the app takes to interact with [HostViewModel] and the database when submitted.
     *
     * @param host The [Host] being submitted.
     */
    protected abstract fun dbInteractionFun(host: Host)

    /**
     * Display the warning about unsaved changes to the user.
     */
    private fun confirmDiscardChanges() {
        // If we're already submitting, don't show the warning, just let it finish
        if (submitting) return

        if (hasChanges()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Unsaved changes")
                .setMessage("Discard this host entry?")
                .setPositiveButton("Discard") { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton("Keep editing", null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    /**
     * Extra code for an implementation to run when the database transaction is successful. Used by
     * [EditFragment] to update the [Host] in the [HostViewModel] locally without having to pull
     * from the database.
     */
    open fun onSuccessfulDatabaseTransaction(){}

    /**
     * Displays the loading dialog that appears after submitting a change to the database and
     * awaiting a response.
     */
    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            val padding = resources.getDimensionPixelSize(R.dimen.view_host_entry_margin)
            val progressBar = ProgressBar(requireContext()).apply {
                setPadding(padding, padding, padding, padding)
            }
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(loadingHeader)
                .setView(progressBar)
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
    }

    /**
     * Dismisses the loading dialog.
     */
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
        listOf(nameTIL, hostnameTIL, ipv4TIL, cidrTIL, macTIL, ipv6TIL).forEach { it.error = null }

        val name = nameTIL.editText?.text.toString()
        val hostname = hostnameTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val ipv4String = ipv4TIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val cidr = cidrTIL.editText?.text.toString().toIntOrNull()
        val mac = macTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val ipv6String = ipv6TIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val notes = notesTIL.editText?.text.toString().takeIf { it.isNotBlank() }

        val host = Host(
            name = name,
            hostname = hostname,
            cidr = cidr,
            mac = mac,
            notes = notes
        )

        val errors = host.validate(ipv4String, ipv6String)

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

                HostErrors.IPv6_INVALID -> ipv6TIL.error = getString(R.string.err_ipv6_invalid)
                HostErrors.NO_IDENTIFIER -> {
                    val msg = getString(R.string.err_no_identifier)
                    hostnameTIL.error = msg
                    ipv4TIL.error = msg
                    macTIL.error = msg
                    ipv6TIL.error = msg
                }
            }
        }
        return if (errors.isEmpty()) host else null
    }

    private fun initTILs() {
        // I want my own custom error message here.
        if (view == null) {
            throw IllegalStateException("Tried to initialize TILs without a View.")
        }

        // Initialize the TextInputLayouts
        nameTIL = requireView().findViewById(R.id.txtedit_name)
        hostnameTIL = requireView().findViewById(R.id.txtedit_hostname)
        ipv4TIL = requireView().findViewById(R.id.txtedit_ipv4)
        cidrTIL = requireView().findViewById(R.id.txtedit_cidr)
        macTIL = requireView().findViewById(R.id.txtedit_mac)
        ipv6TIL = requireView().findViewById(R.id.txtedit_ipv6)
        notesTIL = requireView().findViewById(R.id.txtedit_notes)


        // Set the error icons and Guides for each TIL.
        hostnameTIL.setErrorIconDrawable(R.drawable.outline_help_24)
        hostnameTIL.setErrorIconOnClickListener {
            Guides.hostnameGuide(requireContext())
        }

        ipv4TIL.setErrorIconDrawable(R.drawable.outline_help_24)
        ipv4TIL.setErrorIconOnClickListener {
            Guides.ipv4Guide(requireContext())
        }

        ipv6TIL.setErrorIconDrawable(R.drawable.outline_help_24)
        ipv6TIL.setErrorIconOnClickListener {
            Guides.ipv6Guide(requireContext())
        }

        macTIL.setErrorIconDrawable(R.drawable.outline_help_24)
        macTIL.setErrorIconOnClickListener {
            Guides.macGuide(requireContext())
        }

        // Set the name error icon to nothing. This is to avoid confusing the noninteractive
        // ! icon with the interactive ? icons.
        nameTIL.setErrorIconDrawable(R.drawable.literally_nothing)


    }

    abstract fun hasChanges():Boolean
}
