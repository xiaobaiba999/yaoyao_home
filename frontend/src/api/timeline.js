import request from './request'

export const getTimeline = () => request.get('/timeline')

export const addTimelineItem = (data) => request.post('/timeline', data)

export const updateTimelineItem = (id, data) => request.put(`/timeline/${id}`, data)

export const deleteTimelineItem = (id) => request.delete(`/timeline/${id}`)