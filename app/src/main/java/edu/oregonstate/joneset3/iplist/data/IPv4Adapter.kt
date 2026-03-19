package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import inet.ipaddr.ipv4.IPv4Address

class IPv4Adapter {
    @FromJson
    fun fromJson(ipLong: Long?): IPv4Address? {
        return ipLong?.let {
            IPv4Address(it.toInt())
        }
    }

    @ToJson
    fun toJson(ipv4Address: IPv4Address?): Long? {
        return ipv4Address?.value?.toLong()?.let {
            // Ensure we return an unsigned-style Long
            if (it < 0) it + (1L shl 32) else it
        }
    }
}