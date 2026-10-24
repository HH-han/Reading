<script>
	import {
		initTheme
	} from '@/store/theme.js'
	import {
		i18nState
	} from '@/utils/i18n.js'

	export default {
		onLaunch: function() {
			// 初始化主题（读缓存 + 跟随系统）
			initTheme()
			// 初始化语言（模块加载时已读缓存，这里确保已就绪）
			if (!i18nState.locale) {
				i18nState.locale = 'zh'
			}
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	@import '@/styles/theme.scss';
	@import '@/styles/glass.scss';
	@import '@/styles/ui.scss';
	@import '@/styles/icons.scss';
	/*每个页面公共css */
</style>
