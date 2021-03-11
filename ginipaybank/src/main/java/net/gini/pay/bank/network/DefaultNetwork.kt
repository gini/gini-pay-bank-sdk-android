package net.gini.pay.bank.network

import android.content.Context
import net.gini.android.DocumentMetadata
import net.gini.android.capture.accounting.network.GiniCaptureAccountingNetworkApi
import net.gini.android.capture.accounting.network.GiniCaptureAccountingNetworkService
import net.gini.android.capture.network.GiniCaptureDefaultNetworkApi
import net.gini.android.capture.network.GiniCaptureDefaultNetworkService

/**
 * Utility method to create a basic Default Network Service.
 * For more control see [GiniCaptureDefaultNetworkService].
 */
fun getDefaultNetworkService(
    context: Context,
    clientId: String,
    clientSecret: String,
    emailDomain: String,
    documentMetadata: DocumentMetadata
): GiniCaptureDefaultNetworkService =
    GiniCaptureDefaultNetworkService.builder(context)
        .setClientCredentials(clientId, clientSecret, emailDomain)
        .setDocumentMetadata(documentMetadata)
        .build()

/**
 * Utility method to create a basic Default Network Api.
 * For more details see [GiniCaptureDefaultNetworkApi].
 */
fun getDefaultNetworkApi(service: GiniCaptureDefaultNetworkService): GiniCaptureDefaultNetworkApi =
    GiniCaptureDefaultNetworkApi.builder()
        .withGiniCaptureDefaultNetworkService(service)
        .build()

/**
 * Utility method to create a basic Default Accounting Network Service.
 * For more control see [GiniCaptureAccountingNetworkService].
 */
fun getAccountingNetworkService(
    context: Context,
    clientId: String,
    clientSecret: String,
    emailDomain: String,
    documentMetadata: DocumentMetadata
): GiniCaptureAccountingNetworkService =
    GiniCaptureAccountingNetworkService.builder(context)
        .setClientCredentials(clientId, clientSecret, emailDomain)
        .setDocumentMetadata(documentMetadata)
        .build()

/**
 * Utility method to create a basic Default Accounting Network Api.
 * For more details see [GiniCaptureAccountingNetworkApi].
 */
fun getAccountingNetworkApi(service: GiniCaptureAccountingNetworkService): GiniCaptureAccountingNetworkApi =
    GiniCaptureAccountingNetworkApi.builder()
        .withGiniCaptureAccountingNetworkService(service)
        .build()