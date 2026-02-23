/**
 * student.js — Valid8 Student Dashboard
 * Renders lot cards, populates the jump dropdown, and drives the Leaflet map.
 * All data polled from /api/summary every 5 seconds.
 */

// ── Leaflet map setup (center on Rutgers NB) ──────────────────────────────────
const map = L.map('mapContainer', {
    center: [40.5028, -74.4490],
    zoom: 14,
    zoomControl: true
});

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    maxZoom: 19
}).addTo(map);

// ── State ────────────────────────────────────────────────────────────────────
const markers = {};       // lotName → Leaflet CircleMarker
let lotsCache = [];       // last fetched lot data

// ── Color helpers ─────────────────────────────────────────────────────────────
function markerHex(color) {
    if (color === 'green')  return '#22c55e';
    if (color === 'yellow') return '#facc15';
    return '#ef4444';
}

function barClass(color) {
    if (color === 'green')  return 'background:#22c55e';
    if (color === 'yellow') return 'background:#facc15';
    return 'background:#ef4444';
}

function dotColor(color) {
    if (color === 'green')  return '#22c55e';
    if (color === 'yellow') return '#facc15';
    return '#ef4444';
}

// ── Main fetch & render ───────────────────────────────────────────────────────
async function refreshPage() {
    try {
        const res = await fetch('/api/summary');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        lotsCache = await res.json();
        renderCards(lotsCache);
        updateDropdown(lotsCache);
        updateMap(lotsCache);
        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load parking data: ' + e.message);
    }
}

// ── Lot Cards ─────────────────────────────────────────────────────────────────
function renderCards(lots) {
    const grid = document.getElementById('lotGrid');
    if (!lots || lots.length === 0) {
        grid.innerHTML = '<div class="col-span-full text-center text-blue-900 py-12">No data available.</div>';
        return;
    }
    grid.innerHTML = lots.map(lot => {
        const occ = Math.min(100, Math.max(0, lot.occupancyPercent || 0));
        const colorHex = markerHex(lot.availabilityColor);
        const dot = dotColor(lot.availabilityColor);
        const cardId = 'card-' + lot.lotName.replace(/\s+/g, '-');

        return `
        <div class="card-bg rounded-xl p-5 flex flex-col gap-3 transition cursor-pointer" id="${escHtml(cardId)}"
             onclick="jumpToLot('${escHtml(lot.lotName)}')">

            <!-- Name + colored dot -->
            <div class="flex items-start justify-between gap-1">
                <div class="text-sm font-bold text-white leading-snug">${escHtml(lot.lotName)}</div>
                <span style="
                    width:14px;height:14px;border-radius:50%;flex-shrink:0;margin-top:2px;
                    background:${dot};display:inline-block;
                    box-shadow:0 0 6px ${dot}99;
                "></span>
            </div>

            <!-- Big available number -->
            <div class="text-center py-1">
                <div class="text-4xl font-black" style="color:${colorHex}">${lot.availableNow}</div>
                <div class="text-xs text-gray-500 mt-0.5">spots available</div>
            </div>

            <!-- Bar -->
            <div>
                <div class="flex justify-between text-xs text-blue-900 mb-1">
                    <span>${lot.inLotNow} in lot</span>
                    <span>${lot.capacity} capacity</span>
                </div>
                <div class="bar-track w-full">
                    <div style="width:${occ}%;height:10px;border-radius:9999px;${barClass(lot.availabilityColor)};transition:width 0.5s"></div>
                </div>
            </div>

            <!-- Footer -->
            <div class="flex justify-between text-xs text-blue-900 border-t border-blue-950 pt-2">
                <span>Cap: <strong class="text-blue-700">${lot.capacity}</strong></span>
                <span>Sessions: <strong class="text-blue-700">${lot.totalSessions}</strong></span>
            </div>
        </div>`;
    }).join('');
}

// ── Dropdown ──────────────────────────────────────────────────────────────────
function updateDropdown(lots) {
    const sel = document.getElementById('lotJumpSelect');
    const current = sel.value;
    // Rebuild options preserving current selection
    const opts = lots.map(l =>
        `<option value="${escHtml(l.lotName)}" ${l.lotName === current ? 'selected' : ''}>
            ${escHtml(l.lotName)} — ${l.availableNow} available
        </option>`
    ).join('');
    sel.innerHTML = '<option value="">— Select a Lot —</option>' + opts;
}

/**
 * Scroll the card for the given lot into view AND open its map popup.
 * Called from both the dropdown change and card click.
 */
function jumpToLot(lotName) {
    if (!lotName) return;

    // Update dropdown selection
    const sel = document.getElementById('lotJumpSelect');
    sel.value = lotName;

    // Scroll to card
    const cardId = 'card-' + lotName.replace(/\s+/g, '-');
    const card = document.getElementById(cardId);
    if (card) {
        // Highlight briefly
        document.querySelectorAll('.card-bg').forEach(c => c.classList.remove('highlighted-card'));
        card.classList.add('highlighted-card');
        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        setTimeout(() => card.classList.remove('highlighted-card'), 2000);
    }

    // Open map marker popup
    const marker = markers[lotName];
    if (marker) {
        map.setView(marker.getLatLng(), 16, { animate: true });
        marker.openPopup();
    }

    // Scroll to map
    setTimeout(() => {
        document.getElementById('mapContainer').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 400);
}

// ── Leaflet Map ───────────────────────────────────────────────────────────────
function buildPopup(lot) {
    const hex = markerHex(lot.availabilityColor);
    return `
        <div style="min-width:190px;font-family:sans-serif;font-size:13px">
            <div style="font-weight:800;font-size:14px;margin-bottom:7px;color:#1e3a8a">${escHtml(lot.lotName)}</div>
            <table style="width:100%;border-collapse:collapse">
                <tr><td style="color:#6b7280;padding:2px 0">Capacity</td>
                    <td style="text-align:right;font-weight:700">${lot.capacity}</td></tr>
                <tr><td style="color:#6b7280;padding:2px 0">In Lot Now</td>
                    <td style="text-align:right;font-weight:700">${lot.inLotNow}</td></tr>
                <tr><td style="color:#6b7280;padding:2px 0">Available</td>
                    <td style="text-align:right;font-weight:800;color:${hex}">${lot.availableNow}</td></tr>
            </table>
        </div>`;
}

function updateMap(lots) {
    for (const lot of lots) {
        const hex = markerHex(lot.availabilityColor);
        const popup = buildPopup(lot);
        const tooltip = `${lot.lotName} — ${lot.availableNow} spots`;

        // Create a DivIcon that shows the available spots number inside the circle
        const iconHtml = `
            <div style="
                width:42px;height:42px;border-radius:50%;
                background:${hex};
                border:3px solid rgba(0,0,0,0.4);
                display:flex;align-items:center;justify-content:center;
                font-weight:900;font-size:14px;color:#000;
                box-shadow:0 2px 8px rgba(0,0,0,0.5);
                line-height:1
            ">${lot.availableNow}</div>`;

        if (markers[lot.lotName]) {
            const m = markers[lot.lotName];
            m.setIcon(L.divIcon({ html: iconHtml, className: '', iconSize: [42, 42], iconAnchor: [21, 21] }));
            m.setPopupContent(popup);
            m.setTooltipContent(tooltip);
        } else {
            const m = L.marker([lot.latitude, lot.longitude], {
                icon: L.divIcon({ html: iconHtml, className: '', iconSize: [42, 42], iconAnchor: [21, 21] })
            });
            m.bindPopup(popup);
            m.bindTooltip(tooltip, { permanent: false, direction: 'top' });
            m.addTo(map);
            markers[lot.lotName] = m;
        }
    }
}

// ── Utility ───────────────────────────────────────────────────────────────────
function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// Also update the key timestamp
const origSetLastUpdated = typeof setLastUpdated === 'function' ? setLastUpdated : null;
function setLastUpdated() {
    const ts = 'Last updated: ' + new Date().toLocaleString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
    const el  = document.getElementById('lastUpdated');
    const el2 = document.getElementById('lastUpdatedKey');
    if (el)  el.textContent  = ts;
    if (el2) el2.textContent = ts;
}

// ── Start ─────────────────────────────────────────────────────────────────────
refreshPage();
setInterval(refreshPage, 5000);
