package net.gini.pay.bank.capture.digitalinvoice

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import net.gini.android.capture.internal.util.ActivityHelper.forcePortraitOrientationOnPhones
import net.gini.android.capture.network.model.GiniCaptureCompoundExtraction
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction
import net.gini.pay.bank.R
import net.gini.pay.bank.capture.digitalinvoice.onboarding.DigitalInvoiceOnboardingFragment
import net.gini.pay.bank.capture.digitalinvoice.onboarding.DigitalInvoiceOnboardingFragmentListener
import net.gini.pay.bank.capture.util.autoCleared
import net.gini.pay.bank.capture.util.parentFragmentManagerOrNull
import net.gini.pay.bank.databinding.GpbFragmentDigitalInvoiceBinding

/**
 * Created by Alpar Szotyori on 05.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

private const val ARGS_EXTRACTIONS = "GPB_ARGS_EXTRACTIONS"
private const val ARGS_COMPOUND_EXTRACTIONS = "GPB_ARGS_COMPOUND_EXTRACTIONS"
private const val ARGS_RETURN_REASONS = "GPB_ARGS_RETURN_REASONS"

private const val TAG_RETURN_REASON_DIALOG = "TAG_RETURN_REASON_DIALOG"
private const val TAG_WHAT_IS_THIS_DIALOG = "TAG_WHAT_IS_THIS_DIALOG"
private const val TAG_ONBOARDING = "TAG_ONBOARDING"

/**
 * When you use the Component API the `DigitalInvoiceFragment` displays the line items extracted from an invoice document and their total
 * price. The user can deselect line items which should not be paid for and also edit the quantity, price or description of each line item. The
 * total price is always updated to include only the selected line items.
 *
 * The returned extractions in the [DigitalInvoiceFragmentListener.onPayInvoice()] are updated to include the user's midifications:
 * - "amountToPay" is updated to contain the sum of the selected line items' prices,
 * - the line items are updated according to the user's modifications.
 *
 * You should show the `DigitalInvoiceFragment` when the
 * [AnalysisFragmentListener.onProceedToReturnAssistant()] is called.
 *
 * Include the `DigitalInvoiceFragment` into your layout by using the [DigitalInvoiceFragment.createInstance()] factory method to create
 * an instance and display it using the [androidx.fragment.app.FragmentManager].
 *
 * A [DigitalInvoiceFragmentListener] instance must be available before the `DigitalInvoiceFragment` is attached to an Activity. Failing to
 * do so will throw an exception. The listener instance can be provided either implicitly by making the hosting Activity implement the
 * [DigitalInvoiceFragmentListener] interface or explicitly by setting the listener using [DigitalInvoiceFragment.listener].
 *
 * Your Activity is automatically set as the listener in [DigitalInvoiceFragment.onCreate()].
 *
 * ### Customizing the Digital Invoice Screen
 *
 * See the [DigitalInvoiceActivity] for details.
 */
class DigitalInvoiceFragment : Fragment(), DigitalInvoiceScreenContract.View,
    DigitalInvoiceFragmentInterface, LineItemsAdapterListener, DigitalInvoiceOnboardingFragmentListener {

    private var binding by autoCleared<GpbFragmentDigitalInvoiceBinding>()

    override var listener: DigitalInvoiceFragmentListener?
        get() = this.presenter?.listener
        set(value) {
            this.presenter?.listener = value
        }

    private var presenter: DigitalInvoiceScreenContract.Presenter? = null

    private var extractions: Map<String, GiniCaptureSpecificExtraction> = emptyMap()
    private var compoundExtractions: Map<String, GiniCaptureCompoundExtraction> = emptyMap()
    private var returnReasons: List<GiniCaptureReturnReason> = emptyList()

    companion object {

        /**
         * Factory method for creating a new instance of the `DigitalInvoiceFragment` using the provided extractions.
         *
         * **Note:** Always use this method to create new instances. The extractions are required and passed as fragment arguments to the
         * instance.
         *
         * @param extractions a map of [GiniCaptureSpecificExtraction]s
         * @param compoundExtractions a map of [GiniCaptureCompoundExtraction]s
         */
        @JvmStatic
        fun createInstance(
            extractions: Map<String, GiniCaptureSpecificExtraction>,
            compoundExtractions: Map<String, GiniCaptureCompoundExtraction>,
            returnReasons: List<GiniCaptureReturnReason>
        ) = DigitalInvoiceFragment().apply {
            arguments = Bundle().apply {
                putBundle(ARGS_EXTRACTIONS, Bundle().apply {
                    extractions.forEach { putParcelable(it.key, it.value) }
                })
                putBundle(ARGS_COMPOUND_EXTRACTIONS, Bundle().apply {
                    compoundExtractions.forEach { putParcelable(it.key, it.value) }
                })
                putParcelableArrayList(ARGS_RETURN_REASONS, ArrayList(returnReasons))
            }
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this.activity
        checkNotNull(activity) {
            "Missing activity for fragment."
        }
        forcePortraitOrientationOnPhones(activity)
        readArguments()
        createPresenter(activity)
        initListener()
    }

    private fun readArguments() {
        arguments?.run {
            getBundle(ARGS_EXTRACTIONS)?.run {
                extractions = keySet().map { it to getParcelable<GiniCaptureSpecificExtraction>(it)!! }.toMap()
            }
            getBundle(ARGS_COMPOUND_EXTRACTIONS)?.run {
                compoundExtractions = keySet().map { it to getParcelable<GiniCaptureCompoundExtraction>(it)!! }.toMap()
            }
            returnReasons = getParcelableArrayList(ARGS_RETURN_REASONS) ?: emptyList()
        }
    }

    private fun createPresenter(activity: Activity) =
        DigitalInvoiceScreenPresenter(activity, this, extractions, compoundExtractions, returnReasons)

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GpbFragmentDigitalInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setInputHandlers()
    }

    private fun initListener() {
        if (activity is DigitalInvoiceFragmentListener) {
            listener = activity as DigitalInvoiceFragmentListener?
        } else checkNotNull(listener) {
            ("MultiPageReviewFragmentListener not set. "
                    + "You can set it with MultiPageReviewFragment#setListener() or "
                    + "by making the host activity implement the MultiPageReviewFragmentListener.")
        }
    }

    private fun initRecyclerView() {
        activity?.let {
            binding.gpbLineItems.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = LineItemsAdapter(it, this@DigitalInvoiceFragment)
            }
        }
    }

    private fun setInputHandlers() {
        binding.gpbPayButton.setOnClickListener {
            presenter?.pay()
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showLineItems(lineItems: List<SelectableLineItem>) {
        (binding.gpbLineItems.adapter as LineItemsAdapter?)?.lineItems = lineItems
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showSelectedAndTotalLineItems(selected: Int, total: Int) {
        (binding.gpbLineItems.adapter as LineItemsAdapter?)?.selectedAndTotalItems = "${selected}/${total}"
    }

    override fun showAddons(addons: List<DigitalInvoiceAddon>) {
        (binding.gpbLineItems.adapter as LineItemsAdapter?)?.addons = addons
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun enablePayButton(selected: Int, total: Int) {
        binding.gpbPayButton.isEnabled = true
        updatePayButtonTitle(selected, total)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun disablePayButton(selected: Int, total: Int) {
        binding.gpbPayButton.isEnabled = false
        updatePayButtonTitle(selected, total)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showSelectedLineItemsSum(integralPart: String, fractionalPart: String) {
        (binding.gpbLineItems.adapter as LineItemsAdapter?)?.totalGrossPriceIntegralAndFractionalParts =
            Pair(integralPart, fractionalPart)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showReturnReasonDialog(
        reasons: List<GiniCaptureReturnReason>,
        resultCallback: ReturnReasonDialogResultCallback
    ) {
        parentFragmentManagerOrNull()?.let { fm ->
            ReturnReasonDialog.createInstance(reasons).run {
                callback = resultCallback
                show(fm, TAG_RETURN_REASON_DIALOG)
            }
        }
    }

    override fun showOnboarding() {
        (view?.parent as? View)?.let { container ->
            parentFragmentManagerOrNull()?.let { fm ->
                fm.beginTransaction().run {
                    val onboarding = DigitalInvoiceOnboardingFragment.createInstance().apply {
                        listener = this@DigitalInvoiceFragment
                    }
                    add(container.id, onboarding, TAG_ONBOARDING)
                    commit()
                }
            }
        }
    }

    private fun updatePayButtonTitle(selected: Int, total: Int) {
        @SuppressLint("SetTextI18n")
        binding.gpbPayButton.text = resources.getString(R.string.gpb_digital_invoice_pay, selected, total)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun setPresenter(presenter: DigitalInvoiceScreenContract.Presenter) {
        this.presenter = presenter
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onStart() {
        super.onStart()
        presenter?.start()
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onStop() {
        super.onStop()
        presenter?.stop()
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onLineItemClicked(lineItem: SelectableLineItem) {
        presenter?.editLineItem(lineItem)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onLineItemSelected(lineItem: SelectableLineItem) {
        presenter?.selectLineItem(lineItem)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onLineItemDeselected(lineItem: SelectableLineItem) {
        presenter?.deselectLineItem(lineItem)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onWhatIsThisButtonClicked() {
        parentFragmentManagerOrNull()?.let { fm ->
            WhatIsThisDialog.createInstance().run {
                callback = { isHelpful ->
                    if (isHelpful != null) {
                        presenter?.userFeedbackReceived(isHelpful)
                    }
                }
                show(fm, TAG_WHAT_IS_THIS_DIALOG)
            }
        }
    }

    override fun updateLineItem(selectableLineItem: SelectableLineItem) {
        presenter?.updateLineItem(selectableLineItem)
    }

    override fun onCloseOnboarding() {
        parentFragmentManagerOrNull()?.let { fm ->
            (fm.findFragmentByTag(TAG_ONBOARDING) as? DigitalInvoiceOnboardingFragment)?.let { onboardingFragment ->
                onboardingFragment.listener = null
                fm.beginTransaction().run {
                    remove(onboardingFragment)
                    commit()
                }
            }
        }
    }

}
