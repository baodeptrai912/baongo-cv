/**
 * ULTRA PREMIUM TOAST LIBRARY
 * Pure Vanilla JS + Modern CSS (No Libraries)
 * Features: Glassmorphism, Animated Gradients, Advanced Animations
 */

(function injectUltraPremiumToastCSS() {
    if (document.getElementById('toast-premium-styles')) return;

    const style = document.createElement('style');
    style.id = 'toast-premium-styles';
    style.textContent = `
        /* ===== MODERN CSS 2025 - PURE CSS ===== */
        
        @property --angle {
            syntax: '<angle>';
            inherits: false;
            initial-value: 0deg;
        }
        
        @property --gradient-x {
            syntax: '<percentage>';
            inherits: false;
            initial-value: 0%;
        }

        #_pt_container {
            position: fixed;
            top: 24px;
            right: 24px;
            z-index: 9999999;
            display: flex;
            flex-direction: column;
            gap: 16px;
            pointer-events: none;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
        }

        ._pt_item {
            display: flex;
            align-items: center;
            width: 420px;
            max-width: 92vw;
            position: relative;
            padding: 20px 0;
            border-radius: 20px;
            overflow: hidden;
            pointer-events: auto;
            opacity: 0;
            transform: translateX(450px) scale(0.88) rotateY(20deg);
            transition: all 0.65s cubic-bezier(0.34, 1.56, 0.64, 1);
            box-shadow: 
                0 8px 32px rgba(31, 38, 135, 0.15),
                0 2px 8px rgba(0, 0, 0, 0.05),
                inset 0 1px 2px rgba(255, 255, 255, 0.4);
        }

        /* Glassmorphism Background */
        ._pt_item::before {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(
                135deg,
                rgba(255, 255, 255, 0.9) 0%,
                rgba(255, 255, 255, 0.7) 100%
            );
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1.5px solid rgba(255, 255, 255, 0.3);
            border-radius: 20px;
            z-index: 0;
        }

        /* Animated Border Gradient */
        ._pt_item::after {
            content: '';
            position: absolute;
            inset: -2px;
            background: conic-gradient(
                from var(--angle),
                transparent 0%,
                var(--border-color, #3b82f6) 20%,
                transparent 40%,
                transparent 60%,
                var(--border-color, #3b82f6) 80%,
                transparent 100%
            );
            border-radius: 20px;
            z-index: -1;
            animation: _pt_border_spin 6s linear infinite;
            opacity: 0.6;
            filter: blur(8px);
        }

        @keyframes _pt_border_spin {
            to { --angle: 360deg; }
        }

        ._pt_item.show {
            opacity: 1;
            transform: translateX(0) scale(1) rotateY(0);
        }

        ._pt_item:hover {
            transform: translateY(-4px) scale(1.03);
            box-shadow: 
                0 16px 48px rgba(31, 38, 135, 0.25),
                0 4px 16px rgba(0, 0, 0, 0.08),
                inset 0 1px 2px rgba(255, 255, 255, 0.5);
        }

        /* Icon Container with Glow */
        ._pt_icon {
            width: 70px;
            min-width: 70px;
            height: 70px;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            z-index: 2;
            margin-left: 8px;
        }

        ._pt_icon::before {
            content: '';
            position: absolute;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: radial-gradient(
                circle,
                var(--icon-color, #3b82f6) 0%,
                transparent 70%
            );
            opacity: 0.4;
            filter: blur(12px);
            animation: _pt_glow_pulse 2.5s ease-in-out infinite;
        }

        @keyframes _pt_glow_pulse {
            0%, 100% { 
                transform: scale(1);
                opacity: 0.4;
            }
            50% { 
                transform: scale(1.2);
                opacity: 0.6;
            }
        }

        ._pt_icon svg {
            width: 34px;
            height: 34px;
            position: relative;
            z-index: 3;
            fill: var(--icon-color, #3b82f6);
            filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
            animation: _pt_icon_entrance 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        }

        @keyframes _pt_icon_entrance {
            0% {
                opacity: 0;
                transform: scale(0.3) rotate(-180deg);
            }
            60% {
                transform: scale(1.15) rotate(15deg);
            }
            100% {
                opacity: 1;
                transform: scale(1) rotate(0);
            }
        }

        /* Content Body */
        ._pt_body {
            flex: 1;
            padding: 4px 20px 4px 6px;
            position: relative;
            z-index: 2;
        }

        ._pt_title {
            font-size: 17px;
            font-weight: 800;
            color: #1e293b;
            margin: 0 0 6px 0;
            letter-spacing: -0.02em;
            line-height: 1.3;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.03);
        }

        ._pt_msg {
            font-size: 14.5px;
            color: #475569;
            margin: 0;
            line-height: 1.5;
            letter-spacing: -0.01em;
        }

        /* Close Button */
        ._pt_close {
            width: 40px;
            min-width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            border: none;
            background: rgba(148, 163, 184, 0.15);
            border-radius: 12px;
            color: #64748b;
            cursor: pointer;
            position: relative;
            z-index: 2;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        ._pt_close:hover {
            background: rgba(239, 68, 68, 0.15);
            color: #ef4444;
            transform: rotate(90deg) scale(1.1);
        }

        ._pt_close svg {
            width: 20px;
            height: 20px;
        }

        /* Animated Progress Bar */
        ._pt_progress {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(
                90deg,
                rgba(203, 213, 225, 0.3) 0%,
                rgba(226, 232, 240, 0.5) 100%
            );
            overflow: hidden;
            z-index: 3;
        }

        ._pt_progress_bar {
            height: 100%;
            width: 100%;
            position: relative;
            transform-origin: left center;
            background: linear-gradient(
                90deg,
                var(--progress-color, #3b82f6) 0%,
                var(--progress-color-light, #60a5fa) 50%,
                var(--progress-color, #3b82f6) 100%
            );
            background-size: 200% 100%;
            animation: 
                _pt_progress_shrink linear forwards,
                _pt_progress_shimmer 2s ease-in-out infinite;
            box-shadow: 
                0 0 20px var(--progress-color, #3b82f6),
                0 0 40px rgba(59, 130, 246, 0.3);
        }

        @keyframes _pt_progress_shrink {
            from { width: 100%; }
            to { width: 0%; }
        }

        @keyframes _pt_progress_shimmer {
            0% { background-position: 200% 0; }
            100% { background-position: -200% 0; }
        }

        /* Theme Colors */
        ._pt_item._pt_success {
            --border-color: #10b981;
            --icon-color: #10b981;
            --progress-color: #10b981;
            --progress-color-light: #34d399;
        }

        ._pt_item._pt_error {
            --border-color: #ef4444;
            --icon-color: #ef4444;
            --progress-color: #ef4444;
            --progress-color-light: #f87171;
        }

        ._pt_item._pt_warning {
            --border-color: #f59e0b;
            --icon-color: #f59e0b;
            --progress-color: #f59e0b;
            --progress-color-light: #fbbf24;
        }

        ._pt_item._pt_info {
            --border-color: #3b82f6;
            --icon-color: #3b82f6;
            --progress-color: #3b82f6;
            --progress-color-light: #60a5fa;
        }

        /* Mobile Responsive */
        @media (max-width: 640px) {
            #_pt_container {
                top: 16px;
                right: 16px;
                left: 16px;
            }
            ._pt_item {
                width: 100%;
                padding: 16px 0;
            }
            ._pt_icon {
                width: 56px;
                min-width: 56px;
                height: 56px;
            }
            ._pt_icon svg {
                width: 28px;
                height: 28px;
            }
        }

        /* Reduced Motion */
        @media (prefers-reduced-motion: reduce) {
            ._pt_item,
            ._pt_item::after,
            ._pt_icon::before,
            ._pt_icon svg,
            ._pt_progress_bar {
                animation: none !important;
                transition: opacity 0.3s ease;
            }
        }
    `;
    document.head.appendChild(style);
})();

// ICONS (Pure SVG - No Libraries)
const ICONS = {
    success: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>`,
    info: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>`,
    warning: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>`,
    error: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/></svg>`
};

// MAIN TOAST FUNCTION (Pure Vanilla JS)
function showToast(message, type = 'info', duration = 3500) {
    let container = document.getElementById('_pt_container');

    if (!container) {
        container = document.createElement('div');
        container.id = '_pt_container';
        document.body.appendChild(container);
    }

    const toastEl = document.createElement('div');
    toastEl.className = `_pt_item _pt_${type}`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'polite');

    const titles = {
        success: 'Success',
        error: 'Fail',
        warning: 'Warning',
        info: 'Info'
    };

    toastEl.innerHTML = `
        <div class="_pt_icon" aria-hidden="true">
            ${ICONS[type] || ICONS.info}
        </div>
        <div class="_pt_body">
            <h3 class="_pt_title">${titles[type] || 'Thông báo'}</h3>
            <p class="_pt_msg">${message}</p>
        </div>
        <button class="_pt_close" type="button" aria-label="Đóng thông báo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
        </button>
        <div class="_pt_progress" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100">
            <div class="_pt_progress_bar" style="animation-duration: ${(duration / 1000).toFixed(2)}s"></div>
        </div>
    `;

    // Close button handler
    toastEl.querySelector('._pt_close').addEventListener('click', (e) => {
        e.stopPropagation();
        removeToast();
    });

    // Auto remove
    const autoRemoveId = setTimeout(removeToast, duration);

    function removeToast() {
        clearTimeout(autoRemoveId);
        toastEl.style.opacity = '0';
        toastEl.style.transform = 'translateX(450px) scale(0.88) rotateY(-20deg)';
        setTimeout(() => {
            if (toastEl.parentNode) {
                toastEl.parentNode.removeChild(toastEl);
            }
        }, 500);
    }

    container.appendChild(toastEl);

    // Trigger animation
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            toastEl.classList.add('show');
        });
    });
}

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { showToast };
}
