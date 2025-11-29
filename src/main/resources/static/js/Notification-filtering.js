document.addEventListener('DOMContentLoaded', function() {
    // Lấy các elements
    const tabs = document.querySelectorAll('.notification-tab');
    const container = document.getElementById('notification-items-container');
    const placeholder = document.getElementById('no-notifications-placeholder');

    // Hàm xử lý lọc
    function filterNotifications(filterType) {
        // 1. Lấy tất cả thông báo đang có
        const items = container.querySelectorAll('.notification-item');
        let visibleCount = 0;

        items.forEach(item => {
            const isUnread = item.classList.contains('unread');

            // Logic hiển thị dựa trên tab được chọn
            let shouldShow = false;
            if (filterType === 'all') {
                shouldShow = true;
            } else if (filterType === 'unread') {
                shouldShow = isUnread;
            } else if (filterType === 'read') {
                shouldShow = !isUnread;
            }

            // Áp dụng hiển thị/ẩn
            if (shouldShow) {
                item.style.display = ''; // Reset về display mặc định (thường là flex hoặc block)
                item.classList.remove('d-none'); // Bootstrap utility
                visibleCount++;
            } else {
                item.style.display = 'none';
                item.classList.add('d-none');
            }
        });

        // 2. Xử lý hiển thị Placeholder (nếu không có thông báo nào khớp)
        if (placeholder) {
            if (visibleCount === 0) {
                placeholder.style.display = 'block';
                placeholder.classList.remove('d-none');
            } else {
                placeholder.style.display = 'none';
                placeholder.classList.add('d-none');
            }
        }
    }

    // Gán sự kiện click cho các tab
    tabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();

            // 1. Update UI active cho tab
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            // 2. Lấy loại filter (all, unread, read)
            const filterType = this.getAttribute('data-tab');

            // 3. Gọi hàm lọc
            filterNotifications(filterType);
        });
    });
});
