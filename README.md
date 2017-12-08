Android String Obfuscator
=========================

Hide strings easily with that lib (and script)! It uses AES/ECB/PKCS5Padding transformation to convert strings with your app's SHA1 fingerprint.

Installation
------------

Put [AndroidStringObfuscator.jar](https://github.com/efraespada/AndroidStringObfuscator/raw/master/AndroidStringObfuscator.jar) in the root of the project.

#### Gradle

root_project/build.gradle
```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        ...
        classpath files('../AndroidObfuscatorPlugin/build/libs/stringobfuscatorplugin-1.0-SNAPSHOT.jar')
        ...
    }
}

// BOTTOM
apply plugin: com.efraespada.stringobfuscatorplugin.StringObfuscatorPlugin
```

root_project/app/build.gradle
```groovy
repositories {
    jcenter()
}

dependencies {
    implementation project(path: ':androidstringobfuscator')
    // compile 'efraespada:androidstringobfuscator:0.4.1'
}
```

Setup
-----
Initialize the library:
```java
AndroidStringObfuscator.init(getApplicationContext());
```


#### Encrypt
The script will encrypt all string tags with `hidden="true"` as attribute.

```xml
<resources>
	<string name="hello" hidden="true">hello world!</string>
	<string name="app_name">StringObfuscator</string>
</resources>
```

Or encrypt strings programmatically by doing:

```java
String encrypted = AndroidStringObfuscator.encryptString(some_string_var);
```

#### Decrypt
From resources:
```java
String decrypted = AndroidStringObfuscator.getString(R.string.hello);
```
Or from encrypted variables:
```java
String decrypted = AndroidStringObfuscator.decryptString(encrypted_var);
```
Sample
------

```java
AndroidStringObfuscator.init(getApplicationContext());

// getting encrypted string resources
int stringId = R.string.hello;

String message = getString(stringId);
message += " is ";
message += AndroidStringObfuscator.getString(stringId);

// and secret
String mySecret = "lalilulelo";

message += "\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " +
    AndroidStringObfuscator.encryptString(mySecret) +
    "\n\n.. or " +
    AndroidStringObfuscator.decryptString(AndroidStringObfuscator.encryptString(mySecret)) +
    "\"";

((TextView) findViewById(R.id.example)).setText(message);
```

<p align="center"><img width="40%" vspace="20" src="https://raw.githubusercontent.com/efraespada/AndroidStringObfuscator/master/sample.png"></p>

Gradle Console Output Example
-----------------------------
```
...
:sample:mergeDebugResources
:sample:initStringObfuscator
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - debug variant
:sample:obfuscator-script - SHA1 fingerprint: E1:28:0C:3E:65:91:2E:21:E9:98:2B:58:80:9A:25:3A:F6:88:7D:FF
:sample:obfuscator-script - [hello world!] - [D1862D9B434D08E..]
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - v 0.7
:sample:processDebugManifest UP-TO-DATE
...
```


#### More information
If you haven't installed Gradle before you compile:
```
...
:sample:obfuscator-script - Downloading https://services.gradle.org/distributions/gradle-2.14.1-all.zip
:sample:obfuscator-script - Unzipping /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv/gradle-2.14.1-all.zip to /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv
:sample:obfuscator-script - Set executable permissions for: /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv/gradle-2.14.1/bin/gradle
...
```

If `~/.android/debug.keystore` is missing, run your app to generate that file. For non default keystore file, check your project configuration.
```
...
:sample:obfuscator-script - debug variant
:sample:obfuscator-script - Missing keystore
:sample:obfuscator-script - SHA1 fingerprint not detected; try params [module] [variant] [optional:sha1]
...
```
License
-------
    Copyright 2017 Efraín Espada

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

