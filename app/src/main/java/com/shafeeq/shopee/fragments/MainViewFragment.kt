package com.shafeeq.shopee.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shafeeq.shopee.R
import java.util.*


private const val ITEM = 0
@Suppress("unused")
private const val SECT = 1

data class ShopItem(
    var name: String = "",
    var type: Int = ITEM
) {
    override fun toString(): String {
        return name
    }
}

class MainViewFragment : Fragment(), ItemListener {
    private lateinit var mShopItemList: RecyclerView
    private val mDataList = ArrayList<ShopItem>()
    private lateinit var mAdapter: ShopListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        setHasOptionsMenu(true)

        mShopItemList = root.findViewById(R.id.shopItemList)
        mDataList.addAll(arrayListOf(
            ShopItem("Item 1"),
            ShopItem("Item 2", SECT),
            ShopItem("Item 3"),
            ShopItem("Item 4"),
            ShopItem("Item 5")
        ))
        mShopItemList.layoutManager = LinearLayoutManager(requireActivity())
        mAdapter = ShopListAdapter(mDataList, this)
        mShopItemList.adapter = mAdapter

        val callback = DragManageAdapter(
            mAdapter,
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_notify) {
            mAdapter.notifyDataSetChanged()
        }
        return true
    }
}

class ShopListAdapter(private val nameList : ArrayList<ShopItem>, private val listener : ItemListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private var mItemName: CheckBox = itemView.findViewById(R.id.item_text)
        fun bindData(name : String , listener: ItemListener) {
            mItemName.text = name
            mItemName.setOnClickListener {
                listener.onClicked(name)
            }
        }
    }

    class SectionHeadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var mSectionName: TextView = itemView.findViewById(R.id.section_name)
        fun bindData(name: String) {
            mSectionName.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == ITEM) {
            val view = inflater.inflate(R.layout.list_item_layout, parent, false)
            ShopItemViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.shop_list_seperator, parent, false)
            SectionHeadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        if(nameList[position].type == ITEM) {
            (holder as ShopItemViewHolder).bindData(nameList[position].toString(), listener)
        } else {
            (holder as SectionHeadingViewHolder).bindData(nameList[position].toString())
        }
    }

    override fun getItemCount(): Int = nameList.size

    override fun getItemViewType(position: Int): Int {
        return nameList[position].type
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

    fun getItemType(position: Int): Int {
        return nameList[position].type
    }
}

class DragManageAdapter(private var adapter: ShopListAdapter, dragDir: Int, swipeDir: Int):
    ItemTouchHelper.SimpleCallback(dragDir, swipeDir){
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Item should not move over first item since the first item
        // will be always a heading
        if(target.adapterPosition == 0)
            return false

        adapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Don't move section heading
        if(adapter.getItemType(viewHolder.adapterPosition) == SECT)
             return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }
}

interface ItemListener {
    fun onClicked(name :String)
}