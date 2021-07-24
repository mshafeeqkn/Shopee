package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.*


class FullItemListFragment : Fragment() {
    private lateinit var mFullListView: ListView
    private lateinit var mAdapter: FullShopListAdapter
    private lateinit var mActivity: Activity
    private val mShopItemList = ArrayList<ShopItem>()
    private var mDeleteState = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_full_item_list, container, false)
        mFullListView = root.findViewById(R.id.fullShopList)
        setHasOptionsMenu(true)

        val groupId = mActivity.getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mShopItemList.clear()
                    mShopItemList.add(ShopItem(malayalam = "Non-Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (!item.purchase) {
                            if((item.name != null && item.name?.isMalayalam() != true) ||
                                (item.name == null && item.malayalam.isNotEmpty() && !item.malayalam.isMalayalam()))
                                { item.swapContent() }
                            mShopItemList.add(item)
                        }
                    }
                    mShopItemList.add(ShopItem(malayalam = "Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (item.purchase) {

                            if((item.name != null && item.name?.isMalayalam() != true) ||
                                (item.name == null && item.malayalam.isNotEmpty() && !item.malayalam.isMalayalam()))
                                { item.swapContent() }
                            mShopItemList.add(item)
                        }
                    }
                    deleteDuplicates()
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        mAdapter = FullShopListAdapter(mActivity, groupId, mShopItemList)
        mFullListView.adapter = mAdapter
        return root
    }

    private fun deleteDuplicates() {
        val distinctList = mShopItemList.toSet().toList() as ArrayList
        val map = HashMap<String, ShopItem>()
        for(item in distinctList) {
            if(item.id.isEmpty()) continue
            map[item.id] = item
        }
        val groupId = mActivity.getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList").setValue(map)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.full_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_clear_all -> {
                val dataMap = HashMap<String, ShopItem>()
                for(data in mShopItemList) {
                    if(data.type == SECT) continue
                    dataMap[data.id] = data
                    dataMap[data.id]?.purchase = false
                }
                val groupId = mActivity.getGroupId()
                FirebaseDatabase.getInstance().getReference("$groupId/itemList").setValue(dataMap)
            }

            R.id.action_delete -> {
                mDeleteState = !mDeleteState
                mAdapter.notifyDataSetChanged(mDeleteState)
            }
        }
        return true
    }
}

class FullShopListAdapter(
    private var thisContext: Context,
    private var groupId: String?,
    private var mDataList: ArrayList<ShopItem>
) :
    ArrayAdapter<ShopItem>(thisContext, R.layout.full_item_layout, mDataList) {
    private var mCategoryList = ArrayList<CategoryItem>()

    init {
        FirebaseDatabase.getInstance().getReference("categories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mCategoryList.clear()
                    for(item in snapshot.children) {
                        item.getValue(String::class.java)?.let {
                            mCategoryList.add(CategoryItem(id = item.key, name = it))
                        }
                    }
                    thisContext.toast("${mCategoryList.size} Categories loaded")
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    private var mDeleteState: Boolean = false

    private class ViewHolder(view: View) {
        var mItemNameCheck: CheckBox? = null
        var mSectNameTextView: TextView? = null
        var mActionIcon: ImageView? = null
        var mItemNameLabel: TextView? = null

        init {
            mItemNameCheck = view.findViewById(R.id.item_check)
            mSectNameTextView = view.findViewById(R.id.section_name)
            mActionIcon = view.findViewById(R.id.action_icon)
            mItemNameLabel = view.findViewById(R.id.item_label)
        }
    }

    override fun getItemViewType(position: Int) = mDataList[position].type

    override fun getViewTypeCount() = 2

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        val shopItem = mDataList[position]

        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = if (shopItem.type == ITEM) {
                inflater.inflate(R.layout.full_item_layout, null)
            } else {
                inflater.inflate(R.layout.shop_list_seperator, null)
            }
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        if (shopItem.type == ITEM) {
            viewHolder.mItemNameCheck?.setOnCheckedChangeListener(null)
            viewHolder.mItemNameLabel?.text = shopItem.toString()
            viewHolder.mItemNameLabel?.setOnClickListener { showEditDialog(shopItem) }
            viewHolder.mItemNameCheck?.isChecked = shopItem.purchase
            viewHolder.mItemNameLabel?.setTextColor(if(shopItem.manglish.isEmpty() || shopItem.malayalam.isEmpty()) Color.RED else Color.BLACK)
            updateCheckboxView(viewHolder.mItemNameLabel!!, shopItem.purchase)
            if(mDeleteState) {
                viewHolder.mActionIcon?.visibility = View.VISIBLE
                viewHolder.mActionIcon?.setSrc(context, R.drawable.ic_baseline_close_24)
            } else
                viewHolder.mActionIcon?.visibility = View.GONE

            viewHolder.mItemNameCheck?.setOnCheckedChangeListener { _, isChecked ->
                shopItem.purchase = isChecked

                // Add the item like it's not bought in shopping list
                if(isChecked)
                    shopItem.checked = false
                FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}").setValue(shopItem)
            }

            viewHolder.mActionIcon?.setOnClickListener {
                if(mDeleteState) {
                    showDeleteDialog(shopItem)
                }
            }
        } else {
            val count = mDataList.filter { it.type != SECT && it.purchase != shopItem.malayalam.contains("Non") }.size
            viewHolder.mSectNameTextView?.text = "${shopItem.malayalam} ($count items)"
        }

        return view
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

    private fun showDeleteDialog(shopItem: ShopItem) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("Are you really want to delete?")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            notifyDataSetChanged(false)
            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}").setValue(null)
            dialog.cancel()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getIndex(string: String?): Int {
        if(string == null)
            return mCategoryList.size - 1

        mCategoryList.forEachIndexed { index, categoryItem ->
            if(categoryItem.name == string || categoryItem.id == string)
                return index
        }
        return mCategoryList.size - 1
    }

    private fun showEditDialog(shopItem: ShopItem) {
        val dialog = Dialog(thisContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_layout)
        val manglishInput = dialog.findViewById<EditText>(R.id.text1)
        val malayalamInput = dialog.findViewById<EditText>(R.id.text2)
        val category = dialog.findViewById<Spinner>(R.id.text3)
        val adapter = ArrayAdapter(thisContext, android.R.layout.simple_list_item_1, mCategoryList)
        category.adapter = adapter

        manglishInput.setText(shopItem.manglish)
        malayalamInput.setText(shopItem.name ?: shopItem.malayalam)
        category.setSelection(getIndex(shopItem.category))

        val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        val noBtn = dialog.findViewById(R.id.noBtn) as TextView
        yesBtn.setOnClickListener {
            shopItem.manglish = manglishInput.text.toString()
            shopItem.malayalam = malayalamInput.text.toString()
            shopItem.category = mCategoryList[category.selectedItemPosition].id
            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}").setValue(shopItem)
            dialog.dismiss()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.WRAP_CONTENT)
    }

    fun notifyDataSetChanged(delete: Boolean) {
        mDeleteState = delete
        notifyDataSetChanged()
    }
}