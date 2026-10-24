<template>
	<view class="circle-panel">
		<view class="empty-box" v-if="!loading && !circles.length">
			<r-icon name="group-2-line" class="empty-icon" />
			<text>{{ $t('index.circleEmpty') }}</text>
		</view>

		<template v-else>
			<view class="panel-head">
				<text class="panel-title">{{ $t('index.circleMine') }}</text>
				<text class="panel-count">{{ $t('index.circleCount', { n: circles.length }) }}</text>
			</view>

			<!-- 已加入的读书圈：信息与退出操作放在同一个容器内 -->
			<view
				class="circle-card glass-card"
				v-for="c in circles"
				:key="c.id"
				:class="{ 'is-current': c.current }"
			>
				<view class="circle-hero">
					<view class="circle-logo">
						<r-icon name="building-4-line" class="circle-logo-text" color="#FFFFFF" />
					</view>
					<view class="circle-title-wrap">
						<view class="circle-name-row">
							<text class="circle-name">{{ c.name || '-' }}</text>
							<view class="current-tag" v-if="c.current">{{ $t('index.circleCurrent') }}</view>
						</view>
						<text class="circle-sub">{{ c.description || c.name || '-' }}</text>
					</view>
					<view class="member-pill">
						<r-icon name="group-2-line" class="member-icon" />
						<text class="member-num">{{ c.memberCount || 0 }}</text>
					</view>
				</view>

				<view class="circle-rows">
					<view class="circle-row">
						<text class="circle-label">{{ $t('index.circleDept') }}</text>
						<text class="circle-value">{{ c.name || '-' }}</text>
					</view>
					<view class="circle-row">
						<text class="circle-label">{{ $t('index.circleOwner') }}</text>
						<text class="circle-value">{{ c.ownerName || '-' }}</text>
					</view>
				</view>

				<view class="circle-desc" v-if="c.description">{{ c.description }}</view>

				<view class="circle-foot">
					<view class="foot-state">
						<view class="state-dot" :class="{ 'state-dot--current': c.current }"></view>
						<text class="state-text">{{ c.current ? $t('index.circleUsing') : $t('index.circleJoined') }}</text>
					</view>
					<view class="foot-actions">
						<view class="foot-btn foot-btn--manage" v-if="canManage" @click="goMembers(c)">
							<r-icon name="group-2-line" class="foot-btn-icon" />
							<text>{{ $t('index.circleManage') }}</text>
						</view>
						<view class="foot-btn foot-btn--danger" @click="exitOne(c)">
							<r-icon name="close-line" class="foot-btn-icon" />
							<text>{{ $t('index.circleExit') }}</text>
						</view>
					</view>
				</view>
			</view>
		</template>

		<r-modal />
	</view>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { getMyCircles, exitCircleById } from '@/api/circle.js'
	import { fetchUserInfo, isAdmin } from '@/store/user.js'

	const circles = ref([])
	const loading = ref(false)

	// 仅管理员/超级管理员可见成员管理入口
	const canManage = computed(() => isAdmin())

	async function load() {
		loading.value = true
		try {
			const data = await getMyCircles()
			circles.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			circles.value = []
		} finally {
			loading.value = false
		}
	}

	function exitOne(c) {
		showModal({
			title: t('common.tips'),
			content: t('index.exitConfirm', { name: c.name }),
			type: 'danger',
			confirmText: t('index.circleExit'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await exitCircleById(c.id)
					uni.showToast({ title: t('index.exitSuccess'), icon: 'success' })
					await fetchUserInfo().catch(() => {})
					await load()
				} catch (e) {}
			}
		})
	}

	function goMembers(c) {
		uni.navigateTo({ url: '/pages/circle/members' + (c.id ? '?circleId=' + c.id : '') })
	}

	onMounted(load)

	defineExpose({
		refresh: load
	})
</script>

<style lang="scss" scoped>
	.panel-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		padding: 4rpx 8rpx 20rpx;
	}

	.panel-title {
		font-size: 29rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.panel-count {
		font-size: 23rpx;
		color: var(--text-tertiary);
	}

	.circle-card {
		border-radius: var(--radius-lg);
		overflow: hidden;
		margin-bottom: 24rpx;
		border: 1rpx solid var(--border);
	}

	.circle-card.is-current {
		border-color: var(--primary);
		box-shadow: var(--shadow);
	}

	.circle-hero {
		display: flex;
		align-items: center;
		padding: 28rpx;
		background: var(--surface-2);
		border-bottom: 1rpx solid var(--border);
	}

	.circle-card.is-current .circle-hero {
		background: var(--brand-gradient-soft);
	}

	.circle-logo {
		width: 88rpx;
		height: 88rpx;
		border-radius: 26rpx;
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

	.circle-title-wrap {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
	}

	.circle-name-row {
		display: flex;
		align-items: center;
		min-width: 0;
	}

	.circle-name {
		flex: 0 1 auto;
		min-width: 0;
		font-size: 32rpx;
		font-weight: 700;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.current-tag {
		flex-shrink: 0;
		margin-left: 12rpx;
		font-size: 19rpx;
		font-weight: 600;
		line-height: 1.5;
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 2rpx 12rpx;
	}

	.circle-sub {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.member-pill {
		display: inline-flex;
		align-items: center;
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 8rpx 18rpx;
		margin-left: 16rpx;
		flex-shrink: 0;
	}

	.member-icon {
		font-size: 22rpx;
	}

	.member-num {
		font-size: 24rpx;
		font-weight: 700;
		color: var(--primary);
		margin-left: 6rpx;
	}

	.circle-rows {
		padding: 18rpx 28rpx 6rpx;
	}

	.circle-row {
		display: flex;
		align-items: center;
		padding: 12rpx 0;
	}

	.circle-label {
		font-size: 24rpx;
		color: var(--text-secondary);
		width: 160rpx;
		flex-shrink: 0;
	}

	.circle-value {
		font-size: 26rpx;
		color: var(--text-primary);
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.circle-desc {
		font-size: 24rpx;
		color: var(--text-secondary);
		line-height: 1.7;
		margin: 6rpx 28rpx 24rpx;
		padding: 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.circle-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 18rpx 24rpx;
		background: var(--surface-2);
		border-top: 1rpx solid var(--border);
	}

	.foot-state {
		display: flex;
		align-items: center;
		flex-shrink: 0;
	}

	.state-dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: var(--text-tertiary);
		margin-right: 10rpx;
	}

	.state-dot--current {
		background: var(--success);
	}

	.state-text {
		font-size: 22rpx;
		color: var(--text-secondary);
	}

	.foot-actions {
		display: flex;
		align-items: center;
	}

	.foot-btn {
		display: flex;
		align-items: center;
		font-size: 23rpx;
		font-weight: 500;
		border-radius: var(--radius-pill);
		padding: 10rpx 20rpx;
		transition: opacity 0.18s ease;
	}

	.foot-btn:active {
		opacity: 0.75;
	}

	.foot-btn-icon {
		font-size: 22rpx;
		margin-right: 8rpx;
	}

	.foot-btn--manage {
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border-strong);
		margin-right: 12rpx;
	}

	.foot-btn--danger {
		color: var(--danger);
		background: rgba(179, 69, 59, 0.10);
		border: 1rpx solid rgba(179, 69, 59, 0.22);
	}
</style>
