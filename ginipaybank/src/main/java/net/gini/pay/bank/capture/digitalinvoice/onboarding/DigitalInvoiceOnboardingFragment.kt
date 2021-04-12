package net.gini.pay.bank.capture.digitalinvoice.onboarding

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import net.gini.pay.bank.R
import net.gini.pay.bank.capture.util.autoCleared
import net.gini.pay.bank.databinding.GpbFragmentDigitalInvoiceOnboardingBinding

/**
 * Created by Alpar Szotyori on 14.10.2020.
 *
 * Copyright (c) 2020 Gini GmbH.
 */

/**
 * Internal use only.
 *
 * @suppress
 */
internal class DigitalInvoiceOnboardingFragment : Fragment(), DigitalOnboardingScreenContract.View, DigitalOnboardingFragmentInterface {

    companion object {
        @JvmStatic
        fun createInstance() = DigitalInvoiceOnboardingFragment()
    }

    private var binding by autoCleared<GpbFragmentDigitalInvoiceOnboardingBinding>()

    private var presenter: DigitalOnboardingScreenContract.Presenter? = null
    override var listener: DigitalInvoiceOnboardingFragmentListener?
        get() = this.presenter?.listener
        set(value) {
            this.presenter?.listener = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GpbFragmentDigitalInvoiceOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createPresenter(requireActivity())
        initListener()
        enterTransition = TransitionInflater.from(context).inflateTransition(R.transition.fade)
        exitTransition = enterTransition

    }

    private fun createPresenter(activity: Activity) =
        DigitalOnboardingScreenPresenter(
            activity,
            this,
        )

    private fun initListener() {
        if (activity is DigitalInvoiceOnboardingFragmentListener) {
            listener = activity as DigitalInvoiceOnboardingFragmentListener?
        } else checkNotNull(listener) {
            ("DigitalInvoiceOnboardingFragmentListener not set. "
                    + "You can set it with DigitalInvoiceOnboardingFragmentListener#setListener() or "
                    + "by making the host activity implement the DigitalInvoiceOnboardingFragmentListener.")
        }
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun setPresenter(presenter: DigitalOnboardingScreenContract.Presenter) {
        this.presenter = presenter
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onStart() {
        super.onStart()
        presenter?.start()
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onStop() {
        super.onStop()
        presenter?.stop()
    }


    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInputHandlers()
    }

    /**
     * Internal use only.
     *
     * @suppress
     */
    override fun onDestroyView() {
        listener = null
        super.onDestroyView()
    }

    private fun setInputHandlers() {
        binding.gpbDoneButton.setOnClickListener {
            presenter?.dismisOnboarding(false)
        }

        binding.gbpDoNotShowButton.setOnClickListener {
            presenter?.dismisOnboarding(true)
        }
    }
}