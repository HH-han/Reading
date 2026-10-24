<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="unread-bar glass">
				<view class="unread-icon-wrap">
					<r-icon name="notification-3-line" class="unread-icon" />
				</view>
				<text class="unread-text">{{ unread ? $t('notify.unread', { n: unread }) : $t('notify.allRead') }}</text>
				<view class="unread-badge" v-if="unread">
					<text class="unread-badge-text">{{ unread > 99 ? '99+' : unread }}</text>
				</view>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="mail-open-line" class="empty-icon" />
				<text>{{ $t('notify.empty') }}</text>
			</view>

			<view
				class="notify-item glass-card"
				:class="{ read: item.readFlag === 1 || item.read }"
				v-for="item in list"
				:key="item.id"
				@click="onRead(item)"
			>
				<view class="n-icon" :class="{ active: !(item.readFlag === 1 || item.read) }">
					<r-icon name="notification-3-line" class="n-icon-text" />
				</view>
				<view class="n-main">
					<view class="n-title-row">
						<text class="n-title">{{ item.title || '' }}</text>
						<view class="n-dot" v-if="!(item.readFlag === 1 || item.read)"></view>
					</view>
					<text class="n-content">{{ item.content || '' }}</text>
					<text class="n-time">{{ item.createTime || '' }}</text>
				</view>
			</view>

			<view class="load-more" v-if="list.length">
				<view class="load-dot"></view>
				<text class="load-more-text">{{ noMore ? $t('common.noMore') : $t('common.loading') }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getNotifyPage, markRead, getUnreadCount } from '@/api/notify.js'

	const list = ref([])
	const unread = ref(0)
	const current = ref(1)
	const size = 15
	const noMore = ref(false)
	const loading = ref(false)

	async function loadUnread() {
		try {
			unread.value = await getUnreadCount() || 0
		} catch (e) {}
	}

	async function load(reset) {
		if (loading.value) return
		loading.value = true
		if (reset) {
			current.value = 1
			noMore.value = false
		}
		try {
			const data = await getNotifyPage({ current: current.value, size })
			const records = data.records || []
			list.value = reset ? records : list.value.concat(records)
			if (records.length < size) noMore.value = true
		} catch (e) {} finally {
			loading.value = false
		}
	}

	async function onRead(item) {
		if (item.readFlag === 1 || item.read) return
		try {
			await markRead(item.id)
			item.readFlag = 1
			loadUnread()
		} catch (e) {}
	}

	onShow(() => {
		loadUnread()
		load(true)
	})

	onReachBottom(() => {
		if (!noMore.value) {
			current.value++
			load(false)
		}
	})

	onPullDownRefresh(async () => {
		await Promise.all([loadUnread(), load(true)])
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.unread-bar {
		display: flex;
		align-items: center;
		border-radius: var(--radius-lg);
		padding: 20rpx 24rpx;
	}

	.unread-icon-wrap {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: var(--primary-soft);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.unread-icon {
		font-size: 26rpx;
	}

	.unread-text {
		flex: 1;
		font-size: 25rpx;
		color: var(--text-secondary);
		margin-left: 16rpx;
	}

	.unread-badge {
		background: var(--danger);
		border-radius: var(--radius-pill);
		padding: 4rpx 14rpx;
		flex-shrink: 0;
	}

	.unread-badge-text {
		font-size: 20rpx;
		font-weight: 600;
		color: #FFFFFF;
	}

	.notify-item {
		display: flex;
		margin-top: 20rpx;
		padding: 26rpx;
		border-radius: var(--radius-lg);
	}

	.notify-item.read {
		opacity: 0.6;
	}

	.n-icon {
		width: 64rpx;
		height: 64rpx;
		border-radius: 20rpx;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.n-icon.active {
		background: var(--brand-gradient-soft);
	}

	.n-icon-text {
		font-size: 28rpx;
	}

	.n-main {
		flex: 1;
		min-width: 0;
		margin-left: 18rpx;
	}

	.n-title-row {
		display: flex;
		align-items: center;
	}

	.n-title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		flex: 1;
		min-width: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.n-dot {
		width: 14rpx;
		height: 14rpx;
		border-radius: 50%;
		background: var(--danger);
		margin-left: 12rpx;
		flex-shrink: 0;
	}

	.n-content {
		font-size: 24rpx;
		color: var(--text-secondary);
		margin-top: 10rpx;
		display: block;
		line-height: 1.65;
	}

	.n-time {
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 12rpx;
		display: block;
	}

	.load-more {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 32rpx 0;
	}

	.load-dot {
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: var(--text-tertiary);
		margin-right: 12rpx;
	}

	.load-more-text {
		font-size: 23rpx;
		color: var(--text-tertiary);
	}
</style>
