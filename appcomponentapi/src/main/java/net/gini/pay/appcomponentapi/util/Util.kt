package net.gini.pay.appcomponentapi.util

import android.content.Context
import android.net.Uri
import net.gini.android.capture.util.UriHelper

fun Context.hasLessThan5MB(uri: Uri): Boolean =
    UriHelper.getFileSizeFromUri(uri, this) <= 5 * 1024 * 1024