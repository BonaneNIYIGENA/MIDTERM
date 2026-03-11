/**
 * SpecBooking API Client
 * Handles all API communications and notifications
 */

// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// ============================================
// NOTIFICATION SYSTEM
// ============================================

/**
 * Show a notification toast
 * @param {string} type - 'success' | 'error' | 'warning' | 'info'
 * @param {string} title - Notification title
 * @param {string} message - Notification message
 * @param {number} duration - Duration in ms (default: 5000)
 */
function showNotification(type, title, message, duration = 5000) {
    const container = document.getElementById('notification-container');
    if (!container) return;
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    
    // Icon based on type
    const icons = {
        success: 'check_circle',
        error: 'error',
        warning: 'warning',
        info: 'info'
    };
    
    notification.innerHTML = `
        <div class="notification-icon">
            <span class="material-symbols-outlined">${icons[type] || icons.info}</span>
        </div>
        <div class="notification-content">
            <div class="notification-title">${title}</div>
            <div class="notification-message">${message}</div>
        </div>
        <button class="notification-close">
            <span class="material-symbols-outlined">close</span>
        </button>
        <div class="progress-bar" style="animation-duration: ${duration}ms;"></div>
    `;
    
    // Add to container
    container.appendChild(notification);
    
    // Play sound (optional - can be enabled)
    // playNotificationSound(type);
    
    // Close button handler
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', () => dismissNotification(notification));
    
    // Click to dismiss
    notification.addEventListener('click', (e) => {
        if (e.target !== closeBtn && !closeBtn.contains(e.target)) {
            dismissNotification(notification);
        }
    });
    
    // Auto dismiss
    setTimeout(() => {
        if (notification.parentElement) {
            dismissNotification(notification);
        }
    }, duration);
    
    return notification;
}

/**
 * Dismiss a notification with animation
 */
function dismissNotification(notification) {
    notification.classList.add('dismissing');
    setTimeout(() => {
        if (notification.parentElement) {
            notification.parentElement.removeChild(notification);
        }
    }, 300);
}

/**
 * Show loading overlay
 */
function showLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.add('active');
    }
}

/**
 * Hide loading overlay
 */
function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.remove('active');
    }
}

// ============================================
// API CLIENT
// ============================================

const api = {
    /**
     * Get auth headers
     */
    getHeaders() {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    },
    
    /**
     * Make API request
     */
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers: this.getHeaders(),
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => ({}));
            
            if (!response.ok) {
                throw new Error(data.error || data.message || `HTTP error! status: ${response.status}`);
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    // ============================================
    // AUTH ENDPOINTS
    // ============================================
    
    /**
     * Login user
     */
    async login(email, password) {
        const response = await this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        
        if (response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('userRole', response.role);
            localStorage.setItem('userName', response.name);
            localStorage.setItem('userEmail', response.email);
            if (response.doctorId) {
                localStorage.setItem('doctorId', response.doctorId);
            }
        }
        
        return response;
    },
    
    /**
     * Register new user
     */
    async register(userData) {
        return await this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },
    
    /**
     * Logout user
     */
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userName');
        localStorage.removeItem('userEmail');
        window.location.href = 'index.html';
    },
    
    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return !!localStorage.getItem('token');
    },
    
    /**
     * Get current user info
     */
    getCurrentUser() {
        return {
            id: localStorage.getItem('userId'),
            name: localStorage.getItem('userName'),
            email: localStorage.getItem('userEmail'),
            role: localStorage.getItem('userRole')
        };
    },
    
    // ============================================
    // USER ENDPOINTS
    // ============================================
    
    /**
     * Get all users
     */
    async getUsers() {
        return await this.request('/users');
    },
    
    /**
     * Get user by ID
     */
    async getUser(id) {
        return await this.request(`/users/${id}`);
    },
    
    /**
     * Update user
     */
    async updateUser(id, userData) {
        return await this.request(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    },
    
    /**
     * Delete user
     */
    async deleteUser(id) {
        return await this.request(`/users/${id}`, {
            method: 'DELETE'
        });
    },
    
    // ============================================
    // DOCTOR ENDPOINTS
    // ============================================
    
    /**
     * Get all doctors (paginated)
     */
    async getDoctors(page = 0, size = 100, sortBy = 'name') {
        const response = await this.request(`/doctors?page=${page}&size=${size}&sortBy=${sortBy}`);
        return response.content || [];
    },
    
    /**
     * Get doctor by ID
     */
    async getDoctor(id) {
        return await this.request(`/doctors/${id}`);
    },
    
    /**
     * Get doctors by specialty
     */
    async getDoctorsBySpecialty(specialty) {
        return await this.request(`/doctors/specialty/${encodeURIComponent(specialty)}`);
    },
    
    /**
     * Get doctors by province
     */
    async getDoctorsByProvince(province) {
        return await this.request(`/doctors/province/${encodeURIComponent(province)}`);
    },
    
    /**
     * Create doctor profile
     */
    async createDoctor(doctorData) {
        return await this.request('/doctors', {
            method: 'POST',
            body: JSON.stringify(doctorData)
        });
    },
    
    /**
     * Update doctor
     */
    async updateDoctor(id, doctorData) {
        return await this.request(`/doctors/${id}`, {
            method: 'PUT',
            body: JSON.stringify(doctorData)
        });
    },
    
    /**
     * Search doctors by specialty and/or province
     */
    async searchDoctors(specialty, province) {
        const params = new URLSearchParams();
        if (specialty) params.append('specialty', specialty);
        if (province) params.append('province', province);
        return await this.request(`/doctors/search?${params.toString()}`);
    },
    
    /**
     * Search doctors by name
     */
    async searchDoctorsByName(name) {
        return await this.request(`/doctors/search/name/${encodeURIComponent(name)}`);
    },
    
    /**
     * Delete doctor
     */
    async deleteDoctor(id) {
        return await this.request(`/doctors/${id}`, {
            method: 'DELETE'
        });
    },
    
    /**
     * Add regular patient to doctor
     */
    async addRegularPatient(doctorId, userId) {
        return await this.request(`/doctors/${doctorId}/patients/${userId}`, {
            method: 'POST'
        });
    },
    
    // ============================================
    // APPOINTMENT ENDPOINTS
    // ============================================
    
    /**
     * Get all appointments
     */
    async getAppointments() {
        return await this.request('/appointments');
    },
    
    /**
     * Get appointment by ID
     */
    async getAppointment(id) {
        return await this.request(`/appointments/${id}`);
    },
    
    /**
     * Get appointments by user ID
     */
    async getUserAppointments(userId) {
        return await this.request(`/appointments/user/${userId}`);
    },
    
    /**
     * Get appointments by doctor ID
     */
    async getDoctorAppointments(doctorId) {
        return await this.request(`/appointments/doctor/${doctorId}`);
    },
    
    /**
     * Create appointment
     */
    async createAppointment(appointmentData) {
        const response = await this.request('/appointments', {
            method: 'POST',
            body: JSON.stringify(appointmentData)
        });
        
        // Trigger notification event
        if (response.id) {
            triggerEvent('appointmentCreated', response);
        }
        
        return response;
    },
    
    /**
     * Update appointment
     */
    async updateAppointment(id, appointmentData) {
        return await this.request(`/appointments/${id}`, {
            method: 'PUT',
            body: JSON.stringify(appointmentData)
        });
    },
    
    /**
     * Update appointment status
     */
    async updateAppointmentStatus(id, status) {
        const response = await this.request(`/appointments/${id}/status?status=${status}`, {
            method: 'PATCH'
        });
        
        // Trigger notification event
        triggerEvent('appointmentStatusChanged', { id, status });
        
        return response;
    },
    
    /**
     * Cancel appointment
     */
    async cancelAppointment(id) {
        return await this.updateAppointmentStatus(id, 'CANCELLED');
    },
    
    /**
     * Confirm appointment
     */
    async confirmAppointment(id) {
        return await this.updateAppointmentStatus(id, 'CONFIRMED');
    },
    
    /**
     * Delete appointment
     */
    async deleteAppointment(id) {
        return await this.request(`/appointments/${id}`, {
            method: 'DELETE'
        });
    },
    
    // ============================================
    // SCHEDULE ENDPOINTS
    // ============================================
    
    /**
     * Get all schedules
     */
    async getSchedules() {
        return await this.request('/schedules');
    },
    
    /**
     * Get schedule by ID
     */
    async getSchedule(id) {
        return await this.request(`/schedules/${id}`);
    },
    
    /**
     * Get schedules by doctor ID
     */
    async getDoctorSchedules(doctorId) {
        return await this.request(`/schedules/doctor/${doctorId}`);
    },
    
    /**
     * Create schedule
     */
    async createSchedule(scheduleData) {
        return await this.request('/schedules', {
            method: 'POST',
            body: JSON.stringify(scheduleData)
        });
    },
    
    /**
     * Update schedule
     */
    async updateSchedule(id, scheduleData) {
        return await this.request(`/schedules/${id}`, {
            method: 'PUT',
            body: JSON.stringify(scheduleData)
        });
    },
    
    /**
     * Delete schedule
     */
    async deleteSchedule(id) {
        return await this.request(`/schedules/${id}`, {
            method: 'DELETE'
        });
    },
    
    /**
     * Create bulk schedules
     */
    async createBulkSchedules(bulkData) {
        return await this.request('/schedules/bulk', {
            method: 'POST',
            body: JSON.stringify(bulkData)
        });
    },
    
    /**
     * Get available slots for a doctor
     */
    async getAvailableSlots(doctorId) {
        return await this.request(`/schedules/available/${doctorId}`);
    },
    
    // ============================================
    // LOCATION ENDPOINTS
    // ============================================
    
    /**
     * Get all locations
     */
    async getLocations() {
        return await this.request('/locations');
    },
    
    /**
     * Get location by ID
     */
    async getLocation(id) {
        return await this.request(`/locations/${id}`);
    },
    
    /**
     * Create location
     */
    async createLocation(locationData) {
        return await this.request('/locations', {
            method: 'POST',
            body: JSON.stringify(locationData)
        });
    },
    
    /**
     * Update location
     */
    async updateLocation(id, locationData) {
        return await this.request(`/locations/${id}`, {
            method: 'PUT',
            body: JSON.stringify(locationData)
        });
    },
    
    /**
     * Delete location
     */
    async deleteLocation(id) {
        return await this.request(`/locations/${id}`, {
            method: 'DELETE'
        });
    },
    
    /**
     * Get locations by province
     */
    async getLocationsByProvince(province) {
        return await this.request(`/locations/province/${encodeURIComponent(province)}`);
    },
    
    /**
     * Get unique provinces from locations
     */
    async getProvinces() {
        const locations = await this.getLocations();
        const provinces = [...new Set(locations.map(loc => loc.province))];
        return provinces.sort();
    },
    
    /**
     * Get districts for a province from locations
     */
    async getDistrictsByProvince(province) {
        const locations = await this.getLocationsByProvince(province);
        const districts = [...new Set(locations.map(loc => loc.district))];
        return districts.sort();
    },
    
    /**
     * Get users by province
     */
    async getUsersByProvince(province) {
        return await this.request(`/users/province/${encodeURIComponent(province)}`);
    },
    
    // ============================================
    // APPOINTMENT REPORT ENDPOINTS
    // ============================================
    
    /**
     * Get report by appointment ID
     */
    async getAppointmentReport(appointmentId) {
        return await this.request(`/appointments/${appointmentId}/report`);
    },
    
    /**
     * Create appointment report
     */
    async createAppointmentReport(appointmentId, reportData) {
        return await this.request(`/appointments/${appointmentId}/report`, {
            method: 'POST',
            body: JSON.stringify(reportData)
        });
    },
    
    /**
     * Update appointment report
     */
    async updateAppointmentReport(appointmentId, reportData) {
        return await this.request(`/appointments/${appointmentId}/report`, {
            method: 'PUT',
            body: JSON.stringify(reportData)
        });
    }
};

// ============================================
// EVENT SYSTEM
// ============================================

const eventListeners = {};

/**
 * Register event listener
 */
function onEvent(eventName, callback) {
    if (!eventListeners[eventName]) {
        eventListeners[eventName] = [];
    }
    eventListeners[eventName].push(callback);
}

/**
 * Trigger event
 */
function triggerEvent(eventName, data) {
    if (eventListeners[eventName]) {
        eventListeners[eventName].forEach(callback => callback(data));
    }
}

// ============================================
// AUTO NOTIFICATIONS
// ============================================

// Appointment created notification
onEvent('appointmentCreated', (appointment) => {
    showNotification('success', 'Appointment Booked!', 
        `Your appointment has been scheduled successfully.`);
});

// Appointment status changed notification
onEvent('appointmentStatusChanged', ({ id, status }) => {
    const messages = {
        'CONFIRMED': { type: 'success', title: 'Appointment Confirmed', msg: 'Your appointment has been confirmed by the doctor.' },
        'CANCELLED': { type: 'warning', title: 'Appointment Cancelled', msg: 'The appointment has been cancelled.' },
        'COMPLETED': { type: 'info', title: 'Appointment Completed', msg: 'The appointment has been marked as completed.' },
        'NO_SHOW': { type: 'error', title: 'No Show', msg: 'The appointment was marked as no-show.' }
    };
    
    const config = messages[status] || { type: 'info', title: 'Status Updated', msg: `Appointment status changed to ${status}` };
    showNotification(config.type, config.title, config.msg);
});

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Format date for display
 */
function formatDate(dateString) {
    const options = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

/**
 * Format time for display
 */
function formatTime(timeString) {
    const [hours, minutes] = timeString.split(':');
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const hour12 = hour % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
}

/**
 * Get initials from name
 */
function getInitials(name) {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

/**
 * Get status badge class
 */
function getStatusBadgeClass(status) {
    const classes = {
        'PENDING': 'badge-warning',
        'CONFIRMED': 'badge-success',
        'CANCELLED': 'badge-error',
        'COMPLETED': 'badge-info',
        'NO_SHOW': 'badge-error'
    };
    return classes[status] || 'badge-info';
}

/**
 * Debounce function
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Check authentication and redirect if needed
 */
function requireAuth() {
    if (!api.isAuthenticated()) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

/**
 * Check role and redirect if needed
 */
function requireRole(allowedRoles) {
    if (!requireAuth()) return false;
    
    const userRole = localStorage.getItem('userRole');
    if (!allowedRoles.includes(userRole)) {
        showNotification('error', 'Access Denied', 'You do not have permission to access this page.');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 2000);
        return false;
    }
    return true;
}

// Export for use in other files
window.api = api;
window.showNotification = showNotification;
window.showLoading = showLoading;
window.hideLoading = hideLoading;
window.formatDate = formatDate;
window.formatTime = formatTime;
window.getInitials = getInitials;
window.getStatusBadgeClass = getStatusBadgeClass;
window.debounce = debounce;
window.requireAuth = requireAuth;
window.requireRole = requireRole;
window.onEvent = onEvent;
window.triggerEvent = triggerEvent;
