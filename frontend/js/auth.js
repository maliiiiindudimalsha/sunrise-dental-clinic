(function () {
    const page = location.pathname.split('/').pop();
    if (page !== 'index.html' && page !== '' && !localStorage.getItem('token'))
        location.href = 'index.html';
})();

function authFetch(url, options = {}) {
    options.headers = options.headers || {};
    options.headers['X-Auth-Token'] = localStorage.getItem('token') || '';

    return fetch(url, options).then(res => {
        if (res.status === 401) {
            localStorage.clear();
            location.href = 'index.html';
        }
        return res;
    });
}

async function logout() {
    try {
        await authFetch('http://localhost:8080/logout', { method: 'POST' });
    } finally {
        localStorage.clear();
        location.href = 'index.html';
    }
}