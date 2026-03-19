package edu.oregonstate.joneset3.iplist.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host


class HostListAdapter(
    private val onHostClick: (Host) -> Unit
) : RecyclerView.Adapter<HostListAdapter.HostViewHolder>() {
    private var hostList = listOf<Host>()
    val dummy = Host(
        name = "Dummy",
        hostname = null,
        ipv4 = null,
        cidr = null,
        mac = null,
        notes = null,
        id = -1
    )

    fun updateHostList(newRepoList: List<Host>?) {
        notifyItemRangeRemoved(0, hostList.size)

        // Insert the dummy element
        hostList = if (newRepoList != null) newRepoList + listOf(dummy) else listOf()
        notifyItemRangeInserted(0, hostList.size)
    }

    override fun getItemCount() = hostList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_host, parent, false)
        return HostViewHolder(itemView, onHostClick)
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        holder.bind(hostList[position])
    }

    class HostViewHolder(
        itemView: View,
        val onClick: (Host) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTV: TextView = itemView.findViewById(R.id.item_tv_name)
        private val ipv4TV: TextView = itemView.findViewById(R.id.item_tv_ipv4)
        private val hostnameTV: TextView = itemView.findViewById(R.id.item_tv_hostname)

        private var currentHost: Host? = null

        init {
            itemView.setOnClickListener { currentHost?.let(onClick) }
        }

        fun bind(host: Host) {
            currentHost = host

            // If this has an ID of -1, make everything invisible.
            // This ensures one dummy element exists so the final element isn't
            // covered by the floating action button.
            if(host.id == -1){
                this.itemView.visibility = View.INVISIBLE
            }
            else {
                ipv4TV.text = if (host.ipv4 != null) host.ipv4.toString() else ""
                hostnameTV.text = host.hostname ?: "No Hostname"
                nameTV.text = host.name
            }
        }
    }
}