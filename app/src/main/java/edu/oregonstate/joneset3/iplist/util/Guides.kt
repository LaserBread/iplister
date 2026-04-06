package edu.oregonstate.joneset3.iplist.util

import android.content.Context
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.oregonstate.joneset3.iplist.R

object Guides {

    fun loadGuide(context: Context, @RawRes id: Int): Spanned {
        val text = context.resources
            .openRawResource(id)
            .bufferedReader()
            .use{ it.readText() }

        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    fun showDialog(context: Context, title: String, @RawRes guideRes: Int) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(loadGuide(context, guideRes))
            .setPositiveButton(context.getString(R.string.guide_dialog_dismiss), null)
            .show()

        dialog.findViewById<TextView>(android.R.id.message)
            ?.movementMethod = LinkMovementMethod.getInstance()
    }

    fun ipv6Guide(context: Context) =
        showDialog(context, context.getString(R.string.label_ipv6), R.raw.ipv6_help)

    fun ipv4Guide(context: Context) =
        showDialog(context, context.getString(R.string.label_ipv4), R.raw.ipv4_help)

    fun macGuide(context: Context) =
        showDialog(context, context.getString(R.string.label_mac), R.raw.mac_help)

    fun hostnameGuide(requireContext: Context) {
        showDialog(requireContext, requireContext.getString(R.string.label_hostname), R.raw.hostname_help)
    }
}