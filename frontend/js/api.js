const API = '/api';

async function request(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

const api = {
  stats:       () => request(`${API}/stats`),
  persons:     () => request(`${API}/persons`),
  registerSeller: (body) => request(`${API}/persons/seller`, { method: 'POST', body: JSON.stringify(body) }),
  registerBuyer:  (body) => request(`${API}/persons/buyer`,  { method: 'POST', body: JSON.stringify(body) }),
  loginSeller: (body) => request(`${API}/persons/login/seller`, { method: 'POST', body: JSON.stringify(body) }),
  loginBuyer:  (body) => request(`${API}/persons/login/buyer`,  { method: 'POST', body: JSON.stringify(body) }),
  addProperty: (body) => request(`${API}/properties`, { method: 'POST', body: JSON.stringify(body) }),
  search: (params) => {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v) q.set(k, v); });
    return request(`${API}/properties/search?${q}`);
  },
  recordTx: (body) => request(`${API}/transactions`, { method: 'POST', body: JSON.stringify(body) }),
  transactions: () => request(`${API}/transactions`),
  rentals: () => request(`${API}/rentals`),
  renewRent: (propertyId) => request(`${API}/rentals/renew`, {
    method: 'POST', body: JSON.stringify({ propertyId })
  }),
  endRent: (propertyId) => request(`${API}/rentals/end`, {
    method: 'POST', body: JSON.stringify({ propertyId })
  })
};

function formatPKR(n) {
  return 'PKR ' + Number(n).toLocaleString('en-PK');
}

function typeIcon(type) {
  return { HOUSE: '🏠', APARTMENT: '🏢', PLOT: '📐', SHOP: '🏪' }[type] || '🏗️';
}
