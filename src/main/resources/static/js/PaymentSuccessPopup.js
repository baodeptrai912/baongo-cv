(() => {
    const paymentPopupStyles = `
        /* Overlay với backdrop blur */
        .payment-success-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);
            backdrop-filter: blur(8px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1060;
            opacity: 0;
            visibility: hidden;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .payment-success-overlay.show {
            opacity: 1;
            visibility: visible;
        }

        /* Box chính với gradient */
        .payment-success-box {
            background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
            padding: 45px 40px;
            border-radius: 24px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            width: 90%;
            max-width: 460px;
            text-align: center;
            position: relative;
            overflow: hidden;
            transform: scale(0.7) translateY(50px);
            opacity: 0;
            transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
        }

        .payment-success-overlay.show .payment-success-box {
            transform: scale(1) translateY(0);
            opacity: 1;
        }

        /* Hiệu ứng shimmer phía sau */
        .payment-success-box::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: linear-gradient(45deg, transparent, rgba(102, 126, 234, 0.1), transparent);
            transform: rotate(45deg);
            animation: shimmer 3s infinite;
        }

        @keyframes shimmer {
            0%, 100% { transform: translateX(-100%) rotate(45deg); }
            50% { transform: translateX(100%) rotate(45deg); }
        }

        /* Icon với gradient và animation */
        .payment-success-icon {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            margin: 0 auto 25px auto;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #4CAF50, #8BC34A);
            box-shadow: 0 10px 30px rgba(76, 175, 80, 0.4);
            position: relative;
            z-index: 1;
            animation: iconBounce 0.8s ease-in-out 0.2s;
        }

        @keyframes iconBounce {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
        }

        /* Vòng tròn pulse */
     .icon-circle {
    position: absolute;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: 3px solid rgba(76, 175, 80, 0.3);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    animation: pulse 2s infinite;
    pointer-events: none;
}

        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: 0.8; }
            50% { transform: scale(1.2); opacity: 0.3; }
        }

        /* Checkmark animation */
        .payment-success-icon svg {
            width: 50px;
            height: 50px;
            stroke: white;
            stroke-width: 3;
            stroke-linecap: round;
            fill: none;
        }

        .payment-success-icon svg path {
            stroke-dasharray: 50;
            stroke-dashoffset: 50;
            animation: drawCheck 0.6s ease-in-out 0.4s forwards;
        }

        @keyframes drawCheck {
            to { stroke-dashoffset: 0; }
        }

        /* Typography với gradient */
        .payment-success-title {
            font-size: 32px;
            font-weight: 800;
            background: linear-gradient(135deg, #333, #667eea);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 15px;
            position: relative;
            z-index: 1;
        }

        .payment-success-text {
            font-size: 17px;
            color: #666;
            margin-bottom: 35px;
            line-height: 1.7;
            position: relative;
            z-index: 1;
        }

        .payment-success-tier {
            display: inline-block;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            padding: 6px 16px;
            border-radius: 20px;
            font-weight: 700;
            font-size: 14px;
            margin: 0 4px;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        /* Button với ripple effect */
        .payment-success-button {
            background: linear-gradient(135deg, #4CAF50, #45a049);
            color: white;
            border: none;
            padding: 16px 40px;
            border-radius: 50px;
            font-size: 17px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s ease;
            width: 100%;
            box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
            position: relative;
            z-index: 1;
            overflow: hidden;
        }

        .payment-success-button::before {
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

        .payment-success-button:hover::before {
            width: 300px;
            height: 300px;
        }

        .payment-success-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 12px 32px rgba(76, 175, 80, 0.5);
        }

        .payment-success-button:active {
            transform: translateY(0);
        }

        .payment-success-button-content {
            position: relative;
            z-index: 1;
        }

        /* Confetti */
        .confetti {
            position: absolute;
            width: 10px;
            height: 10px;
            background: #667eea;
            opacity: 0;
            z-index: 0;
        }

        @keyframes confettiFall {
            to {
                transform: translateY(500px) rotate(360deg);
                opacity: 0;
            }
        }
    `;

    const styleSheet = document.createElement("style");
    styleSheet.innerText = paymentPopupStyles;
    document.head.appendChild(styleSheet);

    // Hàm tạo confetti
    function createConfetti(container) {
        const colors = ['#667eea', '#764ba2', '#4CAF50', '#FFC107', '#FF5722'];
        for (let i = 0; i < 30; i++) {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.left = Math.random() * 100 + '%';
            confetti.style.background = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.animation = `confettiFall ${2 + Math.random() * 2}s linear forwards`;
            confetti.style.animationDelay = Math.random() * 0.5 + 's';
            confetti.style.opacity = '1';
            container.appendChild(confetti);

            setTimeout(() => confetti.remove(), 4000);
        }
    }

    function showPaymentSuccessPopup(message, tier = 'PREMIUM') {
        const existingPopup = document.querySelector('.payment-success-overlay');
        if (existingPopup) {
            existingPopup.remove();
        }

        const overlay = document.createElement('div');
        overlay.className = 'payment-success-overlay';

        const box = document.createElement('div');
        box.className = 'payment-success-box';

        // Vòng tròn pulse
        const circle = document.createElement('div');
        circle.className = 'icon-circle';

        const icon = document.createElement('div');
        icon.className = 'payment-success-icon';
        icon.innerHTML = `<svg viewBox="0 0 52 52"><path d="M14.1 27.2l7.1 7.2 16.7-16.8"/></svg>`;

        const title = document.createElement('h2');
        title.className = 'payment-success-title';
        title.textContent = 'Payment Successful!';

        const text = document.createElement('p');
        text.className = 'payment-success-text';
        text.innerHTML = message ||
            `Your <span class="payment-success-tier">${tier}</span> plan has been activated successfully.<br>Start exploring premium features now!`;

        const button = document.createElement('button');
        button.className = 'payment-success-button';
        button.innerHTML = '<span class="payment-success-button-content">✓ Done</span>';

        box.appendChild(circle);
        box.appendChild(icon);
        box.appendChild(title);
        box.appendChild(text);
        box.appendChild(button);
        overlay.appendChild(box);

        document.body.appendChild(overlay);

        requestAnimationFrame(() => {
            overlay.classList.add('show');
            // Tạo confetti sau khi popup hiển thị
            setTimeout(() => createConfetti(box), 400);
        });

        const hide = () => {
            overlay.classList.remove('show');
            overlay.addEventListener('transitionend', () => {
                overlay.remove();
                window.location.reload();
            }, {once: true});
        };

        button.addEventListener('click', hide);
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                hide();
            }
        });
    }

    window.PaymentSuccessPopup = {
        show: showPaymentSuccessPopup
    };

})();
