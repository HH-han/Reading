import request from '@/utils/request.js'

export function getNotifyPage(params) {
	return request({ url: '/notify/page', data: params })
}

export function markRead(id) {
	return request({ url: '/notify/' + id + '/read', method: 'PUT' })
}

export function getUnreadCount() {
	return request({ url: '/notify/unread-count' })
}

export default { getNotifyPage, markRead, getUnreadCount }
