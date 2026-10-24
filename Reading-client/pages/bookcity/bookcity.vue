<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 搜索框 -->
			<view class="search-box glass">
				<view class="search-icon-wrap">
					<r-icon name="search-line" class="search-icon" />
				</view>
				<input
					class="search-input"
					v-model="keyword"
					:placeholder="$t('bookcity.searchPh')"
					placeholder-class="input-ph"
					confirm-type="search"
					@confirm="reload"
				/>
				<view class="search-go" @click="reload">
					<r-icon name="arrow-right-s-line" class="search-go-text" />
				</view>
			</view>

			<!-- 分类 tab -->
			<scroll-view scroll-x class="cate-scroll" :show-scrollbar="false">
				<view class="cate-row">
					<view
						v-for="c in categories"
						:key="c"
						class="cate-item"
						:class="{ active: category === c }"
						@click="switchCate(c)"
					>
						<text class="cate-text">{{ c === '' ? $t('common.all') : c }}</text>
					</view>
				</view>
			</scroll-view>

			<!-- 书籍列表 -->
			<view class="empty-box" v-if="!loading && !list.length">
					<r-icon name="book-2-line" class="empty-icon" />
					<text>{{ $t('bookcity.empty') }}</text>
				</view>

			<view class="book-card glass-card" v-for="b in list" :key="b.id" @click="goDetail(b)">
				<view class="cover-wrap">
					<image class="b-cover" v-if="b.cover" :src="b.cover" mode="aspectFill" />
					<view class="b-cover css-cover" v-else>
						<text class="cover-char">{{ (b.title || '书').slice(0, 1) }}</text>
					</view>
				</view>
				<view class="b-main">
					<view class="b-title-row">
						<text class="b-title">{{ b.title }}</text>
						<view class="b-ebook" v-if="b.ebookUrl">
							<text class="b-ebook-text">{{ $t('bookcity.ebook') }}</text>
						</view>
					</view>
					<text class="b-author">{{ b.author || '' }}{{ b.publisher ? ' · ' + b.publisher : '' }}</text>
					<view class="b-foot">
						<view class="b-count-wrap">
							<r-icon name="fire-line" class="b-count-icon" />
							<text class="b-count">{{ $t('bookcity.checkinCount', { n: b.checkinCount || 0 }) }}</text>
						</view>
						<view class="b-add" :class="{ added: b.inShelf }" @click.stop="addShelf(b)">
							<r-icon v-if="b.inShelf" name="check-line" class="b-add-check" />
							<text class="b-add-text">{{ b.inShelf ? $t('bookcity.added') : $t('bookcity.addShelf') }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="load-more" v-if="list.length">
				<view class="load-dot"></view>
				<text class="load-more-text">{{ noMore ? $t('common.noMore') : $t('common.loading') }}</text>
			</view>
		</view>

		<float-tabbar :current="3" />
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { t } from '@/utils/i18n.js'
	import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getBookPage, getCategories, addToShelf } from '@/api/book.js'
	import FloatTabbar from '@/components/float-tabbar/float-tabbar.vue'

	const keyword = ref('')
	const category = ref('')
	const categories = ref([''])
	const list = ref([])
	const current = ref(1)
	const size = 10
	const noMore = ref(false)
	const loading = ref(false)

	async function loadCategories() {
		try {
			const data = await getCategories()
			categories.value = ['', ...(Array.isArray(data) ? data : [])]
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
			const data = await getBookPage({ current: current.value, size, category: category.value || undefined, keyword: keyword.value || undefined })
			const records = data.records || []
			if (reset) {
				list.value = records
			} else {
				list.value = list.value.concat(records)
			}
			if (records.length < size) noMore.value = true
		} catch (e) {} finally {
			loading.value = false
		}
	}

	function reload() {
		load(true)
	}

	function switchCate(c) {
		if (category.value === c) return
		category.value = c
		reload()
	}

	async function addShelf(b) {
		if (b.inShelf) return
		try {
			await addToShelf({ bookId: b.id, shelfType: 1 })
			b.inShelf = true
			uni.showToast({ title: t('bookcity.addSuccess'), icon: 'none' })
		} catch (e) {}
	}

	function goDetail(b) {
		uni.navigateTo({ url: '/pages/book/detail?id=' + b.id })
	}

	onShow(() => {
		loadCategories()
		reload()
	})

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
		padding: 24rpx 24rpx 220rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		border-radius: var(--radius-pill);
		padding: 10rpx 12rpx 10rpx 16rpx;
	}

	.search-icon-wrap {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.search-icon {
		font-size: 24rpx;
	}

	.search-input {
		flex: 1;
		font-size: 26rpx;
		color: var(--text-primary);
		margin: 0 16rpx;
	}

	.search-go {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--primary-fill);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 4rpx 12rpx rgba(46, 74, 107, 0.18);
	}

	.search-go-text {
		font-size: 30rpx;
		color: #FFFFFF;
		line-height: 1;
	}

	.cate-scroll {
		margin-top: 24rpx;
		white-space: nowrap;
		width: 100%;
	}

	.cate-row {
		display: inline-flex;
		flex-shrink: 0;
		gap: 14rpx;
		white-space: nowrap;
	}

	.cate-item {
		flex: 0 0 auto;
		padding: 12rpx 32rpx;
		border-radius: var(--radius-pill);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		box-shadow: var(--shadow-sm);
		transition: all 0.2s ease;
	}

	.cate-item.active {
		background: var(--primary-fill);
		border-color: var(--primary-fill);
		box-shadow: 0 4rpx 12rpx rgba(46, 74, 107, 0.18);
	}

	.cate-text {
		font-size: 24rpx;
		line-height: 1.3;
		letter-spacing: 0.5rpx;
		white-space: nowrap;
		color: var(--text-secondary);
	}

	.cate-item.active .cate-text {
		color: var(--on-primary);
		font-weight: 600;
	}

	.book-card {
		margin-top: 24rpx;
		padding: 24rpx;
		display: flex;
		border-radius: var(--radius-lg);
	}

	.cover-wrap {
		border-radius: var(--radius-sm);
		overflow: hidden;
		box-shadow: 0 12rpx 24rpx rgba(27, 32, 48, 0.18);
		flex-shrink: 0;
	}

	.b-cover {
		width: 160rpx;
		height: 216rpx;
		display: block;
		background: var(--surface-2);
	}

	.css-cover {
		background: var(--primary-fill);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cover-char {
		font-size: 56rpx;
		font-weight: 700;
		color: #FFFFFF;
	}

	.b-main {
		flex: 1;
		min-width: 0;
		margin-left: 24rpx;
		display: flex;
		flex-direction: column;
	}

	.b-title-row {
		display: flex;
		align-items: center;
	}

	.b-title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		flex: 1;
		min-width: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.b-ebook {
		background: var(--primary-soft);
		border-radius: var(--radius-pill);
		padding: 3rpx 12rpx;
		margin-left: 12rpx;
		flex-shrink: 0;
	}

	.b-ebook-text {
		font-size: 19rpx;
		font-weight: 500;
		color: var(--primary);
	}

	.b-author {
		font-size: 23rpx;
		color: var(--text-secondary);
		margin-top: 10rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.b-foot {
		margin-top: auto;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.b-count-wrap {
		display: flex;
		align-items: center;
	}

	.b-count-icon {
			font-size: 22rpx;
			margin-right: 6rpx;
			color: var(--warning);
		}

	.b-count {
		font-size: 21rpx;
		color: var(--text-secondary);
	}

	.b-add {
			padding: 9rpx 26rpx;
			border-radius: var(--radius-pill);
			background: var(--brand-gradient);
			box-shadow: 0 8rpx 16rpx rgba(46, 74, 107, 0.24);
			display: flex;
			align-items: center;
		}

		.b-add-check {
			font-size: 22rpx;
			margin-right: 6rpx;
			color: var(--success);
		}

	.b-add.added {
		background: rgba(46, 125, 91, 0.12);
		box-shadow: none;
	}

	.b-add-text {
		font-size: 21rpx;
		font-weight: 500;
		color: #FFFFFF;
	}

	.b-add.added .b-add-text {
		color: var(--success);
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
