/**
 * unregistered.js — Valid8 Unregistered Plates page.
 * Fetches /api/unregistered and renders table rows every 5 seconds.
 */

async function refreshPage() {
    try {
        const res = await fetch('/api/unregistered');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const items = await res.json();
        renderTable(items);
        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load unregistered data: ' + e.message);
    }
}

function renderTable(items) {
    const tbody = document.getElementById('unregisteredBody');
    const countEl = document.getElementById('unregCount');

    if (countEl) countEl.textContent = items.length + ' unregistered';

    if (!items || items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="text-center py-16 text-slate-400">
                    <div class="font-semibold">No unregistered plates found</div>
                    <div class="text-sm mt-1 text-slate-500">All vehicles in lots have a registration on file.</div>
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = items.map((item, idx) => {
        const rowBg = idx % 2 === 0 ? 'bg-slate-800' : 'bg-slate-850';
        return `
        <tr class="${rowBg} border-b border-slate-700 hover:bg-slate-700 transition">
            <td class="px-6 py-4 text-slate-400 text-sm">${idx + 1}</td>
            <td class="px-6 py-4 font-mono font-bold text-yellow-400">${escHtml(item.licensePlate)}</td>
            <td class="px-6 py-4 text-slate-200">${escHtml(item.lotName)}</td>
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
