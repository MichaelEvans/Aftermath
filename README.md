Aftermath
=========

[![Build Status](https://travis-ci.org/MichaelEvans/Aftermath.svg)](https://travis-ci.org/MichaelEvans/Aftermath)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Aftermath-green.svg?style=flat)](https://android-arsenal.com/details/1/2176)

Aftermath handles the consequences of your `startActivityForResult` calls.

```java
// Make some call to startActivityForResult
public void startPicker(View view) {
    Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
    pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Aftermath.onActivityResult(this, requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
}

@OnActivityResult(PICK_CONTACT_REQUEST)
public void onContactPicked(int resultCode, Intent data) {
    // TODO: handle the result of your intent
}
```

Setup
------------
```groovy
buildscript {
    repositories {
        jcenter() // Also available in maven central
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    apt 'org.michaelevans:aftermath-processor:0.3.1'
    compile 'org.michaelevans:aftermath:0.3.1'
}
```

License
-------

    Copyright 2015 Michael Evans

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


