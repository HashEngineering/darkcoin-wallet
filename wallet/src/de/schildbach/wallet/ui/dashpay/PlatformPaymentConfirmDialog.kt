/*
 * Copyright 2020 Dash Core Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schildbach.wallet.ui.dashpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import de.schildbach.wallet.Constants
import de.schildbach.wallet.ui.BaseBottomSheetDialogFragment
import de.schildbach.wallet.ui.SingleActionSharedViewModel
import de.schildbach.wallet_test.R
import kotlinx.android.synthetic.main.dialog_platform_payment_confirm.*
import org.bitcoinj.core.Coin
import org.bitcoinj.utils.MonetaryFormat
import org.dash.wallet.common.util.GenericUtils

class PlatformPaymentConfirmDialog : BaseBottomSheetDialogFragment() {

    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_AMOUNT = "arg_amount"

        @JvmStatic
        fun createDialog(title: String, messageHtml: String, amount: Long): DialogFragment {
            val dialog = PlatformPaymentConfirmDialog()
            dialog.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, messageHtml)
                putLong(ARG_AMOUNT, amount)
            }
            return dialog
        }
    }

    private val title by lazy {
        requireArguments().getString(ARG_TITLE)!!
    }

    private val message by lazy {
        requireArguments().getString(ARG_MESSAGE)!!
    }

    private val amount by lazy {
        requireArguments().getLong(ARG_AMOUNT)
    }

    private lateinit var viewModel: NewAccountConfirmDialogViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_platform_payment_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        agree_check.setOnCheckedChangeListener { _, isChecked ->
            confirm.isEnabled = isChecked
        }

        confirm.setOnClickListener {
            dismiss()
            sharedViewModel.clickConfirmButtonEvent.call(true)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel = activity?.run {
            ViewModelProvider(this)[SharedViewModel::class.java]
        } ?: throw IllegalStateException("Invalid Activity")
        viewModel = ViewModelProvider(this).get(NewAccountConfirmDialogViewModel::class.java)
        viewModel.exchangeRateData.observe(viewLifecycleOwner, {
            updateView()
        })
        updateView()
    }

    private fun updateView() {
        title_view.text = title
        message_view.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT)

        val amount = Coin.valueOf(amount)
        val amountStr = MonetaryFormat.BTC.noCode().format(amount).toString()
        val fiatAmount = viewModel.exchangeRate?.coinToFiat(amount)
        // if the exchange rate is not available, then show "Not Available"
        val fiatAmountStr = if (fiatAmount != null) Constants.LOCAL_FORMAT.format(fiatAmount).toString() else getString(R.string.transaction_row_rate_not_available)
        val fiatSymbol = if (fiatAmount != null) GenericUtils.currencySymbol(fiatAmount.currencyCode) else ""

        dash_amount_view.text = amountStr
        fiat_symbol_view.text = fiatSymbol
        fiat_amount_view.text = fiatAmountStr
    }

    class SharedViewModel : SingleActionSharedViewModel()
}