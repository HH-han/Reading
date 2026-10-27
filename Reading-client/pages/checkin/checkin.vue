<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 打卡类型：仅模板允许多种类型时展示 -->
			<view class="section glass-card" v-if="availableTypes.length > 1">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="focus-3-line" /></view>
					<text class="section-title">{{ $t('checkin.type') }}</text>
				</view>
				<view class="type-tabs">
					<view
						v-for="tp in availableTypes"
						:key="tp.value"
						class="type-tab"
						:class="{ active: checkinType === tp.value }"
						@click="checkinType = tp.value"
					>
						<r-icon :name="tp.icon" class="type-tab-icon" />
						<text class="type-tab-text">{{ $t(tp.label) }}</text>
					</view>
				</view>
			</view>

			<!-- 选择书籍 -->
			<view class="section glass-card" v-if="requireMap.book">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="book-2-line" /></view>
					<text class="section-title">{{ $t('checkin.selectBook') }}</text>
					<text class="req-pill on">{{ $t('checkin.required') }}</text>
				</view>
				<view class="book-picker" @click="openBookPicker">
					<template v-if="selectedBook.id">
						<view class="picker-cover-wrap">
							<image class="picker-cover" v-if="selectedBook.cover" :src="selectedBook.cover" mode="aspectFill" />
							<view class="picker-cover css-cover" v-else>
								<text class="cover-char">{{ (selectedBook.title || '书').slice(0, 1) }}</text>
							</view>
						</view>
						<view class="picker-info">
							<text class="picker-title">《{{ selectedBook.title }}》</text>
							<text class="picker-author">{{ selectedBook.author || '' }}</text>
						</view>
						<view class="picker-change">{{ $t('checkin.repick') }} <r-icon name="arrow-right-s-line" /></view>
					</template>
					<template v-else>
						<view class="icon-chip icon-chip--sm icon-gray"><r-icon name="add-line" /></view>
						<text class="picker-ph">{{ $t('checkin.selectBookPh') }}</text>
						<r-icon name="arrow-right-s-line" class="picker-arrow" />
					</template>
				</view>
			</view>

			<!-- 章节标题 -->
			<view class="section glass-card" v-if="requireMap.chapter">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="book-open-line" /></view>
					<text class="section-title">{{ $t('checkin.chapter') }}</text>
					<text class="req-pill on">{{ $t('checkin.required') }}</text>
				</view>
				<input
					class="input-item"
					v-model="chapterTitle"
					:placeholder="$t('checkin.chapterPh')"
					placeholder-class="input-ph"
				/>
			</view>

			<!-- 图片 -->
			<view class="section glass-card" v-if="requireMap.image">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-green"><r-icon name="image-line" /></view>
					<text class="section-title">{{ $t('checkin.typeImage') }}</text>
					<text class="req-pill on">{{ $t('checkin.required') }}</text>
				</view>
				<view class="img-grid">
					<view class="img-cell" v-for="(img, i) in imageList" :key="i">
						<image class="img-preview" :src="img" mode="aspectFill" />
						<view class="img-del" @click="imageList.splice(i, 1)">
							<r-icon name="close-line" class="img-del-text" />
						</view>
					</view>
					<view class="img-add" v-if="imageList.length < 9" @click="chooseImage">
						<r-icon name="add-line" class="img-add-icon" />
						<text class="img-add-text">{{ $t('checkin.chooseImage') }}</text>
					</view>
				</view>
			</view>

			<!-- 音频 -->
			<view class="section glass-card" v-if="requireMap.audio">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-pink"><r-icon name="mic-line" /></view>
					<text class="section-title">{{ $t('checkin.typeAudio') }}</text>
					<text class="req-pill on">{{ $t('checkin.required') }}</text>
				</view>
				<view class="audio-picker" @click="chooseAudio">
					<view class="audio-icon-wrap">
						<r-icon name="mic-line" class="audio-picker-icon" />
					</view>
					<text class="audio-picker-text">{{ audioFile ? ($t('checkin.audioChosen') + '：' + audioFile.name) : $t('checkin.chooseAudio') }}</text>
					<r-icon name="arrow-right-s-line" class="audio-arrow" />
				</view>
			</view>

			<!-- 心得 -->
			<view class="section glass-card" v-if="requireMap.note">
				<view class="note-head">
					<view class="section-head">
						<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="file-edit-line" /></view>
						<text class="section-title">{{ $t('checkin.typeNote') }}</text>
					</view>
					<text class="word-tip" v-if="minWordCount">{{ $t('checkin.minWords', { n: minWordCount }) }} · {{ $t('checkin.wordCount', { n: content.length }) }}</text>
				</view>
				<textarea
					class="note-area"
					v-model="content"
					:placeholder="$t('checkin.notePh')"
					placeholder-class="input-ph"
					maxlength="2000"
				/>
			</view>

			<view class="btn-primary submit-btn" @click="submit">{{ editId ? $t('checkin.saveBtn') : $t('checkin.submitBtn') }}</view>
		</view>

		<!-- 书架选择弹层 -->
		<view class="ui-sheet-mask" v-if="showBookPicker" @click="showBookPicker = false">
			<view class="ui-sheet-panel" @click.stop>
				<view class="ui-sheet-handle"></view>
				<view class="ui-sheet-head">
					<text class="ui-sheet-title">{{ $t('checkin.selectBook') }}</text>
					<view class="ui-sheet-close" @click="showBookPicker = false"><r-icon name="close-line" /></view>
				</view>
				<view class="empty-box" v-if="!shelfBooks.length">
					<r-icon name="book-2-line" class="empty-icon" />
					<text>{{ $t('checkin.shelfEmpty') }}</text>
				</view>
				<scroll-view scroll-y class="book-scroll" v-else>
					<view class="shelf-item" v-for="b in shelfBooks" :key="b.bookId || b.id" @click="pickBook(b)">
						<image class="shelf-cover" v-if="b.cover" :src="b.cover" mode="aspectFill" />
						<view class="shelf-cover css-cover" v-else>
							<text class="cover-char small">{{ (b.title || '书').slice(0, 1) }}</text>
						</view>
						<view class="shelf-info">
							<text class="shelf-title">{{ b.title }}</text>
							<text class="shelf-author">{{ b.author || '' }}</text>
						</view>
						<view class="shelf-pick" :class="{ picked: (selectedBook.id && (selectedBook.id === (b.bookId || b.id))) }">
							<r-icon v-if="selectedBook.id && selectedBook.id === (b.bookId || b.id)" name="check-line" class="shelf-pick-text" />
							<r-icon v-else name="arrow-right-s-line" class="shelf-pick-text" />
						</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<float-tabbar :current="2" />
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { t } from '@/utils/i18n.js'
	import { onLoad, onShow } from '@dcloudio/uni-app'
	import { getShelf, getBookDetail } from '@/api/book.js'
	import { getCheckinTemplate, submitCheckin, getCheckinDetail, updateCheckin } from '@/api/checkin.js'
	import { uploadFile } from '@/api/file.js'
	import FloatTabbar from '@/components/float-tabbar/float-tabbar.vue'

	const types = [
		{ value: 1, icon: 'image-line', label: 'checkin.typeImage' },
		{ value: 2, icon: 'mic-line', label: 'checkin.typeAudio' },
		{ value: 3, icon: 'file-edit-line', label: 'checkin.typeNote' }
	]
	const allTypeValues = [1, 2, 3]

	const selectedBook = ref({})
	const chapterTitle = ref('')
	const checkinType = ref(3)
	const imageList = ref([])
	const audioFile = ref(null)
	const content = ref('')
	const minWordCount = ref(0)
	const checkinTemplate = ref(null)
	// 编辑模式下为记录ID
	const editId = ref(null)

	const showBookPicker = ref(false)
	const shelfBooks = ref([])

	// 模板允许的打卡类型，仅这些类型会在类型切换中展示
	const allowedTypes = computed(() => {
		const raw = checkinTemplate.value ? checkinTemplate.value.checkinTypes : ''
		if (!raw) return allTypeValues
		const list = String(raw).split(',').map(Number).filter(v => allTypeValues.includes(v))
		return list.length ? list : allTypeValues
	})
	const availableTypes = computed(() => types.filter(tp => allowedTypes.value.includes(tp.value)))

	function normalizeReq(cfg) {
		const c = cfg || {}
		return {
			book: c.book ? 1 : 0,
			chapter: c.chapter ? 1 : 0,
			image: c.image ? 1 : 0,
			audio: c.audio ? 1 : 0,
			note: c.note ? 1 : 0
		}
	}

	// 模板按打卡类型配置的必填项：{ 1: { book, chapter, image, audio, note }, ... }
	const typeRequireConfig = computed(() => {
		const tpl = checkinTemplate.value
		const result = {}
		if (!tpl) return result
		let obj = tpl.typeConfig
		if (typeof obj === 'string') {
			try {
				obj = JSON.parse(obj)
			} catch (e) {
				obj = null
			}
		}
		if (obj && typeof obj === 'object') {
			Object.keys(obj).forEach(k => {
				result[k] = normalizeReq(obj[k])
			})
			return result
		}
		// 旧模板没有按类型配置时，回退到全局必填开关（书籍/章节默认展示）
		allTypeValues.forEach(tp => {
			result[tp] = {
				book: 1,
				chapter: 1,
				image: tpl.requireImage ? 1 : 0,
				audio: tpl.requireAudio ? 1 : 0,
				note: tpl.requireContent ? 1 : 0
			}
		})
		return result
	})

	// 当前打卡类型需要渲染并校验的卡片
	const requireMap = computed(() => {
		const type = checkinType.value
		const cfg = typeRequireConfig.value[type]
		if (!cfg) {
			// 未配置模板时沿用默认规则
			return { book: type !== 1, chapter: type !== 1, image: type === 1, audio: type === 2, note: type === 3 }
		}
		// 打卡类型自身的核心内容始终展示并必填
		return {
			book: !!cfg.book,
			chapter: !!cfg.chapter,
			image: type === 1 || !!cfg.image,
			audio: type === 2 || !!cfg.audio,
			note: type === 3 || !!cfg.note
		}
	})

	onLoad((options) => {
		loadShelf()
		if (options && options.id) {
			editId.value = Number(options.id)
			uni.setNavigationBarTitle({ title: t('checkin.editTitle') })
			loadRecord(editId.value)
			return
		}
		const bookId = options && options.bookId
		if (bookId && bookId !== 'undefined') {
			getBookDetail(bookId)
				.then(data => {
					selectedBook.value = pickBookInfo(data)
				})
				.catch(() => {})
		}
	})

	// 书籍详情接口返回 { book, chapters }，这里统一取出书籍信息
	function pickBookInfo(data) {
		return (data && data.book) ? { ...data.book } : (data || {})
	}

	// 每次进入页面都按当前选择的读书圈重新拉取模板，保证卡片渲染与所设模板一致
	onShow(() => {
		loadTemplate()
	})

	async function loadShelf() {
		try {
			const data = await getShelf({ shelfType: 1 })
			shelfBooks.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {}
	}

	async function loadTemplate() {
		try {
			const data = await getCheckinTemplate()
			if (!data) return
			checkinTemplate.value = data
			minWordCount.value = data.minWordCount || 0
			if (!allowedTypes.value.includes(checkinType.value)) {
				checkinType.value = allowedTypes.value[0]
			}
		} catch (e) {}
	}

	// 编辑模式：回填本人打卡记录
	async function loadRecord(id) {
		try {
			const rec = await getCheckinDetail(id)
			if (!rec) return
			checkinType.value = rec.checkinType || 3
			chapterTitle.value = rec.chapterTitle || ''
			content.value = rec.content || ''
			imageList.value = rec.mediaUrl ? String(rec.mediaUrl).split(',').filter(Boolean) : []
			audioFile.value = rec.audioUrl
				? { name: String(rec.audioUrl).split('/').pop(), path: rec.audioUrl }
				: null
			if (rec.bookId) {
				try {
					selectedBook.value = pickBookInfo(await getBookDetail(rec.bookId))
				} catch (e) {
					selectedBook.value = { id: rec.bookId }
				}
			}
		} catch (e) {}
	}

	// 已上传的文件（服务器地址）无需再次上传
	function isRemoteUrl(path) {
		return /^https?:\/\//i.test(path) || String(path).startsWith('/uploads/')
	}

	function openBookPicker() {
		loadShelf()
		showBookPicker.value = true
	}

	function pickBook(b) {
		selectedBook.value = { ...b, id: b.bookId || b.id }
		showBookPicker.value = false
	}

	function chooseImage() {
		uni.chooseImage({
			count: 9 - imageList.value.length,
			success: (res) => {
				imageList.value = imageList.value.concat(res.tempFilePaths)
			}
		})
	}

	function chooseAudio() {
		// #ifdef MP-WEIXIN
		uni.chooseMessageFile({
			count: 1,
			type: 'file',
			extension: ['mp3', 'm4a', 'aac', 'wav'],
			success: (res) => {
				if (res.tempFiles && res.tempFiles.length) {
					audioFile.value = res.tempFiles[0]
				}
			}
		})
		// #endif
		// #ifndef MP-WEIXIN
		uni.showToast({ title: t('checkin.audioNotSupport'), icon: 'none' })
		// #endif
	}

	async function submit() {
		if (requireMap.value.book && !selectedBook.value.id) {
			uni.showToast({ title: t('checkin.errBook'), icon: 'none' })
			return
		}
		if (requireMap.value.chapter && !chapterTitle.value.trim()) {
			uni.showToast({ title: t('checkin.errChapter'), icon: 'none' })
			return
		}
		if (requireMap.value.image && !imageList.value.length) {
			uni.showToast({ title: t('checkin.errMedia'), icon: 'none' })
			return
		}
		if (requireMap.value.audio && !audioFile.value) {
			uni.showToast({ title: t('checkin.errAudio'), icon: 'none' })
			return
		}
		if (requireMap.value.note) {
			if (!content.value.trim()) {
				uni.showToast({ title: t('checkin.errNote'), icon: 'none' })
				return
			}
			if (minWordCount.value && content.value.trim().length < minWordCount.value) {
				uni.showToast({ title: t('checkin.errContent'), icon: 'none' })
				return
			}
		}

		uni.showLoading({ title: t('checkin.uploading'), mask: true })
		try {
			let mediaUrl = ''
			let audioUrl = ''
			if (requireMap.value.image && imageList.value.length) {
				const urls = []
				for (const path of imageList.value) {
					urls.push(isRemoteUrl(path) ? path : await uploadFile(path))
				}
				mediaUrl = urls.join(',')
			}
			if (requireMap.value.audio && audioFile.value) {
				audioUrl = isRemoteUrl(audioFile.value.path)
					? audioFile.value.path
					: await uploadFile(audioFile.value.path)
			}
			const payload = {
				bookId: selectedBook.value.id,
				chapterTitle: chapterTitle.value.trim(),
				checkinType: checkinType.value,
				content: content.value.trim(),
				mediaUrl,
				audioUrl,
				audioDuration: 0
			}
			if (editId.value) {
				await updateCheckin(editId.value, payload)
			} else {
				await submitCheckin(payload)
			}
			uni.hideLoading()
			if (editId.value) {
				uni.showToast({ title: t('checkin.saveSuccess'), icon: 'success' })
				setTimeout(() => uni.navigateBack(), 800)
			} else {
				uni.showToast({ title: t('checkin.submitSuccess'), icon: 'success' })
				// 提交成功后回到首页并定位到“动态”面板
				setTimeout(() => uni.reLaunch({ url: '/pages/index/index?tab=2' }), 800)
			}
		} catch (e) {
			// 上传/提交失败时接口层已弹出 toast，toast 会接管并关闭 loading，
			// 这里只在没有 toast 的异常(如响应解析失败)下兜底关闭，避免 hideLoading 与 showLoading 不配对
			if (e instanceof Error) {
				uni.hideLoading()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.content {
		padding: 24rpx 24rpx 220rpx;
	}

	.section {
		padding: 32rpx 28rpx;
		margin-bottom: 24rpx;
		border-radius: var(--radius-lg);
	}

	.section-head {
		display: flex;
		align-items: center;
		margin-bottom: 22rpx;
	}

	.section-title {
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
		margin-left: 14rpx;
	}

	.req-pill {
		margin-left: auto;
		font-size: 20rpx;
		font-weight: 500;
		color: var(--text-tertiary);
		background: var(--surface-2);
		border-radius: var(--radius-pill);
		padding: 4rpx 14rpx;
		flex-shrink: 0;
	}

	.req-pill.on {
		color: var(--danger);
		background: rgba(179, 69, 59, 0.08);
	}

	/* 打卡类型切换 */
	.type-tabs {
		display: flex;
		gap: 16rpx;
	}

	.type-tab {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 84rpx;
		background: var(--surface-2);
		border: 2rpx solid transparent;
		border-radius: var(--radius-md);
		transition: all 0.2s ease;
	}

	.type-tab.active {
		border-color: var(--primary);
		background: var(--primary-soft);
	}

	.type-tab-icon {
		font-size: 30rpx;
		line-height: 1;
		color: var(--text-secondary);
	}

	.type-tab-text {
		font-size: 25rpx;
		color: var(--text-secondary);
		margin-left: 10rpx;
		white-space: nowrap;
	}

	.type-tab.active .type-tab-icon,
	.type-tab.active .type-tab-text {
		color: var(--primary);
		font-weight: 600;
	}

	.book-picker {
		display: flex;
		align-items: center;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		padding: 18rpx 20rpx;
		transition: border-color 0.2s ease;
	}

	.book-picker:active {
		border-color: rgba(46, 74, 107, 0.45);
	}

	.picker-cover-wrap {
		border-radius: 10rpx;
		overflow: hidden;
		box-shadow: 0 8rpx 16rpx rgba(27, 32, 48, 0.16);
		flex-shrink: 0;
	}

	.picker-cover {
		width: 72rpx;
		height: 96rpx;
		display: block;
	}

	.css-cover {
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.cover-char {
		font-size: 36rpx;
		font-weight: 700;
		color: #FFFFFF;
	}

	.cover-char.small {
		font-size: 28rpx;
	}

	.picker-info {
		flex: 1;
		margin-left: 20rpx;
		min-width: 0;
	}

	.picker-title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--text-primary);
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.picker-author {
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		display: block;
	}

	.picker-change {
		font-size: 23rpx;
		font-weight: 500;
		color: var(--primary);
		background: var(--primary-soft);
		border-radius: var(--radius-pill);
		padding: 8rpx 18rpx;
		flex-shrink: 0;
	}

	.picker-ph {
		flex: 1;
		font-size: 26rpx;
		color: var(--text-secondary);
		margin-left: 16rpx;
	}

	.picker-arrow {
		font-size: 32rpx;
		color: var(--text-tertiary);
	}

	.img-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.img-cell {
		position: relative;
		width: calc((100% - 32rpx) / 3);
		height: 200rpx;
		border-radius: var(--radius-sm);
		overflow: visible;
	}

	.img-preview {
		width: 100%;
		height: 100%;
		border-radius: var(--radius-sm);
		background: var(--surface-2);
	}

	.img-del {
		position: absolute;
		top: -12rpx;
		right: -12rpx;
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		background: var(--danger);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 10rpx rgba(179, 69, 59, 0.4);
	}

	.img-del-text {
		color: #FFFFFF;
		font-size: 20rpx;
		line-height: 1;
	}

	.img-add {
		width: calc((100% - 32rpx) / 3);
		height: 200rpx;
		border-radius: var(--radius-sm);
		border: 2rpx dashed var(--border-strong);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background: var(--surface-2);
	}

	.img-add-icon {
		font-size: 48rpx;
		color: var(--text-tertiary);
		line-height: 1;
	}

	.img-add-text {
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 10rpx;
	}

	.audio-picker {
		display: flex;
		align-items: center;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		padding: 24rpx;
	}

	.audio-icon-wrap {
		width: 64rpx;
		height: 64rpx;
		border-radius: 20rpx;
		background: var(--bg-secondary);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: var(--shadow-sm);
	}

	.audio-picker-icon {
		font-size: 34rpx;
		line-height: 1;
	}

	.audio-picker-text {
		flex: 1;
		font-size: 26rpx;
		color: var(--text-primary);
		margin-left: 18rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.audio-arrow {
		font-size: 30rpx;
		color: var(--text-tertiary);
		margin-left: 12rpx;
	}

	.note-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 22rpx;
	}

	.note-head .section-head {
		margin-bottom: 0;
	}

	.word-tip {
		font-size: 20rpx;
		color: var(--text-tertiary);
	}

	.note-area {
		width: 100%;
		height: 260rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		padding: 24rpx;
		font-size: 26rpx;
		color: var(--text-primary);
		box-sizing: border-box;
		line-height: 1.7;
	}

	.submit-btn {
		margin-top: 16rpx;
	}

	.book-scroll {
		max-height: 56vh;
	}

	.shelf-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.shelf-item:last-child {
		border-bottom: none;
	}

	.shelf-cover {
		width: 64rpx;
		height: 88rpx;
		border-radius: 8rpx;
		flex-shrink: 0;
		box-shadow: 0 6rpx 14rpx rgba(27, 32, 48, 0.16);
	}

	.shelf-info {
		margin-left: 20rpx;
		flex: 1;
		min-width: 0;
	}

	.shelf-title {
		font-size: 27rpx;
		font-weight: 600;
		color: var(--text-primary);
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.shelf-author {
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		display: block;
	}

	.shelf-pick {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.shelf-pick.picked {
		background: var(--brand-gradient);
	}

	.shelf-pick-text {
		font-size: 22rpx;
		color: var(--text-secondary);
		line-height: 1;
	}

	.shelf-pick.picked .shelf-pick-text {
		color: #FFFFFF;
	}
</style>
