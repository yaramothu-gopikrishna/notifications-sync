import client from './client';

export const createChannel = (data) => client.post('/notification-channels', data);
export const listChannels = () => client.get('/notification-channels');
export const updateChannel = (id, data) => client.patch(`/notification-channels/${id}`, data);
export const deleteChannel = (id) => client.delete(`/notification-channels/${id}`);
