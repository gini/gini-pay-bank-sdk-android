Customization
=============

Customization is achieved through Android's resourcing system. This means that all `resources of the Gini Pay Bank SDK
<https://github.com/gini/gini-pay-bank-sdk-android/tree/main/ginipaybank/src/main/res>`_ can be overridden by providing
resources with the same name in your application. 

The capture related screens are provided by the `Gini Capture SDK <https://github.com/gini/gini-capture-sdk-android>`_
and for those screens you need to override `it's resources
<https://github.com/gini/gini-capture-sdk-android/tree/main/ginicapture/src/main/res>`_. Please consult the
`customization guide <https://developer.gini.net/gini-capture-sdk-android/html/customization-guide.html>`_ for the Gini
Capture SDK to view a screen based overview of the customizable resources.

When you override styles please make sure you that you use the parent style with the ``Root.`` prefix. This ensures that
your custom style items are merged with the default ones.