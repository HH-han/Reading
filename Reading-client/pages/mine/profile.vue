<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 头像 -->
			<view class="form-card glass-card">
				<view class="form-item" @click="chooseAvatar">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="emotion-happy-line" /></view>
					<text class="form-label">{{ $t('profile.avatar') }}</text>
					<view class="form-right">
						<image class="avatar-preview" :src="form.avatar || '/static/logo.png'" mode="aspectFill" />
						<text class="form-tip">{{ $t('profile.tapChange') }}</text>
						<r-icon name="arrow-right-s-line" class="form-arrow" />
					</view>
				</view>
				<view class="form-item">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="quill-pen-fill" /></view>
					<text class="form-label">{{ $t('profile.nickname') }}</text>
					<input class="form-input" v-model="form.nickname" :placeholder="$t('profile.nicknamePh')" placeholder-class="input-ph" />
				</view>
				<view class="form-item">
					<view class="icon-chip icon-chip--sm icon-green"><r-icon name="smartphone-line" /></view>
					<text class="form-label">{{ $t('profile.phone') }}</text>
					<input class="form-input" type="number" v-model="form.phone" :placeholder="$t('profile.phonePh')" placeholder-class="input-ph" />
				</view>
				<view class="form-item" @click="showGender = true">
					<view class="icon-chip icon-chip--sm icon-pink"><r-icon name="genderless-line" /></view>
					<text class="form-label">{{ $t('profile.gender') }}</text>
					<view class="form-right">
						<text class="form-value">{{ genderLabel }}</text>
						<r-icon name="arrow-right-s-line" class="form-arrow" />
					</view>
				</view>
				<view class="form-item">
					<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="map-pin-line" /></view>
					<text class="form-label">{{ $t('profile.address') }}</text>
					<input class="form-input" v-model="form.address" :placeholder="$t('profile.addressPh')" placeholder-class="input-ph" />
				</view>
				<view class="form-item" @click="chooseBg">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="image-line" /></view>
					<text class="form-label">{{ $t('profile.bg') }}</text>
					<view class="form-right">
						<image class="bg-preview" v-if="form.backgroundImg" :src="form.backgroundImg" mode="aspectFill" />
						<text class="form-tip">{{ $t('profile.tapChange') }}</text>
						<r-icon name="arrow-right-s-line" class="form-arrow" />
					</view>
				</view>
			</view>

			<view class="btn-primary save-btn" @click="save">{{ $t('profile.save') }}</view>
		</view>

		<!-- 性别选择 -->
		<view class="ui-sheet-mask" v-if="showGender" @click="showGender = false">
			<view class="ui-sheet-panel" @click.stop>
				<view class="ui-sheet-handle"></view>
				<view class="ui-sheet-head">
					<text class="ui-sheet-title">{{ $t('profile.gender') }}</text>
					<view class="ui-sheet-close" @click="showGender = false"><r-icon name="close-line" /></view>
				</view>
				<view class="gender-item" v-for="g in genders" :key="g.value" @click="pickGender(g.value)">
					<text class="gender-text" :class="{ active: form.gender === g.value }">{{ $t(g.label) }}</text>
					<view class="gender-check" :class="{ checked: form.gender === g.value }">
						<r-icon v-if="form.gender === g.value" name="check-line" class="gender-check-text" />
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState, setUserInfo } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { onLoad } from '@dcloudio/uni-app'
	import { updateUserInfo } from '@/api/user.js'
	import { uploadFile } from '@/api/file.js'

	const form = ref({
		nickname: '',
		avatar: '',
		phone: '',
		gender: 0,
		address: '',
		backgroundImg: ''
	})

	const genders = [
		{ value: 1, label: 'profile.male' },
		{ value: 2, label: 'profile.female' },
		{ value: 0, label: 'profile.unknown' }
	]

	const showGender = ref(false)

	const genderLabel = computed(() => {
		const g = genders.find(x => x.value === form.value.gender)
		return g ? t(g.label) : t('profile.unknown')
	})

	onLoad(() => {
		const u = userState.userInfo || {}
		form.value = {
			nickname: u.nickname || '',
			avatar: u.avatar || '',
			phone: u.phone || '',
			gender: u.gender == null ? 0 : u.gender,
			address: u.address || '',
			backgroundImg: u.backgroundImg || ''
		}
	})

	function pickGender(v) {
		form.value.gender = v
		showGender.value = false
	}

	function chooseAvatar() {
		uni.chooseImage({
			count: 1,
			success: async (res) => {
				const path = res.tempFilePaths[0]
				try {
					const url = await uploadFile(path)
					form.value.avatar = url
				} catch (e) {}
			}
		})
	}

	function chooseBg() {
		uni.chooseImage({
			count: 1,
			success: async (res) => {
				const path = res.tempFilePaths[0]
				try {
					const url = await uploadFile(path)
					form.value.backgroundImg = url
				} catch (e) {}
			}
		})
	}

	async function save() {
		try {
			const data = await updateUserInfo(form.value)
			setUserInfo(data || { ...(userState.userInfo || {}), ...form.value })
			uni.showToast({ title: t('profile.saveSuccess'), icon: 'none' })
			setTimeout(() => uni.navigateBack(), 600)
		} catch (e) {}
	}
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.form-card {
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.form-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.form-item:last-child {
		border-bottom: none;
	}

	.form-label {
		font-size: 27rpx;
		color: var(--text-primary);
		width: 150rpx;
		flex-shrink: 0;
		margin-left: 18rpx;
	}

	.form-input {
		flex: 1;
		font-size: 27rpx;
		color: var(--text-primary);
		text-align: right;
		height: 60rpx;
	}

	.form-right {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.form-value {
		font-size: 27rpx;
		color: var(--text-primary);
	}

	.form-arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-left: 8rpx;
	}

	.form-tip {
		font-size: 22rpx;
		color: var(--primary);
		margin-left: 14rpx;
	}

	.avatar-preview {
		width: 84rpx;
		height: 84rpx;
		border-radius: 50%;
		background: var(--surface-2);
		border: 2rpx solid var(--border);
		box-sizing: border-box;
	}

	.bg-preview {
		width: 140rpx;
		height: 76rpx;
		border-radius: var(--radius-sm);
		background: var(--surface-2);
	}

	.save-btn {
		margin-top: 40rpx;
	}

	.gender-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 28rpx 20rpx;
		border-bottom: 1rpx solid var(--border);
	}

	.gender-item:last-child {
		border-bottom: none;
	}

	.gender-text {
		font-size: 29rpx;
		color: var(--text-primary);
	}

	.gender-text.active {
		color: var(--primary);
		font-weight: 600;
	}

	.gender-check {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		border: 2rpx solid var(--border-strong);
		display: flex;
		align-items: center;
		justify-content: center;
		box-sizing: border-box;
	}

	.gender-check.checked {
		background: var(--brand-gradient);
		border-color: transparent;
	}

	.gender-check-text {
		font-size: 20rpx;
		color: #FFFFFF;
		line-height: 1;
	}
</style>
