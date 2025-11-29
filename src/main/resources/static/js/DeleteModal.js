(() => {
    // ---- 1. ĐỊNH NGHĨA CSS CHO MODAL ----
    const deleteModalStyles = `
        .delete-modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.75);
            backdrop-filter: blur(10px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1070;
            opacity: 0;
            visibility: hidden;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .delete-modal-overlay.show {
            opacity: 1;
            visibility: visible;
        }

        .delete-modal-box {
            background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
            border-radius: 24px;
            box-shadow: 0 25px 80px rgba(0, 0, 0, 0.4);
            width: 90%;
            max-width: 480px;
            overflow: hidden;
            transform: scale(0.7) translateY(50px);
            opacity: 0;
            transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
            position: relative;
        }

        .delete-modal-overlay.show .delete-modal-box {
            transform: scale(1) translateY(0);
            opacity: 1;
        }

        .delete-modal-box::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: linear-gradient(45deg, transparent, rgba(220, 53, 69, 0.05), transparent);
            transform: rotate(45deg);
            animation: shimmer 3s infinite;
        }

        @keyframes shimmer {
            0%, 100% { transform: translateX(-100%) rotate(45deg); }
            50% { transform: translateX(100%) rotate(45deg); }
        }

        .delete-modal-header {
            background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
            color: white;
            padding: 2rem 2rem 1.5rem;
            text-align: center;
            position: relative;
            z-index: 1;
        }

        .warning-icon-container {
            width: 90px;
            height: 90px;
            margin: 0 auto 1rem;
            position: relative;
        }

        .icon-circle {
            position: absolute;
            width: 100%;
            height: 100%;
            border-radius: 50%;
            border: 3px solid rgba(255, 255, 255, 0.3);
            animation: pulse 2s infinite;
        }

        .icon-circle:nth-child(2) {
            animation-delay: 0.3s;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.2); opacity: 0.3; }
        }

        .warning-icon {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 90px;
            height: 90px;
            background: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            animation: iconBounce 0.6s ease-in-out 0.2s;
        }

        @keyframes iconBounce {
            0%, 100% { transform: translate(-50%, -50%) scale(1); }
            50% { transform: translate(-50%, -50%) scale(1.15); }
        }

        .warning-icon i {
            font-size: 45px;
            color: #ff6b6b;
            animation: shake 0.5s ease-in-out 0.4s;
        }

        @keyframes shake {
            0%, 100% { transform: rotate(0deg); }
            25% { transform: rotate(-10deg); }
            75% { transform: rotate(10deg); }
        }

        .delete-modal-title {
            font-size: 1.75rem;
            font-weight: 800;
            margin: 0;
            text-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .delete-modal-body {
            padding: 2.5rem 2rem;
            font-size: 1rem;
            color: #495057;
            line-height: 1.8;
            text-align: center;
            position: relative;
            z-index: 1;
        }

        .delete-modal-body p {
            margin-bottom: 1rem;
        }

        .delete-modal-body strong {
            color: #343a40;
            font-weight: 700;
        }

        .item-name-box {
            background: linear-gradient(135deg, #fff5f5, #ffe3e3);
            padding: 15px 20px;
            border-radius: 12px;
            margin: 1.5rem 0;
            border-left: 4px solid #ff6b6b;
        }

        #item-to-delete-name {
            font-weight: 700;
            color: #ff6b6b;
            font-size: 1.1rem;
            word-break: break-all;
        }

        .warning-text {
            color: #ff6b6b !important;
            font-weight: 700 !important;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-size: 0.95rem;
        }

        .delete-modal-footer {
            background-color: #f8f9fa;
            padding: 1.5rem 2rem;
            display: flex;
            justify-content: center;
            gap: 1rem;
            border-top: 1px solid #e9ecef;
        }

        .delete-modal-btn {
            border: none;
            padding: 14px 32px;
            border-radius: 50px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s ease;
            font-size: 1rem;
            position: relative;
            overflow: hidden;
        }

        .delete-modal-btn::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
        }

        .delete-modal-btn:hover::before {
            width: 300px;
            height: 300px;
        }

        .btn-cancel {
            background: linear-gradient(135deg, #868e96, #6c757d);
            color: white;
            box-shadow: 0 4px 15px rgba(108, 117, 125, 0.3);
        }

        .btn-cancel:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(108, 117, 125, 0.4);
        }

        .btn-confirm-delete {
            background: linear-gradient(135deg, #ff6b6b, #ee5a6f);
            color: white;
            box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
        }

        .btn-confirm-delete:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(255, 107, 107, 0.6);
        }

        .btn-content {
            position: relative;
            z-index: 1;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .delete-modal-close-btn {
            position: absolute;
            top: 15px;
            right: 15px;
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            font-size: 1.5rem;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10;
        }

        .delete-modal-close-btn:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: rotate(90deg);
        }
    `;

    // ---- 2. TIÊM CSS VÀO TRANG ----
    const styleSheet = document.createElement("style");
    styleSheet.innerText = deleteModalStyles;
    document.head.appendChild(styleSheet);

    let onConfirmCallback = null;

    // ---- 3. HÀM ĐỂ HIỂN THỊ MODAL ----
    function showDeleteConfirmation(itemName, onConfirm) {
        const existingOverlay = document.querySelector('.delete-modal-overlay');
        if (existingOverlay) {
            existingOverlay.remove();
        }

        onConfirmCallback = onConfirm;

        const overlay = document.createElement('div');
        overlay.className = 'delete-modal-overlay';
        overlay.innerHTML = `
            <div class="delete-modal-box">
                <button class="delete-modal-close-btn">&times;</button>
                <div class="delete-modal-header">
                    <div class="warning-icon-container">
                        <div class="icon-circle"></div>
                        <div class="icon-circle"></div>
                        <div class="warning-icon">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                    </div>
                    <h5 class="delete-modal-title">Confirm Deletion</h5>
                </div>
                <div class="delete-modal-body">
                    <p><strong>Are you sure you want to delete this item?</strong></p>
                    <div class="item-name-box">
                        <span id="item-to-delete-name">${itemName}</span>
                    </div>
                    <p class="warning-text">
                        <i class="fas fa-shield-alt"></i>
                        This action cannot be undone
                    </p>
                </div>
                <div class="delete-modal-footer">
                    <button class="delete-modal-btn btn-cancel">
                        <span class="btn-content">
                            <i class="fas fa-times"></i>
                            Cancel
                        </span>
                    </button>
                    <button class="delete-modal-btn btn-confirm-delete">
                        <span class="btn-content">
                            <i class="fas fa-trash-alt"></i>
                            Delete
                        </span>
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        requestAnimationFrame(() => {
            overlay.classList.add('show');
        });

        const hide = () => {
            overlay.classList.remove('show');
            overlay.addEventListener('transitionend', () => {
                if (overlay.parentNode) {
                    overlay.remove();
                }
                onConfirmCallback = null;
            }, { once: true });
        };

        overlay.querySelector('.btn-confirm-delete').addEventListener('click', () => {
            if (typeof onConfirmCallback === 'function') {
                onConfirmCallback();
            }
            hide();
        });

        overlay.querySelector('.btn-cancel').addEventListener('click', hide);
        overlay.querySelector('.delete-modal-close-btn').addEventListener('click', hide);

        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                hide();
            }
        });
    }

    // ---- 4. ĐƯA HÀM RA `window` ĐỂ SỬ DỤNG TOÀN CỤC ----
    window.DeleteModal = {
        show: showDeleteConfirmation
    };

})();
