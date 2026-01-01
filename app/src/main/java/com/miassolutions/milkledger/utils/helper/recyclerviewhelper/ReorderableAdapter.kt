package com.miassolutions.milkledger.utils.helper.recyclerviewhelper

import androidx.recyclerview.widget.ItemTouchHelper

interface ReorderableAdapter<T> {
    /** Adapter's current list of items */
    val items: List<T>

    /** Called when items are reordered via drag-and-drop */
    fun onItemMove(fromPosition: Int, toPosition: Int): List<T>

    /**
     * Optional: Called when an item is swiped.
     * @param position Position of the swiped item
     * @param direction Direction of swipe (ItemTouchHelper.LEFT, RIGHT, etc.)
     */
    fun onItemSwiped(position: Int, direction: Int) {}
}

