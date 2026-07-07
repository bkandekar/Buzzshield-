================================================================================
          BUZZSHIELD - HIGH-PERFORMANCE D2C E-COMMERCE WEBSITE BLUEPRINT
================================================================================

Welcome! This folder contains the complete, production-ready, fully responsive static 
website code for BuzzShield India. It utilizes premium custom design tokens, high-contrast 
glowing vector zapper representations, and biological light-attraction-focused product 
positioning guidelines to maximize online D2C conversions.

--------------------------------------------------------------------------------
1. DIRECTORY STRUCTURE
--------------------------------------------------------------------------------

/index.html              - High-impact visual landing page with product grids and social proof.
/shop.html               - Dynamic product listing page rendering details from products.json.
/product.html            - Dynamic product specifications, technical details, and interactive buy/WhatsApp triggers.
/products.json           - Single source-of-truth JSON containing specifications, prices, and features for 3 zapper models.
/cart.html               - Client-side shopping cart with custom quantity manipulation.
/checkout.html           - Complete checkout decision screen offering online and WhatsApp COD paths.
/about.html              - Highly informative corporate science and brand mission statement.
/contact.html            - Clean customer support form, telephone, and business location guides.
/style.css               - Shared stylesheet with design tokens, responsive typography, and zapper animation rules.
/script.js               - Client-side State Engine handling localStorage, dynamic rendering, and WhatsApp link builders.
/blog/index.html         - Main health advisory & lung protection guide index page.
/blog/posts.json         - JSON database holding 3 professionally written, high-converting articles (600-800 words).
/blog/post-template.html - Single post dynamic reading template rendering details on query parameters.
/README.txt              - This informational deployment file.

--------------------------------------------------------------------------------
2. TECHNICAL PLUGINS & BACKEND INTEGRATIONS
--------------------------------------------------------------------------------

All code is fully decoupled and static-hosting compatible out of the box (suitable for 
immediate deployment via GitHub Pages, Netlify, or Vercel). To link custom live services:

A. RAZORPAY STANDARD WEB CHECKOUT (Online Payments)
-------------------------------------------------
To replace our fully animated simulated payment flow with a live checkout:
1. Include Razorpay's checkout script in `/checkout.html` header:
   <script src="https://checkout.razorpay.com/v1/checkout.js"></script>

2. Open `/script.js` and edit the `initiateRazorpayPayment()` function:
   Replace the simulated alert with standard Razorpay configuration hooks:
   
   var options = {
       "key": "YOUR_RAZORPAY_KEY_ID", // Enter your Razorpay Dashboard Key ID
       "amount": total * 100, // Amount is in currency subunits (Paisa)
       "currency": "INR",
       "name": "BuzzShield India",
       "description": "Secure Mosquito Defense Order",
       "handler": function (response){
           // Post payment success logic (e.g. submit details to database)
           alert("Payment ID: " + response.razorpay_payment_id);
           localStorage.removeItem('buzzshield_cart');
           window.location.href = "index.html";
       }
   };
   var rzp1 = new Razorpay(options);
   rzp1.open();

B. SUPABASE / BACKEND DATABASE SYNCHRONIZATION
---------------------------------------------
To persist customer orders and track inventory in real-time:
1. Initialize the Supabase JS client inside `/script.js`:
   import { createClient } from 'https://esm.sh/@supabase/supabase-js'
   const supabase = createClient('YOUR_SUPABASE_URL', 'YOUR_SUPABASE_ANON_KEY')

2. Post orders to your "orders" database table upon successful Razorpay capture 
   or WhatsApp modal form validation submission:
   
   const { data, error } = await supabase
     .from('orders')
     .insert([
       { 
         customer_name: name, 
         phone: phone, 
         delivery_address: address, 
         city: city, 
         pincode: pincode,
         items_ordered: JSON.stringify(cart),
         total_paid: total,
         status: 'Pending Confirmation'
       }
     ]);

--------------------------------------------------------------------------------
3. WHATSAPP COD FLOW VALIDATION
--------------------------------------------------------------------------------

Our state-of-the-art Cash on Delivery flow acts as an extremely high-conversion checkout 
channel for Indian traffic. It is fully integrated across product cards, the details screen, 
and the checkout page:
- Form fields require valid 10-digit Indian mobile numbers (validated via regex).
- Client-side script automatically serializes ordered items, counts, subtotals, free delivery 
  threshold validation, and address coordinates.
- Code encodes values using standard `encodeURIComponent` formatting and redirects 
  smoothly to the official merchant WhatsApp number (918329931123) via `window.open`.

Enjoy the pristine quality of your BuzzShield D2C eCommerce platform!
================================================================================
