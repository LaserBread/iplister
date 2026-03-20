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
import androidx.fragment.app.activityViewModels
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
    private val viewModel: HostViewModel by activityViewModels()
    private val args: ViewFragmentArgs by navArgs()

    private var loadingDialog: AlertDialog? = null
    private var deleting = false

    private lateinit var titleTV: TextView
    private lateinit var hostnameTV: TextView
    private lateinit var ipv4TV: TextView
    private lateinit var cidrTV: TextView
    private lateinit var macTV: TextView
    private lateinit var notesTV: TextView

    private lateinit var hostnameGroup: ConstraintLayout
    private lateinit var ipv4Group: ConstraintLayout
    private lateinit var macGroup: ConstraintLayout
    private lateinit var cidrGroup: Group
    private lateinit var notesGroup: ConstraintLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTV = view.findViewById<TextView>(R.id.view_title)
        hostnameTV = view.findViewById<TextView>(R.id.view_hostname)
        ipv4TV = view.findViewById<TextView>(R.id.view_ipv4)
        cidrTV = view.findViewById<TextView>(R.id.view_cidr)
        macTV = view.findViewById<TextView>(R.id.view_mac)
        notesTV = view.findViewById<TextView>(R.id.view_notes)

        hostnameGroup = view.findViewById<ConstraintLayout>(R.id.view_group_hostname)
        ipv4Group = view.findViewById<ConstraintLayout>(R.id.view_group_ipv4)
        macGroup = view.findViewById<ConstraintLayout>(R.id.view_group_mac)
        cidrGroup = view.findViewById<Group>(R.id.view_group_cidr)
        notesGroup = view.findViewById<ConstraintLayout>(R.id.view_group_notes)
        
        val host = viewModel.getHostById(args.id)
        if (host == null) {
            findNavController().navigateUp()
            return
        }


        titleTV.text = host.name

        // Check if each of hosts entries are null. If they aren't set text. Otherwise, eliminate them.
        hostnameGroup.visibility = if (host.hostname != null) View.VISIBLE else View.GONE
        hostnameTV.text = host.hostname

        ipv4Group.visibility = if (host.ipv4 != null) View.VISIBLE else View.GONE
        ipv4TV.text = host.ipv4.toString()

        cidrGroup.visibility = if (host.cidr != null) View.VISIBLE else View.GONE
        cidrTV.text = "/${host.cidr.toString()}"

        macGroup.visibility = if (host.mac != null) View.VISIBLE else View.GONE
        macTV.text = host.mac

        notesGroup.visibility = if (host.notes != null) View.VISIBLE else View.GONE
        notesTV.text = host.notes


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_view, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.view_menuact_edit -> {
                        val directions = ViewFragmentDirections.actionNavViewFragmentToEditFragment(host)
                        findNavController().navigate(directions)
                        true
                    }

                    R.id.view_menuact_delete -> {
                        confirmDelete(host)
                        true
                    }

                    android.R.id.home -> {
                        findNavController().navigateUp()
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
                            getString(R.string.deleted_host, host.name),
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
