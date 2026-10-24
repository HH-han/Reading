import request from '@/utils/request.js'

export function getUserInfo() {
	return request({ url: '/user/info' })
}

export function updateUserInfo(data) {
	return request({ url: '/user/info', method: 'PUT', data })
}

export function getGrowth(params) {
	return request({ url: '/user/growth', data: params })
}

export function getFavorites(params) {
	return request({ url: '/user/favorites', data: params })
}

export function getCheckinDays() {
	return request({ url: '/user/checkin-days' })
}

export default { getUserInfo, updateUserInfo, getGrowth, getFavorites, getCheckinDays }
