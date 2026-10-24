import { reactive } from 'vue'
import request from '@/utils/request.js'

export const userState = reactive({
	token: uni.getStorageSync('satoken') || '',
	userInfo: uni.getStorageSync('userInfo') || null
})

export function setLogin(token, user) {
	userState.token = token
	userState.userInfo = user
	uni.setStorageSync('satoken', token)
	uni.setStorageSync('userInfo', user)
}

export function setUserInfo(user) {
	userState.userInfo = user
	uni.setStorageSync('userInfo', user)
}

export async function fetchUserInfo() {
	const data = await request({ url: '/user/info' })
	setUserInfo(data)
	return data
}

export async function logout() {
	try {
		await request({ url: '/auth/logout', method: 'POST' })
	} catch (e) {}
	clearSession()
}

export function clearSession() {
	userState.token = ''
	userState.userInfo = null
	uni.removeStorageSync('satoken')
	uni.removeStorageSync('userInfo')
	uni.reLaunch({ url: '/pages/login/login' })
}

export function hasRole(roles) {
	const u = userState.userInfo
	return !!(u && roles.includes(u.role))
}

export function isAdmin() {
	return hasRole(['TEAM_ADMIN', 'SUPER_ADMIN'])
}

export default { userState, setLogin, setUserInfo, fetchUserInfo, logout, clearSession, hasRole, isAdmin }
