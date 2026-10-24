import request from '@/utils/request.js'

export function getCurrentCircle() {
	return request({ url: '/circle/current' })
}

export function getGroups() {
	return request({ url: '/circle/groups' })
}

export function getMembers(params) {
	return request({ url: '/circle/members', data: params })
}

export function getJobPage(params) {
	return request({ url: '/circle/job/page', data: params })
}

export function joinCircle(data) {
	return request({ url: '/circle/join', method: 'POST', data })
}

export function getAvailableCircles() {
	return request({ url: '/circle/available' })
}

export function getMyCircles() {
	return request({ url: '/circle/mine' })
}

export function joinCircleById(circleId) {
	return request({ url: '/circle/join/' + circleId, method: 'POST' })
}

export function switchCircle(circleId) {
	return request({ url: '/circle/switch/' + circleId, method: 'POST' })
}

export function exitCircle() {
	return request({ url: '/circle/exit', method: 'POST' })
}

export function exitCircleById(circleId) {
	return request({ url: '/circle/exit/' + circleId, method: 'POST' })
}

export default { getCurrentCircle, getGroups, getMembers, getJobPage, joinCircle, getAvailableCircles, getMyCircles, joinCircleById, switchCircle, exitCircle, exitCircleById }
