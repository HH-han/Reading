import request from '@/utils/request.js'

export function getToday() {
	return request({ url: '/dashboard/today' })
}

export function getTrend(params) {
	return request({ url: '/dashboard/trend', data: params })
}

export function getTopGroups() {
	return request({ url: '/dashboard/top-groups' })
}

export function getMy() {
	return request({ url: '/dashboard/my' })
}

export default { getToday, getTrend, getTopGroups, getMy }
