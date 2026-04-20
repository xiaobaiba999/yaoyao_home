import request from './request'

export async function login(username, password) {
  return request.post('/auth/login', { username, password })
}

export async function getMe() {
  return request.get('/auth/me')
}
