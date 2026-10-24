<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="star-line" class="empty-icon" />
				<text>{{ $t('favorites.empty') }}</text>
			</view>

			<view class="fav-item glass-card" v-for="item in list" :key="item.id" @click="goTarget(item)">
				<view class="f-badge" :class="'t' + item.targetType">
					<text class="f-badge-text">{{ item.targetType === 1 ? $t('favorites.badgeBook') : $t('favorites.badgeCheckin') }}</text>
				</view>
				<view class="f-main">
					<text class="f-title">{{ item.title || item.bookTitle || item.targetTitle || '-' }}</text>
					<text class="f-desc" v-if="item.content || item.author">{{ item.content || item.author }}</text>
					<text class="f-time">{{ item.createTime || '' }}</text>
				</view>
				<r-icon name="arrow-right-s-line" class="f-arrow" v-if="item.targetType === 1" />
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
	import { getFavorites } from '@/api/user.js'

	const list = ref([])
	const current = ref(1)
	const size = 15
	const noMore = ref(false)
	const loading = ref(false)

	async function load(reset) {
		if (loading.value) return
		loading.value = true
		if (reset) {
			current.value = 1
			noMore.value = false
		}
		try {
			const data = await getFavorites({ current: current.value, size })
			const records = data.records || []
			list.value = reset ? records : list.value.concat(records)
			if (records.length < size) noMore.value = true
		} catch (e) {} finally {
			loading.value = false
		}
	}

	function goTarget(item) {
		if (item.targetType === 1 && item.targetId) {
			uni.navigateTo({ url: '/pages/book/detail?id=' + item.targetId })
		}
	}

	onShow(() => load(true))

	onReachBottom(() => {
		if (!noMore.value) {
			current.value++
			load(false)
		}
	})

	onPullDownRefresh(async () => {
		await load(true)
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.fav-item {
		margin-top: 20rpx;
		padding: 26rpx 28rpx;
		display: flex;
		border-radius: var(--radius-lg);
	}

	.f-badge {
		border-radius: 14rpx;
		padding: 8rpx 16rpx;
		height: fit-content;
		flex-shrink: 0;
		margin-top: 2rpx;
	}

	.f-badge.t1 {
		background: rgba(46, 74, 107, 0.12);
	}

	.f-badge.t2 {
		background: rgba(46, 125, 91, 0.13);
	}

	.f-badge-text {
		font-size: 20rpx;
		font-weight: 500;
		color: var(--primary);
	}

	.f-badge.t2 .f-badge-text {
		color: var(--success);
	}

	.f-main {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
	}

	.f-title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.f-desc {
		font-size: 24rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.f-time {
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 12rpx;
		display: block;
	}

	.f-arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-left: 12rpx;
		align-self: center;
		flex-shrink: 0;
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
