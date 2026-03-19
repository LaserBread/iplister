package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HostResults(
    val items: List<Host>
)