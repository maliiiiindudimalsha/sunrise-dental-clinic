// Applies saved theme immediately (before paint) to avoid a flash of the wrong theme.
(function () {
    var saved = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', saved);
})();

// Wires up the toggle switch once the page has loaded.
document.addEventListener('DOMContentLoaded', function () {
    var toggle = document.getElementById('themeSwitch');
    if (!toggle) return;

    var current = localStorage.getItem('theme') || 'light';
    toggle.checked = current === 'dark';

    toggle.addEventListener('change', function () {
        var theme = toggle.checked ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    });
});