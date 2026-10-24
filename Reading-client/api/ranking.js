import request from '@/utils/request.js'

export function getRanking(params) {
	return request({ url: '/ranking', data: params })
}

export default { getRanking }
