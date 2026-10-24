<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 总积分卡 -->
			<view class="points-card">
				<view class="points-deco deco-a"></view>
				<view class="points-deco deco-b"></view>
				<view class="points-head">
					<view class="points-icon-wrap">
						<r-icon name="seedling-line" class="points-icon" />
					</view>
					<text class="p-label">{{ $t('growth.total') }}</text>
				</view>
				<text class="p-value">{{ points }}</text>
			</view>

			<view class="records-title">
				<view class="records-accent"></view>
				<text class="records-text">{{ $t('growth.records') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="seedling-line" class="empty-icon" />
				<text>{{ $t('growth.empty') }}</text>
			</view>

			<view class="record-list glass-card" v-else>
				<view class="record-item" v-for="item in list" :key="item.id">
					<view class="icon-chip icon-chip--sm" :class="(item.points || 0) < 0 ? 'icon-red' : 'icon-green'">
						<r-icon :name="(item.points || 0) < 0 ? 'stock-line' : 'seedling-line'" />
					</view>
					<view class="r-main">
						<text class="r-type">{{ $t('growth.type' + item.changeType) }}</text>
						<text class="r-time">{{ item.createTime || '' }}</text>
					</view>
					<view class="r-points-wrap" :class="{ minus: (item.points || 0) < 0 }">
						<text class="r-points">{{ (item.points || 0) > 0 ? '+' : '' }}{{ item.points || 0 }}</text>
					</view>
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
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState } from '@/store/user.js'
	import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getGrowth } from '@/api/user.js'

	const list = ref([])
	const current = ref(1)
	const size = 15
	const noMore = ref(false)
	const loading = ref(false)

	const points = computed(() => (userState.userInfo && userState.userInfo.points) || 0)

	async function load(reset) {
		if (loading.value) return
		loading.value = true
		if (reset) {
			current.value = 1
			noMore.value = false
		}
		try {
			const data = await getGrowth({ current: current.value, size })
			const records = data.records || []
			list.value = reset ? records : list.value.concat(records)
			if (records.length < size) noMore.value = true
		} catch (e) {} finally {
			loading.value = false
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

	.points-card {
		position: relative;
		padding: 44rpx 32rpx;
		border-radius: var(--radius-xl);
		background: var(--brand-gradient);
		box-shadow: 0 18rpx 36rpx rgba(46, 74, 107, 0.28);
		overflow: hidden;
	}

	.points-deco {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.deco-a {
		width: 300rpx;
		height: 300rpx;
		top: -160rpx;
		right: -100rpx;
		background: rgba(255, 255, 255, 0.14);
	}

	.deco-b {
		width: 180rpx;
		height: 180rpx;
		bottom: -110rpx;
		left: 60rpx;
		background: rgba(255, 255, 255, 0.10);
	}

	.points-head {
		display: flex;
		align-items: center;
		position: relative;
		z-index: 1;
	}

	.points-icon-wrap {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		background: rgba(255, 255, 255, 0.2);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 14rpx;
	}

	.points-icon {
		font-size: 26rpx;
	}

	.p-label {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.88);
	}

	.p-value {
		position: relative;
		z-index: 1;
		display: block;
		font-size: 76rpx;
		font-weight: 700;
		color: #FFFFFF;
		margin-top: 22rpx;
		line-height: 1;
		letter-spacing: 2rpx;
	}

	.records-title {
		display: flex;
		align-items: center;
		margin: 34rpx 8rpx 18rpx;
	}

	.records-accent {
		width: 8rpx;
		height: 28rpx;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		margin-right: 14rpx;
	}

	.records-text {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.record-list {
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.record-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.record-item:last-child {
		border-bottom: none;
	}

	.r-main {
		flex: 1;
		min-width: 0;
		margin-left: 18rpx;
	}

	.r-type {
		font-size: 27rpx;
		color: var(--text-primary);
		display: block;
	}

	.r-time {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		display: block;
	}

	.r-points-wrap {
		background: rgba(46, 125, 91, 0.12);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
	}

	.r-points-wrap.minus {
		background: rgba(179, 69, 59, 0.10);
	}

	.r-points {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--success);
	}

	.r-points-wrap.minus .r-points {
		color: var(--danger);
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
