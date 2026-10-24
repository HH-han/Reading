<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 周期 tab -->
			<view class="segmented segmented--brand tab-row">
				<view
					v-for="tb in tabs"
					:key="tb.value"
					class="segmented__item"
					:class="{ 'segmented__item--active': rankType === tb.value }"
					@click="switchType(tb.value)"
				>
					<text class="seg-text">{{ $t(tb.label) }}</text>
				</view>
			</view>

			<!-- 范围 tab -->
			<view class="scope-row">
				<view
					v-for="s in scopes"
					:key="s.value"
					class="scope-item"
					:class="{ active: scopeType === s.value }"
					@click="switchScope(s.value)"
				>
					<text class="scope-text">{{ $t(s.label) }}</text>
				</view>
			</view>

			<!-- 榜单 -->
			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="trophy-line" class="empty-icon" />
				<text>{{ $t('ranking.empty') }}</text>
			</view>

			<template v-else>
				<!-- 前三名 -->
				<view class="podium glass-card">
					<view class="podium-deco"></view>
					<view class="podium-item" v-for="p in podium" :key="p.item.id || p.place" :class="'place-' + p.place">
						<view class="podium-avatar-wrap" :class="'ring-' + p.place">
							<r-icon name="vip-crown-fill" class="podium-crown" color="#C1962A" v-if="p.place === 1" />
							<image
								class="podium-avatar"
								v-if="scopeType === 1"
								:src="p.item.avatar || '/static/logo.png'"
								mode="aspectFill"
							/>
							<view class="podium-avatar group-icon" v-else>
								<r-icon name="group-2-line" class="group-icon-text" />
							</view>
							<view class="podium-medal">
								<r-icon name="medal-fill" :color="p.place === 1 ? '#C1962A' : (p.place === 2 ? '#B9BFC9' : '#C08963')" />
							</view>
						</view>
						<text class="podium-name">{{ displayName(p.item) }}</text>
						<text class="podium-days">{{ $t('ranking.days', { n: itemDays(p.item) }) }}</text>
						<view class="podium-base" :class="'base-' + p.place">
							<text class="base-no">{{ p.place }}</text>
						</view>
					</view>
				</view>

				<!-- 其余名次 -->
				<view class="rank-list glass-card" v-if="rest.length">
					<view class="rank-item" v-for="(item, i) in rest" :key="item.id || i">
						<view class="rank-no">
							<text class="rank-no-text">{{ i + 4 }}</text>
						</view>
						<view class="r-avatar-ring">
							<image class="r-avatar" v-if="scopeType === 1" :src="item.avatar || '/static/logo.png'" mode="aspectFill" />
							<view class="r-avatar group-icon" v-else>
								<r-icon name="group-2-line" class="group-icon-text" />
							</view>
						</view>
						<view class="r-main">
							<text class="r-name">{{ displayName(item) }}</text>
							<text class="r-days">{{ $t('ranking.checkinDays') }}：{{ $t('ranking.days', { n: itemDays(item) }) }}</text>
						</view>
						<view class="r-score">
							<text class="r-score-num">{{ itemDays(item) }}</text>
							<text class="r-score-unit">{{ $t('ranking.daysUnit') }}</text>
						</view>
					</view>
				</view>
			</template>
		</view>

	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getRanking } from '@/api/ranking.js'

	const tabs = [
		{ value: 1, label: 'ranking.week' },
		{ value: 2, label: 'ranking.month' },
		{ value: 3, label: 'ranking.year' }
	]

	const scopes = [
		{ value: 1, label: 'ranking.personal' },
		{ value: 2, label: 'ranking.group' }
	]

	const rankType = ref(1)
	const scopeType = ref(1)
	const list = ref([])
	const loading = ref(false)

	const podium = computed(() => {
		const top = list.value.slice(0, 3)
		const order = [1, 0, 2]
		return order
			.filter(idx => top[idx])
			.map(idx => ({ place: idx + 1, item: top[idx] }))
	})

	const rest = computed(() => list.value.slice(3))

	function displayName(item) {
		return scopeType.value === 1 ? (item.nickname || '-') : (item.groupName || item.name || '-')
	}

	function itemDays(item) {
		return item.days || item.checkinDays || 0
	}

	async function load() {
		loading.value = true
		try {
			const data = await getRanking({ rankType: rankType.value, scopeType: scopeType.value })
			list.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function switchType(v) {
		if (rankType.value === v) return
		rankType.value = v
		load()
	}

	function switchScope(v) {
		if (scopeType.value === v) return
		scopeType.value = v
		load()
	}

	onShow(load)

	onPullDownRefresh(async () => {
		await load()
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx 24rpx 60rpx;
	}

	.tab-row {
		margin-bottom: 4rpx;
	}

	.tab-row .seg-text {
		font-size: 26rpx;
		line-height: 1.3;
		white-space: nowrap;
	}

	.scope-row {
		display: flex;
		margin-top: 24rpx;
		gap: 14rpx;
	}

	.scope-item {
		flex: 0 0 auto;
		padding: 12rpx 34rpx;
		border-radius: var(--radius-pill);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		transition: all 0.2s ease;
	}

	.scope-item.active {
		border-color: var(--primary);
		background: var(--primary-soft);
	}

	.scope-text {
		font-size: 24rpx;
		line-height: 1.3;
		letter-spacing: 0.5rpx;
		white-space: nowrap;
		color: var(--text-secondary);
	}

	.scope-item.active .scope-text {
		color: var(--primary);
		font-weight: 600;
	}

	.podium {
		position: relative;
		display: flex;
		align-items: flex-end;
		justify-content: space-around;
		margin-top: 24rpx;
		padding: 70rpx 24rpx 0;
		border-radius: var(--radius-xl);
		overflow: hidden;
		background: var(--primary-soft);
	}

	.podium-deco {
		position: absolute;
		width: 380rpx;
		height: 380rpx;
		border-radius: 50%;
		top: -220rpx;
		left: 50%;
		transform: translateX(-50%);
		background: rgba(193, 150, 42, 0.08);
		pointer-events: none;
	}

	.podium-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 30%;
		position: relative;
		z-index: 1;
	}

	.podium-item.place-1 {
		margin-bottom: 10rpx;
	}

	.podium-avatar-wrap {
		position: relative;
	}

	.ring-1 {
		padding: 4rpx;
		border-radius: 50%;
		background: #C1962A;
		box-shadow: 0 6rpx 16rpx rgba(193, 150, 42, 0.26);
	}

	.ring-2 {
		padding: 3rpx;
		border-radius: 50%;
		background: #B9BFC9;
	}

	.ring-3 {
		padding: 3rpx;
		border-radius: 50%;
		background: #C08963;
	}

	.podium-avatar {
		width: 108rpx;
		height: 108rpx;
		border-radius: 50%;
		background: var(--bg-secondary);
		display: block;
	}

	.place-2 .podium-avatar,
	.place-3 .podium-avatar {
		width: 90rpx;
		height: 90rpx;
	}

	.group-icon {
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--surface-2);
	}

	.group-icon-text {
		font-size: 36rpx;
	}

	.podium-crown {
		position: absolute;
		top: -38rpx;
		left: 50%;
		transform: translateX(-50%);
		font-size: 38rpx;
		z-index: 2;
	}

	.podium-medal {
		position: absolute;
		bottom: -12rpx;
		left: 50%;
		transform: translateX(-50%);
		font-size: 30rpx;
	}

	.podium-name {
		font-size: 25rpx;
		font-weight: 600;
		color: var(--text-primary);
		margin-top: 22rpx;
		max-width: 180rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.podium-days {
		font-size: 20rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.podium-base {
		width: 100%;
		margin-top: 16rpx;
		border-radius: var(--radius-md) var(--radius-md) 0 0;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.base-1 {
		height: 92rpx;
		background: #C1962A;
	}

	.base-2 {
		height: 66rpx;
		background: #B9BFC9;
	}

	.base-3 {
		height: 52rpx;
		background: #C08963;
	}

	.base-no {
		font-size: 32rpx;
		font-weight: 700;
		color: rgba(255, 255, 255, 0.9);
		text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.12);
	}

	.rank-list {
		margin-top: 24rpx;
		padding: 8rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.rank-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.rank-item:last-child {
		border-bottom: none;
	}

	.rank-no {
		width: 56rpx;
		display: flex;
		justify-content: center;
		flex-shrink: 0;
	}

	.rank-no-text {
		font-size: 26rpx;
		font-weight: 700;
		color: var(--text-tertiary);
	}

	.r-avatar-ring {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: var(--surface-2);
		flex-shrink: 0;
		overflow: hidden;
	}

	.r-avatar {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: var(--surface-2);
	}

	.r-main {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
	}

	.r-name {
		font-size: 27rpx;
		font-weight: 600;
		color: var(--text-primary);
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.r-days {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		display: block;
	}

	.r-score {
		display: flex;
		align-items: baseline;
		flex-shrink: 0;
	}

	.r-score-num {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--primary);
	}

	.r-score-unit {
		font-size: 19rpx;
		color: var(--text-tertiary);
		margin-left: 4rpx;
	}
</style>
