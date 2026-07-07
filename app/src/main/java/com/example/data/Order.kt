package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val phone: String,
    val address: String,
    val pincode: String,
    val productNames: String, // Comma-separated list of items
    val totalPrice: Double,
    val quantity: Int,
    val paymentMethod: String, // "Online Razorpay" or "WhatsApp COD"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending Approval" // "Pending Approval", "Dispatched", "Delivered"
)
