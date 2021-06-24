package com.shafeeq.shopee.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


class SharedPreferenceHelper(activity: Activity) {
    private val mPreferences: String = "APP_PREFERENCES"
    private val mGroupId: String = "GROUP_ID"

    private var gid: String? = null
    private var mPreference: SharedPreferences = activity.getSharedPreferences(mPreferences, Context.MODE_PRIVATE)

    fun setGroupId(gid: String?) {
        val editor: SharedPreferences.Editor = mPreference.edit()
        editor.putString(mGroupId, gid)
        editor.apply()
    }

    fun getGroupId(): String? {
        if(gid == null)
            gid = mPreference.getString(mGroupId, null)
        return gid
    }
}