package edu.oregonstate.joneset3.iplist.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

class ListFragment : Fragment(R.layout.fragment_list) {
    private val tag = "MainActivity"
    private val viewModel: HostViewModel by activityViewModels()
    private val adapter = HostListAdapter(::onHostClick)
    private lateinit var prefs: SharedPreferences
    private lateinit var hostListRV: RecyclerView
    private lateinit var loadErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        loadingIndicator = view.findViewById(R.id.loading_indicator)

        hostListRV = view.findViewById(R.id.rv_hosts)
        hostListRV.layoutManager = LinearLayoutManager(requireContext())
        hostListRV.setHasFixedSize(true)

        hostListRV.adapter = adapter

        loadErrorTV = view.findViewById(R.id.tv_load_error)

        val noItemTitleTV = view.findViewById<TextView>(R.id.tv_no_items_head)
        val noItemBodyTV = view.findViewById<TextView>(R.id.tv_no_items_body)


        val addBtn: FloatingActionButton = view.findViewById(R.id.btn_add)
        addBtn.setOnClickListener {
            val directions = ListFragmentDirections.actionNavListFragmentToAddFragment()
            findNavController().navigate(directions)
        }

        viewModel.hosts.observe(viewLifecycleOwner) { hosts ->
            if (hosts.isNullOrEmpty()) {
                noItemTitleTV.visibility = View.VISIBLE
                noItemBodyTV.visibility = View.VISIBLE
                hostListRV.visibility = View.INVISIBLE
            } else {
                noItemTitleTV.visibility = View.INVISIBLE
                noItemBodyTV.visibility = View.INVISIBLE
                hostListRV.visibility = View.VISIBLE
                adapter.updateHostList(
                    hosts,
                    prefs.getString(
                        getString(R.string.pref_left_side),
                        getString(R.string.pref_display_ipv4)
                    )!!,
                    prefs.getString(
                        getString(R.string.pref_right_side),
                        getString(R.string.pref_display_hostname)
                    )!!
                )
            }
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                LoadingStatus.SUCCESS -> {
                    hostListRV.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.INVISIBLE
                    loadErrorTV.visibility = View.INVISIBLE
                }

                LoadingStatus.LOADING -> {
                    hostListRV.visibility = View.INVISIBLE
                    loadingIndicator.visibility = View.VISIBLE
                    loadErrorTV.visibility = View.INVISIBLE
                }

                LoadingStatus.ERROR -> {
                    hostListRV.visibility = View.INVISIBLE
                    loadingIndicator.visibility = View.INVISIBLE
                    loadErrorTV.visibility = View.VISIBLE
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            loadErrorTV.text = error
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.list_menuact_refresh -> {
                        viewModel.loadHosts()
                        true
                    }

                    R.id.list_menuact_export -> {
                        true
                    }

                    R.id.list_menuact_settings -> {
                        val directions = ListFragmentDirections.actionNavListFragmentToNavSettingsFragment()
                        findNavController().navigate(directions)
                        true
                    }

                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        viewModel.loadHosts()
    }


    override fun onStart() {
        super.onStart()

    }

    private fun onHostClick(host: Host) {
        val directions = ListFragmentDirections.actionNavListFragmentToViewFragment(host)
        findNavController().navigate(directions)
    }
}
