package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.*
import java.util.*
import kotlin.collections.ArrayList


class MainViewFragment : Fragment(), ItemListener {
    private lateinit var mShopItemList: RecyclerView
    private lateinit var mAdapter: ShopListAdapter
    private lateinit var mTouchHelper: ItemTouchHelper
    private lateinit var mInputActv: AutoCompleteTextView
    private lateinit var mAddBtn: Button
    private lateinit var mInputAdapter: ItemSearchAdapter

    private val mDataList = ArrayList<ShopItem>()
    private val mInputDataList = ArrayList<ShopItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        setHasOptionsMenu(true)
        mShopItemList = root.findViewById(R.id.shopItemList)
        mInputActv = root.findViewById(R.id.newItemName)
        mAddBtn = root.findViewById(R.id.addBtn)

        val groupId = requireActivity().getGroupId()
        mAddBtn.setOnClickListener {
            var shopItem = ShopItem(name = mInputActv.text.toString().trim(), type = ITEM)
            if(shopItem.name.trim().isEmpty()) return@setOnClickListener

            if(mInputDataList.contains(shopItem)) {
                shopItem = mInputDataList[mInputDataList.indexOf(shopItem)]
                shopItem.purchase = true
            } else {
                shopItem.id = FirebaseDatabase.getInstance().getReference("$groupId/itemList")
                    .push().key.toString()
            }

            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}")
                .setValue(shopItem)
            mInputActv.setText("")
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
                    loadListViewItems(snapshot)
                    loadInputAutocompleteList(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        return root
    }

    private fun loadInputAutocompleteList(snapshot: DataSnapshot) {
        mInputDataList.clear()
        for(data in snapshot.children) {
            mInputDataList.add(data.getValue(ShopItem::class.java)!!)
        }
        mInputAdapter = ItemSearchAdapter(requireContext(), mInputDataList)
        mInputActv.setAdapter(mInputAdapter)
    }

    private fun loadListViewItems(snapshot: DataSnapshot) {
        mDataList.clear()
        mDataList.add(ShopItem(name = "Non-Purchased Item", type = SECT))
        for (data in snapshot.children) {
            val item = data.getValue(ShopItem::class.java)!!
            if (item.purchase && !item.checked)
                mDataList.add(item)
        }
        mDataList.add(ShopItem(name = "Purchased Item", type = SECT))
        for (data in snapshot.children) {
            val item = data.getValue(ShopItem::class.java)!!
            if (item.purchase && item.checked)
                mDataList.add(item)
        }
        mShopItemList.post { mAdapter.notifyDataSetChanged() }
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }

    override fun onChecked(name: String, position: Int, isChecked: Boolean) {
        val item = mDataList[position]
        item.checked = isChecked
        val groupId = requireActivity().getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_full_list -> navigateToAction(
                MainViewFragmentDirections.actionMainViewFragment2ToFullItemList2()
            )

            R.id.action_settings -> navigateToAction(
                MainViewFragmentDirections.actionMainViewFragment2ToSettingsFragment()
            )

            R.id.action_clear_all -> {

            }
        }
        return true
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        mTouchHelper.startDrag(viewHolder)
    }

    override fun removeItem(item: ShopItem) {
        val groupId = requireActivity().getGroupId()
        item.purchase = false
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)

    }

    override fun saveQuantity(item: ShopItem, quantity: String) {
    }

    private fun navigateToAction(action: NavDirections) {
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(action)
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
        private var mRemove: ImageView = itemView.findViewById(R.id.remove_item)
        private var mQuantity: EditText = itemView.findViewById(R.id.quantity)

        @SuppressLint("ClickableViewAccessibility")
        fun bindData(item: ShopItem, listener: ItemListener) {
            mItemName.text = item.name
            mItemName.isChecked = item.checked
            updateCheckboxView(mItemName, item.checked)

            mQuantity.setText(item.quantity)
            mItemName.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    updateCheckboxView(view, isChecked)
                    listener.onChecked(item.name, adapterPosition, isChecked)
                }
            }
            mDragIcon.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    listener.requestDrag(this)
                }
                return@setOnTouchListener false
            }

            mRemove.setOnClickListener {
                listener.removeItem(item)
            }

            mQuantity.onChange {
                listener.saveQuantity(item, it)
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
            (holder as ShopItemViewHolder).bindData(nameList[position], listener)
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

class ItemSearchAdapter(private var mContext: Context, private var mDataList: ArrayList<ShopItem>):
        ArrayAdapter<ShopItem>(mContext, R.layout.simple_lite_item, mDataList) {
    private var filter = SearchFilter()

    private class ViewHolder(view: View?) {
        var name: TextView? = null
        init {
            name = view?.findViewById(R.id.itemNameText)
        }
    }

    override fun getCount() = mDataList.size

    override fun getFilter(): Filter {
        return filter
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        val item = mDataList[position]

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if(convertView == null) {
            view = inflater.inflate(R.layout.simple_lite_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.name?.text = item.name
        return view
    }

    inner class SearchFilter: Filter() {
        private var completeList = ArrayList<ShopItem>()
        private var filteredList = ArrayList<ShopItem>()

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if(completeList.isEmpty())
                completeList.addAll(mDataList)

            filteredList.clear()
            val results = FilterResults()
            if(constraint == null || constraint.isEmpty()) {
                filteredList.addAll(completeList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ENGLISH).trim()
                for(item in mDataList) {
                    if(item.name.toLowerCase(Locale.ENGLISH).contains(filterPattern) ||
                        item.manglish.toLowerCase(Locale.ENGLISH).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as ShopItem).name
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mDataList.clear()
            @Suppress("UNCHECKED_CAST")
            mDataList.addAll(results?.values as ArrayList<ShopItem>)
            notifyDataSetChanged()
        }
    }
}

interface ItemListener {
    fun onChecked(name: String, position: Int, isChecked: Boolean)
    fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    fun removeItem(item: ShopItem)
    fun saveQuantity(item: ShopItem, quantity: String)
}