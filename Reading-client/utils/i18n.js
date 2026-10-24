import { reactive } from 'vue'
import zh from '@/locale/zh.js'
import en from '@/locale/en.js'

const messages = { zh, en }

export const i18nState = reactive({
	locale: uni.getStorageSync('locale') || 'zh'
})

function resolve(key, locale) {
	const keys = key.split('.')
	let val = messages[locale]
	for (const k of keys) {
		if (val == null) return null
		val = val[k]
	}
	return val
}

export function t(key, params) {
	let val = resolve(key, i18nState.locale)
	if (val == null) val = resolve(key, 'zh')
	if (val == null) return key
	if (typeof val === 'string' && params) {
		Object.keys(params).forEach(k => {
			val = val.replace(new RegExp('\\{' + k + '\\}', 'g'), params[k])
		})
	}
	return val
}

export function setLocale(locale) {
	i18nState.locale = locale
	uni.setStorageSync('locale', locale)
}

export function getLocale() {
	return i18nState.locale
}

export default { t, setLocale, getLocale, i18nState }
