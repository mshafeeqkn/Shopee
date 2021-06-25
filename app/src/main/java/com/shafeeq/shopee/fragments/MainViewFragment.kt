package com.shafeeq.shopee.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.shafeeq.shopee.R
import java.util.*


class MainViewFragment : Fragment() {
    private lateinit var mDataSet: ArrayList<String>
    private lateinit var mShopItemList: RecyclerView
    private lateinit var mItemTouchHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        mShopItemList = root.findViewById(R.id.shopItemList)
        val data = arrayListOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        return root
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }
}