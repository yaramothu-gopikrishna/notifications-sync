import client from './client';

export const createRule = (data) => client.post('/filter-rules', data);
export const listRules = () => client.get('/filter-rules');
export const updateRule = (id, data) => client.put(`/filter-rules/${id}`, data);
export const deleteRule = (id) => client.delete(`/filter-rules/${id}`);
