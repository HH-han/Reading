<template>
	<view class="stat-panel">
		<!-- 恭喜横幅 -->
		<view class="banner">
			<view class="banner-deco"></view>
			<view class="banner-icon-wrap">
				<r-icon name="emotion-happy-line" class="banner-icon" />
			</view>
			<text class="banner-text">{{ $t('index.statCongrats') }}</text>
		</view>

		<!-- 三块数据卡 -->
		<view class="data-cards">
			<view class="data-card glass-card">
				<text class="data-num">{{ myDaysValue }}</text>
				<text class="data-label">{{ $t('index.statMyDays') }}</text>
			</view>
			<view class="data-card glass-card">
				<text class="data-num">{{ groupRankValue }}</text>
				<text class="data-label">{{ $t('index.statGroupRank') }}</text>
			</view>
			<view class="data-card glass-card">
				<text class="data-num">{{ circleRankValue }}</text>
				<text class="data-label">{{ $t('index.statCircleRank') }}</text>
			</view>
		</view>

		<!-- 本周学习标兵 -->
		<view class="section glass-card">
			<view class="sec-head">
				<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="vip-crown-fill" /></view>
				<text class="section-title">{{ $t('index.statStar') }}</text>
			</view>
			<view class="empty-box" v-if="!stars.length">
				<text>{{ $t('index.statEmpty') }}</text>
			</view>
			<view class="stars-row" v-else>
				<view class="star" v-for="(s, i) in stars" :key="i">
					<view class="star-avatar-wrap" :class="'rank-' + i">
						<r-icon
							class="crown"
							:class="'crown-' + i"
							:name="['vip-crown-fill', 'medal-fill', 'medal-fill'][i]"
							:color="['#C1962A', '#B9BFC9', '#C08963'][i]"
						/>
						<image class="star-avatar" :src="s.avatar || '/static/logo.png'" mode="aspectFill" />
					</view>
					<text class="star-name">{{ s.nickname || s.name || '-' }}</text>
					<text class="star-days">{{ $t('ranking.days', { n: starDays(s) }) }}</text>
				</view>
			</view>
		</view>

		<!-- 打卡率图表 -->
		<view class="section glass-card">
			<view class="chart-head">
				<view class="sec-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="stock-line" /></view>
					<text class="section-title">{{ $t('index.statChart') }}</text>
				</view>
				<view class="segmented range-tabs">
					<text
						v-for="r in ranges"
						:key="r.key"
						class="segmented__item range-tab"
						:class="{ 'segmented__item--active': range === r.key }"
						@click="switchRange(r.key)"
					>{{ $t(r.label) }}</text>
				</view>
			</view>
			<view class="days-row" v-if="daysInfo">
				<text class="days-text" v-if="requiredDays != null">
					{{ rangeDays }} / {{ $t('index.requiredDays', { n: requiredDays }) }}
				</text>
				<text class="days-text" v-else>{{ $t('index.daysInRange', { n: rangeDays }) }}</text>
			</view>
			<view class="empty-box" v-if="!trend.length">
				<text>{{ $t('index.statEmpty') }}</text>
			</view>
			<view class="chart" v-else>
				<view class="bar-col" v-for="(b, i) in trend" :key="i">
					<text class="bar-value">{{ b.showValue ? b.value + '%' : '' }}</text>
					<view class="bar-wrap">
						<view class="bar" :style="{ height: barHeight(b) + '%' }"></view>
					</view>
					<text class="bar-label">{{ b.label }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue'
	import { getMy, getTrend } from '@/api/dashboard.js'
	import { getCheckinDays } from '@/api/user.js'
	import { getRanking } from '@/api/ranking.js'

	const my = ref({})
	const stars = ref([])
	const daysInfo = ref(null)
	const trend = ref([])
	const range = ref('week')

	const ranges = [
		{ key: 'week', label: 'index.week', days: 7 },
		{ key: 'month', label: 'index.month', days: 30 },
		{ key: 'year', label: 'index.year', days: 365 }
	]

	const myDaysValue = computed(() => {
		const m = my.value || {}
		return m.checkinDays != null ? m.checkinDays : (m.myDays || 0)
	})

	// 当前时间范围对应的合格天数要求
	const requiredDays = computed(() => {
		const d = daysInfo.value || {}
		if (range.value === 'week') return d.requiredDaysPerWeek
		if (range.value === 'month') return d.requiredDaysPerMonth
		return d.requiredDaysPerYear
	})

	const groupRankValue = computed(() => {
		const m = my.value || {}
		return m.groupRank != null ? m.groupRank : '-'
	})

	const circleRankValue = computed(() => {
		const m = my.value || {}
		return m.circleRank != null ? m.circleRank : '-'
	})

	function starDays(s) {
		if (s.score != null) return s.score
		if (s.days != null) return s.days
		return s.checkinDays || 0
	}

	const rangeDays = computed(() => {
		if (!daysInfo.value) return 0
		if (range.value === 'week') return daysInfo.value.weekDays || 0
		if (range.value === 'month') return daysInfo.value.monthDays || 0
		return daysInfo.value.yearDays || 0
	})

	const maxTrend = computed(() => {
		let m = 0
		trend.value.forEach(b => { if (b.value > m) m = b.value })
		return m || 1
	})

	function barHeight(b) {
		if (!b.value) return 0
		return Math.max(4, Math.round((b.value / maxTrend.value) * 100))
	}

	function pad(n) {
		return n < 10 ? '0' + n : String(n)
	}

	function fmtDate(d) {
		return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
	}

	// 本年按月汇总：当月每日打卡率之和 / 当月已过天数
	function monthlyTrend(rateMap) {
		const today = new Date()
		const year = today.getFullYear()
		const list = []
		for (let m = 0; m < 12; m++) {
			const prefix = year + '-' + pad(m + 1)
			const days = m === today.getMonth() ? today.getDate() : new Date(year, m + 1, 0).getDate()
			const total = Object.keys(rateMap)
				.filter(k => k.startsWith(prefix))
				.reduce((sum, k) => sum + rateMap[k], 0)
			const value = Math.round(total / days)
			list.push({ label: String(m + 1), value, showValue: value > 0 })
		}
		return list
	}

	// 折线数据统一为每日打卡率(%)，并按范围补齐无打卡日期
	function normalizeTrend(data) {
		const arr = Array.isArray(data) ? data : (data.records || [])
		const rateMap = {}
		arr.forEach(it => {
			const date = String(it.date || it.checkinDate || it.statDate || '').slice(0, 10)
			if (!date) return
			rateMap[date] = Math.round(Number(it.rate) || 0)
		})
		if (range.value === 'year') return monthlyTrend(rateMap)
		const isMonth = range.value === 'month'
		const span = isMonth ? 30 : 7
		const today = new Date()
		const list = []
		for (let i = span - 1; i >= 0; i--) {
			const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() - i)
			const key = fmtDate(d)
			const value = rateMap[key] || 0
			list.push({
				// 本月数据点较多，每 5 天显示一次日期，避免标签重叠
				label: isMonth ? (d.getDate() % 5 === 0 || i === 0 ? key.slice(5) : '') : key.slice(5),
				value,
				showValue: !isMonth || value > 0
			})
		}
		return list
	}

	async function loadBase() {
		try {
			my.value = await getMy() || {}
		} catch (e) {}
		try {
			daysInfo.value = await getCheckinDays()
		} catch (e) {}
		try {
			const data = await getRanking({ rankType: 1, scopeType: 1 })
			stars.value = (Array.isArray(data) ? data : (data.records || [])).slice(0, 3)
		} catch (e) {}
	}

	async function loadTrend() {
		const r = ranges.find(x => x.key === range.value)
		try {
			const data = await getTrend({ days: r.days })
			trend.value = normalizeTrend(data)
		} catch (e) {
			trend.value = []
		}
	}

	function switchRange(key) {
		if (range.value === key) return
		range.value = key
		loadTrend()
	}

	onMounted(() => {
		loadBase()
		loadTrend()
	})

	defineExpose({
		refresh: async () => {
			await loadBase()
			await loadTrend()
		}
	})
</script>

<style lang="scss" scoped>
	.banner {
		position: relative;
		display: flex;
		align-items: center;
		background: var(--brand-gradient);
		border-radius: var(--radius-lg);
		padding: 28rpx;
		box-shadow: 0 16rpx 32rpx rgba(46, 74, 107, 0.28);
		overflow: hidden;
	}

	.banner-deco {
		position: absolute;
		width: 220rpx;
		height: 220rpx;
		border-radius: 50%;
		top: -110rpx;
		right: -50rpx;
		background: rgba(255, 255, 255, 0.16);
	}

	.banner-icon-wrap {
		width: 64rpx;
		height: 64rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.2);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.banner-icon {
		font-size: 34rpx;
		line-height: 1;
	}

	.banner-text {
		font-size: 27rpx;
		color: #FFFFFF;
		font-weight: 600;
		flex: 1;
		margin-left: 18rpx;
		position: relative;
		z-index: 1;
	}

	.data-cards {
		display: flex;
		gap: 16rpx;
		margin-top: 24rpx;
	}

	.data-card {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 30rpx 10rpx;
		border-radius: var(--radius-lg);
	}

	.data-num {
		font-size: 38rpx;
		font-weight: 700;
		color: var(--primary);
		line-height: 1.2;
	}

	.data-label {
		font-size: 20rpx;
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

	.stars-row {
		display: flex;
		justify-content: space-around;
		margin-top: 44rpx;
		padding-bottom: 8rpx;
	}

	.star {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.star-avatar-wrap {
		position: relative;
	}

	.crown {
		position: absolute;
		top: -36rpx;
		left: 50%;
		transform: translateX(-50%);
		font-size: 34rpx;
		z-index: 2;
	}

	.crown-0 { font-size: 44rpx; top: -44rpx; }

	.star-avatar {
		width: 96rpx;
		height: 96rpx;
		border-radius: 50%;
		border: 4rpx solid var(--gold);
		background: var(--bg-secondary);
	}

	.rank-1 .star-avatar {
		width: 82rpx;
		height: 82rpx;
		border-color: var(--border-strong);
	}

	.rank-2 .star-avatar {
		width: 82rpx;
		height: 82rpx;
		border-color: #E0A47A;
	}

	.star-name {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--text-primary);
		margin-top: 16rpx;
		max-width: 160rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.star-days {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.chart-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.range-tabs {
		padding: 4rpx;
	}

	.range-tabs .range-tab {
		padding: 8rpx 20rpx;
		font-size: 21rpx;
		flex: none;
	}

	.days-row {
		margin-top: 18rpx;
	}

	.days-text {
		font-size: 23rpx;
		color: var(--text-secondary);
	}

	.chart {
		display: flex;
		align-items: flex-end;
		margin-top: 32rpx;
		height: 280rpx;
	}

	.bar-col {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		height: 100%;
		min-width: 0;
	}

	.bar-value {
		font-size: 18rpx;
		color: var(--text-tertiary);
		margin-bottom: 8rpx;
	}

	.bar-wrap {
		flex: 1;
		width: 100%;
		display: flex;
		align-items: flex-end;
		justify-content: center;
	}

	.bar {
		width: 56%;
		max-width: 36rpx;
		border-radius: 10rpx 10rpx 0 0;
		background: var(--primary);
	}

	.bar-label {
		font-size: 18rpx;
		color: var(--text-tertiary);
		margin-top: 10rpx;
		white-space: nowrap;
	}
</style>
