document.addEventListener('DOMContentLoaded', () => {
    debugAuthenticationState();

    // ✅ Chỉ connect WebSocket nếu user đã authenticated
    if (isUserAuthenticated()) {
        connectStompClient();
        fetchInitialNotifications();
    } else {

        hideNotificationElements();
    }

    updateUserGreeting();

    const userDropdownToggle = document.getElementById('userDropdown');
    const companyDropdownToggle = document.getElementById('companyDropdown');

    if (userDropdownToggle) {
        userDropdownToggle.addEventListener('show.bs.dropdown', updateUserGreeting);
    }

    if (companyDropdownToggle) {
        companyDropdownToggle.addEventListener('show.bs.dropdown', updateUserGreeting);
    }
});

var stompClient = null;
const DEFAULT_AVATAR_URL = '/img/avatars/default-avatar.png';

// ✅ Function to check if user is authenticated
function isUserAuthenticated() {
    // Method 1: Check for user dropdown (indicates logged in user)
    const userDropdown = document.getElementById('userDropdown');
    const companyDropdown = document.getElementById('companyDropdown');

    if (userDropdown || companyDropdown) {
        return true;
    }

    // Method 2: Check for authentication meta tag (if you add one)
    const authMeta = document.querySelector('meta[name="user-authenticated"]');
    if (authMeta && authMeta.getAttribute('content') === 'true') {
        return true;
    }

    // Method 3: Check body class (if you add one)
    if (document.body.classList.contains('authenticated')) {
        return true;
    }

    // Method 4: Check for specific authenticated user elements
    const userSpan = document.querySelector('a#userDropdown > span');
    const companySpan = document.querySelector('a#companyDropdown > span');

    if ((userSpan && userSpan.textContent.trim() && userSpan.textContent.trim().toLowerCase() !== "user") ||
        (companySpan && companySpan.textContent.trim() && companySpan.textContent.trim().toLowerCase() !== "company")) {
        return true;
    }

    return false;
}

// ✅ Hide notification elements for non-authenticated users
function hideNotificationElements() {
    const notificationBadge = document.querySelector('.notification-badge');
    const notificationDropdown = document.querySelector('.notification-dropdown');

    if (notificationBadge) {
        notificationBadge.style.display = 'none';
    }

    if (notificationDropdown) {
        notificationDropdown.style.display = 'none';
    }
}

function debugAuthenticationState() {
    const userDropdown = document.getElementById('userDropdown');
    const companyDropdown = document.getElementById('companyDropdown');
    const isAuth = isUserAuthenticated();


}

function updateUserGreeting() {
    const greetingElements = document.querySelectorAll('.user-greeting');

    if (greetingElements.length === 0) {
        return;
    }

    const hour = new Date().getHours();
    let sessionGreeting = "Chào bạn";
    let greetingIconClass = "fas fa-hand-sparkles";

    if (hour >= 5 && hour < 12) {
        sessionGreeting = "Chào buổi sáng";
        greetingIconClass = "fas fa-sun";
    } else if (hour >= 12 && hour < 18) {
        sessionGreeting = "Chào buổi chiều";
        greetingIconClass = "fas fa-cloud-sun";
    } else {
        sessionGreeting = "Chào buổi tối";
        greetingIconClass = "fas fa-moon";
    }

    let username = null;
    const userSpan = document.querySelector('a#userDropdown > span');
    const companySpan = document.querySelector('a#companyDropdown > span');

    if (userSpan && userSpan.textContent.trim() && userSpan.textContent.trim().toLowerCase() !== "user") {
        username = userSpan.textContent.trim();
    } else if (companySpan && companySpan.textContent.trim() && companySpan.textContent.trim().toLowerCase() !== "company") {
        username = companySpan.textContent.trim();
    }

    greetingElements.forEach((el, index) => {
        const iconHTML = `<i class="${greetingIconClass} me-2 greeting-icon-dynamic"></i>`;
        if (username) {
            el.innerHTML = `${iconHTML}<strong>${sessionGreeting}, ${username}!</strong>`;
        } else {
            el.innerHTML = `${iconHTML}<strong>${sessionGreeting}!</strong>`;
        }
    });
}

function createNotificationElement(notification) {
    const link = document.createElement('a');

    // ✅ SỬA: Tuyệt đối không dùng '#'
    // Nếu không có link, dùng 'javascript:void(0)' để trình duyệt không làm gì cả
    if (notification.href && notification.href.trim() !== '' && notification.href !== '#') {
        link.href = notification.href;
    } else {
        link.href = 'javascript:void(0);';
    }

    // Các class giữ nguyên
    link.className = 'dropdown-item notification-link p-0 notification-item-wrapper';

    if (notification.id) {
        link.onclick = (event) => {
            // Nếu là link rỗng, chặn hành vi mặc định để không bị nhảy trang
            if (link.getAttribute('href') === 'javascript:void(0);') {
                event.preventDefault();
            }
            markNotificationAsRead(notification.id, notification.href);
        };
    }

    const notificationItemDiv = document.createElement('div');
    notificationItemDiv.className = 'notification-item' + (notification.read ? '' : ' unread');

    // --- Avatar ---
    const avatarDiv = document.createElement('div');
    avatarDiv.className = 'notification-avatar';
    const avatarImg = document.createElement('img');
    avatarImg.src = notification.avatar || DEFAULT_AVATAR_URL;
    avatarImg.alt = 'Avatar';
    avatarImg.onerror = function() { this.src = DEFAULT_AVATAR_URL; };
    avatarDiv.appendChild(avatarImg);

    // --- Content ---
    const contentDiv = document.createElement('div');
    contentDiv.className = 'notification-content';

    const messageP = document.createElement('p');
    messageP.className = 'message mb-1';
    // Render nội dung an toàn
    messageP.innerHTML = notification.title || notification.message || 'Nội dung thông báo không có.';

    const timeP = document.createElement('p');
    timeP.className = 'time text-muted mb-0';
    const timeSmall = document.createElement('small');
    const clockIcon = document.createElement('i');
    clockIcon.className = 'fas fa-clock me-1';
    timeSmall.appendChild(clockIcon);
    timeSmall.appendChild(document.createTextNode(notification.createdAt ? formatTimeAgo(notification.createdAt) : 'Vừa xong'));
    timeP.appendChild(timeSmall);

    contentDiv.appendChild(messageP);
    contentDiv.appendChild(timeP);

    // --- Assemble ---
    notificationItemDiv.appendChild(avatarDiv);
    notificationItemDiv.appendChild(contentDiv);
    link.appendChild(notificationItemDiv);

    return link;
}
function formatTimeAgo(dateString) {
    if (!dateString) return 'Không rõ';
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.round((now - date) / 1000);
    const minutes = Math.round(seconds / 60);
    const hours = Math.round(minutes / 60);
    const days = Math.round(hours / 24);

    if (seconds < 5) return `vài giây trước`;
    else if (seconds < 60) return `${seconds} giây trước`;
    else if (minutes < 60) return `${minutes} phút trước`;
    else if (hours < 24) return `${hours} giờ trước`;
    else if (days === 1) return `hôm qua`;
    else if (days < 7) return `${days} ngày trước`;
    else return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function updateNotificationBadgeCount(count) {
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        const actualCount = parseInt(count) || 0;
        badge.textContent = actualCount;
        badge.style.display = actualCount > 0 ? '' : 'none';
    }
}

function toggleNoNotificationsPlaceholder(notificationCount) {
    const placeholder = document.getElementById('no-notifications-placeholder');
    const container = document.getElementById('notification-items-container');

    if (placeholder && container) {
        let actualItemCount = 0;
        const children = container.children;
        for (let i = 0; i < children.length; i++) {
            // ✅ SỬA: Không check tagName === 'LI' nữa
            // Chỉ cần check id không phải là placeholder
            if (children[i].id !== 'no-notifications-placeholder') {
                actualItemCount++;
            }
        }

        // Logic hiển thị: Nếu có item thật thì ẩn placeholder
        if (actualItemCount > 0) {
            placeholder.style.display = 'none';
            // Đảm bảo placeholder nằm cuối cùng hoặc bị ẩn hoàn toàn
        } else {
            placeholder.style.display = 'block'; // Hoặc 'flex' tùy CSS của bạn
        }
    }
}

function fetchInitialNotifications() {
    if (!isUserAuthenticated()) {
        return;
    }

    fetch('/notification/get-notification', {
        credentials: 'include',
        headers: {
            'Accept': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            const notifications = data.notifications || [];
            // Cập nhật số lượng chưa đọc
            const unreadCount = notifications.filter(n => !n.read).length;
            updateNotificationBadgeCount(unreadCount);

            // Cập nhật text "You have X unread" trong header mới (nếu có element đó)
            const unreadTextSpan = document.getElementById('unread-count');
            if (unreadTextSpan) unreadTextSpan.textContent = unreadCount;

            const container = document.getElementById('notification-items-container');
            if (!container) return;

            // ✅ Xóa các notification cũ (giữ lại placeholder)
            const children = Array.from(container.children);
            children.forEach(child => {
                if (child.id !== 'no-notifications-placeholder') {
                    child.remove();
                }
            });

            if (notifications.length > 0) {
                // Sắp xếp mới nhất lên đầu
                notifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

                notifications.forEach(notification => {
                    const notificationElement = createNotificationElement(notification);
                    // Chèn vào trước placeholder (để placeholder luôn ở cuối nếu cần, hoặc placeholder sẽ bị ẩn đi)
                    const placeholder = document.getElementById('no-notifications-placeholder');
                    container.insertBefore(notificationElement, placeholder);
                });
            }

            toggleNoNotificationsPlaceholder(notifications.length);
        })
        .catch(error => {
            console.log('Notification fetch error:', error);
        });
}

function connectStompClient() {
    // ✅ Double-check authentication before connecting WebSocket
    if (!isUserAuthenticated()) {

        return;
    }

    try {

        const socket = new SockJS("https://baongocv.info/ws");
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, (frame) => {
            console.log('WebSocket connected for authenticated user');
            const userDestination = '/user/queue/notifications';

            const subscription = stompClient.subscribe(userDestination, (messageOutput) => {
                try {
                    const message = JSON.parse(messageOutput.body);
                    displayRealtimeNotification(message);
                } catch (e) {
                    console.log('Error parsing WebSocket message:', e);
                }
            });

        }, (error) => {
            console.log('WebSocket connection failed:', error);
        });
    } catch (error) {
        console.log('WebSocket initialization failed:', error);
    }
}

function displayRealtimeNotification(notificationData) {
    console.log("Received WebSocket notification data:", notificationData);

    // ... (Giữ nguyên đoạn xử lý PLAN_UPGRADED) ...
    if (notificationData.type === 'PLAN_UPGRADED') {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modalEl => {
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if (modalInstance) modalInstance.hide();
        });
        if (typeof PaymentSuccessPopup !== 'undefined') {
            PaymentSuccessPopup.show(notificationData.message);
        }
    }

    const container = document.getElementById('notification-items-container');
    if (!container) return;

    const notificationElement = createNotificationElement(notificationData);

    // Ẩn placeholder ngay lập tức
    const placeholder = document.getElementById('no-notifications-placeholder');
    if (placeholder) placeholder.style.display = 'none';

    // ✅ Chèn vào đầu danh sách (ngay sau header nếu có, hoặc đầu container)
    // Container hiện tại chứa các thẻ <a> và 1 thẻ div placeholder
    if (container.firstChild) {
        container.insertBefore(notificationElement, container.firstChild);
    } else {
        container.appendChild(notificationElement);
    }

    // Cập nhật badge
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        // Lấy số hiện tại, cộng thêm 1
        let currentCount = parseInt(badge.textContent || '0');
        if (!notificationData.read) {
            currentCount++;
        }
        updateNotificationBadgeCount(currentCount);

        // Cập nhật cả text trong header dropdown
        const unreadTextSpan = document.getElementById('unread-count');
        if (unreadTextSpan) unreadTextSpan.textContent = currentCount;
    }

    // Play sound notification if you want (Optional)
    // new Audio('/path/to/sound.mp3').play().catch(() => {});
}

function markNotificationAsRead(notificationId, href) {
    fetch(`/notification/mark-as-read/${notificationId}`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
        .then(response => {
            if (!response.ok) return response.text();
            return response.text();
        })
        .finally(() => {
            // ✅ Logic điều hướng an toàn hơn
            if (href && href.trim() !== '' && href !== '#' && href !== 'javascript:void(0);') {
                window.location.href = href;
            }
        });
}

// Global error handlers
window.addEventListener('error', (event) => {
    // Error handling without logging
});

window.addEventListener('unhandledrejection', (event) => {
    // Promise rejection handling without logging
});

document.addEventListener('visibilitychange', () => {
    // Visibility change handling without logging
});
