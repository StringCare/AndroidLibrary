Android String Obfuscator
=======================

Hide strings easyly with that lib! It uses AES/ECB/PKCS5Padding transformation to convert strings with your app's SHA1 fingerprint.

Installation
------------

Put [AndroidStringObfuscator.jar](https://github.com/efraespada/AndroidStringObfuscator/raw/master/sample/AndroidStringObfuscator.jar) on the app's module folder, next to `build.gradle` file and `build` folder.

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
        def path = "build" + File.separator + "intermediates" + File.separator + "res" + File.separator + "merged" + File.separator +  variant.dirName + File.separator + "values" + File.separator + "values.xml"
        def stringsFile = file(path)
        if (stringsFile.isFile()) {
            javaexec {
                main = "-jar";
                args = [
                        "AndroidStringObfuscator.jar",
                        path,
                        variant.dirName,
                        project.name
                ]
            }
            def stringsFileObfus = file("string_obfuscation/strings.xml")
            stringsFile.write(stringsFileObfus.getText('UTF-8'))
            stringsFileObfus.delete()
        } else logger.error("strings.xml file couldn't be found: " + path)
    }
}
```


Get encrypted strings
---------------------
You don't need to do anything. The script will encrypt all string tags with `hidden="true"` as attribute.

```xml
<resources>
	<string name="hello">hello world!</string>
	<string name="app_name" hidden="true">StringObfuscator</string>
</resources>
```


Get decrypted strings
---------------------
```java
String decrypted = AndroidStringObfuscator.getString(context, R.string.app_name);
```


License
-------
    Copyright 2016 Efraín Espada

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

