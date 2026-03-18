#!/bin/sh

rm -rf KotlinSample

if [[ -d KotlinSample ]]
then
    rm -rf KotlinSample
fi

git clone https://github.com/StringCare/KotlinSample.git

cd KotlinSample/

echo "sdk.dir=/Users/efrainespada/Library/Android/sdk" > local.properties

./gradlew build
./gradlew signingReport
