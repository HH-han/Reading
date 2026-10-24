<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="btn-primary invite-entry" v-if="circleId" @click="openInvite">
				<r-icon name="add-line" class="invite-entry__icon" />
				<text>{{ $t('admin.circle.inviteMember') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="group-2-line" class="empty-icon" />
				<text>{{ $t('circlePage.empty') }}</text>
			</view>

			<view class="member-list glass-card" v-else>
				<view class="member-item" v-for="m in list" :key="m.id">
					<view class="m-avatar-wrap">
						<image class="m-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
					</view>
					<view class="m-main">
						<view class="m-name-row">
							<text class="m-name">{{ m.nickname || m.username || '-' }}</text>
							<view class="m-leader" v-if="leaderId && m.id === leaderId">
								<r-icon name="star-fill" />
								<text>{{ $t('circlePage.leader') }}</text>
							</view>
						</view>
						<text class="m-position">{{ m.phone || m.position || '' }}</text>
					</view>
					<view class="m-remove" @click="onRemove(m)">
						<text class="m-remove-text">{{ $t('circlePage.remove') }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 邀请成员弹层 -->
		<view class="ui-modal-mask" v-if="showInvite" @click="showInvite = false">
			<view class="ui-modal-card invite-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.circle.inviteTitle') }}</text>
					<view class="ui-sheet-close" @click="showInvite = false"><r-icon name="close-line" /></view>
				</view>
				<view class="search-box">
					<r-icon name="search-line" class="search-icon" />
					<input
						class="search-input"
						v-model="keyword"
						:placeholder="$t('admin.circle.invitePh')"
						placeholder-class="input-ph"
						@input="onSearchInput"
					/>
				</view>
				<scroll-view scroll-y class="user-scroll">
					<view class="member-item" v-for="u in users" :key="u.id" @click="invite(u)">
						<view class="m-avatar-wrap">
							<image class="m-avatar" :src="u.avatar || '/static/logo.png'" mode="aspectFill" />
						</view>
						<view class="m-main">
							<text class="m-name">{{ u.nickname || u.username || '-' }}</text>
							<text class="m-position">{{ u.phone || u.position || '-' }}</text>
						</view>
						<view class="member-state" v-if="u.circleId === circleId">
							{{ $t('admin.circle.inCircle') }}
						</view>
						<view class="member-invite" v-else>{{ $t('admin.circle.inviteAction') }}</view>
					</view>
					<view class="row-empty" v-if="!users.length">
						<text>{{ userLoading ? $t('common.loading') : $t('admin.circle.noUser') }}</text>
					</view>
				</scroll-view>
			</view>
		</view>

		<r-modal />
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getMembers } from '@/api/circle.js'
	import { getCircleMembers, inviteCircleMember, removeCircleMember, getUserPage } from '@/api/admin.js'

	const list = ref([])
	const leaderId = ref(null)
	const loading = ref(false)
	const groupId = ref(null)
	const circleId = ref(null)

	const showInvite = ref(false)
	const users = ref([])
	const userLoading = ref(false)
	const keyword = ref('')
	let searchTimer = null

	async function load() {
		loading.value = true
		try {
			if (circleId.value) {
				// 管理员通过后台接口读取圈子全量成员
				const data = await getCircleMembers(circleId.value, { current: 1, size: 100 })
				list.value = Array.isArray(data) ? data : (data.records || [])
				return
			}
			const params = {}
			if (groupId.value) params.groupId = groupId.value
			else if (userState.userInfo && userState.userInfo.groupId) params.groupId = userState.userInfo.groupId
			const data = await getMembers(params)
			list.value = Array.isArray(data) ? data : (data.records || [])
			if (data && data.leaderId) leaderId.value = data.leaderId
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function onRemove(m) {
		if (!circleId.value) {
			uni.showToast({ title: t('circlePage.removeTip'), icon: 'none' })
			return
		}
		showModal({
			title: t('common.tips'),
			content: t('admin.circle.removeConfirm', { name: m.nickname || m.username || '' }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await removeCircleMember(circleId.value, m.id)
					uni.showToast({ title: t('admin.circle.removeSuccess'), icon: 'none' })
					load()
				} catch (e) {}
			}
		})
	}

	function openInvite() {
		showInvite.value = true
		keyword.value = ''
		loadUsers()
	}

	function onSearchInput() {
		clearTimeout(searchTimer)
		searchTimer = setTimeout(loadUsers, 300)
	}

	async function loadUsers() {
		userLoading.value = true
		try {
			const data = await getUserPage({ keyword: keyword.value.trim(), current: 1, size: 100 })
			users.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			users.value = []
		} finally {
			userLoading.value = false
		}
	}

	async function invite(u) {
		if (u.circleId === circleId.value) return
		try {
			await inviteCircleMember(circleId.value, u.id)
			uni.showToast({ title: t('admin.circle.inviteSuccess'), icon: 'none' })
			loadUsers()
			load()
		} catch (e) {}
	}

	onLoad((query) => {
		if (query && query.groupId) groupId.value = query.groupId
		if (query && query.leaderId) leaderId.value = Number(query.leaderId)
		if (query && query.circleId) circleId.value = Number(query.circleId)
		load()
	})

	onPullDownRefresh(async () => {
		await load()
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.member-list {
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.member-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.member-item:last-child {
		border-bottom: none;
	}

	.m-avatar-wrap {
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		padding: 3rpx;
		background: var(--brand-gradient-soft);
		flex-shrink: 0;
	}

	.m-avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: var(--bg-secondary);
		border: 2rpx solid var(--bg-secondary);
		box-sizing: border-box;
		display: block;
	}

	.m-main {
		margin-left: 20rpx;
		flex: 1;
		min-width: 0;
	}

	.m-name-row {
		display: flex;
		align-items: center;
	}

	.m-name {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.m-leader {
		font-size: 19rpx;
		color: var(--warning);
		background: rgba(180, 101, 46, 0.14);
		border-radius: var(--radius-pill);
		padding: 2rpx 12rpx;
		margin-left: 12rpx;
		flex-shrink: 0;
	}

	.m-position {
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		display: block;
	}

	.m-remove {
		padding: 8rpx 24rpx;
		border-radius: var(--radius-pill);
		background: rgba(179, 69, 59, 0.08);
		border: 1rpx solid rgba(179, 69, 59, 0.28);
		flex-shrink: 0;
	}

	.m-remove-text {
		font-size: 22rpx;
		color: var(--danger);
	}

	.invite-entry {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 24rpx;
	}

	.invite-entry__icon {
		font-size: 30rpx;
		margin-right: 8rpx;
	}

	.invite-modal {
		padding: 32rpx 28rpx 28rpx;
	}

	.modal-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8rpx;
	}

	.modal-head .ui-modal-title {
		text-align: left;
		font-size: 32rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		margin-top: 20rpx;
		padding: 0 20rpx;
		height: 76rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
	}

	.search-icon {
		font-size: 26rpx;
		color: var(--text-tertiary);
	}

	.search-input {
		flex: 1;
		height: 100%;
		font-size: 26rpx;
		color: var(--text-primary);
		margin-left: 12rpx;
	}

	.user-scroll {
		max-height: 56vh;
		margin-top: 16rpx;
	}

	.user-scroll .member-item {
		padding: 18rpx 0;
	}

	.member-invite {
		flex-shrink: 0;
		font-size: 23rpx;
		color: var(--primary);
		background: var(--surface-2);
		border: 1rpx solid var(--border-strong);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 16rpx;
	}

	.member-state {
		flex-shrink: 0;
		font-size: 22rpx;
		color: var(--text-tertiary);
		margin-left: 16rpx;
	}

	.row-empty {
		padding: 40rpx 0;
		text-align: center;
		font-size: 24rpx;
		color: var(--text-tertiary);
	}
</style>
