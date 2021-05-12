Pay
===

Pay feature provides the API for receiving a payment request started
by a Business app, mark it as payed and return to the app that started
the flow.

The pay feature needs to be initialized by calling GiniPayBank.setGiniApi().
See authentication sections for more details.

To use the pay feature you need to add to your manifest an intent filter for
the ginipay URI so that it is discoverable by the business apps:

.. code-block:: kotlin

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />

        <data
            android:host="payment"
            android:scheme="ginipay" />
    </intent-filter>

The intent filter can be added to the activity which will handle the payment flow of
that payment request.

The flow is:
 1. ``getRequestId`` to extract the id from the ``Intent``
 2. ``getPaymentRequest`` to get payment details set by the business app.
 3. ``resolvePaymentRequest`` to mark the ``PaymentRequest`` as paid.
 4. ``returnToBusiness`` to return to the business app that started the flow.