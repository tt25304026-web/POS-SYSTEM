// Sample Data
const PRODUCTS = [
    { id: 1, name: 'エスプレッソ', price: 500, category: 'coffee', icon: '☕' },
    { id: 2, name: 'カプチーノ', price: 650, category: 'coffee', icon: '🥤' },
    { id: 3, name: 'ラテ', price: 700, category: 'coffee', icon: '🥛' },
    { id: 4, name: '緑茶', price: 480, category: 'tea', icon: '🍵' },
    { id: 5, name: 'アールグレイ', price: 500, category: 'tea', icon: '🫖' },
    { id: 6, name: 'クロワッサン', price: 400, category: 'bakery', icon: '🥐' },
    { id: 7, name: 'チョコマーフィン', price: 450, category: 'bakery', icon: '🧁' },
    { id: 8, name: 'ブルーベリースコーン', price: 480, category: 'bakery', icon: '🥯' },
    { id: 9, name: 'クラブサンドイッチ', price: 1200, category: 'food', icon: '🥪' },
    { id: 10, name: 'シーザーサラダ', price: 1000, category: 'food', icon: '🥗' },
    { id: 11, name: 'ベーグル & クリームチーズ', price: 600, category: 'bakery', icon: '🥯' },
    { id: 12, name: 'モカ', price: 750, category: 'coffee', icon: '🍫' },
];

const NEWS = [
    {
        id: 1,
        title: '春の新メニューが登場！',
        date: '2024-04-01',
        content: '爽やかな季節にぴったりの「さくらラテ」と、ボリュームたっぷりの「春野菜のクラブサンドイッチ」がメニューに加わりました。ぜひレジから注文してみてください。'
    },
    {
        id: 2,
        title: '4月の営業時間変更について',
        date: '2024-03-28',
        content: '社員研修のため、4月15日は17:00までの営業とさせていただきます。ご不便をおかけしますが、何卒ご了承ください。'
    }
];

// App State
let cart = [];
let currentCategory = 'all';
let searchQuery = '';
let orders = JSON.parse(localStorage.getItem('pos_orders') || '[]');
let currentView = 'home';

// DOM Elements
const productGrid = document.getElementById('productGrid');
const cartItemsContainer = document.getElementById('cartItems');
const cartSidebar = document.querySelector('.cart-sidebar');
const appContainer = document.querySelector('.app-container');
const newsList = document.getElementById('newsList');
const latestNewsTitle = document.getElementById('latestNewsTitle');
const subtotalEl = document.getElementById('subtotal');
const taxEl = document.getElementById('tax');
const totalEl = document.getElementById('total');
const checkoutBtn = document.getElementById('checkoutBtn');
const clearCartBtn = document.getElementById('clearCart');
const themeSwitcher = document.getElementById('themeSwitcher');
const productSearch = document.getElementById('productSearch');
const catBtns = document.querySelectorAll('.cat-btn');
const navItems = document.querySelectorAll('.nav-item');
const views = document.querySelectorAll('.view');
const paymentDialog = document.getElementById('paymentDialog');
const modalTotal = document.getElementById('modalTotal');
const cashReceived = document.getElementById('cashReceived');
const changeAmount = document.getElementById('changeAmount');
const confirmPaymentBtn = document.getElementById('confirmPayment');
const cancelPaymentBtn = document.getElementById('cancelPayment');

// Initialize
function init() {
    renderProducts();
    updateCart();
    renderOrders();
    renderNews();
    updateStats();
    
    // Initial view
    switchView('home');

    // Category filtering
    catBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            catBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentCategory = btn.dataset.category;
            renderProducts();
        });
    });

    // View switching
    navItems.forEach(item => {
        const targetView = item.dataset.view;
        if (!targetView) return;

        item.addEventListener('click', () => {
            switchView(targetView);
        });
    });

    // Search
    productSearch.addEventListener('input', (e) => {
        searchQuery = e.target.value.toLowerCase();
        renderProducts();
    });

    // Theme Switcher
    themeSwitcher.addEventListener('click', () => {
        const isDark = document.body.classList.toggle('dark-theme');
        themeSwitcher.textContent = isDark ? '☀️ ライトモード' : '🌙 ダークモード';
    });

    // Keyboard Shortcuts
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            productSearch.focus();
        }
    });

    // Cart Actions
    clearCartBtn.addEventListener('click', () => {
        if (confirm('カートの商品をすべて削除しますか？')) {
            cart = [];
            updateCart();
        }
    });

    // Checkout
    checkoutBtn.addEventListener('click', () => {
        const total = calculateTotals().total;
        modalTotal.textContent = formatCurrency(total);
        cashReceived.value = '';
        changeAmount.textContent = formatCurrency(0);
        paymentDialog.showModal();
    });

    cashReceived.addEventListener('input', () => {
        const received = parseFloat(cashReceived.value) || 0;
        const total = calculateTotals().total;
        const change = Math.max(0, received - total);
        changeAmount.textContent = formatCurrency(change);
    });

    confirmPaymentBtn.addEventListener('click', () => {
        const { subtotal, tax, total } = calculateTotals();
        const order = {
            id: 'ORD-' + Date.now(),
            date: new Date().toISOString(),
            items: [...cart],
            subtotal,
            tax,
            total,
            status: '完了'
        };
        
        orders.push(order);
        localStorage.setItem('pos_orders', JSON.stringify(orders));
        
        cart = [];
        updateCart();
        paymentDialog.close();
        alert('注文が完了しました！');
    });

    cancelPaymentBtn.addEventListener('click', () => {
        paymentDialog.close();
    });
}

function switchView(viewName) {
    currentView = viewName;
    
    // Update Sidebar UI
    navItems.forEach(item => {
        item.classList.toggle('active', item.dataset.view === viewName);
    });

    // Update Content Views
    views.forEach(v => {
        v.classList.toggle('active', v.id === `${viewName}View`);
    });

    // Cart Sidebar visibility
    if (viewName === 'pos') {
        cartSidebar.style.display = 'flex';
        appContainer.style.gridTemplateColumns = '80px 1fr 320px';
    } else {
        cartSidebar.style.display = 'none';
        appContainer.style.gridTemplateColumns = '80px 1fr';
    }

    if (viewName === 'dashboard') updateStats();
    if (viewName === 'orders') renderOrders();
    if (viewName === 'news') renderNews();
}

function renderNews() {
    if (!newsList) return;
    
    newsList.innerHTML = NEWS.map(item => `
        <div class="product-card" style="cursor: default; width: 100%; flex-direction: column; align-items: flex-start; gap: 12px; padding: 24px;">
            <div style="display: flex; justify-content: space-between; width: 100%;">
                <strong>🆕 ${item.title}</strong>
                <small style="color: var(--text-muted);">${item.date}</small>
            </div>
            <p style="color: var(--text-muted); line-height: 1.6; margin: 0;">${item.content}</p>
        </div>
    `).join('');

    if (latestNewsTitle && NEWS.length > 0) {
        latestNewsTitle.textContent = NEWS[0].title;
    }
}

// Rendering Functions
function renderProducts() {
    const filtered = PRODUCTS.filter(p => {
        const matchCat = currentCategory === 'all' || p.category === currentCategory;
        const matchSearch = p.name.toLowerCase().includes(searchQuery);
        return matchCat && matchSearch;
    });

    productGrid.innerHTML = filtered.map(p => `
        <div class="product-card" onclick="addToCart(${p.id})">
            <div class="product-img">${p.icon}</div>
            <div class="product-info">
                <h3>${p.name}</h3>
                <p class="price">${formatCurrency(p.price)}</p>
            </div>
        </div>
    `).join('');
}

function addToCart(productId) {
    const product = PRODUCTS.find(p => p.id === productId);
    const existing = cart.find(item => item.id === productId);

    if (existing) {
        existing.quantity++;
    } else {
        cart.push({ ...product, quantity: 1 });
    }
    updateCart();
}

function updateCart() {
    if (cart.length === 0) {
        cartItemsContainer.innerHTML = '<div class="empty-cart-msg">カートは空です</div>';
        checkoutBtn.disabled = true;
    } else {
        cartItemsContainer.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <h4>${item.name}</h4>
                    <p class="item-price">${formatCurrency(item.price)} x ${item.quantity}</p>
                </div>
                <div class="cart-item-qty">
                    <button class="qty-btn" onclick="changeQty(${item.id}, -1)">-</button>
                    <span>${item.quantity}</span>
                    <button class="qty-btn" onclick="changeQty(${item.id}, 1)">+</button>
                </div>
                <div class="item-total">
                    ${formatCurrency(item.price * item.quantity)}
                </div>
            </div>
        `).join('');
        checkoutBtn.disabled = false;
    }

    const { subtotal, tax, total } = calculateTotals();
    subtotalEl.textContent = formatCurrency(subtotal);
    taxEl.textContent = formatCurrency(tax);
    totalEl.textContent = formatCurrency(total);
}

function changeQty(id, delta) {
    const item = cart.find(i => i.id === id);
    if (!item) return;

    item.quantity += delta;
    if (item.quantity <= 0) {
        cart = cart.filter(i => i.id !== id);
    }
    updateCart();
}

function calculateTotals() {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.1;
    const total = subtotal + tax;
    return { subtotal, tax, total };
}

function renderOrders() {
    const orderList = document.getElementById('orderList');
    if (orders.length === 0) {
        orderList.innerHTML = '<div class="empty-cart-msg">注文履歴がありません</div>';
        return;
    }

    orderList.innerHTML = orders.slice().reverse().map(order => `
        <div class="product-card" style="cursor: default; width: 100%; margin-bottom: 12px; flex-direction: row; align-items: center; justify-content: space-between;">
            <div>
                <strong>${order.id}</strong><br>
                <small>${new Date(order.date).toLocaleString()}</small>
            </div>
            <div>
                ${order.items.length} 点の商品
            </div>
            <div class="price">
                ${formatCurrency(order.total)}
            </div>
            <div style="color: var(--success); font-weight: 600;">
                ${order.status}
            </div>
        </div>
    `).join('');
}

function updateStats() {
    const today = new Date().toISOString().split('T')[0];
    const todayOrders = orders.filter(o => o.date.startsWith(today));
    const todayTotal = todayOrders.reduce((sum, o) => sum + o.total, 0);

    document.getElementById('todaySales').textContent = formatCurrency(todayTotal);
    document.getElementById('totalOrders').textContent = orders.length;
}

// Helpers
function formatCurrency(amount) {
    return new Intl.NumberFormat('ja-JP', {
        style: 'currency',
        currency: 'JPY'
    }).format(amount);
}

// Start App
init();
