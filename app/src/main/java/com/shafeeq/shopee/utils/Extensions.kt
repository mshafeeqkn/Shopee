package com.shafeeq.shopee.utils

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.shafeeq.shopee.fragments.ItemListener
import org.w3c.dom.Text

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun EditText.onChange(cb: (str: String) -> Unit){
    this.removeWatcher()
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }
    }
    this.addTextChangedListener(watcher)
    this.tag = watcher
}

fun EditText.removeWatcher() {
    if(this.tag as TextWatcher? != null) {
        this.removeTextChangedListener(this.tag as TextWatcher)
    }
}

fun Activity.getGroupId(): String? {
    return SharedPreferenceHelper(this).getGroupId()
}

fun ImageView.setSrc(context: Context, id: Int) {
    this.setImageDrawable(ResourcesCompat.getDrawable(context.resources, id, context.applicationContext.theme))
}

fun String.isMalayalam(): Boolean {
    for(c in this.toCharArray()) {
        if(Character.UnicodeBlock.of(c) == Character.UnicodeBlock.MALAYALAM) {
            return true
        }
    }
    return false
}