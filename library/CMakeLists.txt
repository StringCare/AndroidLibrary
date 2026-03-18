# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.22.1)
project("stringcare-native" CXX)

# Prefer submodule path (stringcare-jni), fallback to sibling repo
if(EXISTS "${CMAKE_SOURCE_DIR}/../stringcare-jni/lib/sc-native-lib.cpp")
    set(JNI_SOURCE_DIR "${CMAKE_SOURCE_DIR}/../stringcare-jni/lib")
else()
    set(JNI_SOURCE_DIR "${CMAKE_SOURCE_DIR}/../../stringcare-jni-android-library/lib")
endif()

add_library(sc-native-lib SHARED
    ${JNI_SOURCE_DIR}/sc-native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(sc-native-lib ${log-lib})

# 16 KB page size support for Android 15+ / Google Play (Nov 2025+)
target_link_options(sc-native-lib PRIVATE "-Wl,-z,max-page-size=16384")

set_target_properties(sc-native-lib PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
)
