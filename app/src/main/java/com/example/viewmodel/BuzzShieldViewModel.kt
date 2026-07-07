package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BuzzShieldViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BuzzShieldRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BuzzShieldRepository(database.buzzShieldDao())
    }

    // Products & Blogs
    val products = BuzzShieldData.products
    val blogPosts = BuzzShieldData.blogPosts

    // Database states
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Details
    private val _selectedProduct = MutableStateFlow<Product?>(BuzzShieldData.products.first())
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _selectedBlog = MutableStateFlow<BlogPost?>(BuzzShieldData.blogPosts.first())
    val selectedBlog = _selectedBlog.asStateFlow()

    // Navigation and Tab States
    private val _currentScreen = MutableStateFlow<String>("home") // home, shop, product_detail, cart, blog, blog_detail, checkout, order_confirmation, order_history
    val currentScreen = _currentScreen.asStateFlow()

    // Form inputs for WhatsApp COD or Online order
    val customerName = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val address = MutableStateFlow("")
    val pincode = MutableStateFlow("")
    val selectedPaymentMethod = MutableStateFlow("Online Razorpay") // "Online Razorpay" or "WhatsApp COD"

    // Last placed order state
    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder = _lastPlacedOrder.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        navigateTo("product_detail")
    }

    fun selectBlog(blog: BlogPost) {
        _selectedBlog.value = blog
        navigateTo("blog_detail")
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.id == product.id }
            if (existing != null) {
                repository.insertCartItem(existing.copy(quantity = existing.quantity + quantity))
            } else {
                repository.insertCartItem(
                    CartItem(
                        id = product.id,
                        name = product.name,
                        subtitle = product.subtitle,
                        price = product.price,
                        quantity = quantity,
                        highlightColor = product.highlightColor
                    )
                )
            }
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.id == productId } ?: return@launch
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                repository.deleteCartItem(productId)
            } else {
                repository.insertCartItem(existing.copy(quantity = newQty))
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.deleteCartItem(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun placeOrder(onSuccess: (Order) -> Unit, onError: (String) -> Unit) {
        val nameVal = customerName.value.trim()
        val phoneVal = phone.value.trim()
        val addressVal = address.value.trim()
        val pincodeVal = pincode.value.trim()

        if (nameVal.isEmpty()) {
            onError("Name is required")
            return
        }
        if (phoneVal.length < 10) {
            onError("Enter a valid 10-digit phone number")
            return
        }
        if (addressVal.isEmpty()) {
            onError("Address is required")
            return
        }
        if (pincodeVal.length != 6 || pincodeVal.toIntOrNull() == null) {
            onError("Enter a valid 6-digit PIN code")
            return
        }

        val items = cartItems.value
        if (items.isEmpty()) {
            onError("Your cart is empty")
            return
        }

        val totalQty = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.price * it.quantity }
        val productSummary = items.joinToString { "${it.name} (x${it.quantity})" }

        viewModelScope.launch {
            val order = Order(
                customerName = nameVal,
                phone = phoneVal,
                address = addressVal,
                pincode = pincodeVal,
                productNames = productSummary,
                totalPrice = totalPrice,
                quantity = totalQty,
                paymentMethod = selectedPaymentMethod.value,
                status = if (selectedPaymentMethod.value == "WhatsApp COD") "Awaiting Approval" else "Confirmed & Paid"
            )
            val generatedId = repository.insertOrder(order)
            val completedOrder = order.copy(id = generatedId.toInt())
            _lastPlacedOrder.value = completedOrder
            
            // Clear cart upon placement
            repository.clearCart()
            
            onSuccess(completedOrder)
        }
    }

    fun resetCheckoutForm() {
        customerName.value = ""
        phone.value = ""
        address.value = ""
        pincode.value = ""
        selectedPaymentMethod.value = "Online Razorpay"
    }
}
