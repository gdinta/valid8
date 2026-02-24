/**
 * map.js — Valid8 Map page.
 * Uses Leaflet.js to display parking lots on a Rutgers NB campus map.
 * Markers are color-coded and update every 5 seconds without resetting the view.
 */

// Initialize Leaflet map centered on Rutgers New Brunswick
const map = L.map('mapContainer', {
    center: [40.5028, -74.4490],
    zoom: 14,
    zoomControl: true
});

// OpenStreetMap tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
}).addTo(map);

// Map from lotName -> Leaflet CircleMarker (so we can update in-place)
const markers = {};

/** Return Leaflet color string based on availabilityColor */
function markerColor(color) {
    if (color === 'green')  return '#22c55e';
    if (color === 'yellow') return '#facc15';
    return '#ef4444';
}

/** Build popup HTML for a lot summary */
function buildPopup(lot) {
    const occ = (lot.occupancyPercent || 0).toFixed(1);
    return `
        <div style="min-width:180px; font-family:sans-serif">
            <div style="font-weight:bold; font-size:14px; margin-bottom:6px">${escHtml(lot.lotName)}</div>
            <table style="width:100%; font-size:12px; border-collapse:collapse">
                <tr><td style="color:#6b7280">Capacity</td><td style="text-align:right; font-weight:600">${lot.capacity}</td></tr>
                <tr><td style="color:#6b7280">In Lot Now</td><td style="text-align:right; font-weight:600">${lot.inLotNow}</td></tr>
                <tr><td style="color:#6b7280">Available</td><td style="text-align:right; font-weight:600; color:${markerColor(lot.availabilityColor)}">${lot.availableNow}</td></tr>
                <tr><td style="color:#6b7280">Occupancy</td><td style="text-align:right; font-weight:600">${occ}%</td></tr>
            </table>
        </div>`;
}

/** Build tooltip label */
function buildTooltip(lot) {
    return `${lot.lotName} — ${lot.availableNow} available`;
}

async function refreshPage() {
    try {
        const res = await fetch('/api/summary');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const lots = await res.json();

        for (const lot of lots) {
            const color = markerColor(lot.availabilityColor);
            const popupHtml = buildPopup(lot);
            const tooltipText = buildTooltip(lot);

            if (markers[lot.lotName]) {
                // Update existing marker without removing it (preserves map view)
                const m = markers[lot.lotName];
                m.setStyle({ color: color, fillColor: color });
                m.setPopupContent(popupHtml);
                m.setTooltipContent(tooltipText);
            } else {
                // Create new marker
                const m = L.circleMarker([lot.latitude, lot.longitude], {
                    radius: 14,
                    color: color,
                    fillColor: color,
                    fillOpacity: 0.85,
                    weight: 2
                });
                m.bindPopup(popupHtml);
                m.bindTooltip(tooltipText, { permanent: false, direction: 'top' });
                m.addTo(map);
                markers[lot.lotName] = m;
            }
        }

        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load map data: ' + e.message);
    }
}

function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// Initial load + polling
refreshPage();
setInterval(refreshPage, 5000);
