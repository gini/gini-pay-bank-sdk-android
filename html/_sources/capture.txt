Capture
=======

The Capture feature is a layer of abstraction above `Gini Capture SDK
<https://github.com/gini/gini-capture-sdk-android/>`_ with the additional Return Assistant feature.

The capture feature provides Activities and Fragments for capturing,
reviewing and analyzing photos of invoices and remittance slips.

By integrating this feature into your application you can allow your
users to easily take pictures of documents, review them
and - by implementing the necessary networking interfaces - upload
the document to the Gini Pay API for analysis.

See authentication section for more details on configuring communication with the backend.

Capture feature can be used with:

* the Screen API by calling ``startCaptureFlow`` or ``startCaptureFlowForIntent`` or
* the Component API by building everything around the provided fragments.

See example apps for more details about usage of Screen and Component APIs.

To use capture features, they need to be configured with ``setCaptureConfiguration``.
Note that configuration is immutable. ``releaseCapture`` needs to be called before passing a new configuration.

The capture feature consists of the following screens:

* Onboarding: Provides useful hints to the user on how to take a perfect photo of a document.
* Camera: The actual camera screen to capture the image of the document.
* Review: Offers the opportunity to the user to check the sharpness of the image and to rotate it into reading direction, if necessary.
* Analysis: Provides a UI for the analysis process of the document by showing the user a loading indicator and the image of the document.
* Digital Invoice: Provides UI in which the user case unselect and edit extracted line items.