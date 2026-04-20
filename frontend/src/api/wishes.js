import request from './request'

export const getWishes = () => request.get('/wishes')

export const createWish = (content) => request.post('/wishes', { content })

export const updateWish = (id, data) => request.put(`/wishes/${id}`, data)

export const deleteWish = (id) => request.delete(`/wishes/${id}`)
