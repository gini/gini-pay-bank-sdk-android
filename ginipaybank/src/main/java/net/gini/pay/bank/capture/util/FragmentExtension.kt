package net.gini.pay.bank.capture.util

import androidx.fragment.app.Fragment

internal fun Fragment.parentFragmentManagerOrNull() = if (isAdded) { parentFragmentManager } else { null }