package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address
import okhttp3.internal.toImmutableList

val validateHostname = Regex("^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{1,63}$")
val validateMAC =      Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")

@JsonClass(generateAdapter = true)
/* A host on a network with its IP addresses and all. Holds a list of attributes that correlates to
 * the offsite database and is handled by Moshi. Has validation features.
 *
 * @param id The database ID of this host. Should only ever be assigned by the database.
 *
 * @param name The name the app uses to refer to this host. It's not the same as the hostname.
 *
 * @param ipv4 The host's IPv4 address.
 *
 * @param cidr The IPv4 CIDR mask.
 *
 */
data class Host(
    val name: String,
    val hostname: String?,
    val ipv4: IPv4Address?,
    @Json(name = "cidrmask") val cidr: Int?,
    val mac: String?,
    val notes: String? = null,
    val id: Int? = null
){
    fun validate(): List<HostErrors>{
        val errors: MutableList<HostErrors> = mutableListOf()

        // Check the name is not empty
        if (name == null || name == ""){
            errors.add(HostErrors.NO_NAME)
        }

        if (name.length > 100){
            errors.add(HostErrors.NAME_LONG)
        }

        // Check the hostname
        if (hostname != null && !validateHostname.matches(hostname)){
             errors.add(HostErrors.HOSTNAME_INVALID)
        }

        if (cidr != null && cidr !in 0..32){
            errors.add(HostErrors.CIDR_INVALID)
        }

        if (mac != null && !validateMAC.matches(mac)){
            errors.add(HostErrors.INVALID_MAC)
        }


        return errors.toImmutableList()
    }
}

