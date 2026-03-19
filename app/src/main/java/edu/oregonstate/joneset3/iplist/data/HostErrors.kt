package edu.oregonstate.joneset3.iplist.data

enum class HostErrors {
    NO_NAME, // Name is missing
    NAME_LONG, // Name is too long

    NO_IDENTIFIER, // No identifier

    HOSTNAME_INVALID, // Invalid Hostname

    IPv4_INVALID, // Invalid IPv4 address

    CIDR_INVALID, // Invalid CIDR

    MAC_INVALID, // Invalid MAC Address

    NEEDS_ADDRESS, // CIDR Needs address
}