package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BuzzShieldDao {
    // Cart operations
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Update
    suspend fun updateCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Order operations
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}
