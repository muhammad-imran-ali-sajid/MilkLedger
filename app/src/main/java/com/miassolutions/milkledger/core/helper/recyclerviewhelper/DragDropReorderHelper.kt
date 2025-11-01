package com.miassolutions.milkledger.core.helper.recyclerviewhelper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragDropReorderHelper<T>(
    private val adapter: ReorderableAdapter<T>,
    private val onMoveCompleted: (List<T>) -> Unit,
    private val enableSwipe: Boolean = false,
    private val swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    /**
     * Create an ItemTouchHelper for the RecyclerView.
     * Supports drag-and-drop and optional swipe actions.
     */
    fun createTouchHelper(): ItemTouchHelper {
        val dragDirs = ItemTouchHelper.UP or ItemTouchHelper.DOWN

        val callback = object : ItemTouchHelper.SimpleCallback(
            dragDirs,
            if (enableSwipe) swipeDirs else 0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                if (fromPos != RecyclerView.NO_POSITION && toPos != RecyclerView.NO_POSITION) {
                    val newList = adapter.onItemMove(fromPos, toPos)
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    adapter.onItemSwiped(pos, direction)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                // Notify that move is completed
                onMoveCompleted(adapter.items)
            }
        }

        return ItemTouchHelper(callback)
    }
}
