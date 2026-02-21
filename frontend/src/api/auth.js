import client from './client';

export const forgotPassword = (email) => client.post('/auth/forgot-password', { email });
export const resetPassword = (token, newPassword) => client.post('/auth/reset-password', { token, newPassword });
