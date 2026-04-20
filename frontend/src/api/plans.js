import request from './request'

export const getPlans = () => request.get('/plans')

export const createPlan = (data) => request.post('/plans', data)

export const updatePlan = (id, data) => request.put(`/plans/${id}`, data)

export const deletePlan = (id) => request.delete(`/plans/${id}`)
