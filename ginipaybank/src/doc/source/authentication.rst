Authentication
==============

Capture Feature
---------------

Communication with the Gini Pay API is not part of the Gini Capture SDK in order to allow clients
the freedom to use a networking implementation of their own choosing. The quickest way to add
networking is to use the Gini Capture Network Library, which you can use by providing
``GiniCaptureNetworkService`` and ``GiniCaptureDefaultNetworkApi`` to the configuration.
There are helper methods which provide minimal configuration for Service and API:

* .. code-block:: java

    getDefaultNetworkService(context: Context, clientId: String, clientSecret: String, 
                             emailDomain: String, documentMetadata: DocumentMetadata)
* .. code-block:: java 
  
    getDefaultNetworkApi(service: GiniCaptureDefaultNetworkService)

You may also use the Gini Pay API Library for Android or implement communication with the Gini Pay API yourself.

Pay Feature
-----------
The pay feature depends on the Gini Pay API Library, which provides an entry point through ``Gini`` class.
The ``Gini`` class can be built either with client credentials (clientId and clientSecret)
or with a ``SessionManager`` if you have a token. For these two cases there are helper methods:

* .. code-block:: java

    getGiniApi(context: Context, clientId: String, clientSecret: String, 
               emailDomain: String)
* .. code-block:: java 
  
    getGiniApi(context: Context, sessionManager: SessionManager)

``SessionManager`` is an interface which you need to implement to send the token.

For more details about ``Gini`` see `Gini Pay API lib <https://github.com/gini/gini-pay-api-lib-android/>`_.