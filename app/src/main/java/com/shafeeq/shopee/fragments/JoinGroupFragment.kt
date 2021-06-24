package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.FirebaseDatabase
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.SharedPreferenceHelper

class JoinGroupFragment : Fragment() {
    private lateinit var mSubmitButton: Button
    private lateinit var mUsernameEt: EditText
    private lateinit var mGroupIdEt: EditText

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_join_group, container, false)
        val memory = SharedPreferenceHelper(requireActivity())
        if (memory.getGroupId() != null) {
            navigateToMain()
        } else {
            mSubmitButton = root.findViewById<Button>(R.id.submit).apply {
                setOnClickListener {
                    if ((it as Button).text == "GENERATE GROUP ID") {
                        it.text = "GO AHEAD"
                        val groupId = FirebaseDatabase.getInstance().reference.push().key.toString()
                        mGroupIdEt.setText(groupId)
                    } else {
                        memory.setGroupId(mGroupIdEt.text.toString())
                        navigateToMain()
                    }
                }
            }
            mUsernameEt = root.findViewById(R.id.username)
            mGroupIdEt = root.findViewById(R.id.groupId)
        }
        return root
    }

    private fun navigateToMain() {
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val action = JoinGroupFragmentDirections.actionJoinGroupFragment2ToMainViewFragment2()
        navController.navigate(action)
    }
}