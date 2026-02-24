/**
 * violations.js — Valid8 Violations page.
 * Fetches /api/violations and renders table rows every 5 seconds.
 */

async function refreshPage() {
    try {
        const res = await fetch('/api/violations');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const items = await res.json();
        renderTable(items);
        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load violations: ' + e.message);
    }
}

function renderTable(items) {
    const tbody = document.getElementById('violationsBody');
    const countEl = document.getElementById('violationCount');

    if (countEl) countEl.textContent = items.length + ' violation' + (items.length !== 1 ? 's' : '');

    if (!items || items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-16 text-slate-400">
                    <div class="font-semibold">No violations found</div>
                    <div class="text-sm mt-1 text-slate-500">All active sessions are compliant.</div>
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = items.map((item, idx) => {
        const rowBg = idx % 2 === 0 ? 'bg-slate-800' : 'bg-slate-850';
        const statusBadge = item.status === 'Still In Lot'
            ? 'bg-orange-800 text-orange-200'
            : 'bg-slate-700 text-slate-300';
        const reasonBadge = item.reason === 'Unregistered'
            ? 'bg-red-800 text-red-200'
            : item.reason === 'Overstay'
                ? 'bg-yellow-800 text-yellow-200'
                : 'bg-purple-800 text-purple-200';

        return `
        <tr class="${rowBg} border-b border-slate-700 hover:bg-slate-700 transition">
            <td class="px-6 py-4 font-mono font-bold text-yellow-400">${escHtml(item.licensePlate)}</td>
            <td class="px-6 py-4 text-slate-200">${escHtml(item.lotName)}</td>
            <td class="px-6 py-4 text-slate-300">${item.durationMinutes} min</td>
            <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-semibold ${statusBadge}">
                    ${escHtml(item.status)}
                </span>
            </td>
            <td class="px-6 py-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-semibold ${reasonBadge}">
                    ${escHtml(item.reason)}
                </span>
            </td>
        </tr>`;
    }).join('');
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
