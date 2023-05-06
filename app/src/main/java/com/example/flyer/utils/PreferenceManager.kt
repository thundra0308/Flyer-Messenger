package com.example.flyer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PreferenceManager {
    private lateinit var sharedPref: SharedPreferences
    constructor(context: Context) {
        sharedPref = context.getSharedPreferences(Constants.KEY_REFERENCE_NAME, Context.MODE_PRIVATE)
    }
    fun putBoolean(key: String, value: Boolean) {
        var editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }
    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key,false)
    }
    fun putString(key: String, value: String) {
        var editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(key,value)
        editor.apply()
    }
    fun getString(key: String): String? {
        return sharedPref.getString(key,null)
    }
    fun clear() {
        var editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
}