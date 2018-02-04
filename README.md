<p align="center"><img width="10%" vspace="20" src="https://raw.githubusercontent.com/StringCare/AndroidLibrary/develop/sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>



String Care Android Library
=========================

Hide strings easily with that lib and plugin! It uses AES/ECB/PKCS5Padding transformation to convert strings with your app's SHA1 fingerprint.

Gradle implementation
------------

```groovy
// root_project/build.gradle
  
buildscript { 
 
    ext {
        stringcare_version = '0.8'
    }
    
    repositories {
        jcenter()
    }
    
    dependencies {
        classpath "com.stringcare:plugin:$stringcare_version"
    }
     
}
 
apply plugin: StringCare
```

```groovy
// root_project/your_module/build.gradle

repositories {
    jcenter()
} 
 
dependencies {
    implementation "com.stringcare:library:$stringcare_version"
}
```

Setup
-----
StringCare library needs the global application's `Context` for access to `PackageManager` and get signatures.
In your `app` (or main) module the package name is obtained from `Context`:
```java
SC.init(getApplicationContext());
```

In the rest of modules (or libraries) you must pass an `Object` in order to obtain its package name:
```java
SC.initForLib(getApplicationContext(), this);
```


#### Encrypt
The plugin will encrypt all string tags with `hidden="true"` as attribute.

```xml
<resources>
    <string name="app_name">StringObfuscator</string>
    <string name="hello" hidden="true">hello world!</string>
    <string name="test_a" hidden="true">%1$s (%2$d)</string>
</resources>
```

Or encrypt strings programmatically by doing:

```java
String encrypted = SC.encryptString(string_var);
```

#### Decrypt
From resources:
```java
String decrypted = SC.getString(R.string.hello);
String decrypted = SC.getString(R.string.test_a, "hi", 3); // hi (3) 
```
Or from encrypted variables:
```java
String decrypted = SC.decryptString(encrypted_var);
```
Sample
------

```java
SC.init(getApplicationContext());

// getting encrypted string resources
int stringId = R.string.hello;

String message = getString(stringId);
message += " is ";
message += SC.getString(stringId);

// and secret
String mySecret = "lalilulelo";

message += "\n\nFor Metal Gear lovers:\n\n\"Snake, the password is " +
    SC.encryptString(mySecret) +
    "\n\n.. or " +
    SC.decryptString(SC.encryptString(mySecret)) +
    "\"";

((TextView) findViewById(R.id.example)).setText(message);
```

<p align="center"><img width="40%" vspace="20" src="https://raw.githubusercontent.com/efraespada/AndroidStringObfuscator/master/sample.png"></p>

Library Sample
--------------
```java
public class YourApplication extends Application {
 
    @Override
    public void onCreate() {
        super.onCreate();
        Library.init(this);
        String secretPass = Library.getPassword(); // should return -> =^UCrE4zR#}kpCu~
    }
 
}
```
In library package:
```java
public class Library {
    
    private Library() {
        // ..
    }
    
    public static void init(Context context) {
        SC.initForLib(context, new Library());
    }
    
    public static String getPassword() {
        return SC.getString(your.lib.module.R.string.pass);
    }
 
}
```
```xml
<resources>
    <string name="pass" hidden="true">=^UCrE4zR#}kpCu~</string>
</resources>
```

Configuration
-----------------------------
By default the plugin will encrypt every `strings.xml` file inside `src/main`folder but you can choose a different configuration.
```groovy
// root_folder/build.gradle
 
apply plugin: StringCare
 
stringcare {
 
    debug true  // prints details
 
    modules {
     
        sample {
            stringFiles = ['strings.xml',"other_file.xml"]
            srcFolders = ['src/main', "other_folder"]
        }
        
        // root_folder/sample/src/main/res/.../strings.xml
        // root_folder/sample/src/main/res/.../other_file.xml
        // root_folder/sample/other_folder/res/.../strings.xml
        // root_folder/sample/other_folder/res/.../other_file.xml
        
        other_module {
            srcFolders = ['src/moduleB']
        }
        
        // root_folder/other_module/src/moduleB/res/.../strings.xml
        
        other_module_ {} // 
        
        // root_folder/other_module_/src/main/res/.../strings.xml
        
    }
     
}
```

Gradle Console Output Example
-----------------------------
```
...
:sample:generateDebugResValues UP-TO-DATE
:sample:generateDebugResources UP-TO-DATE
:sample:mergeDebugResources
:sample:debug:B8:DC:47:58:9B:5F:2C:21:45:C4:04:37:0E:56:53:DC:24:6B:2C:66
:sample:backupStringResources
	- values/strings.xml
:sample:encryptStringResources
	- values/strings.xml
		[hello world!] - [A8590C43DA85D67..]
:sample:mergeDebugResources UP-TO-DATE
:sample:restoreStringResources
	- values/strings.xml
:sample:createDebugCompatibleScreenManifests UP-TO-DATE
...
```
    
Plugin won't work if there is no config defined for the selected variant:
```bash
...
:sample:mergeReleaseResources
    🤯 no config defined for variant release
:sample:createReleaseCompatibleScreenManifests
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

