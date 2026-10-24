import { reactive } from 'vue'

export const themeState = reactive({
	isDark: false
})

export function initTheme() {
	const saved = uni.getStorageSync('theme')
	if (saved === 'dark' || saved === 'light') {
		themeState.isDark = saved === 'dark'
	} else {
		try {
			const info = uni.getSystemInfoSync()
			themeState.isDark = info.theme === 'dark'
		} catch (e) {
			themeState.isDark = false
		}
	}
	applyNavBar()
}

export function toggleTheme() {
	setTheme(!themeState.isDark)
}

export function setTheme(isDark) {
	themeState.isDark = isDark
	uni.setStorageSync('theme', isDark ? 'dark' : 'light')
	applyNavBar()
}

function applyNavBar() {
	const p = uni.setNavigationBarColor({
		frontColor: themeState.isDark ? '#ffffff' : '#000000',
		backgroundColor: themeState.isDark ? '#121212' : '#F6F7FB'
	})
	// H5 端 onLaunch 时页面尚未创建，会报 page not found，静默处理
	if (p && typeof p.catch === 'function') p.catch(() => {})
}

export default { themeState, initTheme, toggleTheme, setTheme }
