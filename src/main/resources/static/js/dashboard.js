/**
 * dashboard.js — Valid8 Dashboard page.
 * Fetches /api/summary and renders responsive lot cards every 5 seconds.
 */

async function refreshPage() {
    try {
        const res = await fetch('/api/summary');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const lots = await res.json();
        renderCards(lots);
        setLastUpdated();
        showError(null);
    } catch (e) {
        showError('Failed to load lot data: ' + e.message);
    }
}

function renderCards(lots) {
    const grid = document.getElementById('lotGrid');
    if (!lots || lots.length === 0) {
        grid.innerHTML = '<div class="col-span-full text-center text-slate-500 py-16">No lot data available.</div>';
        return;
    }

    grid.innerHTML = lots.map(lot => {
        const occ = Math.min(100, Math.max(0, lot.occupancyPercent || 0));
        const barColor = colorToBarClass(lot.availabilityColor);
        const badgeColor = colorToBadgeClass(lot.availabilityColor);

        return `
        <div class="bg-slate-800 rounded-xl shadow-xl p-6 flex flex-col gap-4 border border-slate-700 hover:border-yellow-400 transition">
            <!-- Lot name + availability badge -->
            <div class="flex items-start justify-between gap-2">
                <h2 class="text-lg font-bold text-white leading-tight">${escHtml(lot.lotName)}</h2>
                <span class="text-xs font-bold px-2 py-1 rounded-full whitespace-nowrap ${badgeColor}">
                    ${escHtml(lot.availabilityColor.toUpperCase())}
                </span>
            </div>

            <!-- Big numbers row -->
            <div class="grid grid-cols-3 gap-2 text-center">
                <div>
                    <div class="text-2xl font-extrabold text-yellow-400">${lot.inLotNow}</div>
                    <div class="text-xs text-slate-400 mt-0.5">In Lot</div>
                </div>
                <div>
                    <div class="text-2xl font-extrabold text-green-400">${lot.availableNow}</div>
                    <div class="text-xs text-slate-400 mt-0.5">Available</div>
                </div>
                <div>
                    <div class="text-2xl font-extrabold text-white">${lot.capacity}</div>
                    <div class="text-xs text-slate-400 mt-0.5">Capacity</div>
                </div>
            </div>

            <!-- Occupancy bar -->
            <div>
                <div class="flex justify-between text-xs text-slate-400 mb-1">
                    <span>Occupancy</span>
                    <span>${occ.toFixed(1)}%</span>
                </div>
                <div class="w-full bg-slate-700 rounded-full h-3">
                    <div class="h-3 rounded-full transition-all duration-500 ${barColor}"
                         style="width: ${occ}%"></div>
                </div>
            </div>

            <!-- Footer stats -->
            <div class="flex justify-between text-xs text-slate-400 border-t border-slate-700 pt-3">
                <span>Sessions: <strong class="text-white">${lot.totalSessions}</strong></span>
                <span>Violations: <strong class="text-red-400">${lot.violationsCount}</strong></span>
            </div>
        </div>`;
    }).join('');
}

function colorToBarClass(color) {
    if (color === 'green')  return 'bg-green-500';
    if (color === 'yellow') return 'bg-yellow-400';
    return 'bg-red-500';
}

function colorToBadgeClass(color) {
    if (color === 'green')  return 'bg-green-800 text-green-300';
    if (color === 'yellow') return 'bg-yellow-800 text-yellow-300';
    return 'bg-red-800 text-red-300';
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
