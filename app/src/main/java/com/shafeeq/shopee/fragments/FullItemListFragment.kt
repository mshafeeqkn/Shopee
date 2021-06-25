package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
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
import com.shafeeq.shopee.utils.ITEM
import com.shafeeq.shopee.utils.SECT
import com.shafeeq.shopee.utils.ShopItem
import com.shafeeq.shopee.utils.getGroupId

class FullItemListFragment : Fragment() {
    private lateinit var mFullListView: ListView
    private lateinit var mAdapter: FullShopListAdapter
    private val mShopItemList = ArrayList<ShopItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_full_item_list, container, false)
        mFullListView = root.findViewById(R.id.fullShopList)
        val groupId = requireActivity().getGroupId()
        FirebaseDatabase.getInstance().getReference("$groupId/itemList")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mShopItemList.clear()
                    mShopItemList.add(ShopItem(name = "Non-Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (!item.purchase)
                            mShopItemList.add(item)
                    }
                    mShopItemList.add(ShopItem(name = "Purchased Item", type = SECT))
                    for (data in snapshot.children) {
                        val item = data.getValue(ShopItem::class.java)!!
                        if (item.purchase)
                            mShopItemList.add(item)
                    }
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        mAdapter = FullShopListAdapter(requireContext(), groupId, mShopItemList)
        mFullListView.adapter = mAdapter
        return root
    }
}

class FullShopListAdapter(
    private var thisContext: Context,
    private var groupId: String?,
    private var mDataList: ArrayList<ShopItem>
) :
    ArrayAdapter<ShopItem>(thisContext, R.layout.list_item_layout, mDataList) {

    private class ViewHolder(view: View) {
        var mItemNameCheck: CheckBox? = null
        var mSectNameTextView: TextView? = null
        var mEditImageView: ImageView? = null

        init {
            mItemNameCheck = view.findViewById(R.id.item_text)
            mSectNameTextView = view.findViewById(R.id.section_name)
            mEditImageView = view.findViewById(R.id.edit_icon)
        }
    }

    override fun getItemViewType(position: Int) = mDataList[position].type

    override fun getViewTypeCount() = 2

    @SuppressLint("InflateParams")
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
            viewHolder.mItemNameCheck?.text = shopItem.name
            viewHolder.mItemNameCheck?.isChecked = shopItem.purchase
            viewHolder.mItemNameCheck?.setTextColor(if(shopItem.maglish.isEmpty()) Color.RED else Color.BLACK)
            updateCheckboxView(viewHolder.mItemNameCheck!!, shopItem.purchase)
            viewHolder.mItemNameCheck?.setOnCheckedChangeListener { _, isChecked ->
                shopItem.purchase = isChecked
                FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}").setValue(shopItem)
            }

            viewHolder.mEditImageView?.setOnClickListener {
                showDialog(shopItem)
            }
        } else {
            viewHolder.mSectNameTextView?.text = shopItem.name
        }

        return view
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

    private fun showDialog(shopItem: ShopItem) {
        val dialog = Dialog(thisContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_layout)
        val input = dialog.findViewById<EditText>(R.id.text)
        input.setText(shopItem.maglish)
        val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        val noBtn = dialog.findViewById(R.id.noBtn) as TextView
        yesBtn.setOnClickListener {
            shopItem.maglish = input.text.toString()
            FirebaseDatabase.getInstance().getReference("$groupId/itemList/${shopItem.id}").setValue(shopItem)
            dialog.dismiss()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.WRAP_CONTENT)
    }
}