import request from '@/utils/request.js'

export function submitCheckin(data) {
	return request({ url: '/checkin', method: 'POST', data })
}

export function getCheckinTemplate() {
	return request({ url: '/checkin/template' })
}

export function getFeed(params) {
	return request({ url: '/checkin/feed', data: params })
}

export function getMyCheckin(params) {
	return request({ url: '/checkin/my', data: params })
}

export function getCheckinDetail(id) {
	return request({ url: '/checkin/' + id })
}

export function updateCheckin(id, data) {
	return request({ url: '/checkin/' + id, method: 'PUT', data })
}

export function deleteCheckin(id) {
	return request({ url: '/checkin/' + id, method: 'DELETE' })
}

export function likeCheckin(id) {
	return request({ url: '/checkin/' + id + '/like', method: 'POST' })
}

export function unlikeCheckin(id) {
	return request({ url: '/checkin/' + id + '/like', method: 'DELETE' })
}

export function getComments(id) {
	return request({ url: '/checkin/' + id + '/comments' })
}

export function postComment(id, data) {
	return request({ url: '/checkin/' + id + '/comment', method: 'POST', data })
}

export default { submitCheckin, getCheckinTemplate, getFeed, getMyCheckin, getCheckinDetail, updateCheckin, deleteCheckin, likeCheckin, unlikeCheckin, getComments, postComment }
