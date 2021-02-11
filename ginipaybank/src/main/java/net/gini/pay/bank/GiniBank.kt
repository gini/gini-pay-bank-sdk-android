package net.gini.pay.bank

import android.content.Context
import net.gini.android.capture.DocumentImportEnabledFileTypes
import net.gini.android.capture.GiniCapture
import net.gini.android.capture.network.GiniCaptureNetworkApi
import net.gini.android.capture.network.GiniCaptureNetworkService
import net.gini.android.capture.onboarding.OnboardingPage
import net.gini.android.capture.tracking.AnalysisScreenEvent
import net.gini.android.capture.tracking.CameraScreenEvent
import net.gini.android.capture.tracking.Event
import net.gini.android.capture.tracking.EventTracker
import net.gini.android.capture.tracking.OnboardingScreenEvent
import net.gini.android.capture.tracking.ReviewScreenEvent

data class CaptureConfiguration(
    val shouldShowOnboardingAtFirstRun: Boolean = true,
    val onboardingPages: List<OnboardingPage> = emptyList(),
    val shouldShowOnboarding: Boolean = false,
    val multiPageEnabled: Boolean = false,
    val giniCaptureNetworkService: GiniCaptureNetworkService,
    val giniCaptureNetworkApi: GiniCaptureNetworkApi,
    val documentImportEnabledFileTypes: DocumentImportEnabledFileTypes = DocumentImportEnabledFileTypes.NONE,
    val fileImportEnabled: Boolean = false,
    val qrCodeScanningEnabled: Boolean = false,
    val mIsSupportedFormatsHelpScreenEnabled: Boolean = true,
    val mFlashButtonEnabled: Boolean = false,
    val mBackButtonsEnabled: Boolean = true,
    val mIsFlashOnByDefault: Boolean = true,
    val eventTracker: EventTracker = object : EventTracker {
        override fun onOnboardingScreenEvent(event: Event<OnboardingScreenEvent>?) {
        }

        override fun onCameraScreenEvent(event: Event<CameraScreenEvent>?) {
        }

        override fun onReviewScreenEvent(event: Event<ReviewScreenEvent>?) {
        }

        override fun onAnalysisScreenEvent(event: Event<AnalysisScreenEvent>?) {
        }
    }
)

object GiniBank {

    private var giniCapture: GiniCapture? = null

    fun setCaptureConfiguration(captureConfiguration: CaptureConfiguration) {
        check(giniCapture != null) { "Gini Capture already configured. Call releaseCapture() before setting a new configuration" }
        GiniCapture.newInstance()
                // TODO apply configuration
            .build()
        giniCapture = GiniCapture.getInstance()
    }

    fun releaseCapture(context: Context) {
        GiniCapture.cleanup(context)
    }
}