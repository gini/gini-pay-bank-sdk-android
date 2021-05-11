package net.gini.pay.bank.capture.digitalinvoice.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import net.gini.android.capture.internal.util.ActivityHelper.enableHomeAsUp
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.pay.bank.R
import net.gini.pay.bank.capture.digitalinvoice.DigitalInvoiceActivity
import net.gini.pay.bank.capture.digitalinvoice.SelectableLineItem

private const val LINE_ITEM_DETAILS_FRAGMENT = "LINE_ITEM_DETAILS_FRAGMENT"
private const val EXTRA_IN_SELECTABLE_LINE_ITEM = "EXTRA_IN_SELECTABLE_LINE_ITEM"
private const val EXTRA_IN_RETURN_REASONS = "EXTRA_IN_RETURN_REASONS"

/**
 * When you use the Screen API, the `LineItemDetailsActivity` displays a line item to be edited by the user. The user can modify the
 * following:
 * - deselect the line item,
 * - edit the line item description,
 * - edit the quantity,
 * - edit the price.
 *
 * The extractions returned in the [CameraActivity.EXTRA_OUT_COMPOUND_EXTRACTIONS] are updated to include the user's modifications.
 *
 * The `LineItemDetailsActivity` is started by the [DigitalInvoiceActivity] when the user taps on a line item to edit it.
 *
 * ### Customizing the Line Item Details Screen
 *
 * Customizing the look of the Line Item Details Screen is done via overriding of app resources.
 *
 * Detailed description of the customization options is available in the
 * [customization guide](http://developer.gini.net/gini-vision-lib-android/html/customization-guide.html#line-item-details-screen).
 *
 * **Important:** All overriden styles must have their respective `Root.` prefixed style as their parent. Ex.: the parent of
 * `GiniCaptureTheme.Snackbar.Error.TextStyle` must be `Root.GiniCaptureTheme.Snackbar.Error.TextStyle`.
 *
 * ### Customizing the Action Bar
 *
 * Customizing the Action Bar is also done via overriding of app resources and each one - except the title string resource - is global to
 * all Activities.
 *
 * The following items are customizable:
 * - **Background color:** via the color resource named `gpb_action_bar` (highly recommended for Android 5+: customize the status bar color
 * via `gpb_status_bar`)
 * - **Back button:** via images for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi named {@code gpb_action_bar_back}
 */
class LineItemDetailsActivity : AppCompatActivity(), LineItemDetailsFragmentListener {

    companion object {
        internal const val EXTRA_OUT_SELECTABLE_LINE_ITEM = "EXTRA_OUT_SELECTABLE_LINE_ITEM"

        @JvmSynthetic
        internal fun createIntent(
            activity: Activity, selectableLineItem: SelectableLineItem,
            returnReasons: List<GiniCaptureReturnReason>
        ) = Intent(
            activity,
            LineItemDetailsActivity::class.java
        ).apply {
            putExtra(EXTRA_IN_SELECTABLE_LINE_ITEM, selectableLineItem)
            putParcelableArrayListExtra(EXTRA_IN_RETURN_REASONS, ArrayList(returnReasons))
        }
    }

    private var fragment: LineItemDetailsFragment? = null

    private lateinit var lineItem: SelectableLineItem
    private lateinit var returnReasons: List<GiniCaptureReturnReason>

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gpb_activity_line_item_details)
        readExtras()
        if (savedInstanceState == null) {
            initFragment()
        } else {
            retainFragment()
        }
        enableHomeAsUp(this)
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun readExtras() {
        intent.extras?.let {
            lineItem = checkNotNull(it.getParcelable(EXTRA_IN_SELECTABLE_LINE_ITEM)) {
                ("LineItemDetailsActivity requires a SelectableLineItem. " +
                        "Set it as an extra using the EXTRA_IN_SELECTABLE_LINE_ITEM key.")
            }
            returnReasons = it.getParcelableArrayList(EXTRA_IN_RETURN_REASONS) ?: emptyList()
        }
    }

    private fun initFragment() {
        if (!isFragmentShown()) {
            createFragment()
            showFragment()
        }
    }

    private fun isFragmentShown() = supportFragmentManager.findFragmentByTag(
        LINE_ITEM_DETAILS_FRAGMENT
    ) != null

    private fun createFragment() {
        fragment = LineItemDetailsFragment.createInstance(lineItem, returnReasons)
    }

    private fun showFragment() = fragment?.let {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_line_item_details, it, LINE_ITEM_DETAILS_FRAGMENT)
            .commit()
    }

    private fun retainFragment() {
        fragment = supportFragmentManager.findFragmentByTag(
            LINE_ITEM_DETAILS_FRAGMENT
        ) as LineItemDetailsFragment?
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onSave(selectableLineItem: SelectableLineItem) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_OUT_SELECTABLE_LINE_ITEM, selectableLineItem)
        })
        finish()
    }
}