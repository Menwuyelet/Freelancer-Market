// Show toast alerts from URL params

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);

    // Message keys
    const messages = {
        'logout_success': { text: 'Logout Successful!', type: 'success', icon: '' },
        'signup_success': { text: 'Registration Successful! Please Login.', type: 'success', icon: '' },
        'welcome': { text: 'Sign In Successful!', type: 'success', icon: '' },
        'job_posted': { text: 'Job Posted Successfully!', type: 'success', icon: '' },
    };

    // Check for msg param
    if (params.has('msg')) {
        const msgKey = params.get('msg');
        if (messages[msgKey]) {
            showToast(messages[msgKey].text, messages[msgKey].type, messages[msgKey].icon);
        }
    }

    // CHECK FOR 'registered' PARAM (Legacy support)
    if (params.has('registered')) {
        showToast('Registration Successful! Please Login.', 'success', '✅');
    }

    // CHECK FOR 'error' PARAM
    if (params.has('error')) {
        showToast('Operation Failed. Please check your credentials.', 'error', '');
    }

    // Clear URL params
    if (params.has('msg') || params.has('error') || params.has('registered')) {
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
});

function showToast(message, type = 'success', icon = '') {
    // Create Container if not exists
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    // Create Toast Element
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span>${message}</span>
    `;

    // Append to container
    container.appendChild(toast);

    // Remove after animation (4.5s matches CSS)
    setTimeout(() => {
        toast.remove();
        if (container.children.length === 0) {
            container.remove();
        }
    }, 5000);
}
