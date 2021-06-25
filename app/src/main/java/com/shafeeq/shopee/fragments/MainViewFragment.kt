package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.getGroupId
import java.util.*


private const val ITEM = 0

@Suppress("unused")
private const val SECT = 1

data class ShopItem(
    var name: String = "",
    var type: Int = ITEM,
    var id: String = "",
    var checked: Boolean = false,
) {
    override fun toString(): String {
        return name
    }
}

class MainViewFragment : Fragment(), ItemListener {
    private lateinit var mShopItemList: RecyclerView
    private val mDataList = ArrayList<ShopItem>()
    private lateinit var mAdapter: ShopListAdapter
    private lateinit var mTouchHelper: ItemTouchHelper
    private lateinit var mInputEt: EditText
    private lateinit var mAddBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        setHasOptionsMenu(true)
        mShopItemList = root.findViewById(R.id.shopItemList)
        mInputEt = root.findViewById(R.id.newItemName)
        mAddBtn = root.findViewById(R.id.addBtn)

        val groupId = requireActivity().getGroupId()
        mAddBtn.setOnClickListener {
            val shopItem = ShopItem(name = mInputEt.text.toString().trim(), type = ITEM)
            shopItem.id = FirebaseDatabase.getInstance().getReference("$groupId/itemList")
                .push().key.toString()
            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}")
                .setValue(shopItem)
            mInputEt.setText("")
        }

        mShopItemList.layoutManager = LinearLayoutManager(requireActivity())
        mAdapter = ShopListAdapter(mDataList, this)
        mShopItemList.adapter = mAdapter

        val callback = DragManageAdapter(
            mAdapter,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        )
        mTouchHelper = ItemTouchHelper(callback)
        mTouchHelper.attachToRecyclerView(mShopItemList)

        FirebaseDatabase.getInstance().getReference("$groupId/itemList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mDataList.clear()
                    mDataList.add(ShopItem(name = "Non-Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (!item.checked)
                            mDataList.add(item)
                    }
                    mDataList.add(ShopItem(name = "Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (item.checked)
                            mDataList.add(item)
                    }
                    mShopItemList.post { mAdapter.notifyDataSetChanged() }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        return root
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }

    override fun onChecked(name: String, position: Int, isChecked: Boolean) {
        val item = mDataList[position]
        item.checked = isChecked
        val groupId = requireActivity().getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)
        if (isChecked)
            mAdapter.swapItem(position, mAdapter.itemCount - 1)
        else
            mAdapter.swapItem(position, 1)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notify) {
            mAdapter.notifyDataSetChanged()
        }
        return true
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        mTouchHelper.startDrag(viewHolder)
    }
}

class ShopListAdapter(
    private val nameList: ArrayList<ShopItem>,
    private val listener: ItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mRecyclerView: RecyclerView

    class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mItemName: CheckBox = itemView.findViewById(R.id.item_text)
        private var mDragIcon: ImageView = itemView.findViewById(R.id.drag_icon)

        @SuppressLint("ClickableViewAccessibility")
        fun bindData(name: String, listener: ItemListener, checked: Boolean) {
            mItemName.text = name
            mItemName.isChecked = checked
            updateCheckboxView(mItemName, checked)
            mItemName.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    updateCheckboxView(view, isChecked)
                    listener.onChecked(name, adapterPosition, isChecked)
                }
            }
            mDragIcon.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    listener.requestDrag(this)
                }
                return@setOnTouchListener false
            }
        }

        private fun updateCheckboxView(view: CompoundButton, isChecked: Boolean) {
            if (isChecked) {
                view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                view.alpha = 0.3F
            } else {
                view.paintFlags = view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                view.alpha = 1.0F
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    class SectionHeadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (nameList[position].type == ITEM) {
            (holder as ShopItemViewHolder).bindData(
                nameList[position].toString(),
                listener,
                nameList[position].checked
            )
        } else {
            (holder as SectionHeadingViewHolder).bindData(nameList[position].toString())
        }
    }

    override fun getItemCount(): Int = nameList.size

    override fun getItemViewType(position: Int) = nameList[position].type

    fun swapItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val item = nameList.removeAt(fromPosition)
        nameList.add(toPosition, item)
        mRecyclerView.post { notifyItemMoved(fromPosition, toPosition) }
    }

    fun getItemType(position: Int): Int {
        return nameList[position].type
    }
}

class DragManageAdapter(private var adapter: ShopListAdapter, dragDir: Int, swipeDir: Int) :
    ItemTouchHelper.SimpleCallback(dragDir, swipeDir) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Item should not move over first item since the first item
        // will be always a heading
        if (target.adapterPosition == 0)
            return false

        adapter.swapItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Don't move section heading
        if (adapter.getItemType(viewHolder.adapterPosition) == SECT)
            return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun isLongPressDragEnabled() = false
}

interface ItemListener {
    fun onChecked(name: String, position: Int, isChecked: Boolean)
    fun requestDrag(viewHolder: RecyclerView.ViewHolder)
}