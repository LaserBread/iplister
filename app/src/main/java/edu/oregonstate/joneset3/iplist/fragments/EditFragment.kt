package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import kotlin.getValue

class EditFragment : HostFormFragment(R.string.edited_host, R.string.loading_message_edit) {
    override val logTag = "EditFragment"

    val args: EditFragmentArgs by navArgs()

    private var updatedHost: Host? = null

    /**
     * Adds code to populate the fields with the [Host]'s current info.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill fields with existing host data
        nameTIL.editText?.setText(args.host.name)
        hostnameTIL.editText?.setText(args.host.hostname)
        ipv4TIL.editText?.setText(args.host.ipv4?.toString())
        cidrTIL.editText?.setText(args.host.cidr?.toString())
        macTIL.editText?.setText(args.host.mac)
        notesTIL.editText?.setText(args.host.notes)
        ipv6TIL.editText?.setText(args.host.ipv6?.toString())
    }

    override fun onSuccessfulDatabaseTransaction() {
        if (updatedHost == null) {
            throw IllegalStateException("Ran onSuccessfulDatabaseTransaction without setting updatedHost.")
        }
        viewModel.replaceOrAppendHost(updatedHost!!)
    }

    override fun dbInteractionFun(host: Host) {
        updatedHost = host.copy(id = args.host.id)
        viewModel.update(updatedHost!!)
    }

    override fun hasChanges(): Boolean {
        val currentName = nameTIL.editText?.text?.toString() ?: ""
        val currentHostname = hostnameTIL.editText?.text?.toString()?.takeIf { it.isNotBlank() }
        val currentIpv4 = ipv4TIL.editText?.text?.toString()?.takeIf { it.isNotBlank() }
        val currentCidr = cidrTIL.editText?.text?.toString()?.toIntOrNull()
        val currentMac = macTIL.editText?.text?.toString()?.takeIf { it.isNotBlank() }
        val currentIpv6 = ipv6TIL.editText?.text?.toString()?.takeIf { it.isNotBlank() }
        val currentNotes = notesTIL.editText?.text?.toString()?.takeIf { it.isNotBlank() }

        return currentName != args.host.name ||
                currentHostname != args.host.hostname ||
                currentIpv4 != args.host.ipv4?.toString() ||
                currentCidr != args.host.cidr ||
                currentMac != args.host.mac ||
                currentIpv6 != args.host.ipv6?.toString() ||
                currentNotes != args.host.notes
    }
}
