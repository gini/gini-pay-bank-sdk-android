Gini Pay Bank SDK for Android
===============================

.. note:: **Deprecation Notice**

   The Gini Pay Bank SDK for Android was replaced by the new `Gini Bank SDK
   <https://github.com/gini/gini-mobile-android/blob/main/bank-sdk/>`_.

   This SDK won't be developed further and we kindly ask you to switch to the new Gini Bank SDK. Migration entails
   only a few steps which you can find in this `migration guide
   <https://github.com/gini/gini-mobile-android/blob/main/bank-sdk/migrate-from-other-bank-sdk.md>`_.

Introduction
------------

The Gini Pay Bank SDK for Android provides all the UI and functionality needed to use the Gini Pay API in your app to
extract payment information from invoices and to resolve payment requests originating from other apps.

The Gini Pay API provides an information extraction service for analyzing invoices. Specifically it extracts information
such as the document sender or the payment relevant information (amount to pay, IBAN, etc.). In addition it also
provides a secure channel for sharing payment related information between clients. 

Table of contents
-----------------

.. toctree::
    :maxdepth: 2

    getting-started
    integration
    capture-features
    customization
    migrating-from-gvl
    reference
    license
