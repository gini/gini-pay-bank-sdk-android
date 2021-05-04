package net.gini.pay.bank.pay

import android.content.Context
import net.gini.android.Gini
import net.gini.android.models.PaymentRequest
import net.gini.android.models.ResolvePaymentInput

class GiniBankPay(
    val giniApi: Gini,
) {
    private val documentManager = giniApi.documentManager

    suspend fun getPaymentRequest(id: String): PaymentRequest {
        return documentManager.getPaymentRequest(id)
    }

    suspend fun resolvePaymentRequest(requestId: String, resolvePaymentInput: ResolvePaymentInput): String {
        return documentManager.resolvePaymentRequest(requestId, resolvePaymentInput)
    }

    fun returnToBusiness(context: Context, paymentRequest: PaymentRequest) {
        context.startActivity(paymentRequest.getBusinessIntent())
    }
}