
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

const api = axios.create({
    timeout: 30000,
    validateStatus: function (status) {
        return status >= 200 && status < 300;
    }
});

api.interceptors.request.use((config) => {
    if (csrfToken && csrfHeaderName) {
        config.headers[csrfHeaderName] = csrfToken;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

api.interceptors.response.use(

    (response) => {
        const apiResponse = response.data;

        if (apiResponse && typeof apiResponse.success === 'boolean') {

            if (apiResponse.success === false) {

                const error = new Error(apiResponse.message || 'Business logic error');
                error.normalizedMessage = apiResponse.message || 'Có lỗi xảy ra';
                error.fieldErrors = apiResponse.details; // Field validation errors
                error.errorCode = apiResponse.error;
                return Promise.reject(error);
            }

            // success=true → Unwrap data và lưu message
            response.data = apiResponse.data;
            response.message = apiResponse.message;
        }

        return response;
    },

    (error) => {
        let normalizedMessage = 'Có lỗi không xác định xảy ra';
        let fieldErrors = null;
        let errorCode = 'UNKNOWN_ERROR';

        if (error.response?.data) {
            const apiResponse = error.response.data;

            // Nếu server trả về ApiResponse format
            if (apiResponse && typeof apiResponse.success === 'boolean') {
                normalizedMessage = apiResponse.message || normalizedMessage;
                errorCode = apiResponse.error || errorCode;
                fieldErrors = apiResponse.details;
            }

            else {
                normalizedMessage =
                    apiResponse.message ||
                    apiResponse.error ||
                    error.response.statusText ||
                    normalizedMessage;
            }
        }

        else {
            normalizedMessage = error.message || normalizedMessage;
        }

        error.normalizedMessage = normalizedMessage;
        error.fieldErrors = fieldErrors;
        error.errorCode = errorCode;

        console.error('API Error:', {
            message: normalizedMessage,
            errorCode,
            fieldErrors,
            originalError: error
        });

        return Promise.reject(error);
    }
);

