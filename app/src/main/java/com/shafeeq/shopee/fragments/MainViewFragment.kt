package com.shafeeq.shopee.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shafeeq.shopee.R
import java.util.*


class MainViewFragment : Fragment(), ItemListener {
    private lateinit var mShopItemList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        mShopItemList = root.findViewById(R.id.shopItemList)
        val data = arrayListOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        mShopItemList.layoutManager = LinearLayoutManager(requireActivity())
        val shopItemAdapter = NameAdapter(data, this)
        mShopItemList.adapter = shopItemAdapter

        val callback = DragManageAdapter(
            shopItemAdapter,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        )
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(mShopItemList)
        return root
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }

    override fun onClicked(name: String) {
        Toast.makeText(context, "The name is $name", Toast.LENGTH_SHORT).show()
    }
}

class NameAdapter(private val nameList : ArrayList<String>, private val listener : ItemListener) :
    RecyclerView.Adapter<NameAdapter.NameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return NameViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int)
    {
        holder.bindData(nameList[position], listener)
    }

    override fun getItemCount(): Int = nameList.size

    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private var tvName: CheckBox = itemView.findViewById(R.id.item_text)
        fun bindData(name : String , listener: ItemListener) {
            tvName.text = name
            tvName.setOnClickListener {
                listener.onClicked(name)
            }
        }
    }

    fun swapItems(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                nameList[i] = nameList.set(i+1, nameList[i])
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                nameList[i] = nameList.set(i-1, nameList[i])
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }
}

class DragManageAdapter(private var adapter: NameAdapter, dragDir: Int, swipeDir: Int):
    ItemTouchHelper.SimpleCallback(dragDir, swipeDir){
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }
}

interface ItemListener {
    fun onClicked(name :String)
}