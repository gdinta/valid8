/**
 * common.js — Valid8 shared frontend utilities.
 * Included on every page.
 */

/** Update the "Last updated" label */
function setLastUpdated() {
    const ts = 'Last updated: ' + new Date().toLocaleString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
    document.querySelectorAll('#lastUpdated').forEach(el => el.textContent = ts);
}

/** Show or hide the error banner */
function showError(msg) {
    const banner = document.getElementById('errorBanner');
    if (!banner) return;
    if (msg) {
        banner.classList.remove('hidden');
        const inner = banner.querySelector('div');
        if (inner) inner.textContent = '⚠ ' + msg;
    } else {
        banner.classList.add('hidden');
    }
}

/**
 * Call POST /api/reload then refresh the current page's data.
 * Each page defines a global refreshPage() function.
 */
async function reloadData() {
    try {
        const res = await fetch('/api/reload', { method: 'POST' });
        const data = await res.json();
        if (!data.success) {
            showError('Reload failed: ' + (data.error || 'unknown'));
        } else {
            showError(null);
        }
    } catch (e) {
        showError('Reload request failed: ' + e.message);
    }
    if (typeof refreshPage === 'function') refreshPage();
}
