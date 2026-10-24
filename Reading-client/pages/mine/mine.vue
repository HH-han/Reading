<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 用户头部 -->
			<view class="user-card" @click="goProfile">
				<view class="user-deco deco-1"></view>
				<view class="user-deco deco-2"></view>
				<view class="avatar-ring">
					<image class="u-avatar" :src="(user && user.avatar) || '/static/logo.png'" mode="aspectFill" />
				</view>
				<view class="u-main">
					<text class="u-name">{{ (user && user.nickname) || '-' }}</text>
					<text class="u-position">{{ (user && user.position) || '' }}</text>
				</view>
				<view class="u-edit">
					<r-icon name="arrow-right-s-line" class="u-edit-text" />
				</view>
			</view>

			<!-- 统计行 -->
			<view class="stat-row glass-card">
				<view class="stat-item" v-for="(s, i) in stats" :key="s.label" :class="{ divided: i > 0 }">
					<text class="stat-num">{{ s.value }}</text>
					<text class="stat-label">{{ $t(s.label) }}</text>
				</view>
			</view>

			<!-- 功能列表（分组卡片） -->
			<view class="menu glass-card" v-for="g in menuGroups" :key="g.title">
				<view class="menu-group-title">{{ $t(g.title) }}</view>
				<view class="menu-item" v-for="m in g.items" :key="m.label" @click="onMenu(m)">
					<view class="icon-chip icon-chip--sm" :class="m.color">
						<r-icon :name="m.icon" />
					</view>
					<text class="menu-label">{{ $t(m.label) }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<!-- 管理区 -->
			<view class="menu glass-card" v-if="admin">
				<view class="menu-group-title">{{ $t('mine.adminTitle') }}</view>
				<view class="menu-item" v-for="m in adminMenus" :key="m.label" @click="onMenu(m)">
					<view class="icon-chip icon-chip--sm" :class="m.color">
						<r-icon :name="m.icon" />
					</view>
					<text class="menu-label">{{ $t(m.label) }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<!-- 主题与语言 -->
			<view class="menu glass-card">
				<view class="menu-item">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="contrast-2-line" /></view>
					<text class="menu-label">{{ $t('mine.theme') }}</text>
					<theme-switch />
				</view>
				<view class="menu-item" @click="switchLang">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="global-line" /></view>
					<text class="menu-label">{{ $t('mine.language') }}</text>
					<text class="menu-value">{{ langLabel }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<view class="logout-btn" @click="onLogout">{{ $t('common.logout') }}</view>
		</view>

		<float-tabbar :current="4" />
		<r-modal />
	</view>
</template>

<script setup>
	import { computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState, fetchUserInfo, logout, isAdmin, hasRole } from '@/store/user.js'
	import { t, i18nState, setLocale } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import FloatTabbar from '@/components/float-tabbar/float-tabbar.vue'
	import ThemeSwitch from '@/components/theme-switch/theme-switch.vue'

	const user = computed(() => userState.userInfo)
	const admin = computed(() => isAdmin())
	const superAdmin = computed(() => hasRole(['SUPER_ADMIN']))

	const stats = computed(() => {
		const u = userState.userInfo || {}
		return [
			{ label: 'mine.follow', value: u.followingCount || 0 },
			{ label: 'mine.fans', value: u.followerCount || 0 },
			{ label: 'mine.likes', value: u.likeCount || 0 },
			{ label: 'mine.points', value: u.points || 0 },
			{ label: 'mine.checkin', value: u.checkinDays || 0 }
		]
	})

	const menuGroups = [
		{
			title: 'mine.groupStudy',
			items: [
				{ icon: 'user-3-line', label: 'mine.profile', url: '/pages/mine/profile', color: 'icon-blue' },
				{ icon: 'notification-3-line', label: 'mine.notify', url: '/pages/mine/notify', color: 'icon-amber' },
				{ icon: 'seedling-line', label: 'mine.growth', url: '/pages/mine/growth', color: 'icon-green' },
				{ icon: 'star-line', label: 'favorites.title', url: '/pages/mine/favorites', color: 'icon-amber' }
			]
		},
		{
			title: 'mine.groupService',
			items: [
				{ icon: 'megaphone-line', label: 'mine.promotion', color: 'icon-pink' },
				{ icon: 'chat-3-line', label: 'mine.help', color: 'icon-cyan' },
				{ icon: 'phone-line', label: 'mine.hotline', hotline: true, color: 'icon-cyan' },
				{ icon: 'map-pin-line', label: 'mine.address', address: true, color: 'icon-red' }
			]
		},
		{
			title: 'mine.groupGeneral',
			items: [
				{ icon: 'file-text-line', label: 'mine.agreement', modal: 'agreement', color: 'icon-gray' },
				{ icon: 'question-answer-line', label: 'mine.about', modal: 'about', color: 'icon-violet' },
				{ icon: 'settings-3-line', label: 'mine.settings', url: '/pages/mine/settings', color: 'icon-gray' }
			]
		}
	]

	const adminMenus = computed(() => {
		const list = [
			{ icon: 'book-2-line', label: 'mine.adminCircle', url: '/pages/admin/circle', color: 'icon-blue' },
			{ icon: 'file-edit-line', label: 'mine.adminJob', url: '/pages/admin/job', color: 'icon-blue' },
			{ icon: 'ruler-line', label: 'mine.adminRule', url: '/pages/admin/rule', color: 'icon-violet' },
			{ icon: 'puzzle-line', label: 'mine.adminTemplate', url: '/pages/admin/template', color: 'icon-cyan' },
			{ icon: 'bar-chart-2-line', label: 'mine.adminData', url: '/pages/admin/data', color: 'icon-green' },
			{ icon: 'robot-line', label: 'mine.adminReadExam', color: 'icon-pink' },
			{ icon: 'cloud-line', label: 'mine.adminCloudExam', color: 'icon-amber' }
		]
		if (superAdmin.value) {
			list.unshift({ icon: 'shield-check-line', label: 'mine.superConsole', url: '/pages/admin/super', color: 'icon-red' })
		}
		return list
	})

	const langLabel = computed(() => i18nState.locale === 'zh' ? t('common.langZh') : t('common.langEn'))

	function switchLang() {
		setLocale(i18nState.locale === 'zh' ? 'en' : 'zh')
	}

	function goProfile() {
		uni.navigateTo({ url: '/pages/mine/profile' })
	}

	function onMenu(m) {
		if (m.url) {
			uni.navigateTo({ url: m.url })
		} else if (m.hotline) {
			showModal({
				title: t('mine.hotline'),
				content: t('mine.hotlineNumber') + '\n' + t('mine.hotlineTime'),
				showCancel: false,
				confirmText: t('common.confirm')
			})
		} else if (m.address) {
			uni.showToast({ title: t('mine.addressTip'), icon: 'none' })
		} else if (m.modal === 'agreement') {
			showModal({ title: t('mine.agreement'), content: t('settings.agreementText'), showCancel: false, confirmText: t('common.confirm') })
		} else if (m.modal === 'about') {
			showModal({ title: t('mine.about'), content: t('settings.aboutText'), showCancel: false, confirmText: t('common.confirm') })
		} else {
			uni.showToast({ title: t('common.building'), icon: 'none' })
		}
	}

	function onLogout() {
		showModal({
			title: t('common.tips'),
			content: t('common.logoutConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: (res) => {
				if (res.confirm) logout()
			}
		})
	}

	onShow(() => {
		fetchUserInfo().catch(() => {})
	})

	onPullDownRefresh(async () => {
		try {
			await fetchUserInfo()
		} catch (e) {}
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx 24rpx 220rpx;
	}

	.user-card {
		position: relative;
		display: flex;
		align-items: center;
		background: var(--brand-gradient);
		border-radius: var(--radius-xl);
		padding: 36rpx 30rpx;
		box-shadow: 0 18rpx 36rpx rgba(46, 74, 107, 0.26);
		overflow: hidden;
	}

	.user-deco {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.deco-1 {
		width: 260rpx;
		height: 260rpx;
		top: -140rpx;
		right: -60rpx;
		background: rgba(255, 255, 255, 0.14);
	}

	.deco-2 {
		width: 160rpx;
		height: 160rpx;
		bottom: -100rpx;
		left: 120rpx;
		background: rgba(255, 255, 255, 0.10);
	}

	.avatar-ring {
		width: 124rpx;
		height: 124rpx;
		border-radius: 50%;
		padding: 4rpx;
		background: rgba(255, 255, 255, 0.35);
		flex-shrink: 0;
		position: relative;
		z-index: 1;
	}

	.u-avatar {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: var(--bg-secondary);
		border: 2rpx solid rgba(255, 255, 255, 0.85);
		box-sizing: border-box;
	}

	.u-main {
		margin-left: 24rpx;
		flex: 1;
		min-width: 0;
		position: relative;
		z-index: 1;
	}

	.u-name {
		font-size: 38rpx;
		font-weight: 700;
		color: #FFFFFF;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.u-position {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
		margin-top: 10rpx;
		display: block;
	}

	.u-edit {
		width: 52rpx;
		height: 52rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		z-index: 1;
		flex-shrink: 0;
	}

	.u-edit-text {
		font-size: 30rpx;
		color: #FFFFFF;
		line-height: 1;
	}

	.stat-row {
		margin-top: -18rpx;
		margin-left: 16rpx;
		margin-right: 16rpx;
		padding: 26rpx 0;
		display: flex;
		border-radius: var(--radius-lg);
	}

	.stat-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat-item.divided {
		border-left: 1rpx solid var(--border);
	}

	.stat-num {
		font-size: 32rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.stat-label {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
	}

	.menu {
		margin-top: 24rpx;
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.menu-group-title {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		font-weight: 600;
		color: var(--text-secondary);
		padding: 24rpx 0 10rpx;
	}

	.menu-group-title::before {
		content: '';
		width: 6rpx;
		height: 24rpx;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		margin-right: 12rpx;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.menu-item:last-child {
		border-bottom: none;
	}

	.menu-item:active {
		opacity: 0.7;
	}

	.menu-label {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		line-height: 1.5;
		color: var(--text-primary);
		margin-left: 20rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.menu-arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-left: 10rpx;
		flex-shrink: 0;
	}

	.menu-value {
		font-size: 25rpx;
		color: var(--text-secondary);
		flex-shrink: 0;
		white-space: nowrap;
	}

	.logout-btn {
		margin-top: 40rpx;
		text-align: center;
		font-size: 28rpx;
		font-weight: 500;
		color: var(--danger);
		background: transparent;
		border: 1rpx solid rgba(179, 69, 59, 0.35);
		border-radius: var(--radius-pill);
		padding: 24rpx 0;
	}

	.logout-btn:active {
		background: rgba(179, 69, 59, 0.08);
	}
</style>
