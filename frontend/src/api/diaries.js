import request from './request'

export const getDiaries = () => request.get('/diaries')

export const createDiary = (data) => request.post('/diaries', data)

export const updateDiary = (id, data) => request.put(`/diaries/${id}`, data)

export const deleteDiary = (id) => request.delete(`/diaries/${id}`)
