#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_stringcare_library_SC_sign(
        JNIEnv *env,
        jclass /* this */,
        jstring key) {
    std::string hello = "Hello from C++";
    // return env->NewStringUTF(hello.c_str());
    return key;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_stringcare_library_SC_jniObfuscate(
        JNIEnv *env,
        jclass /* this */,
        jobject context,
        jstring key,
        jstring value) {
    std::string hello = "Hello from C++";
    // return env->NewStringUTF(hello.c_str());
    return value;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_stringcare_library_SC_jniDeobfuscate(
        JNIEnv *env,
        jclass /* this */,
        jobject context,
        jstring key,
        jstring value) {
    std::string hello = "Hello from C++";
    // return env->NewStringUTF(hello.c_str());
    return value;
}
