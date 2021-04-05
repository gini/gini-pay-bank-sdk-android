package net.gini.pay.bank.capture.digitalinvoice

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import net.gini.pay.bank.R
import com.google.android.material.card.MaterialCardView
import java.util.Collections.emptyList
import net.gini.pay.bank.capture.digitalinvoice.ViewType.*
import net.gini.pay.bank.capture.digitalinvoice.ViewType.LineItem
import net.gini.pay.bank.databinding.GpbItemDigitalInvoiceAddonBinding
import net.gini.pay.bank.databinding.GpbItemDigitalInvoiceFooterBinding
import net.gini.pay.bank.databinding.GpbItemDigitalInvoiceHeaderBinding
import net.gini.pay.bank.databinding.GpbItemDigitalInvoiceLineItemBinding

/**
 * Created by Alpar Szotyori on 11.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

/**
 * Internal use only.
 *
 * @suppress
 */
internal interface LineItemsAdapterListener {
    fun onLineItemClicked(lineItem: SelectableLineItem)
    fun onLineItemSelected(lineItem: SelectableLineItem)
    fun onLineItemDeselected(lineItem: SelectableLineItem)
    fun onWhatIsThisButtonClicked()
    fun payButtonClicked()
    fun skipButtonClicked()
}

/**
 * Internal use only.
 *
 * @suppress
 */
internal class LineItemsAdapter(context: Context, val listener: LineItemsAdapterListener) :
    RecyclerView.Adapter<ViewHolder<*>>() {


    var lineItems: List<SelectableLineItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var addons: List<DigitalInvoiceAddon> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var isInaccurateExtraction: Boolean = false

    var footerDetails: DigitalInvoiceScreenContract.FooterDetails? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val layoutInflater = LayoutInflater.from(context)

    private val footerButtonClickListener = View.OnClickListener {
        listener.payButtonClicked()
    }
    private val footerSkipButtonClickListener = View.OnClickListener {
        listener.skipButtonClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeId: Int): ViewHolder<*> {
        val viewHolder =
            ViewHolder.forViewTypeId(viewTypeId, layoutInflater, parent)
        if (viewHolder is ViewHolder.FooterViewHolder) {
            viewHolder.payButton.setOnClickListener(footerButtonClickListener)
            viewHolder.skipButton.setOnClickListener(footerSkipButtonClickListener)
        }

        if (viewHolder is ViewHolder.HeaderViewHolder) {
            viewHolder.skipButton.setOnClickListener(footerSkipButtonClickListener)
        }

        return viewHolder
    }


    override fun getItemCount(): Int =
        lineItems.size + addons.size + if (isInaccurateExtraction) 2 else 1

    private fun footerPosition() =
        lineItems.size + addons.size + if (isInaccurateExtraction) 1 else 0

    private fun addonsRange() =
        (lineItems.size + 1)..(lineItems.size + addons.size + if (isInaccurateExtraction) 1 else 0)

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> if (isInaccurateExtraction) Header.id else LineItem.id
        footerPosition() -> Footer.id
        in addonsRange() -> Addon.id
        else -> LineItem.id
    }

    override fun onBindViewHolder(viewHolder: ViewHolder<*>, position: Int) {
        when (viewHolder) {
            is ViewHolder.HeaderViewHolder -> {
                viewHolder.listener = listener
                viewHolder.bind(Unit)
            }
            is ViewHolder.LineItemViewHolder -> {
                val index = if (isInaccurateExtraction) position - 1 else position
                lineItems.getOrNull(index)?.let {
                    viewHolder.listener = listener
                    viewHolder.bind(it, lineItems, index)
                }
            }
            is ViewHolder.AddonViewHolder -> {
                addonForPosition(position, addons, lineItems)?.let {
                    viewHolder.bind(it, addons)
                }
            }
            is ViewHolder.FooterViewHolder -> {
                footerDetails?.also {
                    viewHolder.bind(it)
                }
            }
        }
    }

    override fun onViewRecycled(viewHolder: ViewHolder<*>) {
        viewHolder.unbind()
    }
}

@JvmSynthetic
internal fun addonForPosition(
    position: Int,
    addons: List<DigitalInvoiceAddon>,
    lineItems: List<SelectableLineItem>
): DigitalInvoiceAddon? =
    addons.getOrElse(position - lineItems.size - 1) { null }

/**
 * Internal use only.
 *
 * @suppress
 */
internal sealed class ViewType {
    internal abstract val id: Int

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal object Header : ViewType() {
        override val id: Int = 1
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal object LineItem : ViewType() {
        override val id: Int = 2
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal object Addon : ViewType() {
        override val id: Int = 3
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal object Footer : ViewType() {
        override val id: Int = 4
    }

    internal companion object {
        fun from(viewTypeId: Int): ViewType = when (viewTypeId) {
            1 -> Header
            2 -> LineItem
            3 -> Addon
            4 -> Footer
            else -> throw IllegalStateException("Unknow adapter view type id: $viewTypeId")
        }
    }
}

/**
 * Internal use only.
 *
 * @suppress
 */
internal sealed class ViewHolder<in T>(itemView: View, val viewType: ViewType) :
    RecyclerView.ViewHolder(itemView) {

    internal abstract fun bind(data: T, allData: List<T>? = null, dataIndex: Int? = null)

    internal abstract fun unbind()

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class HeaderViewHolder(
        itemView: GpbItemDigitalInvoiceHeaderBinding
    ) :
        ViewHolder<Unit>(itemView.root, Header) {
        internal var listener: LineItemsAdapterListener? = null
        val skipButton = itemView.gpbHeaderButton2
        private val infoTitleText = itemView.gpbHeaderTitle
        private val rootMotionLayout = itemView.root
        private val collapseButton = itemView.gpbCollapseButton
        private val okButton = itemView.gpbHeaderButton1

        private val toggleClickListener = View.OnClickListener {
            when (rootMotionLayout.currentState) {
                R.id.end -> {
                    rootMotionLayout.transitionToState(R.id.start)
                }
                else -> {
                    rootMotionLayout.transitionToState(R.id.end)
                }
            }
        }

        private val transitionListener = object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                rootMotionLayout.requestLayout()
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                rootMotionLayout.requestLayout()
            }

            override fun onTransitionTrigger(
                p0: MotionLayout?,
                p1: Int,
                p2: Boolean,
                p3: Float
            ) {
            }

        }

        override fun bind(data: Unit, allData: List<Unit>?, dataIndex: Int?) {
            rootMotionLayout.setTransitionListener(transitionListener)
            collapseButton.setOnClickListener(toggleClickListener)
            okButton.setOnClickListener(toggleClickListener)
            infoTitleText.setOnClickListener(toggleClickListener)
        }

        override fun unbind() {
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class LineItemViewHolder(itemView: GpbItemDigitalInvoiceLineItemBinding) :
        ViewHolder<SelectableLineItem>(itemView.root, LineItem) {
        private val strokeView = itemView.gbpStrokeBackgroundView
        private val checkbox: SwitchCompat = itemView.gbpSwitch
        private val description: TextView = itemView.gpbDescription
        private val indexLabel: TextView = itemView.gpbItemIndexLabel

        //        private val quantityLabel: TextView = itemView.gpbQuantityLabel
        private val quantity: TextView = itemView.gpbQuantity
        private val edit: Button = itemView.gpbEdit
        private val priceIntegralPart: TextView = itemView.gpbGrossPriceIntegralPart
        private val priceFractionalPart: TextView = itemView.gpbGrossPriceFractionalPart
        internal var listener: LineItemsAdapterListener? = null

        override fun bind(
            data: SelectableLineItem,
            allData: List<SelectableLineItem>?,
            dataIndex: Int?
        ) {
            if (data.selected) {
                enable()
            } else {
                disable()
            }
            val articleIndex = (dataIndex ?: 0) + 1
            indexLabel.text = indexLabel.resources.getString(
                R.string.gpb_digital_invoice_line_item_index,
                articleIndex,
                allData?.size ?: 0
            )
            checkbox.isChecked = data.selected
//            data.reason?.let { reason ->
//                quantity.text = reason.labelInLocalLanguageOrGerman
//            } ?: run {
//                quantity.text =
//                    itemView.resources.getText(R.string.gpb_digital_invoice_line_item_quantity)
//            }
            data.lineItem.let { li ->
                description.text = li.description
                quantity.text = when {
                    data.reason != null -> data.reason!!.labelInLocalLanguageOrGerman
                    else -> quantity.resources.getString(
                        R.string.gpb_digital_invoice_line_item_quantity,
                        li.quantity
                    )
                }
                DigitalInvoice.lineItemTotalGrossPriceIntegralAndFractionalParts(li)
                    .let { (integral, fractional) ->
                        priceIntegralPart.text = integral
                        @SuppressLint("SetTextI18n")
                        priceFractionalPart.text = fractional
                    }
            }
            itemView.setOnClickListener {
                allData?.getOrNull(dataIndex ?: -1)?.let {
                    listener?.onLineItemClicked(it)
                }

            }
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                allData?.getOrNull(dataIndex ?: -1)?.let {
                    if (it.selected != isChecked) {
                        listener?.apply {
                            if (isChecked) {
                                onLineItemSelected(it)
                            } else {
                                onLineItemDeselected(it)
                            }
                        }
                    }
                }
            }
        }

        override fun unbind() {
            listener = null
            itemView.setOnClickListener(null)
            checkbox.setOnCheckedChangeListener(null)
        }

        fun enable() {
            itemView.isEnabled = true
            strokeView.background = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.gpb_digital_invoice_line_item_stroke_background
            )
            description.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_description_text
                )
            )
            edit.isEnabled = true

            quantity.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_quantity_text
                )
            )
            priceIntegralPart.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_gross_price_text
                )
            )
            priceFractionalPart.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_gross_price_text
                )
            )
        }


        fun disable() {
            itemView.isEnabled = false
            val disabledColor = ContextCompat.getColor(
                itemView.context,
                R.color.gpb_digital_invoice_line_item_disabled
            )
            strokeView.background = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.gpb_digital_invoice_line_item_stroke_background_disabled
            )
            description.setTextColor(disabledColor)
            edit.isEnabled = false
            quantity.setTextColor(disabledColor)
            priceIntegralPart.setTextColor(disabledColor)
            priceFractionalPart.setTextColor(disabledColor)
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class AddonViewHolder(itemView: GpbItemDigitalInvoiceAddonBinding) :
        ViewHolder<DigitalInvoiceAddon>(itemView.root, Addon) {
        private val addonName = itemView.gpbAddonName
        private val priceIntegralPart: TextView = itemView.gpbAddonPriceTotalIntegralPart
        private val priceFractionalPart: TextView = itemView.gpbAddonPriceTotalFractionalPart

        override fun bind(
            data: DigitalInvoiceAddon,
            allData: List<DigitalInvoiceAddon>?,
            dataIndex: Int?
        ) {
            @SuppressLint("SetTextI18n")
            addonName.text = "${itemView.context.getString(data.nameStringRes)}:"
            DigitalInvoice.addonPriceIntegralAndFractionalParts(data)
                .let { (integral, fractional) ->
                    priceIntegralPart.text = integral
                    @SuppressLint("SetTextI18n")
                    priceFractionalPart.text = fractional
                }
        }

        override fun unbind() {
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class FooterViewHolder(itemView: GpbItemDigitalInvoiceFooterBinding) :
        ViewHolder<DigitalInvoiceScreenContract.FooterDetails>(itemView.root, Footer) {
        val payButton = itemView.gpbPayButton
        val skipButton = itemView.gpbSkipButton
        private val integralPart = itemView.gpbGrossPriceTotalIntegralPart
        private val fractionalPart = itemView.gpbGrossPriceTotalFractionalPart

        override fun bind(
            data: DigitalInvoiceScreenContract.FooterDetails,
            allData: List<DigitalInvoiceScreenContract.FooterDetails>?,
            dataIndex: Int?
        ) {
            skipButton.visibility = if (data.inaccurateExtraction) View.VISIBLE else View.GONE
            payButton.isEnabled = data.buttonEnabled
            payButton.text =
                payButton.resources.getString(
                    R.string.gpb_digital_invoice_pay,
                    data.count,
                    data.total
                )

            val (integral, fractional) = data.totalGrossPriceIntegralAndFractionalParts
            integralPart.text = integral
            fractionalPart.text = fractional
        }

        override fun unbind() {
        }
    }

    companion object {
        fun forViewTypeId(
            viewTypeId: Int, layoutInflater: LayoutInflater, parent: ViewGroup
        ) =
            when (ViewType.from(viewTypeId)) {
                Header -> HeaderViewHolder(
                    GpbItemDigitalInvoiceHeaderBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
                LineItem -> LineItemViewHolder(
                    GpbItemDigitalInvoiceLineItemBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
                Addon -> AddonViewHolder(
                    GpbItemDigitalInvoiceAddonBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
                Footer -> FooterViewHolder(
                    GpbItemDigitalInvoiceFooterBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
            }
    }
}
