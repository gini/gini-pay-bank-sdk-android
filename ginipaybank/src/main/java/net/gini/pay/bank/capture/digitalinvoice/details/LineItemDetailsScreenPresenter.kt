package net.gini.pay.bank.capture.digitalinvoice.details

import android.app.Activity
import net.gini.pay.bank.capture.digitalinvoice.DigitalInvoice
import net.gini.pay.bank.capture.digitalinvoice.SelectableLineItem
import net.gini.pay.bank.capture.digitalinvoice.details.LineItemDetailsScreenContract.Presenter
import net.gini.pay.bank.capture.digitalinvoice.details.LineItemDetailsScreenContract.View
import net.gini.pay.bank.capture.digitalinvoice.toPriceString
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.ParseException
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.pay.bank.R
import java.util.*

/**
 * Created by Alpar Szotyori on 17.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

@JvmSynthetic
internal val GROSS_PRICE_FORMAT = DecimalFormat("#,##0.00").apply { isParseBigDecimal = true }

/**
 * Internal use only.
 *
 * @suppress
 */
internal class LineItemDetailsScreenPresenter(
    activity: Activity, view: View,
    var selectableLineItem: SelectableLineItem,
    val returnReasons: List<GiniCaptureReturnReason> = emptyList(),
    private val grossPriceFormat: DecimalFormat = GROSS_PRICE_FORMAT
) :
    Presenter(activity, view) {

    override var listener: LineItemDetailsFragmentListener? = null

    private val originalLineItem: SelectableLineItem = selectableLineItem.copy()

    init {
        view.setPresenter(this)
    }

    override fun selectLineItem() {
        if (selectableLineItem.selected) {
            return
        }
        selectableLineItem.selected = true
        selectableLineItem.reason = null
        view.apply {
            enableInput()
            updateCheckboxAndSaveButton()
        }
    }

    override fun deselectLineItem() {
        if (!selectableLineItem.selected) {
            return
        }
        if (returnReasons.isEmpty()) {
            selectableLineItem.selected = false
            selectableLineItem.reason = null
            view.disableInput()
            updateCheckboxAndSaveButton()
        } else {
            view.showReturnReasonDialog(returnReasons) { selectedReason ->
                if (selectedReason != null) {
                    selectableLineItem.selected = false
                    selectableLineItem.reason = selectedReason
                    view.disableInput()
                } else {
                    selectableLineItem.selected = true
                    selectableLineItem.reason = null
                    view.enableInput()
                }
                updateCheckboxAndSaveButton()
            }
        }
    }

    private fun updateCheckboxAndSaveButton() = selectableLineItem.let {
        view.apply {
            showCheckbox(it.selected, it.lineItem.quantity, !it.addedByUser)
            updateSaveButton(it, originalLineItem, it.lineItem.grossPrice > BigDecimal.ZERO)
        }
    }

    override fun setDescription(description: String) {
        if (selectableLineItem.lineItem.description == description) {
            return
        }
        selectableLineItem = selectableLineItem.copy(
            lineItem = selectableLineItem.lineItem.copy(description = description)
        ).also {
            view.updateSaveButton(it, originalLineItem, it.lineItem.grossPrice > BigDecimal.ZERO)
        }
    }

    override fun setQuantity(quantity: Int) {
        if (selectableLineItem.lineItem.quantity == quantity) {
            return
        }
        selectableLineItem = selectableLineItem.copy(
            lineItem = selectableLineItem.lineItem.copy(quantity = quantity)
        )
        view.showTotalGrossPrice(selectableLineItem)
        updateCheckboxAndSaveButton()
    }

    override fun setGrossPrice(displayedGrossPrice: String) {
        val grossPrice = try {
            grossPriceFormat.parse(displayedGrossPrice) as BigDecimal
        } catch (_: ParseException) {
            view.apply {
                updateSaveButton(selectableLineItem, originalLineItem, false)
            }
            return
        }
        if (selectableLineItem.lineItem.grossPrice == grossPrice) {
            return
        }
        selectableLineItem = selectableLineItem.copy(
            lineItem = selectableLineItem.lineItem.copy(
                rawGrossPrice = grossPrice.toPriceString(selectableLineItem.lineItem.rawCurrency)
            )
        ).also {
            view.apply {
                showTotalGrossPrice(it)
                updateSaveButton(it, originalLineItem, grossPrice > BigDecimal.ZERO)
            }
        }
    }

    override fun save(isBack: Boolean) {
        if (selectableLineItem.addedByUser && selectableLineItem.lineItem.description.isBlank()) {
            selectableLineItem = selectableLineItem.copy(
                lineItem = selectableLineItem.lineItem.copy(description = activity.getString(R.string.gpb_digital_invoice_line_item_description_additional))
            )
        }
        when {
            isBack && selectableLineItem.lineItem.id.isBlank() -> {
                view.dismiss()
            }

            selectableLineItem.lineItem.id.isBlank() -> {
                val lineItem = selectableLineItem.lineItem.copy(UUID.randomUUID().toString())
                listener?.onSave(selectableLineItem.copy(lineItem = lineItem))
            }

            else -> {
                listener?.onSave(selectableLineItem)
            }
        }
    }

    override fun start() {
        view.apply {
            selectableLineItem.run {
                lineItem.run {
                    showDescription(description)
                    showQuantity(quantity)
                    showGrossPrice(grossPriceFormat.format(grossPrice), currency?.symbol ?: "")
                }
                showTotalGrossPrice(this)
            }
            updateCheckboxAndSaveButton()
        }
    }

    override fun stop() {
    }
}

private fun View.showTotalGrossPrice(selectableLineItem: SelectableLineItem) {
    DigitalInvoice.lineItemTotalGrossPriceIntegralAndFractionalParts(
        selectableLineItem.lineItem
    ).let { (integral, fractional) ->
        showTotalGrossPrice(integral, fractional)
    }
}

private fun View.updateSaveButton(new: SelectableLineItem, old: SelectableLineItem, isPriceValid: Boolean) {
    if (new == old || !isPriceValid) {
        disableSaveButton()
    } else {
        enableSaveButton()
    }
}