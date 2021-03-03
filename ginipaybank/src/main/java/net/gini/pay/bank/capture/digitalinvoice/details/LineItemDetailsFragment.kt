package net.gini.pay.bank.capture.digitalinvoice.details


import android.app.Activity
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.gini.android.capture.internal.util.ActivityHelper.forcePortraitOrientationOnPhones
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.pay.bank.R
import net.gini.pay.bank.capture.digitalinvoice.LineItem
import net.gini.pay.bank.capture.digitalinvoice.ReturnReasonDialog
import net.gini.pay.bank.capture.digitalinvoice.ReturnReasonDialogResultCallback
import net.gini.pay.bank.capture.digitalinvoice.SelectableLineItem
import net.gini.pay.bank.capture.util.autoCleared
import net.gini.pay.bank.capture.util.parentFragmentManagerOrNull
import net.gini.pay.bank.databinding.GpbFragmentLineItemDetailsBinding

/**
 * Created by Alpar Szotyori on 17.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

private const val ARG_SELECTABLE_LINE_ITEM = "GPB_ARG_SELECTABLE_LINE_ITEM"
private const val ARGS_RETURN_REASONS = "GPB_ARGS_RETURN_REASONS"
private const val TAG_RETURN_REASON_DIALOG = "TAG_RETURN_REASON_DIALOG"

/**
 * When you use the Component API the `LineItemDetailsFragment` displays a line item to be edited by the user. The user can modify the
 * following:
 * - deselect the line item,
 * - edit the line item description,
 * - edit the quantity,
 * - edit the price.
 *
 * The returned line item in the [LineItemDetailsFragmentListener.onSave()] is updated to contain the user's modifications.
 *
 * You should show the `LineItemDetailsFragment` when the
 * [DigitalInvoiceFragmentListener.onEditLineItem()] is called.
 *
 * Include the `LineItemDetailsFragment` into your layout by using the [LineItemDetailsFragment.createInstance()] factory method to create
 * an instance and display it using the [androidx.fragment.app.FragmentManager].
 *
 * A [LineItemDetailsFragmentListener] instance must be available before the `LineItemDetailsFragment` is attached to an activity. Failing
 * to do so will throw an exception. The listener instance can be provided either implicitly by making the hosting Activity implement the
 * [LineItemDetailsFragmentListener] interface or explicitly by setting the listener using [LineItemDetailsFragment.listener].
 *
 * Your Activity is automatically set as the listener in [LineItemDetailsFragment.onCreate()].
 *
 * ### Customizing the Digital Invoice Screen
 *
 * See the [LineItemDetailsActivity] for details.
 */
class LineItemDetailsFragment : Fragment(), LineItemDetailsScreenContract.View, LineItemDetailsFragmentInterface {

    private var binding by autoCleared<GpbFragmentLineItemDetailsBinding>()

    override var listener: LineItemDetailsFragmentListener?
        get() = this.presenter?.listener
        set(value) {
            this.presenter?.listener = value
        }

    private var presenter: LineItemDetailsScreenContract.Presenter? = null

    private lateinit var lineItem: SelectableLineItem
    private lateinit var returnReasons: List<GiniCaptureReturnReason>

    private var descriptionTextWatcher: TextWatcher? = null
    private var quantityTextWatcher: TextWatcher? = null
    private var grossPriceTextWatcher: TextWatcher? = null

    companion object {

        /**
         * Factory method for creating a new instance of the `LineItemDetailsFragment` using the provided line item.
         *
         * **Note:** Always use this method to create new instances. The selectable line item is required and passed as fragment arguments
         * to the instance.
         *
         * @param selectableLineItem the [SelectableLineItem] to be edited
         */
        @JvmStatic
        fun createInstance(
            selectableLineItem: SelectableLineItem,
            returnReasons: List<GiniCaptureReturnReason>
        ) = LineItemDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_SELECTABLE_LINE_ITEM, selectableLineItem)
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
            lineItem = getParcelable(ARG_SELECTABLE_LINE_ITEM) ?: SelectableLineItem(
                selected = false,
                lineItem = LineItem("", "", 0, "")
            )
            returnReasons = getParcelableArrayList(ARGS_RETURN_REASONS) ?: emptyList()
        }
    }

    private fun createPresenter(activity: Activity) = LineItemDetailsScreenPresenter(
        activity, this,
        lineItem, returnReasons
    )

    private fun initListener() {
        if (activity is LineItemDetailsFragmentListener) {
            listener = activity as LineItemDetailsFragmentListener?
        } else checkNotNull(listener) {
            ("LineItemDetailsFragmentListener not set. "
                    + "You can set it with LineItemDetailsFragmentListener#setListener() or "
                    + "by making the host activity implement the LineItemDetailsFragmentListener.")
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GpbFragmentLineItemDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInputHandlers()
    }

    private fun setInputHandlers() {
        binding.gpbCheckbox.setOnCheckedChangeListener { _, isChecked ->
            presenter?.let {
                if (isChecked) {
                    it.selectLineItem()
                } else {
                    it.deselectLineItem()
                }
            }
        }
        descriptionTextWatcher = binding.gpbDescription.doAfterTextChanged {
            presenter?.setDescription(it)
        }
        quantityTextWatcher = binding.gpbQuantity.doAfterTextChanged {
            presenter?.setQuantity(
                try {
                    it.toInt()
                } catch (_: NumberFormatException) {
                    0
                }
            )
        }
        grossPriceTextWatcher = binding.gpbGrossPrice.doAfterTextChanged {
            presenter?.setGrossPrice(it)
        }
        binding.gpbSaveButton.setOnClickListener {
            presenter?.save()
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding.gpbDescription.removeTextChangedListener(descriptionTextWatcher)
        binding.gpbQuantity.removeTextChangedListener(quantityTextWatcher)
        binding.gpbGrossPrice.removeTextChangedListener(grossPriceTextWatcher)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun setPresenter(presenter: LineItemDetailsScreenContract.Presenter) {
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
    override fun showDescription(description: String) {
        binding.gpbDescription.setText(description)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showQuantity(quantity: Int) {
        binding.gpbQuantity.setText(quantity.toString())
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showGrossPrice(displayedGrossPrice: String, currency: String) {
        binding.gpbGrossPrice.setText(displayedGrossPrice)
        binding.gpbCurrency.text = currency
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showTotalGrossPrice(integralPart: String, fractionalPart: String) {
        binding.gpbGrossPriceTotalIntegralPart.text = integralPart
        binding.gpbGrossPriceTotalFractionalPart.text = fractionalPart
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun enableSaveButton() {
        binding.gpbSaveButton.isEnabled = true
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun disableSaveButton() {
        binding.gpbSaveButton.isEnabled = false
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun enableInput() {
        binding.gpbDescription.isEnabled = true
        binding.gpbQuantity.isEnabled = true
        binding.gpbGrossPrice.isEnabled = true
        binding.gpbCurrency.isEnabled = true
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun disableInput() {
        binding.gpbDescription.isEnabled = false
        binding.gpbQuantity.isEnabled = false
        binding.gpbGrossPrice.isEnabled = false
        binding.gpbCurrency.isEnabled = false
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

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun showCheckbox(selected: Boolean, quantity: Int) {
        binding.gpbCheckbox.isChecked = selected
        binding.gpbCheckbox.text =
            resources.getQuantityString(
                R.plurals.gpb_digital_invoice_line_item_details_selected_line_items,
                quantity, quantity, if (selected) resources.getString(
                    R.string.gpb_digital_invoice_line_item_details_selected
                ) else ""
            )
    }

}
