package net.gini.pay.bank

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.gini.android.capture.AsyncCallback
import net.gini.android.capture.Document
import net.gini.android.capture.GiniCapture
import net.gini.android.capture.ImportedFileValidationException
import net.gini.android.capture.requirements.GiniCaptureRequirements
import net.gini.android.capture.requirements.RequirementsReport
import net.gini.android.capture.util.CancellationToken
import net.gini.pay.bank.capture.CaptureConfiguration
import net.gini.pay.bank.capture.CaptureImportInput
import net.gini.pay.bank.capture.applyConfiguration
import net.gini.pay.bank.capture.util.getImportFileCallback

object GiniBank {

    private var giniCapture: GiniCapture? = null

    fun setCaptureConfiguration(captureConfiguration: CaptureConfiguration) {
        check(giniCapture == null) { "Gini Capture already configured. Call releaseCapture() before setting a new configuration." }
        GiniCapture.newInstance()
            .applyConfiguration(captureConfiguration)
            .build()
        giniCapture = GiniCapture.getInstance()
    }

    fun releaseCapture(context: Context) {
        GiniCapture.cleanup(context)
        giniCapture = null
    }

    fun checkCaptureRequirements(context: Context): RequirementsReport = GiniCaptureRequirements.checkRequirements(context)

    fun startCaptureFlow(resultLauncher: ActivityResultLauncher<Unit>) {
        check(giniCapture != null) { "Capture feature is not configured. Call setCaptureConfiguration before starting the flow." }
        resultLauncher.launch(Unit)
    }

    fun startCaptureFlowForIntent(resultLauncher: ActivityResultLauncher<CaptureImportInput>, context: Context, intent: Intent): CancellationToken {
        giniCapture.let { capture ->
            check(capture != null) { "Capture feature is not configured. Call setCaptureConfiguration before starting the flow." }
            return capture.createIntentForImportedFiles(intent, context, getImportFileCallback(resultLauncher))
        }
    }

    fun createDocumentForImportedFiles(intent: Intent, context: Context, callback: AsyncCallback<Document, ImportedFileValidationException>) {
        giniCapture.let { capture ->
            check(capture != null) { "Capture feature is not configured. Call setCaptureConfiguration before creating the document." }
            capture.createDocumentForImportedFiles(intent, context, callback)
        }
    }
}