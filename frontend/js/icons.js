/** Inline SVG icon helper — loads from sprite */
function icon(name, cls = '') {
  return `<svg class="svg-icon ${cls}" aria-hidden="true"><use href="/icons/sprite.svg#icon-${name}"></use></svg>`;
}

const TYPE_ICON = {
  HOUSE: 'house',
  APARTMENT: 'apartment',
  PLOT: 'plot',
  SHOP: 'shop'
};

function typeIcon(type) {
  return icon(TYPE_ICON[type] || 'house', 'icon-lg');
}
