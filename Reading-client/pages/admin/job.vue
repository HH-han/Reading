<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="btn-primary create-btn" @click="openForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.job.create') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="file-edit-line" class="empty-icon" />
				<text>{{ $t('admin.job.empty') }}</text>
			</view>

			<view class="job-card glass-card" v-for="item in list" :key="item.id">
				<view class="j-head">
					<text class="j-title">{{ item.title }}</text>
					<view class="j-status" :class="'s' + item.status">
						<view class="j-status-dot"></view>
						<text class="j-status-text">{{ $t('admin.job.status' + item.status) }}</text>
					</view>
				</view>
				<view class="j-meta-row">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="book-2-line" /></view>
					<text class="j-meta">{{ item.bookTitle || '-' }}</text>
				</view>
				<view class="j-meta-row" v-if="item.chapterStart || item.chapterEnd">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="book-open-line" /></view>
					<text class="j-meta">{{ $t('admin.job.chapterRange', { s: item.chapterStart || '-', e: item.chapterEnd || '-' }) }}</text>
				</view>
				<view class="j-meta-row">
					<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="calendar-line" /></view>
					<text class="j-meta">{{ item.startDate || '-' }} → {{ item.endDate || '-' }}</text>
				</view>
				<view class="j-ops">
					<view class="j-op" @click="openForm(item)">
						<r-icon name="ball-pen-line" class="j-op-text" />
						<text class="j-op-text">{{ $t('common.edit') }}</text>
					</view>
					<view class="j-op danger" @click="onDelete(item)">
						<r-icon name="delete-bin-line" class="j-op-text danger" />
						<text class="j-op-text danger">{{ $t('common.delete') }}</text>
					</view>
				</view>
			</view>

			<view class="load-more" v-if="list.length">
				<view class="load-dot"></view>
				<text class="load-more-text">{{ noMore ? $t('common.noMore') : $t('common.loading') }}</text>
			</view>
		</view>

		<!-- 新建/编辑弹层 -->
		<view class="ui-modal-mask" v-if="showForm" @click="showForm = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ form.id ? $t('admin.job.edit') : $t('admin.job.create') }}</text>
					<view class="ui-sheet-close" @click="showForm = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<view class="field">
						<text class="field__label">{{ $t('admin.job.name') }}</text>
						<input class="input-item" v-model="form.title" :placeholder="$t('admin.job.namePh')" placeholder-class="input-ph" />
					</view>

					<view class="field">
						<text class="field__label">{{ $t('admin.job.book') }}</text>
						<r-picker
							:range="bookList"
							range-key="title"
							:value="bookIndex"
							:title="$t('admin.job.book')"
							:placeholder="$t('admin.job.bookPh')"
							@change="onBookChange"
						/>
					</view>

					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.job.chapterStart') }}</text>
							<input class="input-item" type="number" v-model="form.chapterStart" placeholder-class="input-ph" />
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.job.chapterEnd') }}</text>
							<input class="input-item" type="number" v-model="form.chapterEnd" placeholder-class="input-ph" />
						</view>
					</view>

					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.job.startDate') }}</text>
							<r-picker
								mode="date"
								:value="form.startDate"
								:title="$t('admin.job.startDate')"
								:placeholder="$t('admin.job.startDate')"
								@change="e => form.startDate = e.detail.value"
							/>
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.job.endDate') }}</text>
							<r-picker
								mode="date"
								:value="form.endDate"
								:title="$t('admin.job.endDate')"
								:placeholder="$t('admin.job.endDate')"
								@change="e => form.endDate = e.detail.value"
							/>
						</view>
					</view>

					<view class="field">
						<text class="field__label">{{ $t('admin.job.content') }}</text>
						<textarea class="input-item f-area" v-model="form.content" :placeholder="$t('admin.job.contentPh')" placeholder-class="input-ph" />
					</view>
				</scroll-view>
				<view class="modal-ops">
					<view class="btn-plain modal-btn" @click="showForm = false">{{ $t('common.cancel') }}</view>
					<view class="btn-primary modal-btn" @click="save">{{ $t('common.save') }}</view>
				</view>
			</view>
		</view>

		<r-modal />
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getJobPage, createJob, updateJob, deleteJob } from '@/api/admin.js'
	import { getBookPage } from '@/api/book.js'

	const list = ref([])
	const current = ref(1)
	const size = 20
	const noMore = ref(false)
	const loading = ref(false)

	const showForm = ref(false)
	const bookList = ref([])
	const bookIndex = ref(-1)
	const form = ref({})

	async function load(reset) {
		if (loading.value) return
		loading.value = true
		if (reset) {
			current.value = 1
			noMore.value = false
		}
		try {
			const data = await getJobPage({ current: current.value, size })
			const records = data.records || []
			list.value = reset ? records : list.value.concat(records)
			if (records.length < size) noMore.value = true
		} catch (e) {} finally {
			loading.value = false
		}
	}

	async function loadBooks() {
		if (bookList.value.length) return
		try {
			const data = await getBookPage({ current: 1, size: 100 })
			bookList.value = data.records || []
		} catch (e) {}
	}

	async function openForm(item) {
		await loadBooks()
		if (item) {
			form.value = { ...item }
			bookIndex.value = bookList.value.findIndex(b => b.id === item.bookId)
		} else {
			form.value = { title: '', bookId: null, chapterStart: '', chapterEnd: '', startDate: '', endDate: '', content: '' }
			bookIndex.value = -1
		}
		showForm.value = true
	}

	function onBookChange(e) {
		bookIndex.value = Number(e.detail.value)
		form.value.bookId = bookList.value[bookIndex.value].id
	}

	async function save() {
		if (!form.value.title) {
			uni.showToast({ title: t('admin.job.errName'), icon: 'none' })
			return
		}
		if (!form.value.bookId) {
			uni.showToast({ title: t('admin.job.errBook'), icon: 'none' })
			return
		}
		try {
			if (form.value.id) {
				await updateJob(form.value)
			} else {
				await createJob(form.value)
			}
			showForm.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load(true)
		} catch (e) {}
	}

	function onDelete(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.job.delConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteJob(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load(true)
				} catch (e) {}
			}
		})
	}

	onShow(() => load(true))

	onPullDownRefresh(async () => {
		await load(true)
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.create-btn {
		margin-bottom: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.create-icon {
		font-size: 30rpx;
		margin-right: 8rpx;
	}

	.job-card {
		position: relative;
		margin-bottom: 20rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
		overflow: hidden;
	}

	.job-card::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 6rpx;
		background: var(--brand-gradient);
		z-index: 2;
	}

	.j-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.j-title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		flex: 1;
		min-width: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.j-status {
		display: inline-flex;
		align-items: center;
		font-size: 20rpx;
		padding: 6rpx 16rpx;
		border-radius: var(--radius-pill);
		flex-shrink: 0;
		margin-left: 16rpx;
	}

	.j-status-dot {
		width: 8rpx;
		height: 8rpx;
		border-radius: 50%;
		background: currentColor;
		margin-right: 8rpx;
	}

	.j-status-text {
		font-size: 20rpx;
		color: inherit;
	}

	.j-status.s0 {
		background: var(--surface-2);
		color: var(--text-secondary);
	}

	.j-status.s1 {
		background: rgba(46, 125, 91, 0.13);
		color: var(--success);
	}

	.j-status.s2 {
		background: rgba(180, 101, 46, 0.15);
		color: var(--warning);
	}

	.j-meta-row {
		display: flex;
		align-items: center;
		margin-top: 18rpx;
	}

	.j-meta {
		font-size: 24rpx;
		color: var(--text-secondary);
		margin-left: 14rpx;
		flex: 1;
		min-width: 0;
	}

	.j-ops {
		display: flex;
		justify-content: flex-end;
		margin-top: 22rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid var(--border);
	}

	.j-op {
		padding: 8rpx 26rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		margin-left: 16rpx;
	}

	.j-op.danger {
		background: rgba(179, 69, 59, 0.08);
	}

	.j-op-text {
		font-size: 22rpx;
		color: var(--text-primary);
	}

	.j-op-text.danger {
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

	.form-modal {
		max-height: 84vh;
		display: flex;
		flex-direction: column;
		padding: 32rpx 28rpx 28rpx;
	}

	.modal-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8rpx;
	}

	.modal-head .ui-modal-title {
		text-align: left;
		font-size: 32rpx;
	}

	.modal-scroll {
		max-height: 56vh;
	}

	.field__row {
		display: flex;
		gap: 20rpx;
	}

	.field__col {
		flex: 1;
		min-width: 0;
	}

	.f-area {
		height: 170rpx;
		min-height: 170rpx;
	}

	.modal-ops {
		display: flex;
		margin-top: 28rpx;
		gap: 20rpx;
	}

	.modal-btn {
		flex: 1;
	}
</style>
