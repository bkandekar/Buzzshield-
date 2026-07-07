package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String, // same as productId
    val name: String,
    val subtitle: String,
    val price: Double,
    val quantity: Int,
    val highlightColor: String
)
