<template>
	<view class="board-panel">
		<!-- 顶部统计 -->
		<view class="top-cards">
			<view class="top-card glass-card">
				<view class="top-badge icon-blue"><r-icon name="check-line" /></view>
				<text class="top-num">{{ today.checkinUsers || 0 }}/{{ today.totalMembers || 0 }}</text>
				<text class="top-label">{{ $t('index.boardToday') }}</text>
			</view>
			<view class="top-card glass-card">
				<view class="top-badge icon-violet"><r-icon name="stock-line" /></view>
				<text class="top-num">{{ completeRate }}%</text>
				<text class="top-label">{{ $t('index.boardRate') }}</text>
			</view>
			<view class="top-card glass-card">
				<view class="top-badge icon-amber"><r-icon name="group-2-line" /></view>
				<text class="top-num">{{ today.activeGroups || 0 }}{{ $t('index.groupUnit') }}</text>
				<text class="top-label">{{ $t('index.boardActive') }}</text>
			</view>
		</view>

		<!-- 实时打卡态势 -->
		<view class="section glass-card">
			<view class="sec-head">
				<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="bar-chart-2-line" /></view>
				<text class="section-title">{{ $t('index.boardRealtime') }}</text>
			</view>
			<view class="realtime-row" v-for="row in realtimeRows" :key="row.label">
				<text class="realtime-label">{{ $t(row.label) }}</text>
				<view class="realtime-bar">
					<view class="realtime-inner" :style="{ width: row.pct + '%' }"></view>
				</view>
				<text class="realtime-value">{{ row.value }}/{{ today.totalMembers || 0 }} · {{ row.pct }}%</text>
			</view>
		</view>

		<!-- Top 5 小组 -->
		<view class="section glass-card">
			<view class="sec-head">
				<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="trophy-line" /></view>
				<text class="section-title">{{ $t('index.boardTop') }}</text>
			</view>
			<view class="empty-box" v-if="!groups.length">
				<text>{{ $t('index.statEmpty') }}</text>
			</view>
			<view class="group-row" v-for="(g, i) in groups" :key="i">
				<view class="group-rank" :class="{ top: i < 3 }">
					<text class="group-rank-text">{{ i + 1 }}</text>
				</view>
				<text class="group-name">{{ g.name || g.groupName || '-' }}</text>
				<view class="group-bar">
					<view class="group-inner" :style="{ width: groupPct(g) + '%' }"></view>
				</view>
				<text class="group-value">{{ groupValue(g) }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue'
	import { getToday, getTopGroups } from '@/api/dashboard.js'

	const today = ref({})
	const groups = ref([])

	const realtimeRows = computed(() => {
		const total = today.value.totalMembers || 0
		const mk = (label, v) => ({
			label,
			value: v,
			pct: total ? Math.round((v / total) * 100) : 0
		})
		return [
			mk('index.boardAudio', today.value.audioUsers || 0),
			mk('index.boardRead', today.value.readUsers || 0),
			mk('index.boardNote', today.value.noteUsers || 0)
		]
	})

	// 后端 completeRate 已是 0-100 的百分比
	const completeRate = computed(() => Math.round(Number(today.value.completeRate) || 0))

	function groupValue(g) {
		const v = g.totalCheckins != null
			? g.totalCheckins
			: (g.activeUsers != null ? g.activeUsers : (g.value != null ? g.value : g.score))
		return v != null ? v : (g.checkinCount || 0)
	}

	const maxGroupValue = computed(() => {
		let m = 0
		groups.value.forEach(g => {
			const v = groupValue(g)
			if (v > m) m = v
		})
		return m || 1
	})

	function groupPct(g) {
		return Math.max(4, Math.round((groupValue(g) / maxGroupValue.value) * 100))
	}

	async function load() {
		try {
			today.value = await getToday() || {}
		} catch (e) {}
		try {
			const data = await getTopGroups()
			groups.value = (Array.isArray(data) ? data : (data.records || [])).slice(0, 5)
		} catch (e) {}
	}

	onMounted(load)

	defineExpose({
		refresh: load
	})
</script>

<style lang="scss" scoped>
	.top-cards {
		display: flex;
		gap: 16rpx;
	}

	.top-card {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 28rpx 10rpx 24rpx;
		border-radius: var(--radius-lg);
	}

	.top-badge {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		margin-bottom: 12rpx;
	}

	.top-num {
		font-size: 34rpx;
		font-weight: 700;
		color: var(--text-primary);
		line-height: 1.2;
	}

	.top-label {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		text-align: center;
	}

	.section {
		padding: 30rpx;
		margin-top: 24rpx;
		border-radius: var(--radius-lg);
	}

	.sec-head {
		display: flex;
		align-items: center;
	}

	.section-title {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
		margin-left: 14rpx;
	}

	.realtime-row {
		display: flex;
		align-items: center;
		margin-top: 26rpx;
	}

	.realtime-label {
		width: 130rpx;
		font-size: 24rpx;
		color: var(--text-secondary);
		flex-shrink: 0;
	}

	.realtime-bar {
		flex: 1;
		height: 14rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		overflow: hidden;
		margin: 0 16rpx;
	}

	.realtime-inner {
		height: 100%;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		transition: width 0.4s ease;
	}

	.realtime-value {
		font-size: 21rpx;
		color: var(--text-tertiary);
		flex-shrink: 0;
		min-width: 150rpx;
		text-align: right;
	}

	.group-row {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
	}

	.group-rank {
		width: 44rpx;
		height: 44rpx;
		border-radius: 14rpx;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.group-rank.top {
		background: #C1962A;
		box-shadow: 0 4rpx 12rpx rgba(193, 150, 42, 0.24);
	}

	.group-rank-text {
		font-size: 24rpx;
		font-weight: 700;
		color: var(--text-secondary);
	}

	.group-rank.top .group-rank-text {
		color: #FFFFFF;
	}

	.group-name {
		width: 180rpx;
		font-size: 24rpx;
		color: var(--text-primary);
		margin-left: 14rpx;
		flex-shrink: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.group-bar {
		flex: 1;
		height: 14rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		overflow: hidden;
		margin: 0 16rpx;
	}

	.group-inner {
		height: 100%;
		border-radius: var(--radius-pill);
		background: #C1962A;
	}

	.group-value {
		font-size: 24rpx;
		font-weight: 700;
		color: var(--text-primary);
		flex-shrink: 0;
	}
</style>
