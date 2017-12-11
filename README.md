Android String Obfuscator
=========================

Hide strings easily with that lib and plugin! It uses AES/ECB/PKCS5Padding transformation to convert strings with your app's SHA1 fingerprint.

Gradle implementation
------------

```groovy
// root_project/build.gradle
 
apply plugin: com.efraespada.stringobfuscatorplugin.StringObfuscatorPlugin
 
buildscript { 
 
    ext {
        aso_sop_version = '0.5.0'
    }
    
    repositories {
        jcenter()
    }
    
    dependencies {
        classpath "com.efraespada:stringobfuscatorplugin:$aso_sop_version"
    }
     
}
```

```groovy
// root_project/your_module/build.gradle

repositories {
    jcenter()
} 
 
dependencies {
    implementation "efraespada:androidstringobfuscator:$aso_sop_version"
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


Configuration
-----------------------------
By default the plugin will encrypt every `strings.xml` file inside `src/main`folder but you can choose a different configuration.
```groovy
// root_folder/build.gradle
apply plugin: com.efraespada.stringobfuscatorplugin.StringObfuscatorPlugin
 
stringobfuscator {
 
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

