package edu.oregonstate.joneset3.iplist.data

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okhttp3.internal.toImmutableList

val validateHostname = Regex("^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{1,63}$")
val validateMAC =      Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
val validateIP = Regex("^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}\$")

/**
 * Parceler for IPv4Address to allow it to be passed through Safe Args.
 */
object IPv4AddressParceler : Parceler<IPv4Address?> {
    override fun create(parcel: Parcel): IPv4Address? {
        val hasValue = parcel.readInt() != 0
        return if (hasValue) IPv4Address(parcel.readInt()) else null
    }

    override fun IPv4Address?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeInt(0)
        } else {
            parcel.writeInt(1)
            parcel.writeInt(this.value.toInt())
        }
    }
}

@Parcelize
@TypeParceler<IPv4Address?, IPv4AddressParceler>
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
    @Json(name = "cidrmask") val cidr: Int?,
    val mac: String?,
    var ipv4: IPv4Address? = null,
    val notes: String? = null,
    val id: Int? = null
) : Parcelable {
    fun validate(ipStr: String? = null): List<HostErrors> {
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

        // CIDR needs an IP address
        if (cidr != null &&  ipv4 == null){
            errors.add(HostErrors.NEEDS_ADDRESS)
        }

        // CIDR must be between 0 and 32
        if (cidr != null && cidr !in 0..32){
            errors.add(HostErrors.CIDR_INVALID)
        }

        if (mac != null && !validateMAC.matches(mac)){
            errors.add(HostErrors.MAC_INVALID)
        }

        if (!ipStr.isNullOrBlank()) {
            if (validateIP.matches(ipStr)) {
                ipv4 = IPAddressString(ipStr).address.toIPv4()
            } else {
                errors.add(HostErrors.IPv4_INVALID)
            }
        }

        if(mac == null && ipStr == null && hostname == null){
            errors.add(HostErrors.NO_IDENTIFIER)
        }

        return errors.toImmutableList()
    }
}

