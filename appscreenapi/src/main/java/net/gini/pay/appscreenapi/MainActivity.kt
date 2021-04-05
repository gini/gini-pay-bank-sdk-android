package net.gini.pay.appscreenapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.gini.android.DocumentMetadata
import net.gini.android.GiniApiType
import net.gini.android.capture.DocumentImportEnabledFileTypes
import net.gini.android.capture.network.GiniCaptureNetworkApi
import net.gini.android.capture.network.GiniCaptureNetworkService
import net.gini.android.capture.requirements.RequirementsReport
import net.gini.android.capture.util.CancellationToken
import net.gini.pay.appscreenapi.databinding.ActivityMainBinding
import net.gini.pay.appscreenapi.util.PermissionHandler
import net.gini.pay.appscreenapi.util.SimpleSpinnerSelectListener
import net.gini.pay.bank.GiniBank
import net.gini.pay.bank.capture.CaptureConfiguration
import net.gini.pay.bank.capture.CaptureFlowContract
import net.gini.pay.bank.capture.CaptureFlowImportContract
import net.gini.pay.bank.capture.CaptureResult
import net.gini.pay.bank.capture.ResultError
import net.gini.pay.bank.network.getAccountingNetworkApi
import net.gini.pay.bank.network.getAccountingNetworkService
import net.gini.pay.bank.network.getDefaultNetworkApi
import net.gini.pay.bank.network.getDefaultNetworkService

class MainActivity : AppCompatActivity() {

    private val permissionHandler = PermissionHandler(this)
    private val captureLauncher = registerForActivityResult(CaptureFlowContract(), ::onCaptureResult)
    private val captureImportLauncher = registerForActivityResult(CaptureFlowImportContract(), ::onCaptureResult)
    private val noExtractionsLauncher = registerForActivityResult(NoExtractionContract(), ::onStartAgainResult)
    private var cancellationToken: CancellationToken? = null // should be kept across configuration changes

    private var apiType: GiniApiType = GiniApiType.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showVersions(binding)
        setViewListeners(binding)
        if (savedInstanceState == null) {
            if (isIntentActionViewOrSend(intent)) {
                startGiniCaptureSdk(intent)
            }
        }
    }

    private fun configureGiniCapture() {
        GiniBank.releaseCapture(this)
        val (networkService, networkApi) = getNetworkService()
        GiniBank.setCaptureConfiguration(
            CaptureConfiguration(
                networkService = networkService,
                networkApi = networkApi,
                documentImportEnabledFileTypes = DocumentImportEnabledFileTypes.PDF_AND_IMAGES,
                fileImportEnabled = true,
                qrCodeScanningEnabled = true,
                multiPageEnabled = true,
                flashButtonEnabled = true,
                eventTracker = GiniCaptureEventTracker
            )
        )
    }

    private fun getNetworkService(): Pair<GiniCaptureNetworkService, GiniCaptureNetworkApi> {
        val clientId = getString(R.string.gini_api_client_id)
        val clientSecret = getString(R.string.gini_api_client_secret)
        val domain = "user.gini.net"
        val documentMetadata = DocumentMetadata().apply {
            setBranchId("GiniBankExampleAndroid")
            add("AppFlow", "ScreenAPI")
        }
        return when (apiType) {
            GiniApiType.DEFAULT -> {
                val network = getDefaultNetworkService(this, clientId, clientSecret, domain, documentMetadata)
                network to getDefaultNetworkApi(network)
            }
            GiniApiType.ACCOUNTING -> {
                val network = getAccountingNetworkService(this, clientId, clientSecret, domain, documentMetadata)
                network to getAccountingNetworkApi(network)
            }
        }
    }

    private fun startGiniCaptureSdk(intent: Intent? = null) {
        lifecycleScope.launch {
            if (permissionHandler.grantPermission(Manifest.permission.CAMERA)) {
                val report = GiniBank.checkCaptureRequirements(this@MainActivity)
                if (!report.isFulfilled) {
                    showUnfulfilledRequirementsToast(report)
                }
                configureGiniCapture()

                if (intent != null) {
                    cancellationToken = GiniBank.startCaptureFlowForIntent(captureImportLauncher, this@MainActivity, intent)
                } else {
                    GiniBank.startCaptureFlow(captureLauncher)
                }
            } else {
                if (intent != null) {
                    finish()
                }
            }
        }
    }

    private fun onCaptureResult(result: CaptureResult) {
        when (result) {
            is CaptureResult.Success -> {
                startActivity(ExtractionsActivity.getStartIntent(this, result.specificExtractions))
            }
            is CaptureResult.Error -> {
                when (result.value) {
                    is ResultError.Capture ->
                        Toast.makeText(this, "Error: ${(result.value as ResultError.Capture).giniCaptureError.errorCode} ${(result.value as ResultError.Capture).giniCaptureError.message}", Toast.LENGTH_LONG).show()
                    is ResultError.FileImport ->
                        Toast.makeText(this, "Error: ${(result.value as ResultError.FileImport).code} ${(result.value as ResultError.FileImport).message}", Toast.LENGTH_LONG).show()
                }
            }
            CaptureResult.Empty -> {
                noExtractionsLauncher.launch(Unit)
            }
            CaptureResult.Cancel -> {
            }
        }
    }

    private fun onStartAgainResult(startAgain: Boolean) {
        if (startAgain) {
            GiniBank.startCaptureFlow(captureLauncher)
        }
    }

    private fun showUnfulfilledRequirementsToast(reports: RequirementsReport) {
        reports.requirementReports.joinToString(separator = "\n") { report ->
            if (!report.isFulfilled) {
                "${report.requirementId}: ${report.details}"
            } else ""
        }.also { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setViewListeners(binding: ActivityMainBinding) {
        binding.buttonStartScanner.setOnClickListener {
            startGiniCaptureSdk()
        }

        binding.giniApiTypeSpinner.onItemSelectedListener = object : SimpleSpinnerSelectListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                apiType = when (position) {
                    1 -> GiniApiType.ACCOUNTING
                    else -> GiniApiType.DEFAULT
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showVersions(binding: ActivityMainBinding) {
        binding.textGiniCaptureVersion.text = "Gini Capture SDK v${net.gini.android.capture.BuildConfig.VERSION_NAME}"
        binding.textAppVersion.text = "v${BuildConfig.VERSION_NAME}"
    }

    private fun isIntentActionViewOrSend(intent: Intent): Boolean =
        Intent.ACTION_VIEW == intent.action || Intent.ACTION_SEND == intent.action || Intent.ACTION_SEND_MULTIPLE == intent.action

    override fun onDestroy() {
        super.onDestroy()
        // cancellationToken shouldn't be canceled when activity is recreated.
        // For example cancel in ViewModel's onCleared() instead.
        cancellationToken?.cancel()
    }
}