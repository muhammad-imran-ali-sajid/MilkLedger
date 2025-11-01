//package com.miassolutions.milkledger.core.helper
//
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.RecyclerView
//import com.miassolutions.milkledger.domain.model.Supplier
//import com.miassolutions.milkledger.presentation.supplier.suppliers.SupplierListAdapter
//
//class DragDropReorderHelper(
//    private val adapter: SupplierListAdapter,
//    private val onMoveCompleted: (List<Supplier>) -> Unit
//) {
//
//    fun createTouchHelper(): ItemTouchHelper {
//        val callback = object : ItemTouchHelper.SimpleCallback(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
//        ) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                val fromPos = viewHolder.bindingAdapterPosition
//                val toPos = target.bindingAdapterPosition
//
//                if (fromPos != RecyclerView.NO_POSITION && toPos != RecyclerView.NO_POSITION) {
//                    adapter.swapItems(fromPos, toPos)
//                    return true
//                }
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                // no swipe
//            }
//
//            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//                super.clearView(recyclerView, viewHolder)
//                onMoveCompleted(adapter.currentList)
//            }
//        }
//        return ItemTouchHelper(callback)
//    }
//}
