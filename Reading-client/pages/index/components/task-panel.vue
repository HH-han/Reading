<template>
	<view class="task-panel">
		<view class="empty-box" v-if="!loading && !jobs.length">
			<r-icon name="file-list-3-line" class="empty-icon" />
			<text>{{ $t('index.taskEmpty') }}</text>
		</view>
		<view class="job-card glass-card" v-for="job in jobs" :key="job.id">
			<view class="job-accent"></view>
			<view class="job-head">
				<text class="job-title">{{ job.title }}</text>
				<view class="job-status" :class="'status-' + job.status">
					<view class="status-dot"></view>
					<text class="status-text">{{ statusText(job.status) }}</text>
				</view>
			</view>
			<view class="job-row" v-if="job.bookTitle">
				<view class="icon-chip icon-chip--sm icon-blue">
					<r-icon name="book-2-line" />
				</view>
				<view class="job-row-main">
					<text class="job-label">{{ $t('index.taskBook') }}</text>
					<text class="job-value">《{{ job.bookTitle }}》</text>
				</view>
			</view>
			<view class="job-row" v-if="job.chapterStart || job.chapterEnd">
				<view class="icon-chip icon-chip--sm icon-violet">
					<r-icon name="book-open-line" />
				</view>
				<view class="job-row-main">
					<text class="job-label">{{ $t('index.taskChapter') }}</text>
					<text class="job-value">{{ $t('index.taskChapterUnit', { s: job.chapterStart || '-', e: job.chapterEnd || '-' }) }}</text>
				</view>
			</view>
			<view class="job-row">
				<view class="icon-chip icon-chip--sm icon-amber">
					<r-icon name="calendar-line" />
				</view>
				<view class="job-row-main">
					<text class="job-label">{{ $t('index.taskStart') }} / {{ $t('index.taskDeadline') }}</text>
					<view class="job-dates">
						<text class="job-value">{{ job.startDate || '-' }}</text>
						<text class="job-arrow">→</text>
						<text class="job-value deadline">{{ job.deadline || '-' }}</text>
					</view>
				</view>
			</view>
			<view class="job-content-box" v-if="job.content">
				<text class="job-content">{{ job.content }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue'
	import { t } from '@/utils/i18n.js'
	import { getJobPage } from '@/api/circle.js'

	const jobs = ref([])
	const loading = ref(false)

	function statusText(s) {
		return t('admin.job.status' + (s != null ? s : 1))
	}

	async function load() {
		loading.value = true
		try {
			const data = await getJobPage({ current: 1, size: 50 })
			jobs.value = data.records || []
		} catch (e) {
			jobs.value = []
		} finally {
			loading.value = false
		}
	}

	onMounted(load)

	defineExpose({
		refresh: load
	})
</script>

<style lang="scss" scoped>
	.job-card {
		position: relative;
		padding: 30rpx;
		margin-bottom: 24rpx;
		overflow: hidden;
	}

	.job-accent {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 6rpx;
		background: var(--brand-gradient);
	}

	.job-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.job-title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		flex: 1;
		min-width: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.job-status {
		display: inline-flex;
		align-items: center;
		font-size: 20rpx;
		font-weight: 500;
		padding: 6rpx 16rpx;
		border-radius: var(--radius-pill);
		margin-left: 16rpx;
		flex-shrink: 0;
	}

	.status-dot {
		width: 8rpx;
		height: 8rpx;
		border-radius: 50%;
		background: currentColor;
		margin-right: 8rpx;
	}

	.status-text {
		font-size: 20rpx;
		color: inherit;
	}

	.status-0 {
		background: var(--surface-2);
		color: var(--text-secondary);
	}

	.status-1 {
		background: rgba(46, 125, 91, 0.13);
		color: var(--success);
	}

	.status-2 {
		background: rgba(180, 101, 46, 0.15);
		color: var(--warning);
	}

	.job-row {
		display: flex;
		align-items: flex-start;
		margin-top: 20rpx;
	}

	.job-row-main {
		margin-left: 16rpx;
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
	}

	.job-label {
		font-size: 22rpx;
		color: var(--text-tertiary);
		line-height: 1.4;
	}

	.job-value {
		font-size: 26rpx;
		color: var(--text-primary);
		line-height: 1.5;
		margin-top: 2rpx;
	}

	.job-dates {
		display: flex;
		align-items: center;
	}

	.job-arrow {
		font-size: 22rpx;
		color: var(--text-tertiary);
		margin: 0 10rpx;
	}

	.job-value.deadline {
		color: var(--danger);
		font-weight: 600;
	}

	.job-content-box {
		margin-top: 22rpx;
		padding: 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.job-content {
		display: block;
		font-size: 24rpx;
		color: var(--text-secondary);
		line-height: 1.7;
	}
</style>
