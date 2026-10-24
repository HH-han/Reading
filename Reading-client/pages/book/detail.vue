<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 书籍头部 -->
			<view class="head glass-card">
				<view class="head-glow"></view>
				<view class="cover-wrap">
					<image class="h-cover" v-if="book.cover" :src="book.cover" mode="aspectFill" />
					<view class="h-cover css-cover" v-else>
						<text class="cover-char">{{ (book.title || '书').slice(0, 1) }}</text>
					</view>
				</view>
				<view class="h-main">
					<text class="h-title">{{ book.title || '' }}</text>
					<view class="h-meta-row">
						<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="quill-pen-fill" /></view>
						<text class="h-meta">{{ $t('book.author') }}：{{ book.author || '-' }}</text>
					</view>
					<view class="h-meta-row">
						<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="building-4-line" /></view>
						<text class="h-meta">{{ $t('book.publisher') }}：{{ book.publisher || '-' }}</text>
					</view>
					<view class="h-meta-row" v-if="(book.chapters || []).length">
						<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="book-open-line" /></view>
						<text class="h-meta">{{ $t('book.chapterUnit', { n: book.chapters.length }) }}</text>
					</view>
				</view>
			</view>

			<!-- 简介 -->
			<view class="section glass-card" v-if="book.intro">
				<view class="section-head">
					<view class="section-accent"></view>
					<text class="section-title">{{ $t('book.intro') }}</text>
				</view>
				<text class="section-text">{{ book.intro }}</text>
			</view>

			<!-- 推荐理由 -->
			<view class="section glass-card" v-if="book.recommendReason">
				<view class="section-head">
					<view class="section-accent"></view>
					<text class="section-title">{{ $t('book.recommend') }}</text>
				</view>
				<view class="recommend-box">
					<text class="recommend-quote">“</text>
					<text class="section-text">{{ book.recommendReason }}</text>
				</view>
			</view>

			<!-- 章节列表 -->
			<view class="section glass-card" v-if="(book.chapters || []).length">
				<view class="section-head">
					<view class="section-accent"></view>
					<text class="section-title">{{ $t('book.chapters') }}</text>
				</view>
				<view class="chapter-item" v-for="(c, i) in book.chapters" :key="i">
					<text class="chapter-no">{{ i + 1 }}</text>
					<text class="chapter-name">{{ c.title || c }}</text>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar glass">
			<view class="bar-btn plain" :class="{ added: inShelf }" @click="addShelf">
				<r-icon v-if="inShelf" name="check-line" class="bar-btn-text" />
				<r-icon v-else name="add-line" class="bar-btn-text" />
				<text class="bar-btn-text">{{ inShelf ? $t('book.added') : $t('book.addShelf') }}</text>
			</view>
			<view class="bar-btn primary" @click="goCheckin">
				<r-icon name="quill-pen-fill" class="bar-btn-text light" />
				<text class="bar-btn-text light">{{ $t('book.checkinNow') }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { t } from '@/utils/i18n.js'
	import { onLoad } from '@dcloudio/uni-app'
	import { getBookDetail, addToShelf } from '@/api/book.js'

	const book = ref({})
	const inShelf = ref(false)

	onLoad(async (query) => {
		if (!query.id) return
		try {
			const data = await getBookDetail(query.id)
			book.value = data || {}
			inShelf.value = !!book.value.inShelf
		} catch (e) {}
	})

	async function addShelf() {
		if (inShelf.value || !book.value.id) return
		try {
			await addToShelf({ bookId: book.value.id, shelfType: 1 })
			inShelf.value = true
			uni.showToast({ title: t('bookcity.addSuccess'), icon: 'none' })
		} catch (e) {}
	}

	function goCheckin() {
		uni.navigateTo({ url: '/pages/checkin/checkin?bookId=' + book.value.id })
	}
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx 24rpx 200rpx;
	}

	.head {
		position: relative;
		padding: 32rpx 28rpx;
		display: flex;
		border-radius: var(--radius-xl);
		overflow: hidden;
	}

	.head-glow {
		position: absolute;
		width: 360rpx;
		height: 360rpx;
		border-radius: 50%;
		top: -220rpx;
		right: -120rpx;
		background: var(--brand-gradient-soft);
		pointer-events: none;
	}

	.cover-wrap {
		border-radius: var(--radius-sm);
		overflow: hidden;
		box-shadow: 0 16rpx 32rpx rgba(27, 32, 48, 0.22);
		flex-shrink: 0;
		position: relative;
		z-index: 1;
	}

	.h-cover {
		width: 200rpx;
		height: 270rpx;
		display: block;
		background: var(--surface-2);
	}

	.css-cover {
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cover-char {
		font-size: 64rpx;
		font-weight: 700;
		color: #FFFFFF;
	}

	.h-main {
		flex: 1;
		min-width: 0;
		margin-left: 28rpx;
		display: flex;
		flex-direction: column;
		position: relative;
		z-index: 1;
	}

	.h-title {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--text-primary);
		line-height: 1.35;
		margin-bottom: 8rpx;
	}

	.h-meta-row {
		display: flex;
		align-items: center;
		margin-top: 14rpx;
	}

	.h-meta {
		font-size: 23rpx;
		color: var(--text-secondary);
		margin-left: 12rpx;
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.section {
		margin-top: 24rpx;
		padding: 30rpx;
		border-radius: var(--radius-lg);
	}

	.section-head {
		display: flex;
		align-items: center;
		margin-bottom: 18rpx;
	}

	.section-accent {
		width: 8rpx;
		height: 28rpx;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		margin-right: 14rpx;
	}

	.section-title {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.section-text {
		font-size: 26rpx;
		color: var(--text-secondary);
		line-height: 1.8;
	}

	.recommend-box {
		position: relative;
		background: var(--surface-2);
		border-radius: var(--radius-md);
		padding: 20rpx 24rpx 20rpx 46rpx;
	}

	.recommend-quote {
		position: absolute;
		top: 6rpx;
		left: 14rpx;
		font-size: 46rpx;
		color: var(--primary);
		opacity: 0.5;
		line-height: 1;
	}

	.chapter-item {
		display: flex;
		align-items: center;
		padding: 18rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.chapter-item:last-child {
		border-bottom: none;
	}

	.chapter-no {
		width: 48rpx;
		height: 48rpx;
		border-radius: 14rpx;
		background: var(--primary-soft);
		font-size: 21rpx;
		font-weight: 600;
		color: var(--primary);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.chapter-name {
		font-size: 26rpx;
		color: var(--text-primary);
		margin-left: 20rpx;
		flex: 1;
		min-width: 0;
	}

	.bottom-bar {
		position: fixed;
		left: 24rpx;
		right: 24rpx;
		bottom: calc(24rpx + constant(safe-area-inset-bottom));
		bottom: calc(24rpx + env(safe-area-inset-bottom));
		border-radius: var(--radius-pill);
		padding: 14rpx;
		display: flex;
		box-shadow: var(--shadow-lg);
		z-index: 10;
	}

	.bar-btn {
		flex: 1;
		border-radius: var(--radius-pill);
		text-align: center;
		padding: 20rpx 0;
		margin: 0 6rpx;
	}

	.bar-btn.plain {
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
	}

	.bar-btn.plain.added {
		background: rgba(46, 125, 91, 0.10);
		border-color: rgba(46, 125, 91, 0.3);
	}

	.bar-btn.primary {
		background: var(--brand-gradient);
		box-shadow: 0 10rpx 20rpx rgba(46, 74, 107, 0.3);
	}

	.bar-btn-text {
		font-size: 27rpx;
		color: var(--text-primary);
		font-weight: 600;
	}

	.bar-btn.plain.added .bar-btn-text {
		color: var(--success);
	}

	.bar-btn-text.light {
		color: #FFFFFF;
	}
</style>
