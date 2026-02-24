import axios from 'axios';

const API_URL = typeof __API_URL__ !== 'undefined' ? __API_URL__ : '';

const client = axios.create({
  baseURL: `${API_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const forceSignout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.dispatchEvent(new CustomEvent('auth:signout'));
  window.location.href = '/login';
};

client.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config;

      if (error.response?.status === 403) {
        forceSignout();
        return Promise.reject(error);
      }

      if (error.response?.status === 401 && !original._retry) {
        original._retry = true;
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          try {
            const { data } = await axios.post(`${API_URL}/api/v1/auth/refresh`, { refreshToken });
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            original.headers.Authorization = `Bearer ${data.accessToken}`;
            return client(original);
          } catch {
            forceSignout();
          }
        } else {
          forceSignout();
        }
      }
      return Promise.reject(error);
    }
);

export default client;