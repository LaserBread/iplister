package edu.oregonstate.joneset3.iplist.util

import android.content.Context
import androidx.preference.PreferenceManager
import edu.oregonstate.joneset3.iplist.data.Host
import inet.ipaddr.ipv6.IPv6Address

object IPv6Utils {
    /**
     * Returns the IPv6 address of the host as a string, formatted based on the user's preference
     * for full or shortened IPv6 addresses.
     */
    fun getFormattedIPv6(context: Context, ipv6: IPv6Address): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val showFullIPv6 = sharedPreferences.getBoolean("pref_full_ipv6", false)
        
        return if (showFullIPv6) {
            ipv6.toFullString()
        } else {
            ipv6.toString()
        }
    }
}
