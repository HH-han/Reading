import { t } from '@/utils/i18n.js'

export const BASE_URL = 'http://localhost:2026'

export function request(options) {
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: {
				'Content-Type': 'application/json',
				'satoken': uni.getStorageSync('satoken') || '',
				...(options.header || {})
			},
			success: (res) => {
				const body = res.data || {}
				if (body.code === 200) {
					resolve(body.data)
				} else if (body.code === 401) {
					uni.removeStorageSync('satoken')
					uni.removeStorageSync('userInfo')
					uni.showToast({ title: t('common.loginExpired'), icon: 'none' })
					setTimeout(() => {
						uni.reLaunch({ url: '/pages/login/login' })
					}, 800)
					reject(body)
				} else {
					uni.showToast({ title: body.message || t('common.fail'), icon: 'none' })
					reject(body)
				}
			},
			fail: (err) => {
				uni.showToast({ title: t('common.networkError'), icon: 'none' })
				reject(err)
			}
		})
	})
}

export default request
