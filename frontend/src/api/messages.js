import request from './request'

export const getMessages = (page = 1, limit = 10) => 
  request.get(`/messages?page=${page}&limit=${limit}`)

export const createMessage = (data) => 
  request.post('/messages', data)

export const deleteMessage = (id) => 
  request.delete(`/messages/${id}`)
