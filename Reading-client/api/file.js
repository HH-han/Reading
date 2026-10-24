import { BASE_URL } from '@/utils/request.js'
import { t } from '@/utils/i18n.js'

export function uploadFile(filePath, url = '/file/upload') {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: BASE_URL + url,
			filePath,
			name: 'file',
			header: {
				'satoken': uni.getStorageSync('satoken') || ''
			},
			success: (res) => {
				let body = null
				try {
					body = JSON.parse(res.data)
				} catch (e) {
					reject(e)
					return
				}
				if (body.code === 200) {
					resolve(body.data)
				} else if (body.code === 401) {
					uni.removeStorageSync('satoken')
					uni.removeStorageSync('userInfo')
					uni.reLaunch({ url: '/pages/login/login' })
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

export default { uploadFile }
