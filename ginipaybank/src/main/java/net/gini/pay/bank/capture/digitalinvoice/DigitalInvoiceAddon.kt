package net.gini.pay.bank.capture.digitalinvoice

import androidx.annotation.StringRes
import java.math.BigDecimal
import java.util.*
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction
import net.gini.pay.bank.R

/**
 * Created by Alpar Szotyori on 04.09.2020.
 *
 * Copyright (c) 2020 Gini GmbH.
 */

internal enum class AddonExtraction(val extractionName: String, @StringRes val addonNameStringRes: Int) {
    DISCOUNT("discount-addon", R.string.gpb_digital_invoice_addon_discount),
    GIFTCARD("giftcard-addon", R.string.gpb_digital_invoice_addon_giftcard),
    OTHER_DISCOUNTS("other-discounts-addon", R.string.gpb_digital_invoice_addon_other_discounts),
    OTHER_CHARGES("other-charges-addon", R.string.gpb_digital_invoice_addon_other_charges),
    SHIPMENT("shipment-addon", R.string.gpb_digital_invoice_addon_shipment);

    companion object {
        fun createFromOrNull(extraction: GiniCaptureSpecificExtraction): AddonExtraction? =
                values().firstOrNull { addonExtraction ->
                    addonExtraction.extractionName == extraction.name
                }
    }
}

class DigitalInvoiceAddon private constructor(val price: BigDecimal,
                                                       val currency: Currency?,
                                                       private val addonExtraction: AddonExtraction) {

    val nameStringRes: Int
        get() = addonExtraction.addonNameStringRes

    companion object {
        fun createFromOrNull(extraction: GiniCaptureSpecificExtraction): DigitalInvoiceAddon? =
                AddonExtraction.createFromOrNull(extraction)?.let { addonExtraction ->
                    val (price: BigDecimal?, currency: Currency?) = try {
                        val (price, _, currency) = parsePriceString(extraction.value)
                        Pair(price, currency)
                    } catch (e: Exception) {
                        Pair(null, null)
                    }
                    price?.let {
                        DigitalInvoiceAddon(price, currency, addonExtraction)
                    }
                }
    }

}