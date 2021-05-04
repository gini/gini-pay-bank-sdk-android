package net.gini.pay.appscreenapi.pay

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.gini.android.models.PaymentRequest
import net.gini.android.models.ResolvePaymentInput
import net.gini.pay.appscreenapi.util.ResultWrapper
import net.gini.pay.appscreenapi.util.wrapToResult
import net.gini.pay.bank.pay.GiniBankPay

class PayViewModel(
    private val giniBank: GiniBankPay
) : ViewModel() {

    private val _paymentRequest = MutableStateFlow<ResultWrapper<PaymentRequest>>(ResultWrapper.Loading())
    val paymentRequest: StateFlow<ResultWrapper<PaymentRequest>> = _paymentRequest

    private val _paymentState = MutableStateFlow<ResultWrapper<Unit>>(ResultWrapper.Loading())
    val paymentState: StateFlow<ResultWrapper<Unit>> = _paymentState

    private var requestId: String? = null

    fun fetchPaymentRequest(requestId: String) {
        this.requestId = requestId
        _paymentRequest.value = ResultWrapper.Loading()
        viewModelScope.launch {
            _paymentRequest.value = wrapToResult { giniBank.getPaymentRequest(requestId) }
        }
    }

    fun onPay(paymentDetails: ResolvePaymentInput) {
        _paymentState.value = ResultWrapper.Loading()
        requestId?.let { id ->
            viewModelScope.launch {
                _paymentState.value = wrapToResult { giniBank.resolvePaymentRequest(id, paymentDetails) }
            }
        }
    }

    fun returnToBusiness(context: Context) {
        val request = paymentRequest.value
        if (request is ResultWrapper.Success)
        giniBank.returnToBusiness(context, request.value)
    }
}