package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import edu.oregonstate.cs492.roomgithubsearch.ui.HostViewModel
import edu.oregonstate.joneset3.iplist.R
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.data.HostErrors
import edu.oregonstate.joneset3.iplist.util.Guides
import edu.oregonstate.joneset3.iplist.util.LoadingStatus

class AddFragment : HostFormFragment(R.string.added_host, R.string.loading_message_add) {
    override val logTag = "AddFragment"
    override fun dbInteractionFun(host: Host) {
        viewModel.add(host)
    }

    override fun hasChanges(): Boolean = TILs.all { it.editText?.text.isNullOrBlank() }
}
