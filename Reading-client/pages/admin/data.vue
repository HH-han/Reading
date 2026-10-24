<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 导出 -->
			<view class="section glass-card">
				<view class="sec-head">
					<view class="icon-chip icon-chip--sm icon-green"><r-icon name="upload-2-line" /></view>
					<text class="section-title">{{ $t('admin.data.export') }}</text>
				</view>
				<view class="f-row">
					<view class="f-col">
						<text class="f-label">{{ $t('admin.data.startDate') }}</text>
						<r-picker
							mode="date"
							:value="exportStart"
							:title="$t('admin.data.startDate')"
							:placeholder="$t('admin.data.startDate')"
							@change="e => exportStart = e.detail.value"
						/>
					</view>
					<view class="f-col">
						<text class="f-label">{{ $t('admin.data.endDate') }}</text>
						<r-picker
							mode="date"
							:value="exportEnd"
							:title="$t('admin.data.endDate')"
							:placeholder="$t('admin.data.endDate')"
							@change="e => exportEnd = e.detail.value"
						/>
					</view>
				</view>
				<view class="btn-primary sec-btn" @click="genLink">{{ $t('admin.data.genLink') }}</view>
				<view class="link-box" v-if="exportLink">
					<text class="link-text" user-select>{{ exportLink }}</text>
					<view class="btn-plain sec-btn" @click="copyLink">{{ $t('admin.data.copyLink') }}</view>
					<text class="link-tip">{{ $t('admin.data.linkTip') }}</text>
				</view>
			</view>

			<!-- 导入 -->
			<view class="section glass-card">
				<view class="sec-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="download-2-line" /></view>
					<text class="section-title">{{ $t('admin.data.importData') }}</text>
				</view>
				<view class="btn-plain sec-btn" @click="chooseFile">{{ $t('admin.data.chooseFile') }}</view>
				<view class="file-chip" v-if="importFile">
					<r-icon name="file-text-line" class="file-icon" />
					<text class="file-name">{{ $t('admin.data.fileChosen') }}：{{ importFile.name }}</text>
				</view>
			</view>

			<!-- 管理员代打卡 -->
			<view class="section glass-card">
				<view class="sec-head">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="ball-pen-line" /></view>
					<text class="section-title">{{ $t('admin.data.proxy') }}</text>
				</view>
				<view class="f-row">
					<view class="f-col">
						<text class="f-label">{{ $t('admin.data.member') }}</text>
						<input class="input-item" type="number" v-model="proxy.userId" :placeholder="$t('admin.data.memberPh')" placeholder-class="input-ph" />
					</view>
				</view>

				<text class="f-label">{{ $t('admin.data.book') }}</text>
				<r-picker
					:range="bookList"
					range-key="title"
					:value="bookIndex"
					:title="$t('admin.data.book')"
					:placeholder="$t('admin.data.book')"
					@change="onBookChange"
				/>

				<text class="f-label">{{ $t('admin.data.type') }}</text>
				<r-picker
					:range="typeLabels"
					:value="typeIndex"
					:title="$t('admin.data.type')"
					@change="onTypeChange"
				/>

				<text class="f-label">{{ $t('admin.data.chapter') }}</text>
				<input class="input-item" v-model="proxy.chapterTitle" :placeholder="$t('admin.data.chapterPh')" placeholder-class="input-ph" />

				<view class="btn-primary sec-btn" @click="submitProxy">{{ $t('admin.data.submitCheckin') }}</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { BASE_URL } from '@/utils/request.js'
	import { onShow } from '@dcloudio/uni-app'
	import { proxyCheckin } from '@/api/admin.js'
	import { getBookPage } from '@/api/book.js'
	import { uploadFile } from '@/api/file.js'

	/* 导出 */
	const exportStart = ref('')
	const exportEnd = ref('')
	const exportLink = ref('')

	function genLink() {
		const circleId = (userState.userInfo && userState.userInfo.circleId) || ''
		exportLink.value = `${BASE_URL}/admin/export?circleId=${circleId}&startDate=${exportStart.value}&endDate=${exportEnd.value}`
	}

	function copyLink() {
		uni.setClipboardData({
			data: exportLink.value,
			success: () => uni.showToast({ title: t('common.copied'), icon: 'none' })
		})
	}

	/* 导入 */
	const importFile = ref(null)

	function chooseFile() {
		// #ifdef MP-WEIXIN
		uni.chooseMessageFile({
			count: 1,
			type: 'file',
			extension: ['xlsx'],
			success: async (res) => {
				const file = res.tempFiles[0]
				try {
					await uploadFile(file.path, '/admin/import')
					uni.showToast({ title: t('admin.data.importSuccess'), icon: 'none' })
				} catch (e) {}
			}
		})
		// #endif
		// #ifndef MP-WEIXIN
		uni.showToast({ title: t('admin.data.importNotSupport'), icon: 'none' })
		// #endif
	}

	/* 代打卡 */
	const bookList = ref([])
	const bookIndex = ref(-1)
	const typeIndex = ref(2)
	const typeValues = [1, 2, 3]
	const proxy = ref({ userId: '', bookId: null, checkinType: 3, chapterTitle: '' })

	const typeLabels = computed(() => [t('card.typeImage'), t('card.typeAudio'), t('card.typeNote')])

	async function loadBooks() {
		if (bookList.value.length) return
		try {
			const data = await getBookPage({ current: 1, size: 100 })
			bookList.value = data.records || []
		} catch (e) {}
	}

	function onBookChange(e) {
		bookIndex.value = Number(e.detail.value)
		proxy.value.bookId = bookList.value[bookIndex.value].id
	}

	function onTypeChange(e) {
		typeIndex.value = Number(e.detail.value)
		proxy.value.checkinType = typeValues[typeIndex.value]
	}

	async function submitProxy() {
		if (!proxy.value.userId) {
			uni.showToast({ title: t('admin.data.errMember'), icon: 'none' })
			return
		}
		if (!proxy.value.bookId) {
			uni.showToast({ title: t('admin.data.errBook'), icon: 'none' })
			return
		}
		try {
			await proxyCheckin(proxy.value)
			uni.showToast({ title: t('admin.data.proxySuccess'), icon: 'none' })
		} catch (e) {}
	}

	onShow(loadBooks)
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx;
	}

	.section {
		margin-bottom: 24rpx;
		padding: 30rpx 28rpx;
		border-radius: var(--radius-lg);
	}

	.sec-head {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.section-title {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
		margin-left: 14rpx;
	}

	.f-row {
		display: flex;
		gap: 20rpx;
	}

	.f-col {
		flex: 1;
		min-width: 0;
	}

	.f-label {
		font-size: 24rpx;
		color: var(--text-secondary);
		display: block;
		margin: 22rpx 0 10rpx;
	}

	.sec-btn {
		margin-top: 26rpx;
	}

	.link-box {
		margin-top: 24rpx;
		padding: 22rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.link-text {
		font-size: 22rpx;
		color: var(--primary);
		word-break: break-all;
		display: block;
		line-height: 1.6;
	}

	.link-tip {
		font-size: 21rpx;
		color: var(--text-tertiary);
		display: block;
		margin-top: 14rpx;
	}

	.file-chip {
		display: flex;
		align-items: center;
		margin-top: 18rpx;
		padding: 16rpx 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.file-icon {
		font-size: 26rpx;
		margin-right: 10rpx;
	}

	.file-name {
		font-size: 23rpx;
		color: var(--text-secondary);
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
