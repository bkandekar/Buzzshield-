/**
 * BuzzShield D2C eCommerce Platform Script
 * Offline-first design with local storage cart persistence, dynamic loading, and active WhatsApp checkout.
 */

document.addEventListener('DOMContentLoaded', () => {
  initMobileMenu();
  updateCartBadge();
  
  // Identify current page
  const path = window.location.pathname;
  const page = path.split("/").pop();
  
  if (page === 'shop.html' || page === '') {
    loadShopProducts();
  } else if (page === 'product.html') {
    loadSingleProductDetails();
  } else if (page === 'cart.html') {
    renderCart();
  } else if (page === 'checkout.html') {
    renderCheckout();
  }
  
  // Set up general WhatsApp buttons
  initWhatsAppModals();
});

/* ==========================================================================
   Navigation Menu
   ========================================================================== */
function initMobileMenu() {
  const toggle = document.querySelector('.menu-toggle');
  const links = document.querySelector('.nav-links');
  if (toggle && links) {
    toggle.addEventListener('click', () => {
      links.classList.toggle('active');
    });
  }
}

/* ==========================================================================
   Cart Operations & State (localStorage)
   ========================================================================== */
function getCart() {
  try {
    return JSON.parse(localStorage.getItem('buzzshield_cart')) || [];
  } catch (e) {
    return [];
  }
}

function saveCart(cart) {
  localStorage.setItem('buzzshield_cart', JSON.stringify(cart));
  updateCartBadge();
}

function addToCart(productId, count = 1) {
  fetch('products.json')
    .then(res => res.json())
    .then(products => {
      const product = products.find(p => p.id === productId);
      if (!product) return;
      
      let cart = getCart();
      const existingIndex = cart.findIndex(item => item.id === productId);
      
      if (existingIndex > -1) {
        cart[existingIndex].quantity += count;
      } else {
        cart.push({
          id: product.id,
          name: product.name,
          price: product.price,
          image: product.image,
          quantity: count
        });
      }
      
      saveCart(cart);
      showToast(`${product.name} added to cart!`);
    })
    .catch(err => console.error("Error loading products:", err));
}

function removeFromCart(productId) {
  let cart = getCart();
  cart = cart.filter(item => item.id !== productId);
  saveCart(cart);
  
  // Refresh views
  const page = window.location.pathname.split("/").pop();
  if (page === 'cart.html') renderCart();
  if (page === 'checkout.html') renderCheckout();
}

function updateQuantity(productId, newQty) {
  if (newQty < 1) {
    removeFromCart(productId);
    return;
  }
  let cart = getCart();
  const item = cart.find(i => i.id === productId);
  if (item) {
    item.quantity = parseInt(newQty);
    saveCart(cart);
    
    // Refresh views
    const page = window.location.pathname.split("/").pop();
    if (page === 'cart.html') renderCart();
    if (page === 'checkout.html') renderCheckout();
  }
}

function updateCartBadge() {
  const badge = document.querySelector('.cart-count-badge');
  if (!badge) return;
  const cart = getCart();
  const totalItems = cart.reduce((total, item) => total + item.quantity, 0);
  
  if (totalItems > 0) {
    badge.textContent = totalItems;
    badge.style.display = 'flex';
  } else {
    badge.style.display = 'none';
  }
}

/* ==========================================================================
   Dynamic Pages Loader
   ========================================================================== */
function loadShopProducts() {
  const container = document.getElementById('products-container');
  if (!container) return;
  
  fetch('products.json')
    .then(res => res.json())
    .then(products => {
      container.innerHTML = products.map(product => `
        <div class="product-card">
          ${product.tag ? `<span class="product-tag">${product.tag}</span>` : ''}
          <div class="product-card-visual">
            <svg class="product-thumb-svg" viewBox="0 0 100 150">
              <!-- Custom decorative high-contrast vector zapper graphic inside SVG -->
              <rect x="15" y="30" width="70" height="110" rx="35" fill="#134e52" stroke="#1a5e63" stroke-width="3"/>
              <rect x="25" y="45" width="50" height="80" rx="25" fill="#0d2a2d" stroke="#A4FF3F" stroke-width="1.5" stroke-dasharray="2,2"/>
              <line x1="50" y1="45" x2="50" y2="125" stroke="#A4FF3F" stroke-width="4" stroke-linecap="round" filter="drop-shadow(0 0 4px #A4FF3F)"/>
              <circle cx="50" cy="85" r="10" fill="#A4FF3F" opacity="0.3"/>
              <!-- Handle -->
              <path d="M35 30 A15 15 0 0 1 65 30" fill="none" stroke="#1a5e63" stroke-width="3"/>
            </svg>
          </div>
          <div class="product-info">
            <h3 class="product-title">${product.name}</h3>
            <p class="product-subtitle">${product.subtitle}</p>
            <p style="font-size: 14px; margin-bottom: 16px; opacity: 0.8;">${product.description.substring(0, 85)}...</p>
            <div class="product-price-row">
              <span class="current-price">₹${product.price}</span>
              <span class="original-price">₹${product.originalPrice}</span>
            </div>
            <div class="product-actions">
              <a href="product.html?id=${product.id}" class="btn btn-secondary" style="font-size: 13px;">View Details</a>
              <button class="btn btn-primary" onclick="addToCart('${product.id}')" aria-label="Add to cart" style="font-size: 13px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
              </button>
            </div>
          </div>
        </div>
      `).join('');
    })
    .catch(err => console.error("Error drawing cards:", err));
}

function loadSingleProductDetails() {
  const urlParams = new URLSearchParams(window.location.search);
  const productId = urlParams.get('id') || 'classic';
  
  const mainDetails = document.getElementById('product-detail-container');
  if (!mainDetails) return;
  
  fetch('products.json')
    .then(res => res.json())
    .then(products => {
      const product = products.find(p => p.id === productId);
      if (!product) {
        mainDetails.innerHTML = `<div style="text-align:center; padding: 40px;"><p>Product not found.</p><br><a href="shop.html" class="btn btn-primary">Go back to Shop</a></div>`;
        return;
      }
      
      // Features list
      const featuresHTML = product.features.map(f => `
        <li style="display:flex; gap:12px; margin-bottom:12px; font-size:15px; color:#E1FDF5;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#A4FF3F" stroke-width="2.5" style="flex-shrink:0;">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <span>${f}</span>
        </li>
      `).join('');
      
      // Technical specs list
      const specsHTML = Object.entries(product.specs).map(([key, val]) => `
        <div style="display:flex; justify-content:space-between; padding:12px 0; border-bottom: 1px solid rgba(164, 255, 63, 0.05); font-size:14px;">
          <span style="color:#8AAEA9; font-weight:500;">${key}</span>
          <span style="color:white; font-weight:600; text-align:right;">${val}</span>
        </div>
      `).join('');
      
      mainDetails.innerHTML = `
        <div class="checkout-grid" style="grid-template-columns: 1fr 1fr; gap: 60px; margin-top: 20px;">
          <div class="zapper-container" style="background-color: var(--color-bg-card); border-radius: 24px; border:1px solid var(--color-border); height:450px;">
            <div class="zapper-graphic" style="transform: scale(1.15);">
              <div class="zapper-handle"></div>
              <div class="zapper-glow"></div>
              <div class="zapper-grid"></div>
              <div class="zapper-tubes"></div>
            </div>
          </div>
          <div>
            <span class="product-tag" style="position:static; display:inline-block; margin-bottom:16px;">${product.tag || 'ECO-SAFE'}</span>
            <h1 style="font-size:36px; margin-bottom:12px; font-weight:800; letter-spacing:-0.03em;">${product.name}</h1>
            <p style="font-family: var(--font-mono); color:var(--color-lime); font-size:15px; margin-bottom:20px; font-weight:600;">${product.subtitle}</p>
            <p style="font-size:16px; opacity:0.85; margin-bottom:24px; line-height:1.7;">${product.description}</p>
            
            <div class="product-price-row" style="margin-bottom:32px;">
              <span class="current-price" style="font-size:36px;">₹${product.price}</span>
              <span class="original-price" style="font-size:18px;">₹${product.originalPrice}</span>
              <span style="background-color:rgba(164,255,63,0.1); color:var(--color-lime); padding:4px 10px; border-radius:4px; font-size:12px; font-weight:800; margin-left:12px;">SAVE ${Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)}%</span>
            </div>
            
            <div style="display:flex; gap:16px; margin-bottom:40px;">
              <button class="btn btn-primary" onclick="addToCart('${product.id}')" style="flex-grow:1; font-size:16px; padding:16px 24px;">
                Add to Cart
              </button>
              <button class="btn btn-whatsapp whatsapp-order-trigger" data-product-id="${product.id}" style="padding:16px 24px;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946C.06 5.348 5.397.01 12.008.01c3.202.001 6.212 1.246 8.477 3.514 2.266 2.268 3.507 5.28 3.505 8.484-.004 6.657-5.34 11.997-11.953 11.997-2.005-.001-3.973-.502-5.717-1.455L0 24zm6.59-4.846c1.6.95 3.188 1.449 4.825 1.451 5.436 0 9.859-4.42 9.863-9.864.002-2.637-1.013-5.115-2.86-6.964-1.847-1.849-4.321-2.866-6.953-2.867-5.439 0-9.86 4.42-9.864 9.864-.001 1.748.477 3.456 1.385 4.958l-.98 3.57 3.664-.961zm11.365-5.963c-.1-.167-.367-.267-.767-.467-.4-.2-2.367-1.167-2.734-1.3-.367-.133-.633-.2-.9.2-.267.4-1.034 1.3-1.267 1.567-.233.267-.467.3-.867.1-.4-.2-1.688-.622-3.214-1.984-1.187-1.059-1.988-2.367-2.222-2.767-.233-.4-.025-.617.175-.816.18-.18.4-.467.6-.7.2-.233.267-.4.4-.667.133-.267.067-.5-.033-.7-.1-.2-.9-2.167-1.234-2.967-.324-.783-.655-.678-.9-.69l-.767-.013c-.267 0-.7.1-1.067.5-.367.4-1.4 1.367-1.4 3.333s1.433 3.867 1.633 4.133c.2.267 2.817 4.3 6.825 6.033.953.412 1.696.658 2.275.842.958.304 1.83.261 2.518.158.767-.116 2.367-.967 2.7-1.9.333-.933.333-1.733.233-1.9-.1-.167-.366-.266-.766-.466z"/></svg>
                Order via WhatsApp
              </button>
            </div>
          </div>
        </div>
        
        <div class="checkout-grid" style="grid-template-columns: 1.5fr 1fr; gap: 48px; margin-top: 60px; border-top: 1px solid rgba(164,255,63,0.1); padding-top:40px;">
          <div>
            <h2 style="font-size:24px; margin-bottom:20px;">Why Choose the ${product.name}?</h2>
            <ul style="list-style:none;">
              ${featuresHTML}
            </ul>
          </div>
          <div class="checkout-card">
            <h3 class="checkout-title" style="font-size:18px;">TECHNICAL SPECIFICATIONS</h3>
            ${specsHTML}
          </div>
        </div>
      `;
      
      // Re-trigger WhatsApp events because button was dynamically built
      initWhatsAppModals();
    })
    .catch(err => console.error("Error loading product detail:", err));
}

function renderCart() {
  const container = document.getElementById('cart-items-container');
  const summary = document.getElementById('cart-summary-container');
  if (!container) return;
  
  const cart = getCart();
  if (cart.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding: 60px; background-color: var(--color-bg-card); border-radius:16px; border: 1px solid var(--color-border);">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8AAEA9" stroke-width="1.5" style="margin-bottom:16px;"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
        <p style="font-size:16px; color:var(--color-text-muted); margin-bottom:24px;">Your cart is empty.</p>
        <a href="shop.html" class="btn btn-primary">Go to Shop</a>
      </div>
    `;
    if (summary) summary.style.display = 'none';
    return;
  }
  
  if (summary) summary.style.display = 'block';
  
  container.innerHTML = cart.map(item => `
    <div style="display:flex; justify-content:space-between; align-items:center; background-color: var(--color-bg-card); border: 1px solid var(--color-border); border-radius:12px; padding:20px; margin-bottom:16px; flex-wrap:wrap; gap:16px;">
      <div style="display:flex; align-items:center; gap:20px;">
        <div style="width:60px; height:60px; background:#124a4d; border-radius:8px; display:flex; align-items:center; justify-content:center;">
          <svg width="35" height="45" viewBox="0 0 100 150">
            <rect x="20" y="35" width="60" height="90" rx="20" fill="#134e52" stroke="#1a5e63" stroke-width="4"/>
            <line x1="50" y1="45" x2="50" y2="115" stroke="#A4FF3F" stroke-width="5" filter="drop-shadow(0 0 4px #A4FF3F)"/>
          </svg>
        </div>
        <div>
          <h4 style="font-size:16px; font-weight:700;">${item.name}</h4>
          <p style="font-size:14px; color:var(--color-lime); font-weight:600; font-family:var(--font-mono); margin-top:2px;">₹${item.price}</p>
        </div>
      </div>
      <div style="display:flex; align-items:center; gap:16px;">
        <div style="display:flex; align-items:center; background-color:var(--color-bg-deep); border-radius:8px; border:1px solid rgba(164,255,63,0.1);">
          <button onclick="updateQuantity('${item.id}', ${item.quantity - 1})" style="background:none; border:none; color:white; padding:8px 14px; cursor:pointer; font-weight:bold;">-</button>
          <span style="font-family:var(--font-mono); font-weight:bold; padding:0 4px; width:24px; text-align:center;">${item.quantity}</span>
          <button onclick="updateQuantity('${item.id}', ${item.quantity + 1})" style="background:none; border:none; color:white; padding:8px 14px; cursor:pointer; font-weight:bold;">+</button>
        </div>
        <button onclick="removeFromCart('${item.id}')" style="background:none; border:none; color:#FF5252; padding:8px; cursor:pointer; display:flex;" aria-label="Remove item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
        </button>
      </div>
    </div>
  `).join('');
  
  // Calculate pricing summary
  const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  const delivery = subtotal >= 999 ? 0 : 99;
  const total = subtotal + delivery;
  
  const summaryContainer = document.getElementById('cart-pricing-summary');
  if (summaryContainer) {
    summaryContainer.innerHTML = `
      <div class="checkout-summary-row">
        <span>Subtotal</span>
        <span style="font-family:var(--font-mono); font-weight:600;">₹${subtotal}</span>
      </div>
      <div class="checkout-summary-row">
        <span>Delivery Charges</span>
        <span style="font-family:var(--font-mono); font-weight:600; color:${delivery === 0 ? '#A4FF3F' : 'white'}">${delivery === 0 ? 'FREE' : '₹' + delivery}</span>
      </div>
      <div class="checkout-total-row">
        <span>Total Payable</span>
        <span style="font-family:var(--font-mono);">₹${total}</span>
      </div>
    `;
  }
}

function renderCheckout() {
  const container = document.getElementById('checkout-items-summary');
  if (!container) return;
  
  const cart = getCart();
  if (cart.length === 0) {
    container.innerHTML = `<p style="color:var(--color-text-muted); text-align:center;">Your cart is empty.</p>`;
    return;
  }
  
  container.innerHTML = cart.map(item => `
    <div style="display:flex; justify-content:space-between; margin-bottom:12px; font-size:14px; border-bottom:1px solid rgba(164,255,63,0.05); padding-bottom:12px;">
      <span>${item.name} <strong style="color:var(--color-lime);">x ${item.quantity}</strong></span>
      <span style="font-family:var(--font-mono); font-weight:600;">₹${item.price * item.quantity}</span>
    </div>
  `).join('');
  
  const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  const delivery = subtotal >= 999 ? 0 : 99;
  const total = subtotal + delivery;
  
  const totalBox = document.getElementById('checkout-total-box');
  if (totalBox) {
    totalBox.innerHTML = `
      <div class="checkout-summary-row">
        <span>Subtotal</span>
        <span>₹${subtotal}</span>
      </div>
      <div class="checkout-summary-row">
        <span>Delivery</span>
        <span style="color:${delivery === 0 ? '#A4FF3F' : 'white'}">${delivery === 0 ? 'FREE' : '₹' + delivery}</span>
      </div>
      <div class="checkout-total-row" style="border-top: 1px solid rgba(164, 255, 63, 0.2); margin-top: 12px; padding-top: 12px; font-size:20px;">
        <span>Grand Total</span>
        <span>₹${total}</span>
      </div>
    `;
  }
}

/* ==========================================================================
   WhatsApp Order Modal Generator (Global reusable helper)
   ========================================================================== */
function initWhatsAppModals() {
  // Reusable modal markup insertion if not exists
  let modal = document.getElementById('whatsapp-order-modal');
  if (!modal) {
    modal = document.createElement('div');
    modal.id = 'whatsapp-order-modal';
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal-content">
        <button class="modal-close" id="whatsapp-modal-close" aria-label="Close modal">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
        <div class="modal-header">
          <svg class="modal-whatsapp-logo" width="36" height="36" viewBox="0 0 24 24" fill="currentColor"><path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946C.06 5.348 5.397.01 12.008.01c3.202.001 6.212 1.246 8.477 3.514 2.266 2.268 3.507 5.28 3.505 8.484-.004 6.657-5.34 11.997-11.953 11.997-2.005-.001-3.973-.502-5.717-1.455L0 24zm6.59-4.846c1.6.95 3.188 1.449 4.825 1.451 5.436 0 9.859-4.42 9.863-9.864.002-2.637-1.013-5.115-2.86-6.964-1.847-1.849-4.321-2.866-6.953-2.867-5.439 0-9.86 4.42-9.864 9.864-.001 1.748.477 3.456 1.385 4.958l-.98 3.57 3.664-.961zm11.365-5.963c-.1-.167-.367-.267-.767-.467-.4-.2-2.367-1.167-2.734-1.3-.367-.133-.633-.2-.9.2-.267.4-1.034 1.3-1.267 1.567-.233.267-.467.3-.867.1-.4-.2-1.688-.622-3.214-1.984-1.187-1.059-1.988-2.367-2.222-2.767-.233-.4-.025-.617.175-.816.18-.18.4-.467.6-.7.2-.233.267-.4.4-.667.133-.267.067-.5-.033-.7-.1-.2-.9-2.167-1.234-2.967-.324-.783-.655-.678-.9-.69l-.767-.013c-.267 0-.7.1-1.067.5-.367.4-1.4 1.367-1.4 3.333s1.433 3.867 1.633 4.133c.2.267 2.817 4.3 6.825 6.033.953.412 1.696.658 2.275.842.958.304 1.83.261 2.518.158.767-.116 2.367-.967 2.7-1.9.333-.933.333-1.733.233-1.9-.1-.167-.366-.266-.766-.466z"/></svg>
          <div>
            <h3 class="modal-title">WhatsApp Cash on Delivery (COD)</h3>
            <p style="font-size:12px; color:var(--color-text-muted); margin-top:2px;">Fast, secure order booking & confirmation</p>
          </div>
        </div>
        <form id="whatsapp-order-form">
          <input type="hidden" id="wa-target-product" value="">
          <div class="form-group">
            <label for="wa-name">Full Name</label>
            <input type="text" id="wa-name" class="form-control" required placeholder="Enter your name">
          </div>
          <div class="form-group">
            <label for="wa-phone">WhatsApp Number</label>
            <input type="tel" id="wa-phone" class="form-control" required placeholder="e.g. 9876543210" pattern="[6-9][0-9]{9}" title="Please enter a valid 10-digit mobile number">
          </div>
          <div class="form-group">
            <label for="wa-address">Complete Address</label>
            <textarea id="wa-address" class="form-control" required placeholder="Flat, Street, Area..."></textarea>
          </div>
          <div class="form-group" style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
            <div>
              <label for="wa-city">City</label>
              <input type="text" id="wa-city" class="form-control" required placeholder="City">
            </div>
            <div>
              <label for="wa-pincode">Pincode</label>
              <input type="text" id="wa-pincode" class="form-control" required placeholder="6-digit Pincode" pattern="[1-9][0-9]{5}">
            </div>
          </div>
          <button type="submit" class="btn btn-whatsapp" style="width:100%; margin-top:16px;">
            Confirm Order & Open WhatsApp
          </button>
        </form>
      </div>
    `;
    document.body.appendChild(modal);
    
    // Wire up events
    document.getElementById('whatsapp-modal-close').addEventListener('click', closeWAModal);
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeWAModal();
    });
    
    document.getElementById('whatsapp-order-form').addEventListener('submit', handleWASubmit);
  }
  
  // Attach triggers dynamically
  document.querySelectorAll('.whatsapp-order-trigger').forEach(trigger => {
    trigger.addEventListener('click', (e) => {
      e.preventDefault();
      const pId = trigger.getAttribute('data-product-id');
      document.getElementById('wa-target-product').value = pId || 'cart';
      openWAModal();
    });
  });
}

function openWAModal() {
  document.getElementById('whatsapp-order-modal').classList.add('active');
}

function closeWAModal() {
  document.getElementById('whatsapp-order-modal').classList.remove('active');
  document.getElementById('whatsapp-order-form').reset();
}

function handleWASubmit(e) {
  e.preventDefault();
  
  const targetId = document.getElementById('wa-target-product').value;
  const name = document.getElementById('wa-name').value;
  const phone = document.getElementById('wa-phone').value;
  const address = document.getElementById('wa-address').value;
  const city = document.getElementById('wa-city').value;
  const pincode = document.getElementById('wa-pincode').value;
  
  const recipientNumber = "918329931123";
  let message = "";
  
  if (targetId === 'cart') {
    // Collect from cart
    const cart = getCart();
    if (cart.length === 0) {
      alert("Your cart is empty!");
      return;
    }
    
    let subtotal = 0;
    let itemsText = cart.map(item => {
      subtotal += item.price * item.quantity;
      return `- ${item.name} x${item.quantity} (₹${item.price * item.quantity})`;
    }).join('\n');
    
    const delivery = subtotal >= 999 ? 0 : 99;
    const total = subtotal + delivery;
    
    message = `⚡ *NEW BUZZSHIELD ORDER* ⚡\n\n` +
              `*Customer Details:*\n` +
              `• Name: ${name}\n` +
              `• Contact: ${phone}\n` +
              `• Address: ${address}, ${city} - ${pincode}\n\n` +
              `*Ordered Items:*\n` +
              `${itemsText}\n\n` +
              `• Subtotal: ₹${subtotal}\n` +
              `• Delivery Charge: ${delivery === 0 ? 'FREE' : '₹' + delivery}\n` +
              `• *Total Payable (COD): ₹${total}*\n\n` +
              `Please confirm my Cash on Delivery order and share tracking details!`;
              
    // Empty the cart on successful submission
    localStorage.removeItem('buzzshield_cart');
    updateCartBadge();
  } else {
    // Single product quick order
    fetch('products.json')
      .then(res => res.json())
      .then(products => {
        const product = products.find(p => p.id === targetId);
        if (!product) return;
        
        const subtotal = product.price;
        const delivery = subtotal >= 999 ? 0 : 99;
        const total = subtotal + delivery;
        
        message = `⚡ *NEW BUZZSHIELD QUICK ORDER* ⚡\n\n` +
                  `*Customer Details:*\n` +
                  `• Name: ${name}\n` +
                  `• Contact: ${phone}\n` +
                  `• Address: ${address}, ${city} - ${pincode}\n\n` +
                  `*Product:*\n` +
                  `• Name: ${product.name}\n` +
                  `• Qty: 1\n\n` +
                  `• Subtotal: ₹${subtotal}\n` +
                  `• Delivery Charge: ${delivery === 0 ? 'FREE' : '₹' + delivery}\n` +
                  `• *Total Payable (COD): ₹${total}*\n\n` +
                  `Please confirm my Cash on Delivery order and share tracking details!`;
      })
      .catch(err => console.error(err));
  }
  
  // Timeout ensures fetch can resolve in single-product scenario
  setTimeout(() => {
    const waUrl = `https://wa.me/${recipientNumber}?text=${encodeURIComponent(message)}`;
    window.open(waUrl, '_blank');
    closeWAModal();
    
    // Redirect to home or order-success representation
    alert("Order summary generated! Redirecting to WhatsApp to complete confirmation.");
    window.location.href = "index.html";
  }, 300);
}

/* ==========================================================================
   Online Payments Simulation (Razorpay integration point)
   ========================================================================== */
function initiateRazorpayPayment() {
  const cart = getCart();
  if (cart.length === 0) {
    alert("Your cart is empty!");
    return;
  }
  
  const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  const delivery = subtotal >= 999 ? 0 : 99;
  const total = subtotal + delivery;
  
  // Custom interactive simulation alert highlighting security keys configuration requirements
  alert(
    `🔒 RAZORPAY SECURE GATEWAY INTEGRATION POINT\n` +
    `-----------------------------------------\n` +
    `Simulating safe transaction for amount: ₹${total}\n\n` +
    `// TODO: In production, inject your Razorpay standard API Key:\n` +
    `// const rzp = new Razorpay({ key: "rzp_live_yourPublicKeyHere" });\n\n` +
    `Click OK to simulate a successful payment capture.`
  );
  
  // Handle successful simulated flow
  localStorage.removeItem('buzzshield_cart');
  updateCartBadge();
  alert("🎉 Success! Your payment was simulated successfully. Order recorded.");
  window.location.href = "index.html";
}

/* ==========================================================================
   Helper Utilities
   ========================================================================== */
function showToast(message) {
  const toast = document.createElement('div');
  toast.style.position = 'fixed';
  toast.style.bottom = '24px';
  toast.style.right = '24px';
  toast.style.backgroundColor = 'var(--color-lime)';
  toast.style.color = 'var(--color-bg-deep)';
  toast.style.padding = '12px 24px';
  toast.style.borderRadius = '8px';
  toast.style.fontWeight = '700';
  toast.style.zIndex = '1000';
  toast.style.boxShadow = '0 8px 16px rgba(0,0,0,0.3)';
  toast.style.transition = 'opacity 0.3s ease';
  toast.textContent = message;
  
  document.body.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}
