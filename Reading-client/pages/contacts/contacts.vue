<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 我的读书圈（可加入多个） -->
			<view class="my-circles" v-if="myCircles.length">
				<view class="section-head">
					<text class="section-title">{{ $t('contacts.myCircles') }}</text>
					<text class="section-count">{{ $t('contacts.circleTotal', { n: myCircles.length }) }}</text>
				</view>
				<view
					class="my-circle glass-card"
					v-for="c in myCircles"
					:key="c.id"
					:class="{ 'is-current': c.current }"
					@click="exitOne(c)"
				>
					<view class="circle-logo">
						<r-icon name="building-4-line" class="circle-logo-text" color="#FFFFFF" />
					</view>
					<view class="my-main">
						<view class="my-name-row">
							<text class="my-name">{{ c.name }}</text>
							<view class="my-tag" v-if="c.current">{{ $t('contacts.current') }}</view>
						</view>
						<text class="my-sub">
							{{ c.description || '-' }} · {{ $t('contacts.memberCount', { n: c.memberCount || 0 }) }}
						</text>
					</view>
					<view class="my-view" v-if="!c.current" @click.stop="switchTo(c)">{{ $t('contacts.view') }}</view>
					<view class="my-exit" @click.stop="exitOne(c)">{{ $t('contacts.exit') }}</view>
				</view>
			</view>

			<view class="empty-box" v-if="!loading && !myCircles.length">
				<r-icon name="group-2-line" class="empty-icon" />
				<text>{{ $t('contacts.joinTip') }}</text>
			</view>

			<!-- 加入已有读书圈（无论是否已加入都展示全站可加入的读书圈） -->
			<view class="join-card glass-card" v-if="!loading">
				<view class="join-head">
					<view class="icon-chip icon-chip--sm icon-green"><r-icon name="book-2-line" /></view>
					<text class="join-title">{{ $t('contacts.joinTitle') }}</text>
				</view>
				<view class="join-row">
					<input
						class="input-item join-input"
						v-model="inviteCode"
						:placeholder="$t('contacts.invitePh')"
						placeholder-class="input-ph"
					/>
					<view class="btn-primary join-btn" @click="joinByCode">{{ $t('contacts.join') }}</view>
				</view>
				<view class="join-sub">{{ $t('contacts.availableTitle') }}</view>
				<view class="cj-item" v-for="c in availableList" :key="c.id" @click="joinById(c)">
					<view class="cj-main">
						<text class="cj-name">{{ c.name }}</text>
						<text class="cj-desc" v-if="c.description">{{ c.description }}</text>
					</view>
					<view class="cj-right">
						<text class="cj-count">{{ $t('contacts.memberCount', { n: c.memberCount || 0 }) }}</text>
						<view class="cj-btn">{{ $t('contacts.join') }}</view>
					</view>
				</view>
				<view class="join-empty" v-if="!availableList.length">
					<text>{{ $t('contacts.noAvailable') }}</text>
				</view>
			</view>

			<!-- 小组 → 成员 -->
			<view class="group-block" v-for="g in groupList" :key="g.id">
				<view class="group-head glass">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="team-line" /></view>
					<text class="group-name">{{ g.name }}</text>
					<view class="group-count-pill">
						<text class="group-count">{{ $t('contacts.memberCount', { n: (g.members || []).length }) }}</text>
					</view>
				</view>
				<view class="member-list glass-card">
					<view class="empty-box" v-if="!(g.members || []).length">
						<text>{{ $t('contacts.empty') }}</text>
					</view>
					<view class="member-item" v-for="m in g.members" :key="m.id">
						<view class="m-avatar-ring">
							<image class="m-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
						</view>
						<view class="m-main">
							<view class="m-name-row">
								<text class="m-name">{{ m.nickname || '-' }}</text>
								<view class="m-leader" v-if="g.leaderId && m.id === g.leaderId">
									<r-icon name="star-fill" class="m-leader-text" />
									<text class="m-leader-text">{{ $t('contacts.leader') }}</text>
								</view>
							</view>
							<text class="m-position">{{ m.position || '' }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 无小组时的扁平成员列表 -->
			<view class="member-list glass-card" v-if="circle.id && !groupList.length && flatMembers.length">
				<view class="member-item" v-for="m in flatMembers" :key="m.id">
					<view class="m-avatar-ring">
						<image class="m-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
					</view>
					<view class="m-main">
						<text class="m-name">{{ m.nickname || '-' }}</text>
						<text class="m-position">{{ m.position || '' }}</text>
					</view>
				</view>
			</view>
		</view>

		<float-tabbar :current="1" />
		<r-modal />
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { fetchUserInfo } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { getMyCircles, getGroups, getMembers, getAvailableCircles, joinCircle, joinCircleById, switchCircle, exitCircleById } from '@/api/circle.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import FloatTabbar from '@/components/float-tabbar/float-tabbar.vue'

	const circle = ref({})
	const myCircles = ref([])
	const groupList = ref([])
	const flatMembers = ref([])
	const availableList = ref([])
	const inviteCode = ref('')
	const loading = ref(false)

	async function load() {
		loading.value = true
		groupList.value = []
		flatMembers.value = []
		try {
			const list = await getMyCircles()
			myCircles.value = Array.isArray(list) ? list : (list.records || [])
		} catch (e) {
			myCircles.value = []
		}
		if (!myCircles.value.length) {
			circle.value = {}
			await loadAvailable()
			loading.value = false
			return
		}
		// 当前使用的读书圈
		const active = myCircles.value.find(c => c.current) || myCircles.value[0]
		circle.value = { ...active }
		await loadAvailable()
		try {
			const groups = await getGroups()
			const list = Array.isArray(groups) ? groups : (groups.records || [])
			for (const g of list) {
				try {
					const ms = await getMembers({ groupId: g.id })
					g.members = Array.isArray(ms) ? ms : (ms.records || [])
				} catch (e) {
					g.members = []
				}
			}
			groupList.value = list
			if (!list.length) {
				const ms = await getMembers({})
				flatMembers.value = Array.isArray(ms) ? ms : (ms.records || [])
			}
		} catch (e) {} finally {
			loading.value = false
		}
	}

	async function loadAvailable() {
		try {
			const data = await getAvailableCircles()
			availableList.value = (Array.isArray(data) ? data : (data.records || [])).filter(c => !c.joined)
		} catch (e) {
			availableList.value = []
		}
	}

	async function joinByCode() {
		const code = inviteCode.value.trim()
		if (!code) {
			uni.showToast({ title: t('contacts.invitePh'), icon: 'none' })
			return
		}
		try {
			await joinCircle({ inviteCode: code })
			uni.showToast({ title: t('contacts.joinSuccess'), icon: 'success' })
			inviteCode.value = ''
			await fetchUserInfo().catch(() => {})
			load()
		} catch (e) {}
	}

	function joinById(c) {
		showModal({
			title: t('contacts.joinTitle'),
			content: t('contacts.joinConfirm', { name: c.name }),
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await joinCircleById(c.id)
					uni.showToast({ title: t('contacts.joinSuccess'), icon: 'success' })
					await fetchUserInfo().catch(() => {})
					load()
				} catch (e) {}
			}
		})
	}

	// 切换为当前使用的读书圈
	async function switchTo(c) {
		if (c.current) return
		try {
			await switchCircle(c.id)
			await fetchUserInfo().catch(() => {})
			await load()
			uni.showToast({ title: t('contacts.switchSuccess'), icon: 'none' })
		} catch (e) {}
	}

	// 退出指定的读书圈
	function exitOne(c) {
		showModal({
			title: t('common.tips'),
			content: t('contacts.exitConfirm', { name: c.name }),
			type: 'danger',
			confirmText: t('contacts.exit'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await exitCircleById(c.id)
					uni.showToast({ title: t('contacts.exitSuccess'), icon: 'success' })
					await fetchUserInfo().catch(() => {})
					await load()
				} catch (e) {}
			}
		})
	}

	onShow(async () => {
		// 先同步最新的圈子归属，避免本地缓存的 circleId 过期导致已加入却显示加入卡片
		await fetchUserInfo().catch(() => {})
		load()
	})

	onPullDownRefresh(async () => {
		await fetchUserInfo().catch(() => {})
		await load()
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx 24rpx 220rpx;
	}

	.circle-logo {
		width: 92rpx;
		height: 92rpx;
		border-radius: 28rpx;
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 10rpx 22rpx rgba(46, 74, 107, 0.28);
		flex-shrink: 0;
	}

	.circle-logo-text {
		font-size: 42rpx;
		line-height: 1;
	}

	.section-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		padding: 4rpx 8rpx 16rpx;
	}

	.section-title {
		font-size: 29rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.section-count {
		font-size: 23rpx;
		color: var(--text-tertiary);
	}

	.my-circle {
		display: flex;
		align-items: center;
		padding: 26rpx;
		border-radius: var(--radius-lg);
		margin-bottom: 18rpx;
		background: var(--brand-gradient-soft);
	}

	.my-circle.is-current {
		border: 1rpx solid var(--border-strong);
	}

	.my-main {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
	}

	.my-name-row {
		display: flex;
		align-items: center;
	}

	.my-name {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.my-tag {
		flex-shrink: 0;
		font-size: 19rpx;
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 2rpx 12rpx;
		margin-left: 12rpx;
	}

	.my-sub {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.my-view {
		flex-shrink: 0;
		font-size: 23rpx;
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 12rpx;
	}

	.my-exit {
		flex-shrink: 0;
		font-size: 23rpx;
		color: var(--danger);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 12rpx;
	}

	.group-block {
		margin-top: 28rpx;
	}

	.group-head {
		display: flex;
		align-items: center;
		border-radius: var(--radius-md);
		padding: 18rpx 24rpx;
	}

	.group-name {
		flex: 1;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		margin-left: 14rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.group-count-pill {
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 6rpx 18rpx;
		flex-shrink: 0;
	}

	.group-count {
		font-size: 21rpx;
		color: var(--text-secondary);
	}

	.member-list {
		margin-top: 16rpx;
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.member-item {
		display: flex;
		align-items: center;
		padding: 20rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.member-item:last-child {
		border-bottom: none;
	}

	.m-avatar-ring {
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		padding: 3rpx;
		background: var(--brand-gradient-soft);
		flex-shrink: 0;
	}

	.m-avatar {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: var(--bg-secondary);
		border: 2rpx solid var(--bg-secondary);
		box-sizing: border-box;
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
	}

	.m-leader {
		background: rgba(180, 101, 46, 0.14);
		border-radius: var(--radius-pill);
		padding: 2rpx 12rpx;
		margin-left: 12rpx;
	}

	.m-leader-text {
		font-size: 19rpx;
		color: var(--warning);
	}

	.m-position {
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		display: block;
	}

	.join-card {
		margin-top: 24rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
	}

	.join-head {
		display: flex;
		align-items: center;
	}

	.join-title {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
		font-size: 29rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.join-row {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
	}

	.join-input {
		flex: 1;
		min-width: 0;
	}

	.join-btn {
		flex-shrink: 0;
		width: 150rpx;
		height: 96rpx;
		line-height: 96rpx;
		padding: 0;
		margin-left: 16rpx;
		font-size: 28rpx;
	}

	.join-sub {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--text-secondary);
		margin-top: 32rpx;
	}

	.cj-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.cj-item:last-child {
		border-bottom: none;
	}

	.cj-main {
		flex: 1;
		min-width: 0;
	}

	.cj-name {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.cj-desc {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.cj-right {
		display: flex;
		align-items: center;
		flex-shrink: 0;
		margin-left: 16rpx;
	}

	.cj-count {
		font-size: 21rpx;
		color: var(--text-tertiary);
	}

	.cj-btn {
		font-size: 23rpx;
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border-strong);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 16rpx;
	}

	.join-empty {
		padding: 24rpx 0 8rpx;
		text-align: center;
		font-size: 24rpx;
		color: var(--text-tertiary);
	}
</style>
