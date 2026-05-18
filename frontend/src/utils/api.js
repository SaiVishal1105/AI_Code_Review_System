import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 60000, // 60s — AI calls can take a while
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ---- Auth ----
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// ---- Reviews ----
export const reviewApi = {
  submit: (data) => api.post('/reviews', data),
  getHistory: (page = 0, size = 10) => api.get(`/reviews/history?page=${page}&size=${size}`),
  getOne: (id) => api.get(`/reviews/${id}`),
  refactor: (id) => api.post(`/reviews/${id}/refactor`),
  dashboard: () => api.get('/reviews/dashboard'),
  downloadPdf: (id) => api.get(`/reviews/${id}/pdf`, { responseType: 'blob' }),
  delete: (id) => api.delete(`/reviews/${id}`),
};

export default api;
