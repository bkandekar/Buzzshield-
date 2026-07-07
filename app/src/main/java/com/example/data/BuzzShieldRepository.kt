package com.example.data

import kotlinx.coroutines.flow.Flow

class BuzzShieldRepository(private val dao: BuzzShieldDao) {
    val cartItems: Flow<List<CartItem>> = dao.getCartItems()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()

    suspend fun insertCartItem(item: CartItem) {
        dao.insertCartItem(item)
    }

    suspend fun updateCartItem(item: CartItem) {
        dao.updateCartItem(item)
    }

    suspend fun deleteCartItem(id: String) {
        dao.deleteCartItem(id)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    suspend fun insertOrder(order: Order): Long {
        return dao.insertOrder(order)
    }

    suspend fun clearOrders() {
        dao.clearOrders()
    }
}
