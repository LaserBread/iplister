package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.textfield.TextInputLayout
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.data.HostErrors
import inet.ipaddr.IPAddressString

class EditFragment : Fragment(R.layout.fragment_edit) {
    private val tag = "AddFragment"

    private lateinit var nameTIL: TextInputLayout
    private lateinit var hostnameTIL: TextInputLayout
    private lateinit var ipv4TIL: TextInputLayout
    private lateinit var cidrTIL: TextInputLayout
    private lateinit var macTIL: TextInputLayout
    private lateinit var notesTIL: TextInputLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTIL = view.findViewById(R.id.txtedit_name)
        hostnameTIL = view.findViewById(R.id.txtedit_hostname)
        ipv4TIL = view.findViewById(R.id.txtedit_ipv4)
        cidrTIL = view.findViewById(R.id.txtedit_cidr)
        macTIL = view.findViewById(R.id.txtedit_mac)
        notesTIL = view.findViewById(R.id.txtedit_notes)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_edit, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.edit_action_save -> {
                        if (validateInputs()) {
                            // Perform save operation
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun validateInputs(): Boolean {
        // Clear previous errors
        listOf(nameTIL, hostnameTIL, ipv4TIL, cidrTIL, macTIL).forEach { it.error = null }

        val name = nameTIL.editText?.text.toString()
        val hostname = hostnameTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val ipv4String = ipv4TIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val cidr = cidrTIL.editText?.text.toString().toIntOrNull()
        val mac = macTIL.editText?.text.toString().takeIf { it.isNotBlank() }
        val notes = notesTIL.editText?.text.toString().takeIf { it.isNotBlank() }

        val ipv4 = ipv4String?.let { IPAddressString(it).address?.toIPv4() }

        val host = Host(
            name = name,
            hostname = hostname,
            ipv4 = ipv4,
            cidr = cidr,
            mac = mac,
            notes = notes
        )

        val errors = host.validate()
        
        // Map HostErrors to UI
        errors.forEach { error ->
            when (error) {
                HostErrors.NO_NAME -> nameTIL.error = "Name is required"
                HostErrors.NAME_LONG -> nameTIL.error = "Name is too long"
                HostErrors.HOSTNAME_INVALID -> hostnameTIL.error = "Invalid hostname"
                HostErrors.IPv4_INVALID -> ipv4TIL.error = "Invalid IPv4 address"
                HostErrors.CIDR_INVALID -> cidrTIL.error = "Invalid CIDR (0-32)"
                HostErrors.MAC_INVALID -> macTIL.error = "Invalid MAC address"
                else -> {}
            }
        }

        return errors.isEmpty()
    }
}
