package net.gini.pay.bank.capture.digitalinvoice

import android.app.Activity
import net.gini.android.capture.GiniCaptureBasePresenter
import net.gini.android.capture.GiniCaptureBaseView
import net.gini.android.capture.network.model.GiniCaptureReturnReason

/**
 * Created by Alpar Szotyori on 05.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

/**
 * Internal use only.
 *
 * @suppress
 */
interface DigitalInvoiceScreenContract {

    /**
     * Internal use only.
     *
     * @suppress
     */
    interface View : GiniCaptureBaseView<Presenter> {
        fun showLineItems(lineItems: List<SelectableLineItem>)
        fun showSelectedAndTotalLineItems(selected: Int, total: Int)
        fun showAddons(addons: List<DigitalInvoiceAddon>)
        fun enablePayButton(selected: Int, total: Int)
        fun disablePayButton(selected: Int, total: Int)
        fun showSelectedLineItemsSum(integralPart: String, fractionalPart: String)
        fun showReturnReasonDialog(reasons: List<GiniCaptureReturnReason>,
                                   resultCallback: ReturnReasonDialogResultCallback)
        fun showOnboarding()
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    abstract class Presenter(activity: Activity, view: View) :
            GiniCaptureBasePresenter<View>(activity, view), DigitalInvoiceFragmentInterface {

        abstract fun selectLineItem(lineItem: SelectableLineItem)
        abstract fun deselectLineItem(lineItem: SelectableLineItem)
        abstract fun editLineItem(lineItem: SelectableLineItem)
        abstract fun userFeedbackReceived(helpful: Boolean)
        abstract fun pay()
    }
}