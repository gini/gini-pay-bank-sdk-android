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
        implementation 'net.gini:gini-pay-bank-sdk:1.1.0'
    }

Gini Pay API Client Credentials
-------------------------------

You should have received Gini Pay API client credentials from us. Please get in touch with us in case you don't have them.

Continue to `Authentication <authentication.html>`_ to see how to use the client credentials to initialize the Gini Pay
Business SDK.

Integration
-----------

The entry point into the library is GiniPayBank object which lets you interact with the `Capture <capture.html>`_ and
`Payment <pay.html>`_ features.