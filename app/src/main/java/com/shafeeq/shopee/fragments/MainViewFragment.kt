package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.shafeeq.shopee.MainActivity
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainViewFragment : Fragment(), ItemListener {
    private lateinit var mShopItemList: RecyclerView
    private lateinit var mAdapter: ShopListAdapter
    private lateinit var mTouchHelper: ItemTouchHelper
    private lateinit var mInputActv: AutoCompleteTextView
    private lateinit var mAddBtn: Button
    private lateinit var mInputAdapter: ItemSearchAdapter
    private var mCheckAll = false

    private lateinit var mActivity: Activity
    private var mContext: Context? = null

    private val mDataList = ArrayList<ShopItem>()
    private val mInputDataList = ArrayList<ShopItem>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = mContext ?: context
        mActivity = mContext as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        setHasOptionsMenu(true)
        mShopItemList = root.findViewById(R.id.shopItemList)
        mInputActv = root.findViewById(R.id.newItemName)
        mAddBtn = root.findViewById(R.id.addBtn)

        val groupId = mActivity.getGroupId()
        mAddBtn.setOnClickListener {
            var shopItem = ShopItem(malayalam = mInputActv.text.toString().trim(), type = ITEM)
            if (shopItem.malayalam.trim().isEmpty()) return@setOnClickListener

            if (mInputDataList.contains(shopItem)) {
                shopItem = mInputDataList[mInputDataList.indexOf(shopItem)]
                shopItem.purchase = true
                shopItem.checked = false
            } else {
                shopItem.id = FirebaseDatabase.getInstance().getReference("$groupId/itemList")
                    .push().key.toString()
            }

            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}")
                .setValue(shopItem)
            mInputActv.setText("")
        }

        mShopItemList.layoutManager = LinearLayoutManager(mActivity)
        mAdapter = ShopListAdapter(mContext, mDataList, this)
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
        for (data in snapshot.children) {
            mInputDataList.add(data.getValue(ShopItem::class.java)!!)
        }
        mInputAdapter = ItemSearchAdapter(mContext!!, mInputDataList)
        mInputActv.setAdapter(mInputAdapter)
    }

    private fun loadListViewItems(snapshot: DataSnapshot) {
        mDataList.clear()
        mDataList.add(ShopItem(malayalam = "Non-Purchased Item", type = SECT))
        val tmpList = arrayListOf<ShopItem>()
        for (data in snapshot.children) {
            val item = data.getValue(ShopItem::class.java)!!
            if (item.purchase && !item.checked)
                tmpList.add(item)
        }
        tmpList.sortBy { it.category }
        mDataList.addAll(tmpList)

        mDataList.add(ShopItem(malayalam = "Purchased Item", type = SECT))
        tmpList.clear()
        for (data in snapshot.children) {
            val item = data.getValue(ShopItem::class.java)!!
            if (item.purchase && item.checked)
                tmpList.add(item)
        }
        tmpList.sortBy { it.category }
        mDataList.addAll(tmpList)
        mShopItemList.post { mAdapter.notifyDataSetChanged() }
    }

    override fun onChecked(name: String, position: Int, isChecked: Boolean) {
        if (position < 0)
            return
        val item = mDataList[position]
        item.checked = isChecked
        val groupId = mActivity.getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val checkAllItem = menu.findItem(R.id.action_toggle_check_all)
        checkAllItem.title = (if (mCheckAll) "Uncheck" else "Check") + " All"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_full_list -> navigateToAction(
                MainViewFragmentDirections.actionMainViewFragment2ToFullItemList2()
            )

            R.id.action_settings -> navigateToAction(
                MainViewFragmentDirections.actionMainViewFragment2ToSettingsFragment()
            )

            R.id.action_toggle_check_all -> {
                mCheckAll = !mCheckAll
                checkAllItems(mCheckAll)
                mActivity.invalidateOptionsMenu()
            }

            R.id.action_share -> {
                var strMsg = ""
                for (i in mDataList) {
                    if (i.type == SECT) continue
                    strMsg = "$strMsg\n* ${i.malayalam} - ${i.quantity}"
                }

                val waIntent = Intent(Intent.ACTION_SEND)
                waIntent.type = "text/plain"
                val text = strMsg
                waIntent.setPackage("com.whatsapp")
                waIntent.putExtra(Intent.EXTRA_TEXT, text)
                startActivity(Intent.createChooser(waIntent, "Share with"))
            }
        }
        return true
    }

    private fun checkAllItems(check: Boolean) {
        val dataMap = HashMap<String, ShopItem>()
        for (data in mInputDataList) {
            if (data.type == SECT) continue
            dataMap[data.id] = data
            dataMap[data.id]?.checked = check
        }
        val groupId = mActivity.getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList").setValue(dataMap)
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        mTouchHelper.startDrag(viewHolder)
    }

    override fun removeItem(item: ShopItem) {
        val groupId = mActivity.getGroupId()
        item.purchase = false
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)
    }

    override fun saveQuantity(item: ShopItem, quantity: String) {
        val groupId = mActivity.getGroupId()
        item.quantity = quantity
        FirebaseDatabase.getInstance().getReference("$groupId/itemList/${item.id}").setValue(item)
    }

    private fun navigateToAction(action: NavDirections) {
        val navHostFragment = (mActivity as MainActivity).supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(action)
    }
}

class ShopListAdapter(
    private val context: Context?,
    private val nameList: ArrayList<ShopItem>,
    private val listener: ItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mRecyclerView: RecyclerView

    class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mItemCheck: CheckBox = itemView.findViewById(R.id.item_check)
        private var mItemName: TextView = itemView.findViewById(R.id.item_label)
        private var mDragIcon: ImageView = itemView.findViewById(R.id.drag_icon)
        private var mActionButton: ImageView = itemView.findViewById(R.id.item_action)
        private var mQuantity: EditText = itemView.findViewById(R.id.quantity)

        @SuppressLint("ClickableViewAccessibility")
        fun bindData(context: Context?, item: ShopItem, listener: ItemListener) {
            mItemName.text = item.toString()
            mItemCheck.isChecked = item.checked
            updateCheckboxView(mItemName, item.checked)
            mQuantity.removeWatcher()
            mQuantity.setText(item.quantity)
            mItemCheck.apply {
                setOnCheckedChangeListener { _, isChecked ->
                    listener.onChecked(item.malayalam, adapterPosition, isChecked)
                }
            }
            mDragIcon.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    listener.requestDrag(this)
                }
                return@setOnTouchListener false
            }

            mActionButton.setOnClickListener {
                val saveState = mActionButton.tag as Boolean?
                if (saveState != null && saveState) {
                    mActionButton.setSrc(context!!, R.drawable.ic_baseline_close_24)
                    listener.saveQuantity(item, mQuantity.text.toString())
                    mActionButton.tag = false
                } else {
                    listener.removeItem(item)
                }
            }

            mQuantity.onChange {
                if (it.isNotEmpty() && mActionButton.tag != true) {
                    mActionButton.tag = true
                    mActionButton.setSrc(context!!, R.drawable.ic_baseline_check_24)
                }
            }
        }

        private fun updateCheckboxView(view: TextView, isChecked: Boolean) {
            if (isChecked) {
                view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                (view.parent as View).alpha = 0.3F
            } else {
                view.paintFlags = view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                (view.parent as View).alpha = 1.0F
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
            val view = inflater.inflate(R.layout.list_item_drag_checkbox, parent, false)
            ShopItemViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.shop_list_seperator, parent, false)
            SectionHeadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (nameList[position].type == ITEM) {
            (holder as ShopItemViewHolder).bindData(context, nameList[position], listener)
        } else {
            val secName = nameList[position].malayalam
            val count =
                nameList.filter { it.type != SECT && it.purchase && it.checked != secName.contains("Non") }.size
            (holder as SectionHeadingViewHolder).bindData("$secName ($count items)")
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

class ItemSearchAdapter(private var mContext: Context, private var mDataList: ArrayList<ShopItem>) :
    ArrayAdapter<ShopItem>(mContext, R.layout.shop_item_dropdown_item, mDataList) {
    private var filter = SearchFilter()

    private class ViewHolder(view: View?) {
        var name: TextView? = null
        var english: TextView? = null

        init {
            name = view?.findViewById(R.id.itemNameText)
            english = view?.findViewById(R.id.itemEnglishText)
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
        if (convertView == null) {
            view = inflater.inflate(R.layout.shop_item_dropdown_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.name?.text = item.malayalam
        viewHolder.english?.text = if (item.manglish.isNotEmpty()) "(${item.manglish})" else ""
        return view
    }

    inner class SearchFilter : Filter() {
        private var completeList = ArrayList<ShopItem>()
        private var filteredList = ArrayList<ShopItem>()

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (completeList.isEmpty())
                completeList.addAll(mDataList)

            filteredList.clear()
            val results = FilterResults()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(completeList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ENGLISH).trim()
                for (item in completeList) {
                    if (item.malayalam.toLowerCase(Locale.ENGLISH).contains(filterPattern) ||
                        item.manglish.toLowerCase(Locale.ENGLISH).contains(filterPattern)
                    ) {
                        filteredList.add(item)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as ShopItem).malayalam
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