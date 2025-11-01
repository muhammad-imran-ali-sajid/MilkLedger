package com.miassolutions.milkledger.core.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class DragDropReorderHelper<T>(
    private val getItems: () -> MutableList<T>, // Lazy getter for current adapter list
    private val onReorderFinished: (List<T>) -> Unit
) {
    fun createTouchHelper(adapter: RecyclerView.Adapter<*>): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val items = getItems() // Always get the latest list
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                if (fromPos !in items.indices || toPos !in items.indices) return false

                Collections.swap(items, fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // no-op
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                onReorderFinished(getItems())
            }
        }
        return ItemTouchHelper(callback)
    }
}
