package net.gini.pay.bank.capture.digitalinvoice

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*
import kotlin.collections.ArrayList
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.pay.bank.R
import net.gini.pay.bank.capture.util.autoCleared
import net.gini.pay.bank.databinding.GpbFragmentReturnReasonDialogBinding

/**
 * Created by Alpar Szotyori on 22.01.2020.
 *
 * Copyright (c) 2020 Gini GmbH.
 */

private const val ARG_RETURN_REASONS = "GPB_ARG_SELECTABLE_LINE_ITEM"

internal typealias ReturnReasonDialogResultCallback = (GiniCaptureReturnReason?) -> Unit

/**
 * Internal use only.
 *
 * @suppress
 */
internal class ReturnReasonDialog : BottomSheetDialogFragment() {

    private var binding by autoCleared<GpbFragmentReturnReasonDialogBinding>()
    private lateinit var reasons: List<GiniCaptureReturnReason>

    var callback: ReturnReasonDialogResultCallback? = null

    override fun getTheme(): Int = R.style.GiniCaptureTheme_BottomSheetDialog

    companion object {
        @JvmStatic
        fun createInstance(reasons: List<GiniCaptureReturnReason>) = ReturnReasonDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_RETURN_REASONS, ArrayList(reasons))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readArguments()
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
    }

    private fun readArguments() {
        arguments?.run {
            reasons = getParcelableArrayList(ARG_RETURN_REASONS) ?: emptyList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GpbFragmentReturnReasonDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListView()
    }

    private fun initListView() {
        activity?.let {
            binding.gpbReturnReasonsList.adapter = ArrayAdapter(it, R.layout.gpb_item_return_reason, localizedReasons())
            binding.gpbReturnReasonsList.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    callback?.invoke(reasons[position])
                    dismissAllowingStateLoss()
                }
        }
    }

    private fun localizedReasons() = reasons.map { it.labelInLocalLanguageOrGerman ?: "" }

    override fun onCancel(dialog: DialogInterface) {
        callback?.invoke(null)
    }
}