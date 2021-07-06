Pay
===

Pay feature provides the API for receiving a payment request started
by a Business app, mark it as payed and return to the app that started
the flow.

Setup
-----

The pay feature needs to be initialized by calling ``GiniPayBank.setGiniApi()``.
See the `authentication <authentication.html>`_ page for more details.

Intent filter
~~~~~~~~~~~~~

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

Package name
~~~~~~~~~~~~

You also need to tell us your app's package name. It will be associated with the payment provider we 
create for your banking app in the Gini Pay API. The Gini Pay Business SDK will only open your banking app if
it is installed and it has the same package name as the one known by the Gini Pay API.

If you have different package names for development and production
then please share both of them with us so that we can use the right one for each environment.

Handle payment requests
-----------------------

The flow is:

1. ``getRequestId`` to extract the id from the ``Intent``
2. ``getPaymentRequest`` to get payment details set by the business app.
3. ``resolvePaymentRequest`` to mark the ``PaymentRequest`` as paid.
4. ``returnToBusiness`` to return to the business app that started the flow.

See the ``appscreenapi`` example app's ``pay`` package for more details.

Testing
-------

Testing the payment feature requires an app which uses the Gini Pay Business SDK to create payment requests and to
forward them to your banking app to view and resolve those payment requests.

Requirements
~~~~~~~~~~~~

Example business app
++++++++++++++++++++

An example business app is available in the `Gini Pay Business SDK's <https://github.com/gini/gini-pay-business-sdk-android>`_
repository.

You can use the same Gini Pay API client credentials in the example business app as in your app, if not otherwise
specified.

Development Gini Pay API client credentials
___________________________________________

In order to test using our example business app you need to use development client credentials. This will make sure
the Gini Pay Business SDK will use a payment provider which will open your development banking app.

End to end testing
~~~~~~~~~~~~~~~~~~

After you've set the client credentials in the example business app you can install it along with your banking app on
your device.

Run the example business app and import an invoice or take a picture of one to start the payment flow.

After following the integration steps above your banking app will be launched and you'll be able to fetch the payment
request, show the payment information and resolve the payment after the transaction has been confirmed. At this point,
you may redirect back to the business app.

With these steps completed you have verified that your app, the Gini Pay API, the Gini Pay Business SDK and the Gini Pay
Bank SDK work together correctly.

Testing in production
~~~~~~~~~~~~~~~~~~~~~

The steps are the same but instead of the development client credentials you will need to use production client
credentials. This will make sure the Gini Pay Business SDK receives real payment providers including the one which
opens your production banking app.

For testing the flow using the example business app please make sure that the production client credentials are used
before installing it.

You can also test with a real business app. Please contact us in case you don't know which business app(s) to install
for starting the payment flow.
