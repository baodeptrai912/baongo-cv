class UniversalConfirmationService {
    constructor() {
        this.modal = null;
        this.bsModal = null;
        this.currentResolve = null;

        this.createModal();
        this.setupEventListeners();
    }

    createModal() {
        // Tạo modal HTML động
        const modalHTML = `
            <div id="universalConfirmationModal" class="modal fade" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header" id="modalHeader">
                            <h5 class="modal-title" id="modalTitle"></h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body" id="modalBody">
                            <div id="modalIcon" class="text-center mb-3"></div>
                            <div id="modalMessage" class="mb-3"></div>
                            <div id="modalConsequences"></div>
                        </div>
                        <div class="modal-footer" id="modalFooter">
                            <button type="button" id="modalCancel" class="btn btn-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="button" id="modalConfirm" class="btn">
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);
        this.modal = document.getElementById('universalConfirmationModal');
        this.bsModal = new bootstrap.Modal(this.modal);
    }

    setupEventListeners() {
        document.getElementById('modalConfirm').addEventListener('click', () => {
            this.bsModal.hide();
            if (this.currentResolve) this.currentResolve(true);
        });

        document.getElementById('modalCancel').addEventListener('click', () => {
            this.bsModal.hide();
            if (this.currentResolve) this.currentResolve(false);
        });

        this.modal.addEventListener('hidden.bs.modal', () => {
            if (this.currentResolve) this.currentResolve(false);
        });
    }

    // ✅ Main API method
    async confirm(config) {
        const {
            type = 'default',
            title,
            message,
            consequences = [],
            confirmText = 'Confirm',
            cancelText = 'Cancel',
            resourceName = '',
            customStyle = {}
        } = config;

        // Cập nhật nội dung modal
        this.updateModalContent(type, title, message, consequences, confirmText, cancelText, resourceName, customStyle);

        // Hiển thị modal và trả về Promise
        this.bsModal.show();

        return new Promise((resolve) => {
            this.currentResolve = resolve;
        });
    }

    updateModalContent(type, title, message, consequences, confirmText, cancelText, resourceName, customStyle) {
        const modalHeader = document.getElementById('modalHeader');
        const modalTitle = document.getElementById('modalTitle');
        const modalIcon = document.getElementById('modalIcon');
        const modalMessage = document.getElementById('modalMessage');
        const modalConsequences = document.getElementById('modalConsequences');
        const modalConfirm = document.getElementById('modalConfirm');
        const modalCancel = document.getElementById('modalCancel');

        // Định nghĩa configs cho từng loại
        const typeConfigs = {
            delete: {
                headerClass: 'bg-danger text-white',
                icon: '<i class="fas fa-trash fa-3x text-danger"></i>',
                confirmClass: 'btn-danger',
                defaultTitle: `Delete ${resourceName || 'Item'}`,
                defaultMessage: `Are you sure you want to delete this ${resourceName?.toLowerCase() || 'item'}?`
            },
            deleteAccount: {
                headerClass: 'bg-danger text-white',
                icon: '<i class="fas fa-exclamation-triangle fa-3x text-danger"></i>',
                confirmClass: 'btn-danger',
                defaultTitle: 'Delete Account - Final Confirmation',
                defaultMessage: 'You are about to permanently delete your account. This action cannot be undone!'
            },
            warning: {
                headerClass: 'bg-warning text-dark',
                icon: '<i class="fas fa-exclamation-triangle fa-3x text-warning"></i>',
                confirmClass: 'btn-warning',
                defaultTitle: 'Warning',
                defaultMessage: 'Please confirm this action.'
            },
            info: {
                headerClass: 'bg-info text-white',
                icon: '<i class="fas fa-info-circle fa-3x text-info"></i>',
                confirmClass: 'btn-primary',
                defaultTitle: 'Confirmation Required',
                defaultMessage: 'Please confirm to proceed.'
            }
        };

        const config = typeConfigs[type] || typeConfigs.info;

        // Cập nhật header
        modalHeader.className = `modal-header ${config.headerClass}`;
        modalTitle.textContent = title || config.defaultTitle;

        // Cập nhật icon
        modalIcon.innerHTML = config.icon;

        // Cập nhật message
        modalMessage.innerHTML = `<p class="mb-3">${message || config.defaultMessage}</p>`;

        // Cập nhật consequences
        if (consequences.length > 0) {
            const consequencesList = consequences.map(item =>
                `<li><i class="fas fa-times text-danger me-2"></i>${item}</li>`
            ).join('');
            modalConsequences.innerHTML = `
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    <strong>This action will:</strong>
                </div>
                <ul class="list-unstyled mb-3">${consequencesList}</ul>
            `;
        } else {
            modalConsequences.innerHTML = '';
        }

        // Cập nhật buttons
        modalConfirm.className = `btn ${config.confirmClass}`;
        modalConfirm.textContent = confirmText;
        modalCancel.textContent = cancelText;

        // Áp dụng custom styles
        Object.entries(customStyle).forEach(([key, value]) => {
            if (key === 'headerClass') modalHeader.className += ` ${value}`;
            if (key === 'confirmClass') modalConfirm.className = `btn ${value}`;
        });
    }

    // ✅ Specialized methods cho từng loại resource
    async confirmDeleteJobAlert(jobAlertTitle) {
        return this.confirm({
            type: 'delete',
            resourceName: 'Job Alert',
            title: `Delete Job Alert`,
            message: `Are you sure you want to delete "${jobAlertTitle}"?`,
            consequences: [
                'Remove this job alert permanently',
                'Stop receiving notifications for this alert',
                'Cannot be undone'
            ],
            confirmText: 'Delete Job Alert',
            cancelText: 'Keep Alert'
        });
    }

    async confirmDeleteAccount() {
        return this.confirm({
            type: 'deleteAccount',
            title: 'Delete Account - Final Confirmation',
            message: 'You are about to permanently delete your account.',
            consequences: [
                'Delete your profile and personal information',
                'Remove all job applications',
                'Delete all saved jobs and alerts',
                'Remove your account permanently',
                'Cannot be recovered'
            ],
            confirmText: 'Yes, Delete Permanently',
            cancelText: 'Cancel'
        });
    }

    async confirmDeleteCompany(companyName) {
        return this.confirm({
            type: 'delete',
            resourceName: 'Company',
            title: 'Delete Company',
            message: `Are you sure you want to delete company "${companyName}"?`,
            consequences: [
                'Remove all job postings from this company',
                'Delete company profile and information',
                'Remove all applications to this company',
                'Cannot be undone'
            ],
            confirmText: 'Delete Company',
            cancelText: 'Cancel'
        });
    }

    async confirmDeleteJob(jobTitle) {
        return this.confirm({
            type: 'delete',
            resourceName: 'Job',
            title: 'Delete Job Posting',
            message: `Are you sure you want to delete "${jobTitle}"?`,
            consequences: [
                'Remove job posting from all listings',
                'Notify all applicants about deletion',
                'Remove all applications for this job',
                'Cannot be restored'
            ],
            confirmText: 'Delete Job',
            cancelText: 'Keep Job'
        });
    }

    async confirmDeleteUser(userName) {
        return this.confirm({
            type: 'delete',
            resourceName: 'User',
            title: 'Delete User Account',
            message: `Are you sure you want to delete user "${userName}"?`,
            consequences: [
                'Remove user account permanently',
                'Delete all user data and activity',
                'Cancel all active applications',
                'Remove from all company connections',
                'Admin action - cannot be undone'
            ],
            confirmText: 'Delete User',
            cancelText: 'Cancel'
        });
    }

    // ✅ Generic delete method
    async confirmDelete(resourceType, resourceName, customConsequences = []) {
        const defaultConsequences = {
            'job-alert': ['Stop receiving notifications', 'Remove alert permanently'],
            'job': ['Remove from job listings', 'Notify all applicants'],
            'company': ['Remove all job postings', 'Delete company data'],
            'user': ['Delete all user data', 'Remove account permanently'],
            'application': ['Remove application record', 'Notify employer']
        };

        return this.confirm({
            type: 'delete',
            resourceName: resourceType.charAt(0).toUpperCase() + resourceType.slice(1),
            title: `Delete ${resourceType.charAt(0).toUpperCase() + resourceType.slice(1)}`,
            message: `Are you sure you want to delete "${resourceName}"?`,
            consequences: customConsequences.length > 0 ? customConsequences : defaultConsequences[resourceType] || ['This action cannot be undone'],
            confirmText: `Delete ${resourceType.charAt(0).toUpperCase() + resourceType.slice(1)}`,
            cancelText: 'Cancel'
        });
    }
}

// ✅ Global instance
const confirmationService = new UniversalConfirmationService();
