package net.gini.pay.bank.capture

import net.gini.android.capture.GiniCaptureError
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction

sealed class CaptureResult {
    class Success(val extractions: Map<String, GiniCaptureSpecificExtraction>) : CaptureResult()
    object Empty : CaptureResult()
    object Cancel : CaptureResult()
    class Error(val code: GiniCaptureError.ErrorCode, val message: String) : CaptureResult()
}