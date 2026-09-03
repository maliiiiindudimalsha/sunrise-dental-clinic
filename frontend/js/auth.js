// Redirects to login immediately if there's no session token.
// Runs on every page except index.html (the login page itself).
(function () {
    const currentPage = window.location.pathname.split('/').pop();
    if (currentPage !== 'index.html' && currentPage !== '') {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = 'index.html';
        }
    }
})();

// Drop-in replacement for fetch() - attaches the session token automatically.
// Use this for every call to a protected backend endpoint
// (appointments, bills, dentists, treatments, reports, notifications).
function authFetch(url, options = {}) {
    const token = localStorage.getItem('token');
    options.headers = options.headers || {};
    options.headers['X-Auth-Token'] = token || '';
    return fetch(url, options);
}

// Clears the session and sends the user back to the login page.
function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}