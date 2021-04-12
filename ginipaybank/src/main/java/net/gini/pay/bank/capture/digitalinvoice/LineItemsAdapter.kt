package net.gini.pay.bank.capture.digitalinvoice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.gini.pay.bank.R
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
internal class LineItemsAdapter(private val listener: LineItemsAdapterListener) :
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

    private val footerButtonClickListener = View.OnClickListener {
        listener.payButtonClicked()
    }
    private val footerSkipButtonClickListener = View.OnClickListener {
        listener.skipButtonClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeId: Int): ViewHolder<*> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewHolder =
            ViewHolder.forViewTypeId(viewTypeId, layoutInflater, parent)
        if (viewHolder is ViewHolder.FooterViewHolder) {
            viewHolder.binding.gpbPayButton.setOnClickListener(footerButtonClickListener)
            viewHolder.binding.gpbSkipButton.setOnClickListener(footerSkipButtonClickListener)
        }

        if (viewHolder is ViewHolder.HeaderViewHolder) {
            viewHolder.binding.gpbHeaderButton2.setOnClickListener(footerSkipButtonClickListener)
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
    addons.getOrNull(position - lineItems.size - 1)

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
        val binding: GpbItemDigitalInvoiceHeaderBinding
    ) :
        ViewHolder<Unit>(binding.root, Header) {
        internal var listener: LineItemsAdapterListener? = null

        private val toggleClickListener = View.OnClickListener {
            when (binding.root.currentState) {
                R.id.end -> {
                    binding.root.transitionToState(R.id.start)
                }
                else -> {
                    binding.root.transitionToState(R.id.end)
                }
            }
        }

        override fun bind(data: Unit, allData: List<Unit>?, dataIndex: Int?) {
            binding.gpbCollapseButton.setOnClickListener(toggleClickListener)
            binding.gpbHeaderButton1.setOnClickListener(toggleClickListener)
            binding.gpbHeaderTitle.setOnClickListener(toggleClickListener)
        }

        override fun unbind() {
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class LineItemViewHolder(private val binding: GpbItemDigitalInvoiceLineItemBinding) :
        ViewHolder<SelectableLineItem>(binding.root, LineItem) {
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
            binding.gpbItemIndexLabel.text = binding.gpbItemIndexLabel.resources.getString(
                R.string.gpb_digital_invoice_line_item_index,
                articleIndex,
                allData?.size ?: 0
            )
            binding.gbpSwitch.isChecked = data.selected

            data.lineItem.let { li ->
                binding.gpbDescription.text = li.description
                binding.gpbQuantity.text = when {
                    data.reason != null -> data.reason!!.labelInLocalLanguageOrGerman
                    else -> binding.gpbQuantity.resources.getString(
                        R.string.gpb_digital_invoice_line_item_quantity,
                        li.quantity
                    )
                }
                DigitalInvoice.lineItemTotalGrossPriceIntegralAndFractionalParts(li)
                    .let { (integral, fractional) ->
                        binding.gpbGrossPriceIntegralPart.text = integral
                        @SuppressLint("SetTextI18n")
                        binding.gpbGrossPriceFractionalPart.text = fractional
                    }
            }
            itemView.setOnClickListener {
                allData?.getOrNull(dataIndex ?: -1)?.let {
                    listener?.onLineItemClicked(it)
                }

            }
            binding.gbpSwitch.setOnCheckedChangeListener { _, isChecked ->
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
            binding.gbpSwitch.setOnCheckedChangeListener(null)
        }

        fun enable() {
            itemView.isEnabled = true
            binding.gbpStrokeBackgroundView.background = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.gpb_digital_invoice_line_item_stroke_background
            )
            binding.gpbDescription.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_description_text
                )
            )
            binding.gpbEdit.isEnabled = true

            binding.gpbQuantity.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_quantity_text
                )
            )
            binding.gpbGrossPriceIntegralPart.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.gpb_digital_invoice_line_item_gross_price_text
                )
            )
            binding.gpbGrossPriceFractionalPart.setTextColor(
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
            binding.gbpStrokeBackgroundView.background = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.gpb_digital_invoice_line_item_stroke_background_disabled
            )
            binding.gpbDescription.setTextColor(disabledColor)
            binding.gpbEdit.isEnabled = false
            binding.gpbQuantity.setTextColor(disabledColor)
            binding.gpbGrossPriceIntegralPart.setTextColor(disabledColor)
            binding.gpbGrossPriceFractionalPart.setTextColor(disabledColor)
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class AddonViewHolder(binding: GpbItemDigitalInvoiceAddonBinding) :
        ViewHolder<DigitalInvoiceAddon>(binding.root, Addon) {
        private val addonName = binding.gpbAddonName
        private val priceIntegralPart: TextView = binding.gpbAddonPriceTotalIntegralPart
        private val priceFractionalPart: TextView = binding.gpbAddonPriceTotalFractionalPart

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
    internal class FooterViewHolder(val binding: GpbItemDigitalInvoiceFooterBinding) :
        ViewHolder<DigitalInvoiceScreenContract.FooterDetails>(binding.root, Footer) {

        override fun bind(
            data: DigitalInvoiceScreenContract.FooterDetails,
            allData: List<DigitalInvoiceScreenContract.FooterDetails>?,
            dataIndex: Int?
        ) {
            binding.gpbSkipButton.isVisible = data.inaccurateExtraction
            binding.gpbPayButton.isEnabled = data.buttonEnabled
            binding.gpbPayButton.text =
                binding.gpbPayButton.resources.getString(
                    R.string.gpb_digital_invoice_pay,
                    data.count,
                    data.total
                )

            val (integral, fractional) = data.totalGrossPriceIntegralAndFractionalParts
            binding.gpbGrossPriceTotalIntegralPart.text = integral
            binding.gpbGrossPriceTotalFractionalPart.text = fractional
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
