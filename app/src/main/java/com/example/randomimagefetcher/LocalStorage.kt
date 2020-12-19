package com.example.randomimagefetcher

import android.content.Context
import android.content.SharedPreferences

class LocalStorage(context: Context) {

    var sharedPref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.pref_name), Context.MODE_PRIVATE
    )

    fun setStringValue(key: String, value: String) {
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun setIntegerValue(key: String, value: Int) {
        with(sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun setLongValue(key: String, value: Long) {
        with(sharedPref.edit()) {
            putLong(key, value)
            apply()
        }
    }

    fun setBooleanValue(key: String, value: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getStringValue(key: String, default: String? = null): String? =
        sharedPref.getString(key, default)


    fun getIntValue(key: String, default: Int = -1): Int =
        sharedPref.getInt(key, default)

    fun getBooleanValue(key: String, default: Boolean = false): Boolean =
        sharedPref.getBoolean(key, default)

}