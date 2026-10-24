<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="menu glass-card">
				<view class="menu-item">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="contrast-2-line" /></view>
					<view class="menu-main">
						<text class="menu-label">{{ $t('settings.theme') }}</text>
						<text class="menu-sub">{{ themeState.isDark ? $t('settings.themeDark') : $t('settings.themeLight') }}</text>
					</view>
					<theme-switch />
				</view>
				<view class="menu-item" @click="switchLang">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="global-line" /></view>
					<text class="menu-label">{{ $t('settings.language') }}</text>
					<view class="menu-pill">
						<text class="menu-value">{{ langLabel }}</text>
					</view>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<view class="menu glass-card">
				<view class="menu-item" @click="showText('agreement')">
					<view class="icon-chip icon-chip--sm icon-gray"><r-icon name="file-text-line" /></view>
					<text class="menu-label">{{ $t('settings.agreement') }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
				<view class="menu-item" @click="showText('about')">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="question-answer-line" /></view>
					<text class="menu-label">{{ $t('settings.about') }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<view class="menu glass-card" v-if="superAdmin">
				<view class="menu-item" @click="onCancel">
					<view class="icon-chip icon-chip--sm icon-red"><r-icon name="delete-bin-line" /></view>
					<text class="menu-label danger">{{ $t('settings.cancelAccount') }}</text>
					<r-icon name="arrow-right-s-line" class="menu-arrow" />
				</view>
			</view>

			<view class="logout-btn" @click="onLogout">{{ $t('settings.logout') }}</view>
		</view>

		<r-modal />
	</view>
</template>

<script setup>
	import { computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { logout, clearSession, hasRole } from '@/store/user.js'
	import { t, i18nState, setLocale } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { cancelAccount } from '@/api/admin.js'
	import ThemeSwitch from '@/components/theme-switch/theme-switch.vue'

	const superAdmin = computed(() => hasRole(['SUPER_ADMIN']))
	const langLabel = computed(() => i18nState.locale === 'zh' ? t('common.langZh') : t('common.langEn'))

	function switchLang() {
		setLocale(i18nState.locale === 'zh' ? 'en' : 'zh')
	}

	function showText(type) {
		showModal({
			title: t(type === 'agreement' ? 'settings.agreement' : 'settings.about'),
			content: t(type === 'agreement' ? 'settings.agreementText' : 'settings.aboutText'),
			showCancel: false,
			confirmText: t('common.confirm')
		})
	}

	function onCancel() {
		showModal({
			title: t('common.tips'),
			content: t('settings.cancelConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await cancelAccount()
					uni.showToast({ title: t('settings.cancelSuccess'), icon: 'none' })
					setTimeout(() => clearSession(), 800)
				} catch (e) {}
			}
		})
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
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.menu {
		margin-top: 24rpx;
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.menu:first-child {
		margin-top: 0;
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

	.menu-main {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
		display: flex;
		flex-direction: column;
	}

	.menu-label {
		flex: 1;
		font-size: 27rpx;
		color: var(--text-primary);
		margin-left: 20rpx;
	}

	.menu-main .menu-label {
		flex: none;
		margin-left: 0;
	}

	.menu-sub {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.menu-label.danger {
		color: var(--danger);
	}

	.menu-pill {
		background: var(--surface-2);
		border-radius: var(--radius-pill);
		padding: 6rpx 16rpx;
	}

	.menu-arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-left: 10rpx;
	}

	.menu-value {
		font-size: 23rpx;
		color: var(--text-secondary);
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
