package net.gini.pay.bank.capture.digitalinvoice

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
    var selectedAndTotalItems: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var totalGrossPriceIntegralAndFractionalParts: Pair<String, String> = Pair("", "")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeId: Int) =
            ViewHolder.forViewTypeId(viewTypeId, layoutInflater, parent)

    override fun getItemCount(): Int = lineItems.size + addons.size + 2

    private fun footerPosition() = lineItems.size + addons.size + 1

    private fun addonsRange() = (lineItems.size + 1)..(lineItems.size + addons.size + 1)

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> Header.id
        footerPosition() -> Footer.id
        in addonsRange() -> Addon.id
        else -> LineItem.id
    }

    override fun onBindViewHolder(viewHolder: ViewHolder<*>, position: Int) {
        when (viewHolder) {
            is ViewHolder.HeaderViewHolder -> {
                viewHolder.listener = listener
                viewHolder.bind(selectedAndTotalItems)
            }
            is ViewHolder.LineItemViewHolder -> {
                lineItemForPosition(position, lineItems)?.let {
                    viewHolder.listener = listener
                    viewHolder.bind(it, lineItems)
                }
            }
            is ViewHolder.AddonViewHolder -> {
                addonForPosition(position, addons, lineItems)?.let {
                    viewHolder.bind(it, addons)
                }
            }
            is ViewHolder.FooterViewHolder -> viewHolder.bind(totalGrossPriceIntegralAndFractionalParts)
        }
    }

    override fun onViewRecycled(viewHolder: ViewHolder<*>) {
        viewHolder.unbind()
    }
}

@JvmSynthetic
internal fun lineItemForPosition(position: Int,
                                 lineItems: List<SelectableLineItem>): SelectableLineItem? =
        lineItems.getOrElse(position - 1) { null }

@JvmSynthetic
internal fun addonForPosition(position: Int,
                              addons: List<DigitalInvoiceAddon>,
                              lineItems: List<SelectableLineItem>): DigitalInvoiceAddon? =
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
internal sealed class ViewHolder<in T>(itemView: View, val viewType: ViewType) : RecyclerView.ViewHolder(itemView) {

    internal abstract fun bind(data: T, allData: List<T>? = null)

    internal abstract fun unbind()

    /**
     * Internal use only.
     *
     * @suppress
     */
    internal class HeaderViewHolder(itemView: GpbItemDigitalInvoiceHeaderBinding) : ViewHolder<String>(itemView.root, Header) {
        private val selectedAndTotalItems = itemView.gpbSelectedAndTotalItems
        private val whatIsThisButton = itemView.gpbWhatIsThisButton
        internal var listener: LineItemsAdapterListener? = null

        override fun bind(data: String, allData: List<String>?) {
            @SuppressLint("SetTextI18n")
            selectedAndTotalItems.text = " $data"
            whatIsThisButton.setOnClickListener {
                listener?.onWhatIsThisButtonClicked()
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
    internal class LineItemViewHolder(itemView: GpbItemDigitalInvoiceLineItemBinding) : ViewHolder<SelectableLineItem>(itemView.root, LineItem) {
        private val card: MaterialCardView = itemView.gpbLineItem
        private val checkbox: CheckBox = itemView.gpbCheckbox
        private val description: TextView = itemView.gpbDescription
        private val quantityLabel: TextView = itemView.gpbQuantityLabel
        private val quantity: TextView = itemView.gpbQuantity
        private val edit: Button = itemView.gpbEdit
        private val priceIntegralPart: TextView = itemView.gpbGrossPriceIntegralPart
        private val priceFractionalPart: TextView = itemView.gpbGrossPriceFractionalPart
        internal var listener: LineItemsAdapterListener? = null

        override fun bind(data: SelectableLineItem, allData: List<SelectableLineItem>?) {
            if (data.selected) {
                enable()
            } else {
                disable()
            }
            checkbox.isChecked = data.selected
            data.reason?.let { reason ->
                quantityLabel.text = reason.labelInLocalLanguageOrGerman
                quantity.visibility = View.INVISIBLE
            } ?: run {
                quantityLabel.text =
                        itemView.resources.getText(R.string.gpb_digital_invoice_line_item_quantity)
                quantity.visibility = View.VISIBLE
            }
            data.lineItem.let { li ->
                description.text = li.description
                @SuppressLint("SetTextI18n")
                quantity.text = " ${li.quantity}"
                DigitalInvoice.lineItemTotalGrossPriceIntegralAndFractionalParts(li).let { (integral, fractional) ->
                    priceIntegralPart.text = integral
                    @SuppressLint("SetTextI18n")
                    priceFractionalPart.text = fractional
                }
            }
            itemView.setOnClickListener {
                allData?.let {
                    lineItemForPosition(adapterPosition, allData)?.let {
                        listener?.onLineItemClicked(it)
                    }
                }
            }
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                allData?.let {
                    lineItemForPosition(adapterPosition, allData)?.let {
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
        }

        override fun unbind() {
            listener = null
            itemView.setOnClickListener(null)
            checkbox.setOnCheckedChangeListener(null)
        }

        fun enable() {
            itemView.isEnabled = true
            card.cardElevation = itemView.resources.getDimension(R.dimen.gpb_digital_invoice_line_item_card_elevation)
            card.strokeColor = ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_stroke)
            description.setTextColor(ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_description_text))
            edit.isEnabled = true
            edit.setTextColor(ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_edit_text))
            quantityLabel.setTextColor(ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_quantity_text))
            quantity.setTextColor(ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_quantity_text))
            priceIntegralPart.setTextColor(ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_gross_price_text))
            priceFractionalPart.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_gross_price_text))
        }


        fun disable() {
            itemView.isEnabled = false
            card.cardElevation = 0f
            val disabledColor = ContextCompat.getColor(itemView.context, R.color.gpb_digital_invoice_line_item_disabled)
            card.strokeColor = disabledColor
            description.setTextColor(disabledColor)
            edit.isEnabled = false
            edit.setTextColor(disabledColor)
            quantityLabel.setTextColor(disabledColor)
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
    internal class AddonViewHolder(itemView: GpbItemDigitalInvoiceAddonBinding) : ViewHolder<DigitalInvoiceAddon>(itemView.root, Addon) {
        private val addonName = itemView.gpbAddonName
        private val priceIntegralPart: TextView = itemView.gpbAddonPriceTotalIntegralPart
        private val priceFractionalPart: TextView = itemView.gpbAddonPriceTotalFractionalPart

        override fun bind(data: DigitalInvoiceAddon, allData: List<DigitalInvoiceAddon>?) {
            @SuppressLint("SetTextI18n")
            addonName.text = "${itemView.context.getString(data.nameStringRes)}:"
            DigitalInvoice.addonPriceIntegralAndFractionalParts(data).let { (integral, fractional) ->
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
    internal class FooterViewHolder(itemView: GpbItemDigitalInvoiceFooterBinding) : ViewHolder<Pair<String, String>>(itemView.root, Footer) {
        private val integralPart = itemView.gpbGrossPriceTotalIntegralPart
        private val fractionalPart = itemView.gpbGrossPriceTotalFractionalPart

        override fun bind(data: Pair<String, String>, allData: List<Pair<String, String>>?) {
            val (integral, fractional) = data
            integralPart.text = integral
            fractionalPart.text = fractional
        }

        override fun unbind() {
        }
    }

    companion object {
        fun forViewTypeId(viewTypeId: Int, layoutInflater: LayoutInflater, parent: ViewGroup) = when (ViewType.from(viewTypeId)) {
            Header -> HeaderViewHolder(GpbItemDigitalInvoiceHeaderBinding.inflate(layoutInflater, parent, false))
            LineItem -> LineItemViewHolder(GpbItemDigitalInvoiceLineItemBinding.inflate(layoutInflater, parent, false))
            Addon -> AddonViewHolder(GpbItemDigitalInvoiceAddonBinding.inflate(layoutInflater, parent, false))
            Footer -> FooterViewHolder(GpbItemDigitalInvoiceFooterBinding.inflate(layoutInflater, parent, false))
        }
    }
}
