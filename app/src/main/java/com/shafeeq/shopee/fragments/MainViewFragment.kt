package com.shafeeq.shopee.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.shafeeq.shopee.R

class MainViewFragment : Fragment() {
    private lateinit var mList: DragDropSwipeRecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_view, container, false)
        val dataSet = listOf(
            "Item 11", "Item 12", "Item 13",
            "Item 21", "Item 22", "Item 23",
            "Item 31", "Item 32", "Item 33",
            "Item 41", "Item 42", "Item 43",
            "Item 51", "Item 52", "Item 53",
        )
        val mAdapter = MyAdapter(dataSet)
        mList = root.findViewById(R.id.list)
        mList.layoutManager = LinearLayoutManager(requireContext())
        mList.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

        mList.adapter = mAdapter
        mList.swipeListener = onItemSwipeListener
        mList.dragListener = onItemDragListener
        mList.scrollListener = onListScrollListener

        return root
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }
}

class MyAdapter(dataSet: List<String> = emptyList())
    : DragDropSwipeAdapter<String, MyAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)
        val dragIcon: ImageView = itemView.findViewById(R.id.drag_icon)
    }

    override fun getViewHolder(itemLayout: View) = ViewHolder(itemLayout)

    override fun onBindViewHolder(item: String, viewHolder: ViewHolder, position: Int) {
        // Here we update the contents of the view holder's views to reflect the item's data
        viewHolder.itemText.text = item
    }

    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: ViewHolder, position: Int): View {
        // We return the view holder's view on which the user has to touch to drag the item
        return viewHolder.dragIcon
    }
}

private val onItemSwipeListener = object : OnItemSwipeListener<String> {
    override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: String): Boolean {
        // Handle action of item swiped
        // Return false to indicate that the swiped item should be removed from the adapter's data set (default behaviour)
        // Return true to stop the swiped item from being automatically removed from the adapter's data set (in this case, it will be your responsibility to manually update the data set as necessary)
        return false
    }
}

private val onItemDragListener = object : OnItemDragListener<String> {
    override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
        // Handle action of item being dragged from one position to another
    }

    override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {
        // Handle action of item dropped
    }
}

private val onListScrollListener = object : OnListScrollListener {
    override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
        // Handle change on list scroll state
    }

    override fun onListScrolled(scrollDirection: OnListScrollListener.ScrollDirection, distance: Int) {
        // Handle scrolling
    }
}
