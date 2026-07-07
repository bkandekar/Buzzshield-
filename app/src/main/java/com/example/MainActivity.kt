package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.components.BuzzShieldZapperVisual
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BuzzShieldViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BuzzShieldApp()
            }
        }
    }
}

// Global WhatsApp redirect helper
fun openWhatsAppOrder(context: Context, product: Product, quantity: Int, name: String, phone: String, address: String, pincode: String, notes: String) {
    val messageText = """
*BUZZSHIELD ORDER REQUEST*
----------------------------------
*Product:* ${product.name}
*Quantity:* $quantity
*Total Price:* ₹${(product.price * quantity).toInt()}
----------------------------------
*Customer Details:*
- Name: $name
- Phone: $phone
- Delivery Address: $address
- Pincode: $pincode
- Notes: ${notes.ifBlank { "None" }}
----------------------------------
_Please confirm my Cash on Delivery (COD) order. Thank you!_
""".trimIndent()

    val formattedNumber = "918329931123"
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(messageText)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp is not installed. Opening browser...", Toast.LENGTH_SHORT).show()
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber?text=${Uri.encode(messageText)}"))
        context.startActivity(webIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzzShieldApp() {
    val viewModel: BuzzShieldViewModel = viewModel()
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedBlog by viewModel.selectedBlog.collectAsStateWithLifecycle()
    val lastPlacedOrder by viewModel.lastPlacedOrder.collectAsStateWithLifecycle()

    var showWhatsAppDialogProduct by remember { mutableStateOf<Product?>(null) }
    var showRazorpaySimulator by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Logo",
                            tint = Color(0xFFA4FF3F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BUZZSHIELD",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D3B3E)
                ),
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(
                            onClick = { viewModel.navigateTo("cart") },
                            modifier = Modifier.testTag("header_cart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Shopping Cart",
                                tint = Color.White
                            )
                        }
                        if (cartItems.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(18.dp)
                                    .background(Color(0xFFA4FF3F), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartItems.sumOf { it.quantity }.toString(),
                                    color = Color(0xFF0D3B3E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo("order_history") },
                        modifier = Modifier.testTag("header_orders_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Order History",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF144F53),
                contentColor = Color.White
            ) {
                val tabs = listOf(
                    Triple("home", Icons.Default.Home, "Home"),
                    Triple("shop", Icons.Default.GridView, "Shop"),
                    Triple("blog", Icons.Default.Article, "Blog"),
                    Triple("order_history", Icons.Default.History, "Orders")
                )
                tabs.forEach { (route, icon, label) ->
                    val isSelected = currentScreen == route || 
                        (route == "shop" && currentScreen == "product_detail") ||
                        (route == "blog" && currentScreen == "blog_detail")
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(route) },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0D3B3E),
                            selectedTextColor = Color(0xFFA4FF3F),
                            indicatorColor = Color(0xFFA4FF3F),
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_${route}")
                    )
                }
            }
        },
        containerColor = Color(0xFF0D3B3E)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onOrderWhatsApp = { showWhatsAppDialogProduct = it }
                )
                "shop" -> ShopScreen(
                    viewModel = viewModel,
                    onOrderWhatsApp = { showWhatsAppDialogProduct = it }
                )
                "product_detail" -> selectedProduct?.let { product ->
                    ProductDetailScreen(
                        product = product,
                        viewModel = viewModel,
                        onOrderWhatsApp = { showWhatsAppDialogProduct = it }
                    )
                } ?: viewModel.navigateTo("shop")
                "cart" -> CartScreen(viewModel = viewModel)
                "checkout" -> CheckoutScreen(
                    viewModel = viewModel,
                    onPayOnline = { showRazorpaySimulator = true }
                )
                "blog" -> BlogScreen(viewModel = viewModel)
                "blog_detail" -> selectedBlog?.let { blog ->
                    BlogDetailScreen(blog = blog, viewModel = viewModel)
                } ?: viewModel.navigateTo("blog")
                "order_confirmation" -> lastPlacedOrder?.let { order ->
                    OrderConfirmationScreen(order = order, viewModel = viewModel)
                } ?: viewModel.navigateTo("home")
                "order_history" -> OrderHistoryScreen(viewModel = viewModel)
            }
        }
    }

    // Modal for Single Product WhatsApp COD Flow
    showWhatsAppDialogProduct?.let { product ->
        WhatsAppOrderDialog(
            product = product,
            onDismiss = { showWhatsAppDialogProduct = null },
            onSubmit = { qty, name, phone, address, pincode, notes ->
                openWhatsAppOrder(context, product, qty, name, phone, address, pincode, notes)
                // Also place order locally for tracking!
                viewModel.customerName.value = name
                viewModel.phone.value = phone
                viewModel.address.value = address
                viewModel.pincode.value = pincode
                viewModel.selectedPaymentMethod.value = "WhatsApp COD"
                viewModel.placeOrder(
                    onSuccess = {
                        showWhatsAppDialogProduct = null
                        viewModel.navigateTo("order_confirmation")
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Modal for Razorpay secure online gateway simulator
    if (showRazorpaySimulator) {
        val totalAmount = cartItems.sumOf { it.price * it.quantity }
        RazorpayGatewayDialog(
            amount = totalAmount,
            onDismiss = { showRazorpaySimulator = false },
            onSuccess = {
                viewModel.placeOrder(
                    onSuccess = {
                        showRazorpaySimulator = false
                        viewModel.navigateTo("order_confirmation")
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

// ----------------------------------------------------
// 1. HOME SCREEN
// ----------------------------------------------------
@Composable
fun HomeScreen(viewModel: BuzzShieldViewModel, onOrderWhatsApp: (Product) -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Glow behind zapper
                    BuzzShieldZapperVisual(
                        modifier = Modifier.size(200.dp),
                        isPro = false
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFFF5252), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MONSOON OFFER",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dengue & Malaria Shield",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Shield your kids & family from harmful mosquito-borne diseases without toxic chemical fumes.",
                    fontSize = 14.sp,
                    color = Color(0xFFA0C0C2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo("shop") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_shop_now"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SHOP ONLINE", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            // Direct purchase of Best Seller Classic v2
                            val classic = viewModel.products.first()
                            onOrderWhatsApp(classic)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_whatsapp_order"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ORDER COD", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Trust Badges Grid
        Text(
            text = "WHY BUZZSHIELD?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA4FF3F),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keySellingPoints = listOf(
                Pair(Icons.Default.ChildCare, "100% Safe For Kids & Pets"),
                Pair(Icons.Default.Eco, "Non-Toxic & Chemical Free"),
                Pair(Icons.Default.Bolt, "Low Energy Consumption")
            )
            keySellingPoints.forEach { (icon, text) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            tint = Color(0xFFA4FF3F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // Problem vs Solution Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THE MONSOON THREAT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252)
                )
                Text(
                    text = "Standard Coils vs. BuzzShield",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Row for Coils
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Danger",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Chemical Repellents & Coils", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Release synthetic pesticides (d-allethrin) and particulate smoke that damage young children's lungs and trigger severe allergies.", fontSize = 12.sp, color = Color(0xFFA0C0C2))
                    }
                }
                // Row for BuzzShield
                Row {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Safe Solution",
                        tint = Color(0xFFA4FF3F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("BuzzShield UV Solution", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Uses safe physical 365nm UV waves to lure insects and instantly zaps them. Absolutely zero smoke, zero fumes, and completely odorless.", fontSize = 12.sp, color = Color(0xFFA0C0C2))
                    }
                }
            }
        }

        // How it Works Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HOW IT SHIELDS YOUR HOME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA4FF3F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                val steps = listOf(
                    Triple("1. LURE", "365nm Phototactic UV wave tube mimics human breath and skin warmth to pull mosquitoes in from all directions.", Icons.Default.Lightbulb),
                    Triple("2. ZAP", "The high-voltage grid (up to 2000V) eliminates them silently on physical contact with no chemical agents.", Icons.Default.FlashOn),
                    Triple("3. COLLECT", "Dead mosquitoes drop cleanly into the escape-proof sliding bottom tray, keeping your tables and floors spotless.", Icons.Default.Delete)
                )
                steps.forEach { (stepTitle, stepDesc, stepIcon) ->
                    Row(modifier = Modifier.padding(bottom = 14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0D3B3E), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = stepIcon, contentDescription = stepTitle, tint = Color(0xFFA4FF3F), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stepTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(stepDesc, fontSize = 12.sp, color = Color(0xFFA0C0C2))
                        }
                    }
                }
            }
        }

        // Featured Blogs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FAMILY HEALTH ADVICE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            TextButton(onClick = { viewModel.navigateTo("blog") }) {
                Text("VIEW ALL", color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)
            }
        }

        // Horizontal Blog posts
        viewModel.blogPosts.take(2).forEach { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { viewModel.selectBlog(post) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(post.category.uppercase(), fontSize = 10.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)
                        Text(post.date, fontSize = 10.sp, color = Color(0xFFA0C0C2))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(post.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(post.excerpt, fontSize = 12.sp, color = Color(0xFFA0C0C2), maxLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Read Article", color = Color(0xFFA4FF3F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFA4FF3F), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. SHOP SCREEN
// ----------------------------------------------------
@Composable
fun ShopScreen(viewModel: BuzzShieldViewModel, onOrderWhatsApp: (Product) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "BuzzShield Collection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Select a zapper tailored to your room size & defense needs. All orders include Free Delivery across India.",
                fontSize = 13.sp,
                color = Color(0xFFA0C0C2),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        items(viewModel.products) { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectProduct(product) }
                    .testTag("product_card_${product.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        product.tag?.let { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(tag, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        } ?: Spacer(modifier = Modifier.width(1.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFA4FF3F), modifier = Modifier.size(16.dp))
                            Text(" 4.8", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Custom Drawn Visual representing the product
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color(0xFF0D3B3E), shape = RoundedCornerShape(16.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BuzzShieldZapperVisual(
                                modifier = Modifier.fillMaxSize(),
                                isPro = product.id == "pro"
                            )
                        }

                        // Product Title and Price Info
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                            Text(product.subtitle, fontSize = 12.sp, color = Color(0xFFA0C0C2))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("₹${product.price.toInt()}", color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "₹${product.originalPrice.toInt()}",
                                    color = Color(0xFFA0C0C2),
                                    fontSize = 13.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val discount = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
                                Text("$discount% OFF", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(product.description, fontSize = 12.sp, color = Color(0xFFA0C0C2), maxLines = 2)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addToCart(product)
                                Toast.makeText(viewModel.getApplication(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("shop_add_to_cart_${product.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF144F53)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFA4FF3F))
                        ) {
                            Text("ADD TO BAG", color = Color(0xFFA4FF3F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOrderWhatsApp(product) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("shop_whatsapp_cod_${product.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ORDER COD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. PRODUCT DETAIL SCREEN
// ----------------------------------------------------
@Composable
fun ProductDetailScreen(product: Product, viewModel: BuzzShieldViewModel, onOrderWhatsApp: (Product) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Back Button
        IconButton(
            onClick = { viewModel.navigateTo("shop") },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Hero Visual Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                BuzzShieldZapperVisual(
                    modifier = Modifier.fillMaxSize(),
                    isPro = product.id == "pro"
                )
            }
        }

        // Title info
        Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(product.subtitle, fontSize = 14.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))

        // Price Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("₹${product.price.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "₹${product.originalPrice.toInt()}",
                color = Color(0xFFA0C0C2),
                fontSize = 18.sp,
                textDecoration = TextDecoration.LineThrough
            )
            Spacer(modifier = Modifier.width(12.dp))
            val discount = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
            Box(
                modifier = Modifier
                    .background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("$discount% OFF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Includes free shipping across India • 7-day replacement guarantee",
            fontSize = 11.sp,
            color = Color(0xFFA0C0C2),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Action CTAs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.addToCart(product)
                    Toast.makeText(viewModel.getApplication(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("detail_add_to_cart"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ADD TO BAG", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onOrderWhatsApp(product) },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("detail_order_whatsapp"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ORDER COD", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Description Tab content
        Text("Product Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text(product.description, fontSize = 13.sp, color = Color(0xFFA0C0C2), modifier = Modifier.padding(vertical = 8.dp))

        // Features list
        Text("Key Features", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
        product.features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Check", tint = Color(0xFFA4FF3F), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(feature, fontSize = 12.sp, color = Color.White)
            }
        }

        // Specs Table
        Text("Technical Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                product.specs.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(key, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFA0C0C2))
                        Text(value, fontSize = 12.sp, color = Color.White)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }

        // FAQs Inline
        Text("Frequently Asked Questions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
        val faqs = listOf(
            Pair("Is it safe around infants or toddlers?", "Yes, absolutely. The outer safety grille guard spaces elements at less than 6mm apart, which completely blocks children's tiny fingers or pet paws from reaching the inner zapping grid."),
            Pair("Does it make loud popping noises?", "BuzzShield Classic and Mini operate with a quiet spark that produces a very small, soft snap. The active zapping grid itself runs silently and doesn't interrupt baby or family sleep.")
        )
        faqs.forEach { (q, a) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(q, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFA4FF3F))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(a, fontSize = 11.sp, color = Color(0xFFA0C0C2))
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. CART SCREEN
// ----------------------------------------------------
@Composable
fun CartScreen(viewModel: BuzzShieldViewModel) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Empty Bag",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Cart is Empty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "You haven't added any mosquito shield zappers yet. Secure your family against monsoon bites today!",
                fontSize = 13.sp,
                color = Color(0xFFA0C0C2),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = { viewModel.navigateTo("shop") },
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F))
            ) {
                Text("BROWSE CATALOG", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Secure Shopping Bag",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Items list
            cartItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawing miniature zapper icon
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFF0D3B3E), shape = RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            BuzzShieldZapperVisual(
                                modifier = Modifier.fillMaxSize(),
                                isPro = item.id == "pro"
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("₹${item.price.toInt()} each", fontSize = 12.sp, color = Color(0xFFA0C0C2))
                        }

                        // Quantities
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.updateCartQuantity(item.id, -1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0D3B3E), shape = CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = item.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp),
                                fontSize = 14.sp
                            )
                            IconButton(
                                onClick = { viewModel.updateCartQuantity(item.id, 1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0D3B3E), shape = CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing Summary
            val subtotal = cartItems.sumOf { it.price * it.quantity }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color(0xFFA0C0C2), fontSize = 14.sp)
                        Text("₹${subtotal.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monsoon Delivery", color = Color(0xFFA0C0C2), fontSize = 14.sp)
                        Text("FREE", color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("₹${subtotal.toInt()}", color = Color(0xFFA4FF3F), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo("checkout") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("cart_checkout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0D3B3E))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SECURE CHECKOUT", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// 5. CHECKOUT SCREEN
// ----------------------------------------------------
@Composable
fun CheckoutScreen(viewModel: BuzzShieldViewModel, onPayOnline: () -> Unit) {
    val name by viewModel.customerName.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val address by viewModel.address.collectAsStateWithLifecycle()
    val pincode by viewModel.pincode.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("cart") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Shipping & Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. DELIVERY ADDRESS", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.customerName.value = it },
                    label = { Text("Customer Name (for shipping)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("checkout_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) viewModel.phone.value = it },
                    label = { Text("10-Digit Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("checkout_phone_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { viewModel.address.value = it },
                    label = { Text("Complete Delivery Address (House/Flat, Street)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("checkout_address_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { if (it.length <= 6) viewModel.pincode.value = it },
                    label = { Text("6-Digit PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_pincode_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Payment Mode Select
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. SELECT PAYMENT MODE", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Razorpay online option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedPaymentMethod.value = "Online Razorpay" }
                        .background(
                            if (selectedPaymentMethod == "Online Razorpay") Color(0xFF0D3B3E) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (selectedPaymentMethod == "Online Razorpay") Color(0xFFA4FF3F) else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "Online Razorpay",
                        onClick = { viewModel.selectedPaymentMethod.value = "Online Razorpay" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA4FF3F))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pay Online (Razorpay)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Supports UPI, GPay, Credit/Debit cards & NetBanking", fontSize = 11.sp, color = Color(0xFFA0C0C2))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // WhatsApp COD option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedPaymentMethod.value = "WhatsApp COD" }
                        .background(
                            if (selectedPaymentMethod == "WhatsApp COD") Color(0xFF0D3B3E) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (selectedPaymentMethod == "WhatsApp COD") Color(0xFF25D366) else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "WhatsApp COD",
                        onClick = { viewModel.selectedPaymentMethod.value = "WhatsApp COD" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF25D366))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Order on WhatsApp (COD)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Place order instantly via WhatsApp chat & pay on delivery", fontSize = 11.sp, color = Color(0xFFA0C0C2))
                    }
                }
            }
        }

        // Final Place Order CTA
        val totalAmount = cartItems.sumOf { it.price * it.quantity }
        Button(
            onClick = {
                if (selectedPaymentMethod == "Online Razorpay") {
                    // Pre-verify forms before launching simulator
                    if (name.isBlank() || phone.length < 10 || address.isBlank() || pincode.length != 6) {
                        Toast.makeText(context, "Please complete all shipping address fields accurately", Toast.LENGTH_LONG).show()
                    } else {
                        onPayOnline()
                    }
                } else {
                    // WhatsApp COD flow
                    if (name.isBlank() || phone.length < 10 || address.isBlank() || pincode.length != 6) {
                        Toast.makeText(context, "Please complete all shipping address fields accurately", Toast.LENGTH_LONG).show()
                    } else {
                        // Gather items for pre-fill text
                        val defaultProduct = viewModel.products.first { it.id == cartItems.first().id }
                        openWhatsAppOrder(context, defaultProduct, cartItems.first().quantity, name, phone, address, pincode, "Monsoon Order")
                        viewModel.placeOrder(
                            onSuccess = {
                                viewModel.navigateTo("order_confirmation")
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("checkout_place_order"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedPaymentMethod == "WhatsApp COD") Color(0xFF25D366) else Color(0xFFA4FF3F)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            val labelText = if (selectedPaymentMethod == "WhatsApp COD") "SEND COD ORDER VIA WHATSAPP" else "PAY NOW VIA RAZORPAY (₹${totalAmount.toInt()})"
            Text(labelText, color = if (selectedPaymentMethod == "WhatsApp COD") Color.White else Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// 6. BLOG SCREEN
// ----------------------------------------------------
@Composable
fun BlogScreen(viewModel: BuzzShieldViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Family Safety & Prevention Guide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Monsoon and high-humidity seasons increase mosquito-borne disease risks. Read scientific guides to protect your home.",
                fontSize = 13.sp,
                color = Color(0xFFA0C0C2),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        items(viewModel.blogPosts) { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectBlog(post) }
                    .testTag("blog_post_card_${post.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(post.category.uppercase(), fontSize = 10.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)
                        Text(post.date, fontSize = 10.sp, color = Color(0xFFA0C0C2))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(post.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(post.excerpt, fontSize = 12.sp, color = Color(0xFFA0C0C2))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Read Article", color = Color(0xFFA4FF3F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFA4FF3F), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. BLOG DETAIL SCREEN
// ----------------------------------------------------
@Composable
fun BlogDetailScreen(blog: BlogPost, viewModel: BuzzShieldViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { viewModel.navigateTo("blog") },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Text(blog.category.uppercase(), fontSize = 11.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)
        Text(blog.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
        Text("Published on ${blog.date} • ${blog.readTime}", fontSize = 11.sp, color = Color(0xFFA0C0C2), modifier = Modifier.padding(bottom = 16.dp))

        // Simulated illustration/visual placeholder banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFA4FF3F), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("BuzzShield Wellness Education", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Body Text
        Text(
            text = blog.body,
            fontSize = 14.sp,
            color = Color.White,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Divider(color = Color.White.copy(alpha = 0.1f))

        // Bottom promo for BuzzShield
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monsoon Mosquito Shield", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F), fontSize = 12.sp)
                Text("Protect Your Kids Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 2.dp))
                Text("Switch to toxic-free, smoke-free UV photon zapping starting at just ₹499.", fontSize = 12.sp, color = Color(0xFFA0C0C2))
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.navigateTo("shop") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SHOP BUZZSHIELD", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 8. ORDER CONFIRMATION SCREEN
// ----------------------------------------------------
@Composable
fun OrderConfirmationScreen(order: Order, viewModel: BuzzShieldViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFA4FF3F), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = Color(0xFF0D3B3E), modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Order Placed Successfully!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Order ID: #BS-${1000 + order.id}", fontSize = 14.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // Details summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DELIVERY DETAILS", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${order.customerName}", color = Color.White, fontSize = 13.sp)
                Text("Phone: ${order.phone}", color = Color.White, fontSize = 13.sp)
                Text("Address: ${order.address}", color = Color.White, fontSize = 13.sp)
                Text("Pincode: ${order.pincode}", color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))
                Text("ORDER SUMMARY", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(order.productNames, color = Color.White, fontSize = 13.sp)
                Text("Total Price: ₹${order.totalPrice.toInt()}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Method: ${order.paymentMethod}", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Delivery timeline tracker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MONSOON DISPATCH TIMELINE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                val steps = listOf(
                    Pair("Order Received & Logged", true),
                    Pair("Approval & Packing Process", order.paymentMethod != "WhatsApp COD"),
                    Pair("Dispatched from Warehouse (Mumbai)", false),
                    Pair("Out for Delivery", false)
                )

                steps.forEachIndexed { idx, (stepName, isDone) ->
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isDone) Color(0xFFA4FF3F) else Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF0D3B3E), modifier = Modifier.size(14.dp))
                            } else {
                                Text((idx + 1).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stepName,
                            fontSize = 12.sp,
                            color = if (isDone) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val waUri = Uri.parse("https://wa.me/918329931123?text=${Uri.encode("Hello! Checking status of order BS-${1000 + order.id}")}")
                context.startActivity(Intent(Intent.ACTION_VIEW, waUri))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("WHATSAPP ORDER SUPPORT", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                viewModel.resetCheckoutForm()
                viewModel.navigateTo("home")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA4FF3F)),
            border = BorderStroke(1.dp, Color(0xFFA4FF3F)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("BACK TO HOME", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// 9. ORDER HISTORY SCREEN
// ----------------------------------------------------
@Composable
fun OrderHistoryScreen(viewModel: BuzzShieldViewModel) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (orders.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("CLEAR", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "No orders",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Orders Found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "You haven't placed any orders yet. Select your zappers and pay securely online or via WhatsApp COD.",
                    fontSize = 13.sp,
                    color = Color(0xFFA0C0C2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Button(
                    onClick = { viewModel.navigateTo("shop") },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F))
                ) {
                    Text("SHOP NOW", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Order #BS-${1000 + order.id}", fontWeight = FontWeight.Bold, color = Color(0xFFA4FF3F))
                                Text(
                                    text = if (order.paymentMethod == "WhatsApp COD") "COD Awaiting" else "Paid & Confirmed",
                                    color = if (order.paymentMethod == "WhatsApp COD") Color(0xFFA4FF3F) else Color(0xFF25D366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(order.productNames, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Total Price: ₹${order.totalPrice.toInt()}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Status: ${order.status}", color = Color(0xFFA0C0C2), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 10. WHATSAPP CUSTOM COD DIALOG
// ----------------------------------------------------
@Composable
fun WhatsAppOrderDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSubmit: (qty: Int, name: String, phone: String, address: String, pincode: String, notes: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF144F53))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WhatsApp COD Form",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Card representation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3B3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF144F53), shape = RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            BuzzShieldZapperVisual(modifier = Modifier.fillMaxSize(), isPro = product.id == "pro")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Price: ₹${product.price.toInt()} COD", fontSize = 12.sp, color = Color(0xFFA4FF3F), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Quantity:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                        }
                        Text(quantity.toString(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { quantity++ }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("10-Digit Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { if (it.length <= 6) pincode = it },
                    label = { Text("6-Digit PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Optional Notes (e.g., Ring bell twice)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA4FF3F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || phone.length < 10 || address.isBlank() || pincode.length != 6) {
                            // Validation alert
                        } else {
                            onSubmit(quantity, name, phone, address, pincode, notes)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SUBMIT & CHAT COD", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 11. RAZORPAY ONLINE GATEWAY SIMULATOR
// ----------------------------------------------------
@Composable
fun RazorpayGatewayDialog(
    amount: Double,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09292B))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = "Razorpay SECURE", tint = Color(0xFFA4FF3F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Razorpay Secure", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    CircularProgressIndicator(color = Color(0xFFA4FF3F), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Securing & Authorizing Transaction...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Connecting directly to your bank...", fontSize = 11.sp, color = Color(0xFFA0C0C2))

                    // Auto transition to success after 2 seconds
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        onSuccess()
                    }
                } else {
                    Text("PAYMENT SUMMARY", fontSize = 11.sp, color = Color(0xFFA0C0C2), fontWeight = FontWeight.Bold)
                    Text("₹${amount.toInt()}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA4FF3F))
                    Text("Secure Instant Online Payment Gateway", fontSize = 11.sp, color = Color(0xFFA0C0C2))

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mock UPI QR code simulation using canvas/graphics
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw a simulated QR code grid
                            val size = size.width
                            val step = size / 8
                            for (i in 0..7) {
                                for (j in 0..7) {
                                    if ((i + j) % 2 == 0 || (i in 0..2 && j in 0..2) || (i in 5..7 && j in 0..2) || (i in 0..2 && j in 5..7)) {
                                        drawRect(
                                            color = Color(0xFF0D3B3E),
                                            topLeft = Offset(i * step, j * step),
                                            size = Size(step, step)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Scan to pay via GPay, PhonePe, BHIM or Paytm", fontSize = 11.sp, color = Color(0xFFA0C0C2), textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { isProcessing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA4FF3F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SIMULATE SUCCESSFUL UPI PAYMENT", color = Color(0xFF0D3B3E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
