package com.shafeeq.shopee.utils

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun EditText.onChange(cb: (str: String) -> Unit){
    this.addTextChangedListener(object: TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) { cb(s.toString()) }
    })
}

fun Activity.getGroupId(): String? {
    return SharedPreferenceHelper(this).getGroupId()
}