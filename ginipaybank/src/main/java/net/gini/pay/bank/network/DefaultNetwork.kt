package net.gini.pay.bank.network

import android.content.Context
import net.gini.android.DocumentMetadata
import net.gini.android.capture.accounting.network.GiniCaptureAccountingNetworkApi
import net.gini.android.capture.accounting.network.GiniCaptureAccountingNetworkService
import net.gini.android.capture.network.GiniCaptureDefaultNetworkApi
import net.gini.android.capture.network.GiniCaptureDefaultNetworkService

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

fun getDefaultNetworkApi(service: GiniCaptureDefaultNetworkService): GiniCaptureDefaultNetworkApi =
    GiniCaptureDefaultNetworkApi.builder()
        .withGiniCaptureDefaultNetworkService(service)
        .build()

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

fun getAccountingNetworkApi(service: GiniCaptureAccountingNetworkService): GiniCaptureAccountingNetworkApi =
    GiniCaptureAccountingNetworkApi.builder()
        .withGiniCaptureAccountingNetworkService(service)
        .build()