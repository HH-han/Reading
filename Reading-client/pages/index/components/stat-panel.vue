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
			<view
				class="star"
				v-for="(p, i) in podiumStars"
				:key="i"
				:class="{ 'star--champion': p.place === 0 }"
				:style="{ animationDelay: (i * 110) + 'ms' }"
			>
				<view class="star-avatar-wrap" :class="'rank-' + p.place">
					<r-icon
						class="crown"
						:class="'crown-' + p.place"
						:name="['vip-crown-fill', 'medal-fill', 'medal-fill'][p.place]"
						:color="['#C1962A', '#B9BFC9', '#C08963'][p.place]"
					/>
					<image class="star-avatar" :src="p.item.avatar || '/static/logo.png'" mode="aspectFill" />
				</view>
				<text class="star-name">{{ p.item.nickname || p.item.name || '-' }}</text>
				<text class="star-days">{{ $t('ranking.days', { n: starDays(p.item) }) }}</text>
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
			<view class="goal-row" v-if="daysInfo">
				<view class="goal-meta">
					<text class="goal-my">{{ $t('index.daysInRange', { n: rangeDays }) }}</text>
					<text class="goal-target" v-if="requiredDays != null">{{ $t('index.requiredDays', { n: requiredDays }) }}</text>
				</view>
				<view class="goal-track" v-if="requiredDays">
					<view class="goal-fill" :style="{ width: goalPct + '%' }"></view>
				</view>
			</view>
			<view class="empty-box" v-if="!trend.length">
				<text>{{ $t('index.statEmpty') }}</text>
			</view>
			<view class="chart" v-else>
				<!-- 顶部数值行：仅展示当前选中日期（本月默认今日），避免标签重叠 -->
				<view class="value-row">
					<view class="value-col" v-for="(b, i) in trend" :key="'v' + i">
						<text class="bar-value" :class="{ 'bar-value--active': i === activeIndex }">{{ showValue(i) ? b.value + '%' : '' }}</text>
					</view>
				</view>
				<view class="plot-body">
					<view class="y-axis">
						<text
							v-for="t in yTicks"
							:key="'y' + t"
							class="y-tick"
							:style="{ bottom: t + '%' }"
						>{{ t }}%</text>
					</view>
					<view class="chart-main">
						<view
							v-for="t in gridTicks"
							:key="'g' + t"
							class="grid-line"
							:style="{ bottom: t + '%' }"
						></view>
						<view class="bars">
							<view class="bar-col" v-for="(b, i) in trend" :key="i" @click="selectBar(i)">
								<view
									class="bar"
									:class="{
										'bar--today': isToday(i),
										'bar--zero': !b.value,
										'bar--active': i === activeIndex
									}"
									:style="{ height: barHeight(b) + '%' }"
								></view>
							</view>
						</view>
					</view>
				</view>
				<view class="x-axis">
					<text
						v-for="(b, i) in trend"
						:key="'x' + i"
						class="x-tick"
						:style="xTickStyle(i)"
					>{{ b.showLabel ? b.label : '' }}</text>
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
	const podiumStars = computed(() => {
		const top = stars.value.slice(0, 3)
		const order = [1, 0, 2] // 亚军、冠军、季军
		return order
			.filter(idx => top[idx])
			.map(idx => ({ item: top[idx], place: idx }))
	})
	const daysInfo = ref(null)
	const trend = ref([])
	const range = ref('week')
	// 当前选中柱子的下标（本月默认选中今日）
	const activeIndex = ref(-1)

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
		return m
	})

	// 纵轴上限按 20% 台阶取整，避免柱子过高或全部挤在底部
	const axisMax = computed(() => Math.max(20, Math.ceil((maxTrend.value || 1) / 20) * 20))

	const midTick = computed(() => axisMax.value / 2)

	// 网格线（0、中值、上限）
	const gridTicks = computed(() => [0, midTick.value, axisMax.value])

	// 纵轴刻度文字（只标注中值与上限）
	const yTicks = computed(() => [midTick.value, axisMax.value])

	// 合格目标完成度
	const goalPct = computed(() => {
		const need = requiredDays.value
		if (!need) return 0
		return Math.max(0, Math.min(100, Math.round((rangeDays.value / need) * 100)))
	})

	function barHeight(b) {
		if (!b.value) return 0
		return Math.max(3, Math.round((b.value / axisMax.value) * 100))
	}

	// 本月数据点密集，只显示选中柱子的数值，避免标签相互挤压
	function showValue(i) {
		if (range.value === 'month') return i === activeIndex.value
		return i === activeIndex.value || trend.value[i].value > 0
	}

	function selectBar(i) {
		activeIndex.value = i
	}

	function isToday(i) {
		if (range.value === 'year') return i === new Date().getMonth()
		return i === trend.value.length - 1
	}

	// 首尾刻度贴边，其余居中，避免左右溢出卡片
	function xTickStyle(i) {
		const n = trend.value.length
		if (!n) return {}
		if (i === 0) return { left: '0', transform: 'translateX(0)' }
		if (i === n - 1) return { right: '0', transform: 'translateX(0)' }
		return { left: ((i + 0.5) / n * 100) + '%', transform: 'translateX(-50%)' }
	}

	function fmtShort(d) {
		return (d.getMonth() + 1) + '/' + d.getDate()
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
			list.push({ label: String(m + 1), value, showLabel: true })
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
		const today = new Date()
		// 本月只展示当月已过天数，避免出现上个月的日期
		const span = isMonth ? today.getDate() : 7
		const list = []
		for (let i = span - 1; i >= 0; i--) {
			const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() - i)
			const key = fmtDate(d)
			const value = rateMap[key] || 0
			list.push({
				// 本月数据点较多，只保留首尾和每 5 天的日期，避免刻度重叠
				label: fmtShort(d),
				showLabel: !isMonth || i === 0 || i === span - 1 || d.getDate() % 5 === 0,
				value
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
		// 本月按当月已过天数取数，保证刻度不会跨到上个月
		const days = range.value === 'month' ? new Date().getDate() : r.days
		try {
			const data = await getTrend({ days })
			trend.value = normalizeTrend(data)
		} catch (e) {
			trend.value = []
		}
		// 默认选中今日（本年选中当前月份）
		activeIndex.value = range.value === 'year'
			? new Date().getMonth()
			: trend.value.length - 1
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
		opacity: 0;
		transform: translateY(24rpx);
		animation: starRise 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
	}

	.star--champion {
		margin-top: -18rpx;
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

	.crown-0 {
		font-size: 44rpx;
		top: -44rpx;
		animation: crownFloat 2.2s ease-in-out infinite;
	}

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

	.goal-row {
		margin-top: 18rpx;
	}

	.goal-meta {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
	}

	.goal-my {
		font-size: 23rpx;
		color: var(--text-primary);
		font-weight: 600;
	}

	.goal-target {
		font-size: 21rpx;
		color: var(--text-tertiary);
	}

	.goal-track {
		height: 8rpx;
		margin-top: 12rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		overflow: hidden;
	}

	.goal-fill {
		height: 100%;
		border-radius: var(--radius-pill);
		background: var(--primary);
		transition: width 0.4s ease;
	}

	.chart {
		margin-top: 26rpx;
	}

	.value-row {
		display: flex;
		margin-left: 64rpx;
		height: 30rpx;
	}

	.value-col {
		flex: 1;
		min-width: 0;
		display: flex;
		justify-content: center;
		padding: 0 2rpx;
		box-sizing: border-box;
	}

	.bar-value {
		font-size: 18rpx;
		line-height: 30rpx;
		color: var(--text-tertiary);
		white-space: nowrap;
	}

	.bar-value--active {
		color: var(--primary);
		font-weight: 700;
	}

	.plot-body {
		display: flex;
		height: 250rpx;
		margin-top: 6rpx;
	}

	.y-axis {
		position: relative;
		width: 64rpx;
		flex-shrink: 0;
	}

	.y-tick {
		position: absolute;
		right: 16rpx;
		font-size: 18rpx;
		line-height: 1;
		color: var(--text-tertiary);
		transform: translateY(50%);
	}

	.chart-main {
		position: relative;
		flex: 1;
		min-width: 0;
	}

	.grid-line {
		position: absolute;
		left: 0;
		right: 0;
		height: 1rpx;
		background: var(--border);
	}

	.bars {
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		display: flex;
		align-items: flex-end;
	}

	.bar-col {
		flex: 1;
		height: 100%;
		min-width: 0;
		padding: 0 2rpx;
		box-sizing: border-box;
		display: flex;
		align-items: flex-end;
		justify-content: center;
	}

	.bar {
		width: 70%;
		max-width: 30rpx;
		min-height: 3rpx;
		border-radius: 8rpx 8rpx 0 0;
		background: var(--primary);
		transition: height 0.35s ease, width 0.2s ease;
	}

	.bar--today {
		background: var(--accent);
	}

	.bar--zero {
		background: var(--surface-3);
	}

	.bar--active {
		width: 92%;
	}

	.x-axis {
		position: relative;
		margin-left: 64rpx;
		height: 30rpx;
		margin-top: 10rpx;
	}

	.x-tick {
		position: absolute;
		top: 0;
		font-size: 18rpx;
		line-height: 30rpx;
		color: var(--text-tertiary);
		white-space: nowrap;
	}

	@keyframes starRise {
		0% {
			opacity: 0;
			transform: translateY(24rpx);
		}
		100% {
			opacity: 1;
			transform: translateY(0);
		}
	}

	@keyframes crownFloat {
		0%, 100% {
			transform: translateX(-50%) translateY(0);
		}
		50% {
			transform: translateX(-50%) translateY(-7rpx);
		}
	}
</style>
