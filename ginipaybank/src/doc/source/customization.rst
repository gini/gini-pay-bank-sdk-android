Customization
=============

Customization of is achieved through Android's resourcing system.
This means that all `resources of the library <https://github.com/gini/gini-pay-bank-sdk-android/tree/main/ginipaybank/src/main/res>`_
can be overridden by providing resources with the same name in the
application. Because the library wraps Gini Capture SDK for those
screens you need to override `resources from Capture SDK <https://github.com/gini/gini-capture-sdk-android/tree/main/ginicapture/src/main/res>`_.

Some attributes are set inside the library (styles that start with
``Root.``) to provide the basic UI, but you can hook into the other
styles to customize the screen further.

`See styles file in Gini Bank SDK <https://github.com/gini/gini-pay-bank-sdk-android/blob/main/ginipaybank/src/main/res/values/styles.xml>`_

`See styles file in Gini Capture SDK <https://github.com/gini/gini-capture-sdk-android/blob/main/ginicapture/src/main/res/values/styles.xml>`_