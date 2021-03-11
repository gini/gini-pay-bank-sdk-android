package net.gini.pay.bank.capture.digitalinvoice.details

import net.gini.pay.bank.capture.digitalinvoice.SelectableLineItem

/**
 * Created by Alpar Szotyori on 17.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

/**
 * Interface used by the [LineItemDetailsFragment] to dispatch events to the hosting Activity.
 */
interface LineItemDetailsFragmentListener {

    /**
     * Called when the user presses the save button.
     *
     * The selectable line item is updated to contain the user's modifications.
     *
     * @param selectableLineItem - the modified selectable line item
     */
    fun onSave(selectableLineItem: SelectableLineItem)
}