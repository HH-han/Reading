<template>
	<view class="shelf-panel">
		<!-- 书架 Tab -->
		<view class="segmented segmented--brand shelf-tabs">
			<view
				v-for="(tb, i) in shelfTabs"
				:key="i"
				class="segmented__item"
				:class="{ 'segmented__item--active': shelfType === i + 1 }"
				@click="switchType(i + 1)"
			>{{ $t(tb) }}</view>
		</view>

		<view class="empty-box" v-if="!loading && !books.length">
			<r-icon name="book-2-line" class="empty-icon" />
			<text>{{ $t('index.shelfEmpty') }}</text>
			<text class="empty-desc">{{ $t('index.shelfEmptyDesc') }}</text>
		</view>

		<view class="book-list">
			<view class="book-item glass-card" v-for="b in books" :key="b.bookId || b.id" @click="goDetail(b)">
				<view class="cover-wrap">
					<image class="book-cover" v-if="b.cover" :src="b.cover" mode="aspectFill" />
					<view class="book-cover css-cover" v-else>
						<text class="cover-char">{{ (b.title || '书').slice(0, 1) }}</text>
					</view>
				</view>
				<view class="book-info">
					<text class="book-title">{{ b.title || '-' }}</text>
					<text class="book-author">{{ b.author || '' }}</text>
					<text class="book-progress" v-if="b.progress != null">{{ b.progress }}%</text>
				</view>
				<r-icon name="arrow-right-s-line" class="book-arrow" />
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue'
	import { getShelf } from '@/api/book.js'

	const shelfTabs = ['index.shelfMine', 'index.shelfGroup', 'index.shelfFeatured']
	const shelfType = ref(1)
	const books = ref([])
	const loading = ref(false)

	function switchType(v) {
		if (shelfType.value === v) return
		shelfType.value = v
		load()
	}

	async function load() {
		loading.value = true
		try {
			const data = await getShelf({ shelfType: shelfType.value })
			books.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			books.value = []
		} finally {
			loading.value = false
		}
	}

	function goDetail(b) {
		const id = b.bookId || b.id
		if (id) uni.navigateTo({ url: '/pages/book/detail?id=' + id })
	}

	onMounted(load)

	defineExpose({
		refresh: load
	})
</script>

<style lang="scss" scoped>
	.shelf-tabs {
		margin-bottom: 24rpx;
	}

	.empty-desc {
		font-size: 22rpx;
		margin-top: 12rpx;
	}

	.book-item {
		display: flex;
		align-items: center;
		padding: 24rpx;
		margin-bottom: 20rpx;
		border-radius: var(--radius-lg);
	}

	.cover-wrap {
		border-radius: var(--radius-sm);
		overflow: hidden;
		box-shadow: 0 10rpx 20rpx rgba(27, 32, 48, 0.16);
		flex-shrink: 0;
	}

	.book-cover {
		width: 110rpx;
		height: 150rpx;
		display: block;
	}

	.css-cover {
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cover-char {
		font-size: 48rpx;
		font-weight: 700;
		color: #FFFFFF;
	}

	.book-info {
		margin-left: 24rpx;
		flex: 1;
		min-width: 0;
	}

	.book-title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.book-author {
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 10rpx;
		display: block;
	}

	.book-progress {
		display: inline-block;
		font-size: 20rpx;
		color: var(--primary);
		background: var(--primary-soft);
		padding: 4rpx 14rpx;
		border-radius: var(--radius-pill);
		margin-top: 12rpx;
	}

	.book-arrow {
		font-size: 30rpx;
		color: var(--text-tertiary);
		margin-left: 12rpx;
		flex-shrink: 0;
	}
</style>
