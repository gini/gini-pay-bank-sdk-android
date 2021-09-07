package net.gini.pay.bank.capture.digitalinvoice

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import kotlinx.coroutines.CoroutineScope
import net.gini.android.capture.internal.util.ActivityHelper.forcePortraitOrientationOnPhones
import net.gini.android.capture.network.model.GiniCaptureCompoundExtraction
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction
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
private const val ARGS_INACCURATE_EXTRACTION = "GPB_ARGS_INACCURATE_EXTRACTION"

private const val TAG_RETURN_REASON_DIALOG = "TAG_RETURN_REASON_DIALOG"
private const val TAG_WHAT_IS_THIS_DIALOG = "TAG_WHAT_IS_THIS_DIALOG"

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
 * [AnalysisFragmentListener.onExtractionsAvailable()] is called and you have validated the compound extractions using the
 * [LineItemsValidator].
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
open class DigitalInvoiceFragment : Fragment(), DigitalInvoiceScreenContract.View,
    DigitalInvoiceFragmentInterface, LineItemsAdapterListener {


    private var binding by autoCleared<GpbFragmentDigitalInvoiceBinding>()
    private var lineItemsAdapter by autoCleared<LineItemsAdapter>()

    override var listener: DigitalInvoiceFragmentListener?
        get() = this.presenter?.listener
        set(value) {
            this.presenter?.listener = value
        }

    override val viewLifecycleScope: CoroutineScope
        get() = viewLifecycleOwner.lifecycleScope

    private var presenter: DigitalInvoiceScreenContract.Presenter? = null

    private var extractions: Map<String, GiniCaptureSpecificExtraction> = emptyMap()
    private var compoundExtractions: Map<String, GiniCaptureCompoundExtraction> = emptyMap()
    private var returnReasons: List<GiniCaptureReturnReason> = emptyList()
    private var isInaccurateExtraction: Boolean = false

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
            returnReasons: List<GiniCaptureReturnReason>,
        ) = DigitalInvoiceFragment().apply {
            arguments = Bundle().apply {
                putBundle(ARGS_EXTRACTIONS, Bundle().apply {
                    extractions.forEach { putParcelable(it.key, it.value) }
                })
                putBundle(ARGS_COMPOUND_EXTRACTIONS, Bundle().apply {
                    compoundExtractions.forEach { putParcelable(it.key, it.value) }
                })
                putParcelableArrayList(ARGS_RETURN_REASONS, ArrayList(returnReasons))

                val isInaccurateExtraction = extractions["amountsAreConsistent"]?.let {
                    it.value == "false"
                } ?: true

                putBoolean(ARGS_INACCURATE_EXTRACTION, isInaccurateExtraction)
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
        createPresenter(activity, savedInstanceState)
        initListener()
    }

    private fun readArguments() {
        arguments?.run {
            getBundle(ARGS_EXTRACTIONS)?.run {
                extractions =
                    keySet().map { it to getParcelable<GiniCaptureSpecificExtraction>(it)!! }
                        .toMap()
            }
            getBundle(ARGS_COMPOUND_EXTRACTIONS)?.run {
                compoundExtractions =
                    keySet().map { it to getParcelable<GiniCaptureCompoundExtraction>(it)!! }
                        .toMap()
            }
            returnReasons = getParcelableArrayList(ARGS_RETURN_REASONS) ?: emptyList()

            isInaccurateExtraction = getBoolean(ARGS_INACCURATE_EXTRACTION, false)
        }
    }

    private fun createPresenter(activity: Activity, savedInstanceState: Bundle?) =
        DigitalInvoiceScreenPresenter(
            activity,
            this,
            extractions,
            compoundExtractions,
            returnReasons,
            isInaccurateExtraction,
            savedInstanceState,
        )

    override fun onSaveInstanceState(outState: Bundle) {
        presenter?.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        presenter?.onViewCreated()
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
        lineItemsAdapter = LineItemsAdapter(this)
        activity?.let {
            binding.lineItems.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = lineItemsAdapter
                setHasFixedSize(true)
            }
        }
    }

    private fun setInputHandlers() {
    }

    override fun payButtonClicked() {
        presenter?.pay()
    }

    override fun skipButtonClicked() {
        presenter?.skip()
    }

    override fun addNewArticle() {
        presenter?.addNewArticle()
    }

    override fun removeLineItem(lineItem: SelectableLineItem) {
        presenter?.removeLineItem(lineItem)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showLineItems(
        lineItems: List<SelectableLineItem>,
        isInaccurateExtraction: Boolean
    ) {
        lineItemsAdapter.apply {
            this.isInaccurateExtraction = isInaccurateExtraction
            this.lineItems = lineItems
        }
    }

    private lateinit var smoothScroller: SmoothScroller
    private val scrollListener = object : SmoothScroller.SmoothScrollerListener {
        override fun didStop() {
            scrollList(true)
        }

    }

    /**
     * header and footer are counted as aprox. 3 items
     * in order to have same time spent on scrolling different size views
     */
    override fun animateListScroll() {
        val itemCount =
            3 + (if (lineItemsAdapter.isInaccurateExtraction) 3 else 0) + lineItemsAdapter.lineItems.size
        smoothScroller = SmoothScroller(
            requireContext(),
            itemCount,
            scrollListener
        )
        scrollList(false)
    }

    private fun scrollList(toTop: Boolean) {
        val delay: Long = if (toTop) 350 else 200
        binding.lineItems.postDelayed(Runnable {
            smoothScroller.targetPosition = if (toTop) 0 else lineItemsAdapter.itemCount
            (binding.lineItems.layoutManager as? LinearLayoutManager)?.startSmoothScroll(
                smoothScroller
            )
        }, delay)

    }

    internal class SmoothScroller(
        context: Context,
        private val itemsCount: Int,
        private val listener: SmoothScrollerListener
    ) : LinearSmoothScroller(context) {
        private val totalScrollTime = 2400f

        interface SmoothScrollerListener {
            fun didStop()
        }

        override fun onStop() {
            super.onStop()
            if (targetPosition > 0) {
                listener.didStop()
            }
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return totalScrollTime / (itemsCount * displayMetrics.densityDpi)
        }

    }

    override fun updateFooterDetails(data: DigitalInvoiceScreenContract.FooterDetails) {
        lineItemsAdapter.footerDetails = data
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showAddons(addons: List<DigitalInvoiceAddon>) {
        lineItemsAdapter.addons = addons
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
        parentFragmentManagerOrNull()?.let { fragmentManager ->
            ReturnReasonDialog.createInstance(reasons).run {
                callback = resultCallback
                show(fragmentManager, TAG_RETURN_REASON_DIALOG)
            }
        }
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
        parentFragmentManagerOrNull()?.let { fragmentManager ->
            WhatIsThisDialog.createInstance().run {
                callback = { isHelpful ->
                    if (isHelpful != null) {
                        presenter?.userFeedbackReceived(isHelpful)
                    }
                }
                show(fragmentManager, TAG_WHAT_IS_THIS_DIALOG)
            }
        }
    }

    override fun updateLineItem(selectableLineItem: SelectableLineItem) {
        presenter?.updateLineItem(selectableLineItem)
    }
}
