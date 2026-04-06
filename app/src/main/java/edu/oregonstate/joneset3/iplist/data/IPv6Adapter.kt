package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv6.IPv6Address

class IPv6Adapter {
    @FromJson
    fun fromJson(inString: String?): IPv6Address? {
        if (inString == null) return null
        return IPAddressString(inString).toAddress(IPAddress.IPVersion.IPV6).toIPv6()
    }

    @ToJson
    fun toJson(ipv6Address: IPv6Address?): String? {
        return ipv6Address?.value?.toString()
    }
}