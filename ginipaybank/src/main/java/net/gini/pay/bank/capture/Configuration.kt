package net.gini.pay.bank.capture

import net.gini.android.capture.DocumentImportEnabledFileTypes
import net.gini.android.capture.GiniCapture
import net.gini.android.capture.network.GiniCaptureNetworkApi
import net.gini.android.capture.network.GiniCaptureNetworkService
import net.gini.android.capture.onboarding.OnboardingPage
import net.gini.android.capture.tracking.EventTracker

data class CaptureConfiguration(
    val networkService: GiniCaptureNetworkService,
    val networkApi: GiniCaptureNetworkApi,
    val showOnboardingAtFirstRun: Boolean = true,
    val onboardingPages: List<OnboardingPage> = emptyList(),
    val showOnboarding: Boolean = false,
    val multiPageEnabled: Boolean = false,
    val documentImportEnabledFileTypes: DocumentImportEnabledFileTypes = DocumentImportEnabledFileTypes.NONE,
    val fileImportEnabled: Boolean = false,
    val qrCodeScanningEnabled: Boolean = false,
    val supportedFormatsHelpScreenEnabled: Boolean = true,
    val flashButtonEnabled: Boolean = false,
    val backButtonsEnabled: Boolean = true,
    val flashOnByDefault: Boolean = true,
    val eventTracker: EventTracker? = null,
)

fun GiniCapture.Builder.applyConfiguration(configuration: CaptureConfiguration): GiniCapture.Builder {
    return this.setGiniCaptureNetworkService(configuration.networkService)
        .setGiniCaptureNetworkApi(configuration.networkApi)
        .setShouldShowOnboardingAtFirstRun(configuration.showOnboardingAtFirstRun)
        .setCustomOnboardingPages(arrayListOf<OnboardingPage>().apply { configuration.onboardingPages.forEach { add(it) } })
        .setShouldShowOnboarding(configuration.showOnboarding)
        .setMultiPageEnabled(configuration.multiPageEnabled)
        .setDocumentImportEnabledFileTypes(configuration.documentImportEnabledFileTypes)
        .setFileImportEnabled(configuration.fileImportEnabled)
        .setQRCodeScanningEnabled(configuration.qrCodeScanningEnabled)
        .setSupportedFormatsHelpScreenEnabled(configuration.supportedFormatsHelpScreenEnabled)
        .setFlashButtonEnabled(configuration.flashButtonEnabled)
        .setBackButtonsEnabled(configuration.backButtonsEnabled)
        .setFlashOnByDefault(configuration.flashOnByDefault)
        .apply { configuration.eventTracker?.let { setEventTracker(it) } }
}