Android String Obfuscator
=========================

Hide strings easily with that lib (and script)! It uses AES/ECB/PKCS5Padding transformation to convert strings with your app's SHA1 fingerprint.

Installation
------------

Put [AndroidStringObfuscator.jar](https://github.com/efraespada/AndroidStringObfuscator/raw/master/AndroidStringObfuscator.jar) in the root of the project.

#### Gradle 

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'efraespada:androidstringobfuscator:0.2'
}

android.applicationVariants.all{ variant ->

      variant.mergeResources.doLast{
          println  ":" + project.name + ":initStringObfuscator"
          javaexec {
              main = "-jar";
              args = [
                      "../AndroidStringObfuscator.jar",
                      project.name,
                      variant.dirName
              ]
          }
      }
}
```

Setup
-----
Init the library:
```
AndroidStringObfuscator.init(this);
```


#### Encrypt Strings
The script will encrypt all string tags with `hidden="true"` as attribute.

```xml
<resources>
	<string name="hello">hello world!</string>
	<string name="app_name" hidden="true">StringObfuscator</string>
</resources>
```

Or encrypt strings programmatically by doing:

```java
String encrypted = AndroidStringObfuscator.simulateString(some_string_var);
```

#### Decrypt Strings
```java
String decrypted = AndroidStringObfuscator.getString(R.string.app_name);
```

Gradle Console Output Example
-----------------------------
```
...
:sample:mergeDebugResources
:sample:initStringObfuscator
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - debug variant
:sample:obfuscator-script - SHA1 fingerprint: E1:28:0C:3E:65:91:2E:21:E9:98:2B:58:80:9A:25:3A:F6:88:7D:FF
:sample:obfuscator-script - [StringObfuscato..] - [7CFBFBEE31ABA92..]
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - v 0.5
:sample:processDebugManifest UP-TO-DATE
...
```

#### Possible errors
Missing `~/.android/debug.keystore`. Run your app to generate that file.
```
...
:sample:mergeDebugResources
:sample:initStringObfuscator
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - debug variant
:sample:obfuscator-script - Missing keystore
:sample:obfuscator-script - SHA1 fingerprint not detected; try params [module] [variant] [optional:sha1]
:sample:obfuscator-script - -----------------------------------------------------------------------------
:sample:obfuscator-script - v 0.5
:sample:processDebugManifest
...
```

#### More Info
At first run, if you haven't installed Gradle:
```
...
:sample:obfuscator-script - Downloading https://services.gradle.org/distributions/gradle-2.14.1-all.zip
:sample:obfuscator-script - Unzipping /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv/gradle-2.14.1-all.zip to /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv
:sample:obfuscator-script - Set executable permissions for: /Users/efraespada/.gradle/wrapper/dists/gradle-2.14.1-all/8bnwg5hd3w55iofp58khbp6yv/gradle-2.14.1/bin/gradle
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

