package edu.oregonstate.joneset3.iplist.data

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address
import inet.ipaddr.ipv6.IPv6Address
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okhttp3.internal.toImmutableList

val validateHostname = Regex("^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{1,63}$")
val validateMAC =      Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
val validateIPv4 =     Regex("^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}\$")

/**
 * Parceler for IPv4Address to allow it to be passed through Safe Args.
 *
 * I will admit, I had to get help from an AI to do this one. I did annotate what is happening
 */
object IPv4AddressParceler : Parceler<IPv4Address?> {

    // Parcelers work as a queue. The first element written is the first element read.
    // The format is as follows:

    // | VALUE is null                    | VALUE exists                            |
    // |----------------------------------|-----------------------------------------|
    // | Slot 1 (INT): 0                  | Slot 1 (INT): 1                         |
    // |                                  | Slot 2 (INT): value                     |
    // |----------------------------------|-----------------------------------------|

    override fun create(parcel: Parcel): IPv4Address? {
        // Our first element is an integer that indicates if there is a value.
        val hasValue = parcel.readInt() != 0
        // Our second element is the value.
        return if (hasValue) IPv4Address(parcel.readInt()) else null
    }

    // Writes the value to the parcel.
    override fun IPv4Address?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            // If there is no object, the first int is 0
            parcel.writeInt(0)
        } else {
            // If there is an address, the first object is 1 and the second is the address.
            parcel.writeInt(1)
            parcel.writeInt(this.value.toInt())
        }
    }
}

/**
 * Parceler for IPv6Address to allow it to be passed through Safe Args.
 *
 * I ctrl-c + ctrl-v'd this from the IPv4AddressParceler.
 */
object IPv6AddressParceler : Parceler<IPv6Address?> {

    // | VALUE is null                    | VALUE exists                            |
    // |----------------------------------|-----------------------------------------|
    // | Slot 1 (INT): 0                  | Slot 1 (INT): 1                         |
    // |                                  | Slot 2 (STR): value                     |
    // |----------------------------------|-----------------------------------------|

    override fun create(parcel: Parcel): IPv6Address? {
        val hasValue = parcel.readInt() != 0
        return if (hasValue) IPAddressString(parcel.readString()).toAddress(IPAddress.IPVersion.IPV6).toIPv6() else null
    }

    override fun IPv6Address?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeInt(0)
        } else {
            parcel.writeInt(1)
            parcel.writeString(this.value.toString())
        }
    }
}

@Parcelize
@TypeParceler<IPv4Address?, IPv4AddressParceler>
@TypeParceler<IPv6Address?, IPv6AddressParceler>
@JsonClass(generateAdapter = true)
/* A host on a network with its IP addresses and all. Holds a list of attributes that correlates to
 * the offsite database and is handled by Moshi. Has validation features.
 *
 * @param id The database ID of this host. Should only ever be assigned by the database.
 * @param name The name the app uses to refer to this host. It's not the same as the hostname.
 * @param ipv4 The host's IPv4 address.
 * @param cidr The IPv4 CIDR mask.
 * @param ipv6 The IPv6 address.
 *
 */
data class Host(
    val name: String,
    val hostname: String?,
    @Json(name = "cidrmask") val cidr: Int?,
    val mac: String?,
    var ipv4: IPv4Address? = null,
    var ipv6: IPv6Address? = null,
    val notes: String? = null,
    val id: Int? = null
) : Parcelable {
    fun validate(ipv4Str: String? = null, ipv6Str: String? = null): List<HostErrors> {
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

        // CIDR must be between 0 and 32
        if (cidr != null && cidr !in 0..32){
            errors.add(HostErrors.CIDR_INVALID)
        }

        if (mac != null && !validateMAC.matches(mac)){
            errors.add(HostErrors.MAC_INVALID)
        }

        if (!ipv4Str.isNullOrBlank()) {
            if (validateIPv4.matches(ipv4Str)) {
                ipv4 = IPAddressString(ipv4Str).address.toIPv4()
            } else {
                errors.add(HostErrors.IPv4_INVALID)
            }
        }

        // Instead of using a regex that I have no idea whether or not will work, I instead decided
        // to use the toAddress function, which asserts that an address is correct.
        if (!ipv6Str.isNullOrBlank()) {
            try{
                ipv6 = IPAddressString(ipv6Str).toAddress(IPAddress.IPVersion.IPV6).toIPv6()
            }
            catch (e: AddressStringException){
                errors.add(HostErrors.IPv6_INVALID)
            }
        }

        // CIDR needs an IP address
        if (cidr != null &&  ipv4 == null){
            errors.add(HostErrors.NEEDS_ADDRESS)
        }

        if(mac == null && ipv4Str == null && hostname == null){
            errors.add(HostErrors.NO_IDENTIFIER)
        }

        return errors.toImmutableList()
    }
}

