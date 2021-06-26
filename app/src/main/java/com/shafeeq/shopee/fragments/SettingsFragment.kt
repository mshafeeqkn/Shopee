package com.shafeeq.shopee.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.getGroupId

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_settings, container, false)
        root.findViewById<EditText>(R.id.groupId).setText(requireActivity().getGroupId())
        return root
    }
}