package components

import StringCare
import org.slf4j.LoggerFactory

class PrintUtils {

    companion object {
        private var variant: String? = null
        private var module: String? = null
        private val logger = LoggerFactory.getLogger(StringCare::class.java)

        fun init(module: String, variant: String) {
            PrintUtils.module = module
            PrintUtils.variant = variant
        }

        private fun _print(value: String) {
            println(value)
        }

        fun print(message: String, tab: Boolean = false) {
            if (variant != null && module != null) {
                if (!tab) {
                    _print(":$module:$message")
                } else {
                    _print("\t" + message)
                }
            } else {
                _print(message)
            }
        }

        fun print(module: String?, message: String, tab: Boolean = false) {
            if (module != null) {
                if (!tab) {
                    _print(":$module:$message")
                } else {
                    _print("\t" + message)
                }
            } else {
                _print(message)
            }
        }

        fun uncapitalize(value: String): String {
            return value.substring(0, 1).toLowerCase() + value.substring(1, value.length)
        }
    }

}
