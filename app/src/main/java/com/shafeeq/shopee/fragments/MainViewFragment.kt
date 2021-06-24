package com.shafeeq.shopee.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import com.shafeeq.shopee.R

class MainViewFragment : Fragment() {
    private lateinit var itemList: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        itemList = root.findViewById(R.id.itemList)
        val adapter = ShopItemListAdapter(requireContext(), arrayListOf(
            ListItem("Vegetables", SECTION),
            ListItem("Uluva", ITEM),
            ListItem("Kaduk", ITEM),
            ListItem("Cheera", ITEM),
            ListItem("Fruits", SECTION),
            ListItem("Apple", ITEM),
            ListItem("Orange", ITEM),
            ListItem("Grape", ITEM),
            ListItem("Rumman", ITEM),
            ListItem("Grocery", SECTION),
            ListItem("Rice", ITEM),
            ListItem("Sugar", ITEM),
            ListItem("Nuts", ITEM),
            ListItem("Tea Powder", ITEM),
            ListItem("Tooth Brush", ITEM),
            ListItem("Macrona", ITEM),
            ListItem("kadal", ITEM),
            ListItem("parippu", ITEM),
            ListItem("Vegetables", SECTION),
            ListItem("Uluva", ITEM),
            ListItem("Kaduk", ITEM),
            ListItem("Cheera", ITEM),
            ListItem("Fruits", SECTION),
            ListItem("Apple", ITEM),
            ListItem("Orange", ITEM),
            ListItem("Grape", ITEM),
            ListItem("Rumman", ITEM),
            ListItem("Grocery", SECTION),
            ListItem("Rice", ITEM),
            ListItem("Sugar", ITEM),
            ListItem("Nuts", ITEM),
            ListItem("Tea Powder", ITEM),
            ListItem("Tooth Brush", ITEM),
            ListItem("Macrona", ITEM),
            ListItem("kadal", ITEM),
            ListItem("parippu", ITEM),
        ))
        itemList.adapter = adapter
        return root
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }
}

const val ITEM = 0
const val SECTION = 1

data class ListItem(
    var name: String = "",
    var type: Int = ITEM
)

class ShopItemListAdapter(context: Context, private val data: ArrayList<ListItem>):
        ArrayAdapter<ListItem>(context, R.layout.shop_item, data) {

    private class ViewHolder(view: View?) {
        var itemName: CheckBox? = null
        var sectionName: TextView? = null

        init {
            itemName = view?.findViewById(R.id.itemName)
            sectionName = view?.findViewById(R.id.sectionName)
        }
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        val listItem = data[position]

        if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = if(listItem.type == ITEM) inflater.inflate(R.layout.shop_item, null)
                else inflater.inflate(R.layout.shop_list_seperator, null)

            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        if(listItem.type == ITEM) viewHolder.itemName?.text = listItem.name
            else viewHolder.sectionName?.text = listItem.name

        return view
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }
}