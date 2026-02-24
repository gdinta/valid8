/**
 * rupd.js — Valid8 RUPD Enforcement Dashboard
 * Single flat list of unregistered vehicles.
 * Columns: #, License Plate, Lot, Time In, Time Out.
 */

let allRecords = [];

async function refreshPage() {
    try {
        const res = await fetch('/api/unregistered');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        allRecords = await res.json();
        document.getElementById('totalCount').textContent = allRecords.length;
        populateDropdown(allRecords);
        renderTable(allRecords);
        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load data: ' + e.message);
    }
}

function populateDropdown(records) {
    const sel = document.getElementById('searchBox');
    const current = sel.value;

    sel.innerHTML = '<option value="">— All Lots —</option>';

    const lots = [...new Set(records.map(r => r.lotName))].sort();

    lots.forEach(lotName => {
        const opt = document.createElement('option');
        opt.value = 'lot:' + lotName;
        opt.textContent = lotName;
        if ('lot:' + lotName === current) opt.selected = true;
        sel.appendChild(opt);
    });
}

function applySearch() {
    const val = (document.getElementById('searchBox').value || '');
    if (!val) {
        renderTable(allRecords);
        return;
    }
    if (val.startsWith('plate:')) {
        const plate = val.replace('plate:', '');
        renderTable(allRecords.filter(r => r.licensePlate.toUpperCase() === plate));
    } else if (val.startsWith('lot:')) {
        const lot = val.replace('lot:', '');
        renderTable(allRecords.filter(r => r.lotName === lot));
    }
}

function renderTable(items) {
    const tbody = document.getElementById('unregBody');
    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-16">
            <span class="text-gray-500 font-semibold">No unregistered vehicles found</span>
        </td></tr>`;
        return;
    }
    tbody.innerHTML = items.map((r, i) => {
        const rowBg = i % 2 === 0 ? 'row-a' : 'row-b';
        const timeOutCell = r.timeOut === 'Still In Lot'
            ? `<span style="display:inline-block;background:#1c0a00;color:#fb923c;border:1px solid #92400e;
                padding:2px 10px;border-radius:9999px;font-size:11px;font-weight:700;">Still In Lot</span>`
            : `<span class="font-mono text-gray-300">${esc(r.timeOut)}</span>`;
        return `
        <tr class="${rowBg} border-b border-blue-950">
            <td class="px-5 py-3 text-blue-900 text-sm">${i + 1}</td>
            <td class="px-5 py-3">${plateBadge(r.licensePlate)}</td>
            <td class="px-5 py-3 text-gray-300 text-sm">${esc(r.lotName)}</td>
            <td class="px-5 py-3 font-mono text-gray-400 text-sm">${esc(r.timeIn)}</td>
            <td class="px-5 py-3 text-sm">${timeOutCell}</td>
        </tr>`;
    }).join('');
}

function plateBadge(plateStr) {
    return `<span style="
        display:inline-block;
        background:#f5f0e0;
        color:#111;
        font-family:'Courier New',Courier,monospace;
        font-weight:900;
        font-size:13px;
        letter-spacing:0.12em;
        padding:4px 12px;
        border-radius:4px;
        border:2px solid #222;
        border-bottom:3px solid #111;
        box-shadow:0 2px 6px rgba(0,0,0,0.6);
        min-width:90px;
        text-align:center;
    ">${esc(plateStr).toUpperCase()}</span>`;
}

function esc(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

refreshPage();
setInterval(refreshPage, 5000);