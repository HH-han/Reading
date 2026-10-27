<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<!-- 顶部用户卡 -->
		<view class="header" :style="headerBg">
			<view class="header-deco deco-1"></view>
			<view class="header-deco deco-2"></view>
			<view class="header-mask">
				<view class="user-row">
					<view class="avatar-ring">
						<image class="avatar" :src="user.avatar || '/static/logo.png'" mode="aspectFill" />
					</view>
					<view class="user-main">
						<view class="name-row">
							<text class="nickname">{{ user.nickname || '-' }}</text>
							<view class="position-tag" v-if="user.position">
								<text class="position-text">{{ user.position }}</text>
							</view>
						</view>
						<r-icon :name="roleText" class="role-text" />
					</view>
				</view>
				<view class="stats-row">
					<view
						class="stat"
						:class="{ 'stat--divided': idx > 0 }"
						v-for="(s, idx) in stats"
						:key="s.key"
					>
						<text class="stat-num">{{ s.value }}</text>
						<text class="stat-label">{{ $t(s.key) }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 快捷入口 -->
		<view class="quick-row">
			<view class="quick-item glass-card" @click="go('/pages/mine/favorites')">
				<view class="icon-chip icon-amber">
					<r-icon name="star-line" />
				</view>
				<view class="quick-main">
					<text class="quick-text">{{ $t('index.favorites') }}</text>
				</view>
				<r-icon name="arrow-right-s-line" class="quick-arrow" />
			</view>
			<view class="quick-item glass-card" @click="callService">
				<view class="icon-chip icon-violet">
					<r-icon name="headphone-line" />
				</view>
				<view class="quick-main">
					<text class="quick-text">{{ $t('index.service') }}</text>
					<text class="quick-sub">{{ $t('index.serviceTime') }}</text>
				</view>
				<r-icon name="arrow-right-s-line" class="quick-arrow" />
			</view>
		</view>

		<!-- 常用功能卡片 -->
		<view class="nav-cards">
			<view
				v-for="(tab, i) in cardTabs"
				:key="tab.key"
				class="nav-card glass-card"
				:class="{ 'nav-card--active': activeTab === i }"
				@click="activeTab = i"
			>
				<view class="icon-chip" :class="tab.chip">
					<r-icon :name="tab.icon" />
				</view>
				<text class="nav-card-text">{{ $t(tab.key) }}</text>
			</view>
		</view>

		<!-- 其他 Tab -->
		<view class="tabs-row">
			<view
				v-for="(tab, j) in pillTabs"
				:key="tab.key"
				class="tab"
				:class="{ active: activeTab === j + cardTabs.length }"
				@click="activeTab = j + cardTabs.length"
			>
				<text class="tab-text">{{ $t(tab.key) }}</text>
			</view>
		</view>

		<!-- 面板 -->
		<view class="panel-wrap">
			<stat-panel v-show="activeTab === 0" ref="statRef" />
			<task-panel v-show="activeTab === 1" ref="taskRef" v-if="visited.has(1)" />
			<feed-panel v-show="activeTab === 2" ref="feedRef" v-if="visited.has(2)" />
			<board-panel v-show="activeTab === 3" ref="boardRef" v-if="visited.has(3)" />
			<circle-panel v-show="activeTab === 4" ref="circleRef" v-if="visited.has(4)" />
			<shelf-panel v-show="activeTab === 5" ref="shelfRef" v-if="visited.has(5)" />
		</view>

		<float-tabbar :current="0" />
		<r-modal />
	</view>
</template>

<script setup>
	import { ref, computed, reactive, watch } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState, fetchUserInfo } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
	import FloatTabbar from '@/components/float-tabbar/float-tabbar.vue'
	import TaskPanel from './components/task-panel.vue'
	import FeedPanel from './components/feed-panel.vue'
	import BoardPanel from './components/board-panel.vue'
	import StatPanel from './components/stat-panel.vue'
	import CirclePanel from './components/circle-panel.vue'
	import ShelfPanel from './components/shelf-panel.vue'

	const cardTabs = [
		{ key: 'index.tabs.stat', icon: 'stock-line', chip: 'icon-blue' },
		{ key: 'index.tabs.task', icon: 'file-list-3-line', chip: 'icon-green' },
		{ key: 'index.tabs.feed', icon: 'chat-3-line', chip: 'icon-amber' },
		{ key: 'index.tabs.board', icon: 'bar-chart-2-line', chip: 'icon-violet' }
	]
	const pillTabs = [
		{ key: 'index.tabs.circle' },
		{ key: 'index.tabs.shelf' }
	]
	const activeTab = ref(0)
	const visited = reactive(new Set([0]))

	const statRef = ref(null)
	const taskRef = ref(null)
	const feedRef = ref(null)
	const boardRef = ref(null)
	const circleRef = ref(null)
	const shelfRef = ref(null)
	const panelRefs = [statRef, taskRef, feedRef, boardRef, circleRef, shelfRef]

	watch(activeTab, (i) => {
		visited.add(i)
		// 切到圈简介时刷新，保证通讯录中新增/退出的读书圈及时同步
		if (i === 4) refreshCircle()
		// 切到动态时刷新，保证切换当前读书圈后动态及时更新
		if (i === 2) refreshFeed()
	})

	function refreshFeed() {
		const panel = feedRef.value
		if (panel && panel.refresh) panel.refresh()
	}

	function refreshCircle() {
		const panel = circleRef.value
		if (panel && panel.refresh) panel.refresh()
	}

	const user = computed(() => userState.userInfo || {})

	const roleText = computed(() => {
		const map = { SUPER_ADMIN: 'vip-crown-fill', TEAM_ADMIN: 'shield-check-line', MEMBER: 'book-open-line' }
		return map[user.value.role] || ''
	})

	const headerBg = computed(() => {
		if (user.value.backgroundImg) {
			return { backgroundImage: `url(${user.value.backgroundImg})`, backgroundSize: 'cover', backgroundPosition: 'center' }
		}
		return { backgroundColor: themeState.isDark ? '#243244' : '#2E4A6B' }
	})

	const stats = computed(() => [
		{ key: 'index.follow', value: user.value.followingCount || 0 },
		{ key: 'index.fans', value: user.value.followerCount || 0 },
		{ key: 'index.likes', value: user.value.likeCount || 0 },
		{ key: 'index.points', value: user.value.points || 0 },
		{ key: 'index.checkin', value: user.value.checkinDays || 0 }
	])

	onLoad((options) => {
		// 支持 ?tab=2 从其他页面直接定位到指定面板（如打卡提交后回到动态）
		const tab = Number(options && options.tab)
		if (!Number.isNaN(tab) && panelRefs[tab]) {
			activeTab.value = tab
			visited.add(tab)
		}
	})

	onShow(() => {
		fetchUserInfo().catch(() => {})
		// 返回首页时刷新动态，同步切换读书圈后的模板分类与数据
		refreshFeed()
	})

	function go(url) {
		uni.navigateTo({ url })
	}

	function callService() {
		uni.makePhoneCall({
			phoneNumber: t('mine.hotlineNumber'),
			fail: () => {}
		})
	}

	onPullDownRefresh(async () => {
		fetchUserInfo().catch(() => {})
		const panel = panelRefs[activeTab.value].value
		if (panel && panel.refresh) {
			try { await panel.refresh() } catch (e) {}
		}
		uni.stopPullDownRefresh()
	})

	onReachBottom(() => {
		const panel = panelRefs[activeTab.value].value
		if (panel && panel.loadMore) panel.loadMore()
	})
</script>

<style lang="scss" scoped>
	.header {
		position: relative;
		border-radius: 0 0 44rpx 44rpx;
		overflow: hidden;
		box-shadow: 0 10rpx 28rpx rgba(29, 30, 34, 0.1);
	}

	.header-deco {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.deco-1 {
		width: 320rpx;
		height: 320rpx;
		top: -160rpx;
		right: -80rpx;
		background: rgba(255, 255, 255, 0.14);
	}

	.deco-2 {
		width: 200rpx;
		height: 200rpx;
		bottom: -120rpx;
		left: 40rpx;
		background: rgba(255, 255, 255, 0.10);
	}

	.header-mask {
		position: relative;
		z-index: 1;
		background: rgba(20, 22, 28, 0.16);
		padding: 44rpx 32rpx 40rpx;
	}

	.user-row {
		display: flex;
		align-items: center;
	}

	.avatar-ring {
		width: 116rpx;
		height: 116rpx;
		border-radius: 50%;
		padding: 4rpx;
		background: rgba(255, 255, 255, 0.35);
		box-shadow: 0 8rpx 20rpx rgba(10, 16, 40, 0.18);
		flex-shrink: 0;
	}

	.avatar {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: #FFFFFF;
		border: 2rpx solid rgba(255, 255, 255, 0.8);
		box-sizing: border-box;
	}

	.user-main {
		margin-left: 24rpx;
		flex: 1;
		min-width: 0;
	}

	.name-row {
		display: flex;
		align-items: center;
	}

	.nickname {
		font-size: 38rpx;
		font-weight: 700;
		color: #FFFFFF;
		max-width: 380rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.position-tag {
		display: inline-flex;
		align-items: center;
		background: rgba(255, 255, 255, 0.18);
		border: 1rpx solid rgba(255, 255, 255, 0.32);
		border-radius: var(--radius-pill);
		padding: 4rpx 16rpx;
		margin-left: 14rpx;
		flex-shrink: 0;
	}

	.position-text {
		font-size: 20rpx;
		color: #FFFFFF;
	}

	.role-text {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.85);
		margin-top: 10rpx;
		letter-spacing: 2rpx;
	}

	.stats-row {
		display: flex;
		margin-top: 32rpx;
		background: rgba(255, 255, 255, 0.12);
		border: 1rpx solid rgba(255, 255, 255, 0.16);
		border-radius: var(--radius-lg);
		padding: 22rpx 0;
	}

	.stat {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat--divided {
		border-left: 1rpx solid rgba(255, 255, 255, 0.16);
	}

	.stat-num {
		font-size: 34rpx;
		font-weight: 700;
		color: #FFFFFF;
		line-height: 1.2;
	}

	.stat-label {
		font-size: 21rpx;
		color: rgba(255, 255, 255, 0.8);
		margin-top: 6rpx;
	}

	.quick-row {
		display: flex;
		padding: 24rpx 24rpx 0;
		gap: 20rpx;
	}

	.quick-item {
		flex: 1;
		display: flex;
		align-items: center;
		padding: 22rpx 20rpx;
		border-radius: var(--radius-lg);
	}

	.quick-main {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
		display: flex;
		flex-direction: column;
	}

	.quick-text {
		font-size: 26rpx;
		font-weight: 600;
		line-height: 1.45;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.quick-sub {
		font-size: 20rpx;
		line-height: 1.45;
		color: var(--text-secondary);
		margin-top: 4rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.quick-arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-left: 8rpx;
	}

	.nav-cards {
		display: flex;
		gap: 18rpx;
		padding: 26rpx 24rpx 0;
	}

	.nav-card {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 24rpx 0 20rpx;
		border-radius: var(--radius-lg);
		border: 1rpx solid transparent;
		transition: all 0.2s ease;
	}

	.nav-card--active {
		border-color: var(--primary);
		box-shadow: 0 6rpx 16rpx rgba(46, 74, 107, 0.16);
	}

	.nav-card-text {
		font-size: 24rpx;
		font-weight: 600;
		line-height: 1.3;
		margin-top: 14rpx;
		color: var(--text-primary);
	}

	.nav-card--active .nav-card-text {
		color: var(--primary);
	}

	.tabs-row {
		display: flex;
		gap: 14rpx;
		margin-top: 24rpx;
		padding: 0 24rpx;
	}

	.tab {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 14rpx 30rpx;
		border-radius: var(--radius-pill);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		box-shadow: var(--shadow-sm);
		transition: all 0.2s ease;
	}

	.tab-text {
		font-size: 26rpx;
		line-height: 1.3;
		letter-spacing: 0.5rpx;
		white-space: nowrap;
		color: var(--text-secondary);
	}

	.tab.active {
		background: var(--primary-fill);
		border-color: var(--primary-fill);
		box-shadow: 0 4rpx 12rpx rgba(46, 74, 107, 0.18);
	}

	.tab.active .tab-text {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--on-primary);
	}

	.panel-wrap {
		padding: 24rpx 24rpx 220rpx;
	}
</style>
