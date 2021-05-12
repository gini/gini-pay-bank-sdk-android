Getting started
===============

Installation
------------

To install add our Maven repo to the root build.gradle file and add it as a dependency to your app
module's build.gradle.

build.gradle:

.. code-block:: groovy

    repositories {
        maven {
            url 'https://repo.gini.net/nexus/content/repositories/open
        }
    }

app/build.gradle:

.. code-block:: groovy

    dependencies {
        implementation 'net.gini:gini-pay-bank-sdk:1.0.0'
    }

Integration
-----------

The entry point into the library is GiniPayBank object which lets you interact with the Capture and Payment features.
The Capture feature is a layer of abstraction above `Gini Capture SDK <https://github.com/gini/gini-capture-sdk-android/>`_ and the Return Assistant feature.

Capture feature can be used with:
 - the Screen API by calling ``startCaptureFlow`` or ``startCaptureFlowForIntent``.
 - the Component API by building everything around the provided fragments.
See example apps for more details about usage of Screen and Component APIs.

To use capture features, they need to be configured with ``setCaptureConfiguration``.
Note that configuration is immutable. ``releaseCapture`` needs to be called before passing a new configuration.

To use the pay feature you need to add to your manifest an intent filter for the ginipay URI:

.. code-block:: kotlin

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />

        <data
            android:host="payment"
            android:scheme="ginipay" />
    </intent-filter>