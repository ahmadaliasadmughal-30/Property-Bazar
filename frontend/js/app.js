/* EstateVault — Frontend Application */

let selectedRole = 'seller';
let loginRole = 'seller';

const SESSION_KEYS = { seller: 'ev_seller', buyer: 'ev_buyer' };

// ── Session helpers ──
function getSession(role) {
  try { return JSON.parse(localStorage.getItem(SESSION_KEYS[role])); }
  catch { return null; }
}

function setSession(role, user) {
  localStorage.setItem(SESSION_KEYS[role], JSON.stringify(user));
  updateSessionUI();
}

function clearSession(role) {
  localStorage.removeItem(SESSION_KEYS[role]);
  updateSessionUI();
}

function isLoggedIn(role) {
  return !!getSession(role);
}

function updateSessionUI() {
  const seller = getSession('seller');
  const buyer = getSession('buyer');
  const sellerEl = document.getElementById('session-seller');
  const buyerEl = document.getElementById('session-buyer');

  if (seller) {
    sellerEl.textContent = `🏷️ Seller: #${seller.id} ${seller.name}`;
    sellerEl.classList.remove('hidden');
  } else {
    sellerEl.classList.add('hidden');
  }

  if (buyer) {
    buyerEl.textContent = `🛒 Buyer: #${buyer.id} ${buyer.name}`;
    buyerEl.classList.remove('hidden');
  } else {
    buyerEl.classList.add('hidden');
  }

  updateLoginPage();
  updateListGate();
  updateDealGate();
}

// ── Loading screen ──
let loadingTimer = null;

function showLoading(text = 'Loading...') {
  const screen = document.getElementById('loading-screen');
  screen.querySelector('.loading-text').textContent = text;
  screen.classList.remove('hidden');
}

function hideLoading() {
  const screen = document.getElementById('loading-screen');
  screen.classList.add('hidden');
}

async function withLoading(fn, text = 'Loading...', minMs = 450) {
  showLoading(text);
  const start = Date.now();
  try {
    return await fn();
  } finally {
    const elapsed = Date.now() - start;
    const remaining = Math.max(0, minMs - elapsed);
    await new Promise(r => setTimeout(r, remaining));
    hideLoading();
  }
}

// ── Navigation ──
document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', async () => {
    const page = item.dataset.page;
    await navigateTo(page);
  });
});

document.querySelectorAll('[data-goto]').forEach(btn => {
  btn.addEventListener('click', () => navigateTo(btn.dataset.goto));
});

async function navigateTo(page) {
  document.querySelectorAll('.nav-item').forEach(n => {
    n.classList.toggle('active', n.dataset.page === page);
  });

  await withLoading(async () => {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const el = document.getElementById('page-' + page);
    el.classList.add('active');
    el.classList.remove('page-enter');
    void el.offsetWidth;
    el.classList.add('page-enter');

    if (page === 'dashboard') await loadDashboard();
    if (page === 'history') await loadHistory();
    if (page === 'deal') updateDealGate();
    if (page === 'list') updateListGate();
    if (page === 'login') updateLoginPage();
    if (page === 'rentals') await loadRentals();
  }, 'Loading section...');
}

// ── Toast ──
function toast(id, msg, ok = true) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.className = 'toast show ' + (ok ? 'success' : 'error');
  setTimeout(() => el.classList.remove('show'), 4000);
}

// ── Dashboard ──
async function loadDashboard() {
  try {
    const s = await api.stats();
    document.getElementById('stat-users').textContent = s.users;
    document.getElementById('stat-available').textContent = s.available;
    document.getElementById('stat-sold').textContent = s.sold;
    document.getElementById('stat-rented').textContent = s.rented;
    document.getElementById('stat-tx').textContent = s.transactions;
    document.getElementById('stat-commission').textContent = formatPKR(s.totalCommission);
  } catch (e) { console.error(e); }
}

// ── Login ──
document.querySelectorAll('.login-role-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    loginRole = btn.dataset.role;
    document.querySelectorAll('.login-role-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    updateLoginPage();
  });
});

function updateLoginPage() {
  const user = getSession(loginRole);
  const logoutBtn = document.getElementById('logout-btn');
  const form = document.getElementById('login-form');

  if (user) {
    logoutBtn.classList.remove('hidden');
    form.querySelector('button[type="submit"]').classList.add('hidden');
    document.getElementById('login-id').value = user.id;
    document.getElementById('login-email').value = user.email;
  } else {
    logoutBtn.classList.add('hidden');
    form.querySelector('button[type="submit"]').classList.remove('hidden');
  }
}

document.getElementById('login-form').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    id:    document.getElementById('login-id').value,
    email: document.getElementById('login-email').value.trim()
  };
  try {
    const fn = loginRole === 'seller' ? api.loginSeller : api.loginBuyer;
    const user = await fn(body);
    setSession(loginRole, user);
    toast('login-toast', `${user.type} logged in! Welcome, ${user.name}`);
  } catch (err) { toast('login-toast', err.message, false); }
});

document.getElementById('logout-btn').addEventListener('click', () => {
  clearSession(loginRole);
  document.getElementById('login-form').reset();
  document.getElementById('login-toast').className = 'toast';
  toast('login-toast', `Logged out from ${loginRole} account`);
});

// ── Auth gates ──
function updateListGate() {
  const seller = getSession('seller');
  const gate = document.getElementById('list-login-gate');
  const content = document.getElementById('list-content');

  if (seller) {
    gate.classList.add('hidden');
    content.classList.remove('hidden');
    document.getElementById('add-seller').value = seller.id;
    document.getElementById('list-logged-as').textContent =
      `Logged in as Seller #${seller.id} — ${seller.name}`;
  } else {
    gate.classList.remove('hidden');
    content.classList.add('hidden');
  }
}

function updateDealGate() {
  const buyer = getSession('buyer');
  const gate = document.getElementById('deal-login-gate');
  const content = document.getElementById('deal-content');

  if (buyer) {
    gate.classList.add('hidden');
    content.classList.remove('hidden');
    document.getElementById('deal-buyer').value = buyer.id;
    document.getElementById('deal-logged-as').textContent =
      `Logged in as Buyer #${buyer.id} — ${buyer.name}`;
  } else {
    gate.classList.remove('hidden');
    content.classList.add('hidden');
  }
}

// ── Register ──
document.querySelectorAll('.role-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    selectedRole = btn.dataset.role;
    document.querySelectorAll('.role-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

document.getElementById('register-form').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    name:  document.getElementById('reg-name').value.trim(),
    phone: document.getElementById('reg-phone').value.trim(),
    email: document.getElementById('reg-email').value.trim()
  };
  try {
    const fn = selectedRole === 'seller' ? api.registerSeller : api.registerBuyer;
    const user = await fn(body);
    toast('reg-toast', `${user.type} registered! Your ID is #${user.id}. Please log in before trading.`);
    e.target.reset();
  } catch (err) { toast('reg-toast', err.message, false); }
});

// ── Add Property ──
document.getElementById('add-form').addEventListener('submit', async e => {
  e.preventDefault();
  if (!isLoggedIn('seller')) {
    toast('add-toast', 'Please log in as a seller first', false);
    return;
  }
  const body = {
    type:     document.getElementById('add-type').value,
    title:    document.getElementById('add-title').value.trim(),
    location: document.getElementById('add-location').value.trim(),
    area:     document.getElementById('add-area').value,
    price:    document.getElementById('add-price').value,
    sellerId: document.getElementById('add-seller').value
  };
  try {
    const p = await api.addProperty(body);
    toast('add-toast', `Property #${p.id} listed — ${p.title}`);
    e.target.reset();
    document.getElementById('add-seller').value = getSession('seller').id;
    loadDashboard();
  } catch (err) { toast('add-toast', err.message, false); }
});

// ── Search ──
document.getElementById('search-form').addEventListener('submit', async e => {
  e.preventDefault();
  const params = {
    location: document.getElementById('search-location').value.trim(),
    minPrice: document.getElementById('search-min-price').value,
    maxPrice: document.getElementById('search-max-price').value,
    minArea:  document.getElementById('search-min-area').value,
    maxArea:  document.getElementById('search-max-area').value,
    type:     document.getElementById('search-type').value
  };
  try {
    const results = await api.search(params);
    renderProperties(results);
  } catch (err) { toast('search-toast', err.message, false); }
});

function renderProperties(list) {
  const grid = document.getElementById('property-grid');
  if (!list.length) {
    grid.innerHTML = `<div class="empty full-width"><div class="empty-icon">🔍</div><p>No properties match your filters.</p></div>`;
    return;
  }
  grid.innerHTML = list.map((p, i) => `
    <div class="property-card" style="animation: fadeUp 0.5s ease ${i * 0.08}s both">
      <div class="card-top">
        <span class="type-badge">${p.type}</span>
        <span class="card-icon">${typeIcon(p.type)}</span>
      </div>
      <div class="card-body">
        <h3>${p.title}</h3>
        <div class="location">📍 ${p.location}</div>
        <div class="meta">
          <span>${p.area} marla</span>
          <span>Seller #${p.sellerId}</span>
          <span>ID #${p.id}</span>
        </div>
        <div class="price">${formatPKR(p.price)}</div>
        <div class="commission">Commission: ${formatPKR(p.commission)}</div>
      </div>
    </div>
  `).join('');
}

// ── Deal ──
document.getElementById('deal-form').addEventListener('submit', async e => {
  e.preventDefault();
  if (!isLoggedIn('buyer')) {
    toast('deal-toast', 'Please log in as a buyer first', false);
    return;
  }
  const body = {
    propertyId: document.getElementById('deal-property').value,
    buyerId:    document.getElementById('deal-buyer').value,
    type:       document.getElementById('deal-type').value
  };
  try {
    const tx = await api.recordTx(body);
    const extra = tx.type === 'RENT'
      ? ' Manage renewals in Manage Rentals.'
      : '';
    toast('deal-toast',
      `Deal recorded! ${tx.type} — Amount ${formatPKR(tx.amount)}, Commission ${formatPKR(tx.commission)}.${extra}`);
    e.target.reset();
    document.getElementById('deal-buyer').value = getSession('buyer').id;
    loadDashboard();
  } catch (err) { toast('deal-toast', err.message, false); }
});

// ── Rentals ──
async function loadRentals() {
  const grid = document.getElementById('rentals-grid');
  grid.innerHTML = `<div class="empty full-width"><div class="loading-spinner small"></div><p>Loading rentals...</p></div>`;
  try {
    const list = await api.rentals();
    renderRentals(list);
  } catch (err) {
    grid.innerHTML = `<div class="empty full-width"><p>${err.message}</p></div>`;
  }
}

function renderRentals(list) {
  const grid = document.getElementById('rentals-grid');
  if (!list.length) {
    grid.innerHTML = `<div class="empty full-width"><div class="empty-icon">🏠</div><p>No active rentals. Record a Rent deal to see properties here.</p></div>`;
    return;
  }
  grid.innerHTML = list.map((r, i) => {
    const p = r.property;
    const t = r.tenant;
    return `
    <div class="rental-card" style="animation: fadeUp 0.5s ease ${i * 0.08}s both">
      <div class="card-top">
        <span class="type-badge rented">RENTED</span>
        <span class="card-icon">${typeIcon(p.type)}</span>
      </div>
      <div class="card-body">
        <h3>${p.title}</h3>
        <div class="location">📍 ${p.location}</div>
        <div class="meta">
          <span>Property #${p.id}</span>
          <span>${p.area} marla</span>
        </div>
        <div class="tenant-info">
          <strong>Tenant:</strong> #${t.id} — ${t.name}
        </div>
        <div class="rent-details">
          <span>Monthly: ${formatPKR(r.monthlyAmount)}</span>
          <span>Last payment: ${r.lastRentDate}</span>
        </div>
        <div class="rental-actions">
          <button class="btn btn-primary btn-sm" data-renew="${p.id}">Renew Monthly Rent</button>
          <button class="btn btn-danger btn-sm" data-end="${p.id}">End Lease &amp; Relist</button>
        </div>
      </div>
    </div>`;
  }).join('');

  grid.querySelectorAll('[data-renew]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.renew;
      btn.disabled = true;
      try {
        const tx = await api.renewRent(id);
        toast('rentals-toast',
          `Rent renewed for Property #${id}! Amount ${formatPKR(tx.amount)}, Commission ${formatPKR(tx.commission)}`);
        await loadRentals();
        loadDashboard();
      } catch (err) {
        toast('rentals-toast', err.message, false);
        btn.disabled = false;
      }
    });
  });

  grid.querySelectorAll('[data-end]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.end;
      if (!confirm(`End lease for Property #${id} and put it back on sale?`)) return;
      btn.disabled = true;
      try {
        const p = await api.endRent(id);
        toast('rentals-toast', `Lease ended. Property #${p.id} is now available for sale.`);
        await loadRentals();
        loadDashboard();
      } catch (err) {
        toast('rentals-toast', err.message, false);
        btn.disabled = false;
      }
    });
  });
}

// ── History ──
async function loadHistory() {
  const tbody = document.getElementById('history-body');
  try {
    const list = await api.transactions();
    if (!list.length) {
      tbody.innerHTML = `<tr><td colspan="8" class="empty">No transactions yet.</td></tr>`;
      return;
    }
    tbody.innerHTML = list.map(t => `
      <tr>
        <td>#${t.id}</td>
        <td>#${t.propertyId}</td>
        <td>#${t.buyerId}</td>
        <td>#${t.sellerId}</td>
        <td><span class="badge badge-${t.type === 'BUY' ? 'buy' : 'rent'}">${t.type.replace('_', ' ')}</span></td>
        <td>${formatPKR(t.amount)}</td>
        <td style="color:var(--gold)">${formatPKR(t.commission)}</td>
        <td>${t.date}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty">${err.message}</td></tr>`;
  }
}

// ── Init ──
updateSessionUI();
loadDashboard();
api.search({}).then(renderProperties).catch(() => {});
