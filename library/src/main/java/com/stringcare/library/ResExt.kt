package com.stringcare.library

import android.content.res.Resources
import android.os.Build
import java.util.*

fun Resources.locale(): Locale {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> this.configuration.locales.get(0)
        else -> this.configuration.locale
    }
}