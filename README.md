Gini Pay Bank SDK for Android
===============================

Documentation
-------------
[Integration Guide](https://developer.gini.net/gini-pay-bank-sdk-android/html/)


Installation
------------

To install add our Maven repo to the root build.gradle file and add it as a dependency to your app
module's build.gradle.

build.gradle:

```
repositories {
    maven {
        url 'https://repo.gini.net/nexus/content/repositories/open
    }
}
```

app/build.gradle:

```
dependencies {
    implementation 'net.gini:gini-pay-bank-sdk:1.0.0'
}
```

## License

Gini Pay Bank SDK is available under a commercial license.
See the LICENSE file for more info.
