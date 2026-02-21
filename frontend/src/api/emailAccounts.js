import client from './client';

export const connectGmail = () => client.post('/email-accounts/connect');
export const listAccounts = () => client.get('/email-accounts');
export const getAccount = (id) => client.get(`/email-accounts/${id}`);
export const pauseAccount = (id) => client.patch(`/email-accounts/${id}/pause`);
export const resumeAccount = (id) => client.patch(`/email-accounts/${id}/resume`);
export const disconnectAccount = (id) => client.delete(`/email-accounts/${id}`);
