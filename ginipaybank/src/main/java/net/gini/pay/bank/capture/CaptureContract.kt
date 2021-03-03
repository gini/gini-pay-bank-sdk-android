package net.gini.pay.bank.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import net.gini.android.capture.GiniCaptureError
import net.gini.android.capture.ImportedFileValidationException
import net.gini.android.capture.camera.CameraActivity
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction

class CaptureContract : ActivityResultContract<Unit, CaptureResult>() {
    override fun createIntent(context: Context, input: Unit) = Intent(
        context, CameraActivity::class.java
    )

    override fun parseResult(resultCode: Int, result: Intent?): CaptureResult {
        return internalParseResult(resultCode, result)
    }
}

class CaptureImportContract : ActivityResultContract<CaptureImportInput, CaptureResult>() {
    override fun createIntent(context: Context, input: CaptureImportInput) = when (input) {
        is CaptureImportInput.Start -> {
            input.intent
        }
        is CaptureImportInput.Error -> {
            // TODO
            Intent()
        }
    }

    override fun parseResult(resultCode: Int, result: Intent?): CaptureResult {
        return internalParseResult(resultCode, result)
    }
}

sealed class CaptureImportInput {
    data class Start(val intent: Intent): CaptureImportInput()
    data class Error(val exception: ImportedFileValidationException?): CaptureImportInput()
}

private fun internalParseResult(resultCode: Int, result: Intent?): CaptureResult {
    if (resultCode != Activity.RESULT_OK) {
        val error: GiniCaptureError? = result?.getParcelableExtra(CameraActivity.EXTRA_OUT_ERROR)
        return if (error != null) {
            CaptureResult.Error(error.errorCode, error.message)
        } else {
            CaptureResult.Cancel
        }
    }
    val bundle: Bundle? = result?.getBundleExtra(CameraActivity.EXTRA_OUT_EXTRACTIONS)
    return if (bundle == null || !pay5ExtractionsAvailable(bundle) && !epsPaymentAvailable(bundle)) {
        CaptureResult.Empty
    } else {
        CaptureResult.Success(bundle.keySet().asSequence()
            .mapNotNull { name -> bundle.getParcelable<GiniCaptureSpecificExtraction>(name)?.let { name to it } }
            .associate { it }
        )
    }
}

private fun isPay5Extraction(extractionName: String): Boolean {
    return extractionName == "amountToPay" ||
            extractionName == "bic" ||
            extractionName == "iban" ||
            extractionName == "paymentReference" ||
            extractionName == "paymentRecipient"
}

private fun pay5ExtractionsAvailable(extractionsBundle: Bundle) =
    extractionsBundle.keySet().any { key -> isPay5Extraction(key) }

private fun epsPaymentAvailable(extractionsBundle: Bundle) =
    extractionsBundle.keySet().contains("epsPaymentQRCodeUrl")
