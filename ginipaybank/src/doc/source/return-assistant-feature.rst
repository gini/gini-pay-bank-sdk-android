Return Assistant Feature
========================

The return assistant feature is helpful for users who are partially paying an invoice with multiple items. It helps
users pay only for the items they are keeping. The line items from the scanned invoice are shown in a list and users
can then deselect the items they are returning and the total price is automatically updated to contain only the items
they are keeping. This dynamically calculated total price is returned to the client applications in the ``amountToPay``
extraction.

Users can also edit the line items shown in the return assistant. We extended GVL's API to also return the line items
including any modifications the users have made.

Requirements
^^^^^^^^^^^^

To use this feature your client id must be configured to include line item extractions.

Enable the Return Assistant
^^^^^^^^^^^^^^^^^^^^^^^^^^^

The return assistant is enabled by default. You can disable it when creating a ``CaptureConfiguration`` instance:

.. code-block:: java

    GiniPayBank.setCaptureConfiguration(
            CaptureConfiguration(
                returnAssistantEnabled = false
            )
        )

Screens
^^^^^^^

The return assistant consists of two screens: the Digital Invoice Screen and the Line Item Details Screen.

Digital Invoice Screen
~~~~~~~~~~~~~~~~~~~~~~

TODO: check if this is still accurate

This is the main screen of the return assistant. It displays the line items from the invoice along with additional
costs, discounts and the total price. It also allows users to pick a return reason when deselecting a line item.

If you use the Screen API, then this screen is shown by the Analysis Screen when line item extractions were received.

If you use the Component API, then you need to implement the new
``AnalysisFragmentListener.onProceedToReturnAssistant()`` method and show the ``DigitalInvoiceFragment`` when its
called.

Users can deselect line items and the total price is updated accordingly. By tapping on a line item users can go to the
Line Item Details Screen to edit it.

The updated total price and the line items are returned to your application when the user taps the pay button.

Customizing the UI
++++++++++++++++++

TODO: check link

Detailed description of the customization options is available in the
`Customization Guide <ui-customization.html#return-assistant-feature>`_.

Line Item Details Screen
~~~~~~~~~~~~~~~~~~~~~~~~

TODO: check if this is still accurate

This screen shows the details of a line item and allows the user to edit them. The changes are taken over by the Digital
Invoice Screen when the user taps on the save button.

If you use the Screen API, then this screen is shown by the Digital Invoice Screen when a user taps on a line item.

If you use the Component API, then you need to implement the ``DigitalInvoiceFragmentListener`` and show the
``LineItemDetailsFragment`` when the ``onEditLineItem()`` method is called.

Customizing the UI
++++++++++++++++++

TODO: check link

Detailed description of the customization options is available in the
`Customization Guide <ui-customization.html#return-assistant-feature>`_.

Receiving the results
^^^^^^^^^^^^^^^^^^^^^

TODO: check if these are still accurate

Total price of the selected line items
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The total price is returned in the ``amountToPay`` extraction. If you use the Screen API, then you don't need to change
anything.

If you use the Component API, then you need to use the new signature of the
``AnalysisFragmentListener.onExtractionsAvailable()``. No other changes needed.

Line items
~~~~~~~~~~

If you use the Screen API, then the ``CameraActivity`` returns an additional extra in the
``CameraActivity.EXTRA_OUT_COMPOUND_EXTRACTIONS`` containing a map of compound extraction labels as keys and the
compound extractions as values. Currently the only compound extractions returned are the line items which have the
``lineItems`` label.

If you use the Component API, then you need to use the new signature of the
``AnalysisFragmentListener.onExtractionsAvailable()`` which now also returns the map of compound extractions. This map
is identical to the one the CameraActivity returns in the Screen API.