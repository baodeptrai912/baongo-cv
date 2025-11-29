// Hàm này cần được truy cập toàn cục bởi onchange=""
function updateJobStatus(selectElement) {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const newStatus = selectElement.value;
    const id = selectElement.getAttribute('data-id');

    const wrapper = selectElement.closest('.status-display-wrapper');
    const badgeElement = wrapper.querySelector('.status-badge');

    if (!badgeElement) {
        console.error('Could not find the badge element to update!');
        return;
    }

    selectElement.disabled = true;

    fetch(`/company/update-job-posting-status/${id}/${newStatus}`, {
        method: 'PUT',
        headers: {
            'X-CSRF-TOKEN': csrfToken,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Server error: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            const jobStatus = data.status;
            badgeElement.textContent = jobStatus.charAt(0).toUpperCase() + jobStatus.slice(1).toLowerCase();

            let newClass = 'status-badge badge ';
            switch (jobStatus) {
                case 'OPEN': newClass += 'bg-success'; break;
                case 'CLOSED': newClass += 'bg-secondary'; break;
                case 'EXPIRED': newClass += 'bg-warning text-dark'; break;
                case 'FILLED': newClass += 'bg-info text-dark'; break;
                default: newClass += 'bg-light text-dark';
            }
            badgeElement.className = newClass;
        })
        .catch(error => {
            console.error("Error updating status:", error);
            selectElement.value = badgeElement.textContent.toUpperCase(); // Revert on fail
            alert('Failed to update status. Please try again.');
        })
        .finally(() => {
            if (selectElement.value !== 'EXPIRED' && selectElement.value !== 'FILLED') {
                selectElement.disabled = false;
            }
        });
}

// Hàm này cần được truy cập toàn cục bởi onclick=""
function deleteJob(element) {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const jobId = element.getAttribute('data-id');

    if (confirm('Are you sure you want to delete this job posting?')) {
        fetch(`/company/delete-job-posting/${jobId}`, {
            method: 'DELETE',
            headers: { 'X-CSRF-TOKEN': csrfToken },
        })
            .then(response => {
                if (response.ok) {
                    // Xóa card khỏi giao diện để không cần reload
                    element.closest('.job-card-wrapper').remove();
                    alert('Job deleted successfully!');
                } else {
                    alert('Failed to delete the job posting.');
                }
            })
            .catch(error => {
                console.error('Error deleting job:', error);
                alert('An error occurred while deleting the job posting.');
            });
    }
}


// Logic cho modal Edit và các sự kiện khác
document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const editJobModal = document.getElementById('editJobModal');

    // Lắng nghe sự kiện khi modal được mở
    if (editJobModal) {
        editJobModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const jobDataString = button.getAttribute('data-job');
            const job = JSON.parse(jobDataString);
            const modal = this;

            // Điền dữ liệu vào các input
            modal.querySelector('#editJobId').value = job.id;
            modal.querySelector('#editCompany').value = job.companyName;
            modal.querySelector('#editTitle').value = job.title;
            modal.querySelector('#editJobType').value = job.jobType;
            modal.querySelector('#editLocation').value = job.location;
            modal.querySelector('#editCategory').value = job.industryId;
            modal.querySelector('#editExperience').value = job.experience;
            modal.querySelector('#editSalary').value = job.salaryRange;
            modal.querySelector('#editDescription').value = job.description;
            modal.querySelector('#editBenefit').value = job.benefit;
            modal.querySelector('#editMaxApplicants').value = job.maxApplicants;
            modal.querySelector('#editDeadline').value = job.applicationDeadline;

            // Điền requirements
            const requirementsContainer = modal.querySelector('#editRequirementsContainer');
            requirementsContainer.innerHTML = ''; // Xóa các yêu cầu cũ
            if (job.requirements && job.requirements.length > 0) {
                job.requirements.forEach(req => {
                    const div = document.createElement('div');
                    div.className = 'input-group mb-2';
                    div.innerHTML = `
                            <input type="text" class="form-control" name="requirements" value="${req}" required>
                            <button type="button" class="btn btn-danger remove-edit-requirement">-</button>`;
                    requirementsContainer.appendChild(div);
                });
            }
        });

        // Sử dụng event delegation để xử lý nút xóa requirement
        editJobModal.addEventListener('click', function(e) {
            if (e.target.classList.contains('remove-edit-requirement')) {
                e.target.closest('.input-group').remove();
            }
        });
    }

    // Lắng nghe sự kiện nút "Add Requirement"
    const addReqBtn = document.getElementById('add-edit-requirement');
    if (addReqBtn) {
        addReqBtn.addEventListener('click', function () {
            const container = document.getElementById('editRequirementsContainer');
            const div = document.createElement('div');
            div.className = 'input-group mb-2';
            div.innerHTML = `
                    <input type="text" class="form-control" name="requirements" placeholder="Enter requirement" required>
                    <button type="button" class="btn btn-danger remove-edit-requirement">-</button>`;
            container.appendChild(div);
        });
    }

    // Lắng nghe sự kiện submit form Edit
    const editJobForm = document.getElementById('editJobForm');
    if (editJobForm) {
        editJobForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const currentJobId = document.getElementById('editJobId').value;
            const formData = new FormData(editJobForm);
            const data = {};

            // Gom data từ form, xử lý mảng requirements
            const requirements = [];
            formData.forEach((value, key) => {
                if (key === 'requirements') {
                    requirements.push(value);
                } else {
                    data[key] = value;
                }
            });
            data.requirements = requirements.join(','); // Gửi lên server dưới dạng chuỗi

            fetch(`/company/update-job-posting/${currentJobId}`, {
                method: 'PUT',
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json().then(body => ({ ok: response.ok, body })))
                .then(({ ok, body }) => {
                    if (ok && body.status === 'success') {
                        alert('Job posting updated successfully!');
                        window.location.reload();
                    } else {
                        // Nối các lỗi từ backend (nếu có)
                        const errorMessages = body.errors ? Object.values(body.errors).join('\n') : body.message;
                        alert('Error updating job posting:\n' + errorMessages);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An unexpected error occurred.');
                });
        });
    }
});