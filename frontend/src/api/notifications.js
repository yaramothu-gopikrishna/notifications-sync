import client from './client';

export const listNotifications = (page = 0, size = 20) =>
  client.get(`/notifications?page=${page}&size=${size}`);
