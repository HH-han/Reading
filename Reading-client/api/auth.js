import request from '@/utils/request.js'

export function login(data) {
	return request({ url: '/auth/login', method: 'POST', data })
}

export function register(data) {
	return request({ url: '/auth/register', method: 'POST', data })
}

export function wxLogin(data) {
	return request({ url: '/auth/wx-login', method: 'POST', data })
}

export function logout() {
	return request({ url: '/auth/logout', method: 'POST' })
}

export default { login, register, wxLogin, logout }
