(function () {
    const page = location.pathname.split('/').pop();

    if (page !== 'index.html' && page !== '' &&
        !localStorage.getItem('token')) {
        location.href = 'index.html';
    }
})();

function authFetch(url, options = {}) {
    options.headers = options.headers || {};
    options.headers['X-Auth-Token'] =
        localStorage.getItem('token') || '';

    return fetch(url, options).then(res => {
        if (res.status === 401) {
            localStorage.clear();
            location.href = 'index.html';
        }

        return res;
    });
}

async function logout() {
    const token = localStorage.getItem('token');

    try {
        await fetch('http://localhost:8080/logout', {
            method: 'POST',
            headers: {
                'X-Auth-Token': token || ''
            }
        });
    } finally {
        localStorage.clear();
        location.href = 'index.html';
    }
}

function toggleSidebar() {
    document.querySelector('.sidebar')?.classList.toggle('open');
    document.querySelector('.sidebar-overlay')?.classList.toggle('active');
}

function closeSidebar() {
    document.querySelector('.sidebar')?.classList.remove('open');
    document.querySelector('.sidebar-overlay')?.classList.remove('active');
}