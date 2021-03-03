package net.gini.pay.bank.capture.util

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.gini.android.capture.AsyncCallback
import net.gini.android.capture.ImportedFileValidationException
import net.gini.pay.bank.capture.CaptureImportInput

internal fun getImportFileCallback(resultLauncher: ActivityResultLauncher<CaptureImportInput>) =
    object : AsyncCallback<Intent, ImportedFileValidationException> {
        override fun onSuccess(result: Intent) {
            resultLauncher.launch(CaptureImportInput.Start(result))
        }

        override fun onError(exception: ImportedFileValidationException?) {
            resultLauncher.launch(CaptureImportInput.Error(exception))
        }

        override fun onCancelled() {
        }
    }