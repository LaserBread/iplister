package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address


@JsonClass(generateAdapter = true)
/* A host on a network with its IP addresses and all. Holds a list of attributes that correlates to
 * the offsite database and is handled by Moshi. Has validation features.
 *
 * @param id The database ID of this host. Should only ever be assigned by the database.
 *
 * @param name The name the app uses to refer to this host. It's not the same as the hostname.
 *
 * @param ipv4 The host's IPv4 address. Credit to Sean Foley's IPAddress library
 */
data class Host(
    val name: String,
    val hostname: String?,
    val ipv4: IPv4Address?,
    @Json(name = "cidrmask") val cidr: Int,
    val mac: String?,
    val id: Int? = null
)