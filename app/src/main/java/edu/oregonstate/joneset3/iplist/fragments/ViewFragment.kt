package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

class ViewFragment : Fragment(R.layout.fragment_view) {
    private val tag = "MainActivity"
    private val viewModel: HostViewModel by viewModels()
    private val args: ViewFragmentArgs by navArgs()



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
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater){
                menuInflater.inflate(R.menu.menu_fragment_view, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean{
                return when (menuItem.itemId){
                    R.id.view_menuact_edit ->{
                        true
                    }
                    R.id.view_menuact_delete ->{
                        true
                    }
                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    override fun onStart() {
        super.onStart()

    }
}