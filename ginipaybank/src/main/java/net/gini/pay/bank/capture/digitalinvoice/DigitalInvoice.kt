package net.gini.pay.bank.capture.digitalinvoice

import androidx.annotation.VisibleForTesting
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction
import net.gini.android.capture.network.model.GiniCaptureCompoundExtraction
import net.gini.android.capture.network.model.GiniCaptureReturnReason

/**
 * Created by Alpar Szotyori on 11.03.2020.
 *
 * Copyright (c) 2020 Gini GmbH.
 */

@JvmSynthetic
internal val INTEGRAL_FORMAT = DecimalFormat("#,###")

@JvmSynthetic
internal val FRACTION_FORMAT = DecimalFormat(".00").apply { roundingMode = RoundingMode.DOWN }

/**
 * Internal use only.
 *
 * @suppress
 */
internal class DigitalInvoice(extractions: Map<String, GiniCaptureSpecificExtraction>,
                              compoundExtractions: Map<String, GiniCaptureCompoundExtraction>) {

    private var _extractions: Map<String, GiniCaptureSpecificExtraction> = extractions
    val extractions
        get() = _extractions

    private var _compoundExtractions: Map<String, GiniCaptureCompoundExtraction> = compoundExtractions
    val compoundExtractions
        get() = _compoundExtractions

    private var _selectableLineItems: List<SelectableLineItem>
    val selectableLineItems
        get() = _selectableLineItems

    private var _addons: List<DigitalInvoiceAddon>
    val addons
        get() = _addons

    init {
        _selectableLineItems = lineItemsFromCompoundExtractions(compoundExtractions).map { SelectableLineItem(lineItem = it) }

        _addons = extractions.mapNotNull { (_, extraction) -> DigitalInvoiceAddon.createFromOrNull(extraction) }
    }

    companion object {
        fun lineItemTotalGrossPriceIntegralAndFractionalParts(lineItem: LineItem): Pair<String, String> {
            return lineItem.run {
                Pair(priceIntegralPartWithCurrencySymbol(totalGrossPrice, currency),
                        totalGrossPrice.fractionalPart(FRACTION_FORMAT))
            }
        }

        fun addonPriceIntegralAndFractionalParts(addon: DigitalInvoiceAddon): Pair<String, String> {
            return addon.run {
                Pair(priceIntegralPartWithCurrencySymbol(price, currency),
                        price.fractionalPart(FRACTION_FORMAT))
            }
        }

        @VisibleForTesting
        fun priceIntegralPartWithCurrencySymbol(price: BigDecimal, currency: Currency?) =
                currency?.let { c ->
                    price.integralPartWithCurrency(c, INTEGRAL_FORMAT)
                } ?: price.integralPart(INTEGRAL_FORMAT)
    }

    private fun lineItemsFromCompoundExtractions(compoundExtractions: Map<String, GiniCaptureCompoundExtraction>): List<LineItem> =
            compoundExtractions["lineItems"]?.run {
                specificExtractionMaps.mapIndexed { index, lineItem ->
                    LineItem(index.toString(),
                            lineItem["description"]?.value ?: "",
                            lineItem["quantity"]?.value?.toIntOrNull() ?: 0,
                            lineItem["baseGross"]?.value ?: "0.00:EUR")
                }
            } ?: emptyList()

    fun updateLineItem(selectableLineItem: SelectableLineItem) {
        _selectableLineItems =
                selectableLineItems.map { sli -> if (sli.lineItem.id == selectableLineItem.lineItem.id) selectableLineItem else sli }
    }

    fun selectLineItem(selectableLineItem: SelectableLineItem) {
        selectableLineItems.find { sli -> sli.lineItem.id == selectableLineItem.lineItem.id }?.let { sli ->
            sli.selected = true
            sli.reason = null
        }
    }

    fun deselectLineItem(selectableLineItem: SelectableLineItem, reason: GiniCaptureReturnReason?) {
        selectableLineItems.find { sli -> sli.lineItem.id == selectableLineItem.lineItem.id }?.let { sli ->
            sli.selected = false
            sli.reason = reason
        }
    }

    fun totalPriceIntegralAndFractionalParts(): Pair<String, String> {
        val price = totalPrice()
        val currency = lineItemsCurency()
        return Pair(priceIntegralPartWithCurrencySymbol(price, currency),
                price.fractionalPart(FRACTION_FORMAT))
    }

    @VisibleForTesting
    fun lineItemsCurency(): Currency? =
            selectableLineItems.firstOrNull()?.lineItem?.currency

    fun selectedLineItemsTotalGrossPriceSum(): BigDecimal =
            selectableLineItems.fold<SelectableLineItem, BigDecimal>(BigDecimal.ZERO) { sum, sli ->
                if (sli.selected) sum.add(sli.lineItem.totalGrossPrice) else sum
            }

    private fun addonsPriceSum(): BigDecimal =
            addons.fold<DigitalInvoiceAddon, BigDecimal>(BigDecimal.ZERO) { sum, addon ->
                sum.add(addon.price)
            }

    private fun totalPrice(): BigDecimal =
            selectedLineItemsTotalGrossPriceSum().add(addonsPriceSum()).max(BigDecimal.ZERO)

    fun selectedAndTotalLineItemsCount(): Pair<Int, Int> =
            Pair(selectedLineItemsCount(), totalLineItemsCount())

    private fun selectedLineItemsCount(): Int =
            selectableLineItems.fold(0) { c, sli -> if (sli.selected) c + sli.lineItem.quantity else c }

    private fun totalLineItemsCount(): Int =
            selectableLineItems.fold(0) { c, sli -> c + sli.lineItem.quantity }

    fun updateLineItemExtractionsWithReviewedLineItems() {
        _compoundExtractions = compoundExtractions.mapValues { (name, extraction) ->
            when (name) {
                "lineItems" -> GiniCaptureCompoundExtraction(name,
                        extraction.specificExtractionMaps.mapIndexed { index, lineItemExtractions ->
                            selectableLineItems.find { it.lineItem.id.toInt() == index }?.let { sli ->
                                var extractions = lineItemExtractions.mapValues { (name, lineItemExtraction) ->
                                    when (name) {
                                        "description" -> copyGiniCaptureSpecificExtraction(lineItemExtraction, sli.lineItem.description)
                                        "baseGross" -> copyGiniCaptureSpecificExtraction(lineItemExtraction, sli.lineItem.rawGrossPrice)
                                        "quantity" -> copyGiniCaptureSpecificExtraction(lineItemExtraction,
                                                if (sli.selected) {
                                                    sli.lineItem.quantity.toString()
                                                } else {
                                                    "0"
                                                })
                                        else -> lineItemExtraction
                                    }
                                }.toMutableMap()
                                sli.reason?.let { returnReason ->
                                    extractions.put("returnReason", GiniCaptureSpecificExtraction(
                                            "returnReason", returnReason.id, "", null, emptyList()))
                                }
                                extractions
                            }
                        }.filterNotNull())
                else -> extraction
            }
        }
    }

    fun updateAmountToPayExtractionWithTotalPrice() {
        val totalPrice = totalPrice().toPriceString(selectableLineItems.firstOrNull()?.lineItem?.rawCurrency ?: "EUR")

        _extractions = if (extractions.containsKey("amountToPay")) {
            extractions.mapValues { (name, extraction) ->
                when (name) {
                    "amountToPay" -> copyGiniCaptureSpecificExtraction(extraction, totalPrice)
                    else -> extraction
                }
            }
        } else {
            extractions.toMutableMap().apply {
                put("amountToPay", GiniCaptureSpecificExtraction("amountToPay", totalPrice, "amount", null, emptyList()))
            }
        }
    }

    @JvmSynthetic
    private fun copyGiniCaptureSpecificExtraction(other: GiniCaptureSpecificExtraction, value: String) =
            GiniCaptureSpecificExtraction(other.name, value, other.entity, other.box, other.candidates)
}