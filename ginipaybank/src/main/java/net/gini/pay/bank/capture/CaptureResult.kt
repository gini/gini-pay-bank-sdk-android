package net.gini.pay.bank.capture

import android.content.Intent
import android.os.Bundle
import net.gini.android.capture.GiniCaptureError
import net.gini.android.capture.camera.CameraActivity
import net.gini.android.capture.internal.util.FileImportValidator
import net.gini.android.capture.network.model.GiniCaptureCompoundExtraction
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction

sealed class CaptureResult {
    class Success(
        val specificExtractions: Map<String, GiniCaptureSpecificExtraction>,
        val compoundExtractions: Map<String, GiniCaptureCompoundExtraction>,
        val returnReasons: List<GiniCaptureReturnReason>,
    ) : CaptureResult()

    object Empty : CaptureResult()
    object Cancel : CaptureResult()
    class Error(val value: ResultError) : CaptureResult()
}

sealed class ResultError {
    data class Capture(val giniCaptureError: GiniCaptureError) : ResultError()
    data class FileImport(val code: FileImportValidator.Error? = null, val message: String? = null) : ResultError()
}

fun CaptureResult.Success.toIntent(): Intent {
    return Intent().apply {
        this.putExtra(CameraActivity.EXTRA_OUT_EXTRACTIONS, Bundle().apply {
            specificExtractions.forEach { putParcelable(it.key, it.value) }
        })
        this.putExtra(CameraActivity.EXTRA_OUT_COMPOUND_EXTRACTIONS, Bundle().apply {
            compoundExtractions.forEach { putParcelable(it.key, it.value) }
        })
    }
}