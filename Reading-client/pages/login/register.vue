<template>
	<view class="page register-page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="bg-orb orb-a"></view>
		<view class="bg-orb orb-b"></view>

		<view class="register-wrap">
			<view class="logo-area">
				<view class="logo-tile">
					<image class="logo" src="/static/logo.png" mode="aspectFit" />
				</view>
				<text class="app-name">{{ $t('register.title') }}</text>
				<text class="app-sub">{{ $t('register.subtitle') }}</text>
			</view>

			<view class="form-card glass-card">
				<view class="form-group">
					<text class="form-label">{{ $t('register.username') }}</text>
					<view class="input-wrap">
						<r-icon name="user-3-line" class="input-icon" />
						<input
							class="input-item has-icon"
							v-model="username"
							type="text"
							:placeholder="$t('register.usernamePh')"
							placeholder-class="input-ph"
						/>
					</view>
				</view>
				<view class="form-group pwd">
					<text class="form-label">{{ $t('register.password') }}</text>
					<view class="pwd-row">
						<r-icon name="lock-2-line" class="input-icon" />
						<input
							class="input-item has-icon pwd-row__input"
							v-model="password"
							:type="showPassword ? 'text' : 'password'"
							:placeholder="$t('register.passwordPh')"
							placeholder-class="input-ph"
						/>
						<view class="eye-btn" @click="showPassword = !showPassword">
							<r-icon :name="showPassword ? 'eye-line' : 'eye-close-line'" class="eye-icon" />
						</view>
					</view>
				</view>
				<view class="form-group pwd">
					<text class="form-label">{{ $t('register.confirmPassword') }}</text>
					<view class="pwd-row">
						<r-icon name="lock-password-line" class="input-icon" />
						<input
							class="input-item has-icon pwd-row__input"
							v-model="confirmPassword"
							:type="showConfirmPassword ? 'text' : 'password'"
							:placeholder="$t('register.confirmPasswordPh')"
							placeholder-class="input-ph"
						/>
						<view class="eye-btn" @click="showConfirmPassword = !showConfirmPassword">
							<r-icon :name="showConfirmPassword ? 'eye-line' : 'eye-close-line'" class="eye-icon" />
						</view>
					</view>
				</view>
				<view class="btn-primary register-btn" :class="{ loading }" @click="doRegister">
					{{ loading ? $t('register.registering') : $t('register.registerBtn') }}
				</view>
				<view class="to-login" @click="goLogin">
					<text class="to-login-text">{{ $t('register.toLogin') }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { setLogin, userState } from '@/store/user.js'
	import { register } from '@/api/auth.js'
	import { t } from '@/utils/i18n.js'
	import { onShow } from '@dcloudio/uni-app'

	const username = ref('')
	const password = ref('')
	const confirmPassword = ref('')
	const loading = ref(false)
	const showPassword = ref(false)
	const showConfirmPassword = ref(false)

	onShow(() => {
		if (userState.token) {
			uni.reLaunch({ url: '/pages/index/index' })
		}
	})

	async function doRegister() {
		const u = username.value.trim()
		const p = password.value
		const cp = confirmPassword.value
		if (!u || !p || !cp) {
			uni.showToast({ title: t('register.emptyTip'), icon: 'none' })
			return
		}
		if (u.length < 3 || u.length > 32) {
			uni.showToast({ title: t('register.usernameInvalid'), icon: 'none' })
			return
		}
		if (p.length < 6 || p.length > 32) {
			uni.showToast({ title: t('register.passwordInvalid'), icon: 'none' })
			return
		}
		if (p !== cp) {
			uni.showToast({ title: t('register.pwdNotMatch'), icon: 'none' })
			return
		}
		loading.value = true
		try {
			const data = await register({ username: u, password: p })
			uni.showToast({ title: t('register.success'), icon: 'success' })
			setLogin(data.token, data.user)
			setTimeout(() => {
				uni.reLaunch({ url: '/pages/index/index' })
			}, 600)
		} catch (e) {} finally {
			loading.value = false
		}
	}

	function goLogin() {
		uni.navigateBack()
	}
</script>

<style lang="scss" scoped>
	.register-page {
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
		top: -160rpx;
		right: -160rpx;
		background: var(--glow-1);
	}

	.orb-b {
		width: 380rpx;
		height: 380rpx;
		bottom: 40rpx;
		left: -160rpx;
		background: var(--glow-2);
	}

	.register-wrap {
		position: relative;
		z-index: 1;
		padding: 100rpx 48rpx 60rpx;
	}

	.logo-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 48rpx;
	}

	.logo-tile {
		width: 152rpx;
		height: 152rpx;
		border-radius: 44rpx;
		background: var(--bg-secondary);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 24rpx 48rpx rgba(27, 32, 48, 0.12);
		border: 1rpx solid var(--glass-border);
	}

	.logo {
		width: 104rpx;
		height: 104rpx;
	}

	.app-name {
		font-size: 44rpx;
		font-weight: 700;
		letter-spacing: 1rpx;
		color: var(--text-primary);
		margin-top: 26rpx;
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

	.pwd {
		margin-top: 30rpx;
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

	.has-icon {
		padding-left: 76rpx;
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

	.register-btn {
		margin-top: 42rpx;
	}

	.register-btn.loading {
		opacity: 0.75;
	}

	.to-login {
		margin-top: 30rpx;
		display: flex;
		justify-content: center;
	}

	.to-login-text {
		font-size: 25rpx;
		color: var(--primary);
		font-weight: 500;
		padding: 10rpx 24rpx;
		border-radius: var(--radius-pill);
		background: var(--primary-soft);
	}
</style>
