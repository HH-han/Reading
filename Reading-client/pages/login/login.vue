<template>
	<view class="page login-page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="bg-orb orb-a"></view>
		<view class="bg-orb orb-b"></view>

		<view class="login-wrap">
			<view class="logo-area">
				<view class="logo-tile">
					<image class="logo" src="/static/logo.png" mode="aspectFit" />
				</view>
				<text class="app-name">{{ $t('login.title') }}</text>
				<text class="app-sub">{{ $t('login.subtitle') }}</text>
			</view>

			<view class="form-card glass-card">
				<view class="form-group">
					<text class="form-label">{{ $t('login.username') }}</text>
					<view class="input-wrap">
						<r-icon name="user-3-line" class="input-icon" />
						<input
							class="input-item has-icon"
							v-model="username"
							type="text"
							:placeholder="$t('login.usernamePh')"
							placeholder-class="input-ph"
						/>
					</view>
				</view>
				<view class="form-group pwd">
					<text class="form-label">{{ $t('login.password') }}</text>
					<view class="pwd-row">
						<r-icon name="lock-2-line" class="input-icon" />
						<input
							class="input-item has-icon pwd-row__input"
							v-model="password"
							:type="showPassword ? 'text' : 'password'"
							:placeholder="$t('login.passwordPh')"
							placeholder-class="input-ph"
						/>
						<view class="eye-btn" @click="showPassword = !showPassword">
							<r-icon :name="showPassword ? 'eye-line' : 'eye-close-line'" class="eye-icon" />
						</view>
					</view>
				</view>
				<view class="btn-primary login-btn" :class="{ loading }" @click="doLogin">
					{{ loading ? $t('login.logging') : $t('login.loginBtn') }}
				</view>
				<view class="to-register" @click="goRegister">
					{{ $t('login.toRegister') }}
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { setLogin } from '@/store/user.js'
	import { login } from '@/api/auth.js'
	import { t } from '@/utils/i18n.js'
	import { onShow } from '@dcloudio/uni-app'

	const username = ref('')
	const password = ref('')
	const loading = ref(false)
	const showPassword = ref(false)

	onShow(() => {
		if (uni.getStorageSync('satoken')) {
			uni.reLaunch({ url: homeUrl(uni.getStorageSync('userInfo')) })
		}
	})

	function homeUrl(user) {
		return user && user.role === 'SUPER_ADMIN' ? '/pages/admin/super' : '/pages/index/index'
	}

	async function doLogin() {
		if (!username.value.trim() || !password.value.trim()) {
			uni.showToast({ title: t('login.emptyTip'), icon: 'none' })
			return
		}
		loading.value = true
		try {
			const data = await login({ username: username.value.trim(), password: password.value.trim() })
			setLogin(data.token, data.user)
			uni.reLaunch({ url: homeUrl(data.user) })
		} catch (e) {} finally {
			loading.value = false
		}
	}

	function goRegister() {
		uni.navigateTo({ url: '/pages/login/register' })
	}
</script>

<style lang="scss" scoped>
	.login-page {
		position: relative;
		overflow: hidden;
	}

	.bg-orb {
		position: absolute;
		border-radius: 50%;
		filter: blur(60rpx);
		opacity: 0.7;
		pointer-events: none;
	}

	.orb-a {
		width: 460rpx;
		height: 460rpx;
		top: -140rpx;
		right: -160rpx;
		background: var(--glow-1);
	}

	.orb-b {
		width: 380rpx;
		height: 380rpx;
		bottom: 60rpx;
		left: -160rpx;
		background: var(--glow-2);
	}

	.login-wrap {
		position: relative;
		z-index: 1;
		padding: 120rpx 48rpx 80rpx;
	}

	.logo-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 56rpx;
	}

	.logo-tile {
		width: 168rpx;
		height: 168rpx;
		border-radius: 48rpx;
		background: var(--bg-secondary);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 24rpx 48rpx rgba(27, 32, 48, 0.12);
		border: 1rpx solid var(--glass-border);
	}

	.logo {
		width: 112rpx;
		height: 112rpx;
	}

	.app-name {
		font-size: 46rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		color: var(--text-primary);
		margin-top: 28rpx;
	}

	.app-sub {
		font-size: 24rpx;
		color: var(--text-secondary);
		margin-top: 12rpx;
	}

	.form-card {
		padding: 44rpx 36rpx;
		border-radius: var(--radius-xl);
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.form-label {
		font-size: 24rpx;
		font-weight: 500;
		color: var(--text-secondary);
		padding-left: 4rpx;
	}

	.input-wrap,
	.pwd-row {
		position: relative;
	}

	.input-icon {
		position: absolute;
		left: 26rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 26rpx;
		z-index: 2;
		opacity: 0.75;
	}

	.pwd-row .input-icon {
		z-index: 2;
	}

	.has-icon {
		padding-left: 76rpx;
	}

	.pwd {
		margin-top: 32rpx;
	}

	.pwd-row__input {
		padding-right: 100rpx;
	}

	.eye-btn {
		position: absolute;
		right: 12rpx;
		top: 50%;
		transform: translateY(-50%);
		width: 64rpx;
		height: 64rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 2;
	}

	.eye-btn:active {
		background: var(--surface-3);
	}

	.eye-icon {
		font-size: 28rpx;
	}

	.login-btn {
		margin-top: 44rpx;
	}

	.login-btn.loading {
		opacity: 0.75;
	}

	.to-register {
		margin-top: 32rpx;
		text-align: center;
		font-size: 26rpx;
		color: var(--text-secondary);
	}
</style>
