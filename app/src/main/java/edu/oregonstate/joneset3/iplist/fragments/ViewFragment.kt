package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

class ViewFragment : Fragment(R.layout.fragment_view) {
    private val tag = "MainActivity"
    private val viewModel: HostViewModel by viewModels()
    private val args: ViewFragmentArgs by navArgs()

    private var loadingDialog: AlertDialog? = null
    private var deleting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTV = view.findViewById<TextView>(R.id.view_title)
        val hostnameTV = view.findViewById<TextView>(R.id.view_hostname)
        val ipv4TV = view.findViewById<TextView>(R.id.view_ipv4)
        val cidrTV = view.findViewById<TextView>(R.id.view_cidr)
        val macTV = view.findViewById<TextView>(R.id.view_mac)
        val notesTV = view.findViewById<TextView>(R.id.view_notes)

        val hostnameGroup = view.findViewById<ConstraintLayout>(R.id.view_group_hostname)
        val ipv4Group = view.findViewById<ConstraintLayout>(R.id.view_group_ipv4)
        val macGroup = view.findViewById<ConstraintLayout>(R.id.view_group_mac)
        val cidrGroup = view.findViewById<Group>(R.id.view_group_cidr)
        val notesGroup = view.findViewById<ConstraintLayout>(R.id.view_group_notes)

        titleTV.text = args.host.name

        // Check if each of hosts entries are null. If they aren't set text. Otherwise, eliminate them.
        hostnameGroup.visibility = if (args.host.hostname != null) View.VISIBLE else View.GONE
        hostnameTV.text = args.host.hostname

        ipv4Group.visibility = if (args.host.ipv4 != null) View.VISIBLE else View.GONE
        ipv4TV.text = args.host.ipv4.toString()

        cidrGroup.visibility = if (args.host.cidr != null) View.VISIBLE else View.GONE
        cidrTV.text = "/${args.host.cidr.toString()}"

        macGroup.visibility = if (args.host.mac != null) View.VISIBLE else View.GONE
        macTV.text = args.host.mac

        notesGroup.visibility = if (args.host.notes != null) View.VISIBLE else View.GONE
        notesTV.text = args.host.notes


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_view, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.view_menuact_edit -> {
                        true
                    }

                    R.id.view_menuact_delete -> {
                        confirmDelete(args.host)
                        true
                    }

                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        viewModel.loadingStatus.observe(viewLifecycleOwner)
        { status ->
            if (deleting) {
                when (status) {
                    LoadingStatus.LOADING -> showLoadingDialog()
                    LoadingStatus.SUCCESS -> {
                        hideLoadingDialog()
                        deleting = false
                        Snackbar.make(
                            requireView(),
                            getString(R.string.deleted_host, args.host.name),
                            Snackbar.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }

                    LoadingStatus.ERROR -> {
                        hideLoadingDialog()
                        deleting = false
                        viewModel.errorMessage.value?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }

        }
    }

    // Warn and ask the user if they wish to delete this host.
    private fun confirmDelete(hostToDelete: Host) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete ${hostToDelete.name}?")
            .setMessage("Are you sure you want to delete ${hostToDelete.name}")
            .setPositiveButton("Delete") { _, _ ->
                deleting = true
                showLoadingDialog()
                viewModel.delete(hostToDelete)
            }
            .setNegativeButton("Cancel", null)
            .show()


    }

    override fun onStart() {
        super.onStart()

    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            val padding = resources.getDimensionPixelSize(R.dimen.view_host_entry_margin)
            val progressBar = ProgressBar(requireContext()).apply {
                setPadding(padding, padding, padding, padding)
            }
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.loading_message_delete))
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
}