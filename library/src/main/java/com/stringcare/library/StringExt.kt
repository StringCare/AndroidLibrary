package com.stringcare.library

fun Int.string(): String = SC.context.getString(this)

fun Int.reveal(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version)

fun Int.reveal(
        vararg formatArgs: Any,
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version, formatArgs)

fun String.obfuscate(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.obfuscate(this, androidTreatment, version)

fun String.reveal(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version)
