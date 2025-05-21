package cz.novavesodpad.util

import android.util.Log

/**
 * Simple logger interface
 */
interface Logger {
    fun debug(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * Implementation of logger that logs to Android's LogCat
 */
class LogcatLogger : Logger {
    override fun debug(message: String) {
        Log.d(TAG, message)
    }

    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }

    companion object {
        private const val TAG = "NovaVesOdpad"
    }
}