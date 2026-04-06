package edu.oregonstate.joneset3.iplist.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.util.IPv6Utils


class HostListAdapter(
    private val onHostClick: (Host) -> Unit
) : RecyclerView.Adapter<HostListAdapter.HostViewHolder>() {
    private var hostList = listOf<Host>()

    private var _leftSide: String = "pref_display_ipv4"
    private var _rightSide: String = "pref_display_hostname"


    // A dummy object that prevents the FAB from covering up the end side of the fragment.
    val dummy = Host(
        name = "Dummy",
        hostname = null,
        ipv4 = null,
        cidr = null,
        mac = null,
        notes = null,
        id = -1
    )

    fun updateHostList(newRepoList: List<Host>?, leftSide: String, rightSide: String) {
        notifyItemRangeRemoved(0, hostList.size)

        _leftSide = leftSide
        _rightSide = rightSide

        // Insert the dummy element
        hostList = if (newRepoList != null) newRepoList + listOf(dummy) else listOf()
        notifyItemRangeInserted(0, hostList.size)
    }

    override fun getItemCount() = hostList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_host, parent, false)
        return HostViewHolder(itemView, onHostClick,leftSide = _leftSide, rightSide = _rightSide)
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        holder.bind(hostList[position])
    }

    class HostViewHolder(
        itemView: View,
        val onClick: (Host) -> Unit,
        val leftSide: String,
        val rightSide: String
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTV: TextView = itemView.findViewById(R.id.item_tv_name)
        private val leftTV: TextView = itemView.findViewById(R.id.item_tv_ipv4)
        private val rightTV: TextView = itemView.findViewById(R.id.item_tv_hostname)

        private var currentHost: Host? = null

        init {
            itemView.setOnClickListener { currentHost?.let(onClick) }
        }

        fun bind(host: Host) {
            currentHost = host

            // If this has an ID of -1, make everything on it invisible.
            // This ensures one dummy element exists so the final element isn't
            // covered by the floating action button.
            if(host.id == -1){
                this.itemView.visibility = View.INVISIBLE
            }
            else {
                this.itemView.visibility = View.VISIBLE
                leftTV.text = setTextSide(leftSide,host)
                rightTV.text = setTextSide(rightSide,host)
                nameTV.text = host.name
            }
        }

        private fun setTextSide(side: String,host: Host): String = when (side) {
            "pref_display_hostname" -> host.hostname ?: "[No hostname]"
            "pref_display_ipv4" -> host.ipv4?.toString() ?: "[No IPv4]"
            "pref_display_cidr" -> host.cidr?.toString() ?: "[No CIDR]"
            "pref_display_mac" -> host.mac ?: "[No MAC]"
            "pref_display_ipv6" -> host.ipv6?.let { IPv6Utils.getFormattedIPv6(itemView.context, it) } ?: "[No IPv6]"
            else -> ""
        }


    }
}