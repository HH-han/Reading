<template>
	<view class="checkin-card glass-card">
		<!-- 头部 -->
		<view class="card-header">
			<view class="avatar-ring">
				<image class="avatar" :src="item.avatar || '/static/logo.png'" mode="aspectFill" />
			</view>
			<view class="header-main">
				<view class="name-row">
					<text class="nickname">{{ item.nickname || '-' }}</text>
					<view class="type-badge" :class="typeClass">
						<view class="badge-dot"></view>
						<text class="badge-text">{{ typeText }}</text>
					</view>
				</view>
				<view class="sub-row">
					<text class="date">{{ item.checkinDate || item.createTime || '' }}</text>
					<text class="dot">·</text>
					<text class="sub">{{ $t('card.daysChecked', { n: item.checkinDays || 0 }) }}</text>
					<text class="dot">·</text>
					<text class="sub">{{ $t('card.views', { n: item.viewCount || 0 }) }}</text>
				</view>
			</view>
		</view>

		<!-- 章节标题 -->
		<view class="chapter" v-if="item.chapterTitle">
			<view class="chapter-bar"></view>
			<text class="chapter-text">《{{ item.bookTitle || bookTitleFallback }}》{{ item.chapterTitle }}</text>
		</view>

		<!-- 音频 -->
		<view class="audio-bar" v-if="item.checkinType === 2 && item.audioUrl" @click="toggleAudio">
			<view class="audio-btn">
					<r-icon :name="playing ? 'pause-fill' : 'play-fill'" class="audio-btn-icon" />
				</view>
			<view class="audio-info">
				<text class="audio-text">{{ playing ? $t('card.audioPlaying') : $t('card.audioPlay') }}</text>
				<view class="audio-progress">
					<view class="audio-progress-inner" :style="{ width: playing ? '60%' : '0%' }"></view>
				</view>
			</view>
			<text class="audio-duration" v-if="item.audioDuration">{{ item.audioDuration }}″</text>
		</view>

		<!-- 心得文字 -->
		<view class="note-content" v-if="item.content">
			<text class="note-text" :class="{ collapsed: !expanded }">{{ item.content }}</text>
			<text class="expand-btn" v-if="needExpand" @click="expanded = !expanded">
				{{ expanded ? $t('card.collapse') : $t('card.expand') }}
			</text>
		</view>

		<!-- 图片九宫格 -->
		<view class="image-grid" v-if="images.length">
			<image
				v-for="(img, idx) in images"
				:key="idx"
				class="grid-img"
				:class="{ single: images.length === 1 }"
				:src="img"
				mode="aspectFill"
				@click="preview(idx)"
			/>
		</view>

		<!-- 操作行 -->
		<view class="action-row">
			<view class="action" :class="{ liked }" @click="toggleLike">
					<r-icon :name="liked ? 'heart-3-fill' : 'heart-3-line'" class="action-icon" />
					<text class="action-num">{{ likeCount || $t('card.like') }}</text>
				</view>
				<view class="action" @click="toggleComments">
					<r-icon name="chat-3-line" class="action-icon" />
					<text class="action-num">{{ item.commentCount || $t('card.comment') }}</text>
				</view>
				<view class="action" @click="onFav">
					<r-icon name="star-line" class="action-icon" />
					<text class="action-num">{{ $t('card.fav') }}</text>
				</view>
				<view class="action" @click="onShare">
					<r-icon name="share-forward-line" class="action-icon" />
					<text class="action-num">{{ $t('card.share') }}</text>
				</view>
				<view class="action" @click="onEdit" v-if="isOwner">
					<r-icon name="edit-2-line" class="action-icon" />
					<text class="action-num">{{ $t('card.edit') }}</text>
				</view>
				<view class="action" @click="onDelete" v-if="isOwner">
					<r-icon name="delete-bin-line" class="action-icon" />
					<text class="action-num">{{ $t('card.delete') }}</text>
				</view>
		</view>

		<!-- 评论区 -->
		<view class="comment-box" v-if="showComments">
			<view class="comment-list" v-if="comments.length">
				<view class="comment-item" v-for="c in comments" :key="c.id">
					<text class="comment-user">{{ c.nickname || '-' }}：</text>
					<text class="comment-content">{{ c.content }}</text>
				</view>
			</view>
			<view class="comment-empty" v-else>{{ $t('card.commentEmpty') }}</view>
			<view class="comment-input-row">
				<input
					class="comment-input"
					v-model="commentText"
					:placeholder="$t('card.commentPh')"
					placeholder-class="input-ph"
				/>
				<view class="comment-send" @click="sendComment">
					<text class="comment-send-text">{{ $t('card.send') }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { t } from '@/utils/i18n.js'
	import { userState } from '@/store/user.js'
	import { showModal } from '@/utils/modal.js'
	import { likeCheckin, unlikeCheckin, getComments, postComment, deleteCheckin } from '@/api/checkin.js'

	const props = defineProps({
		item: {
			type: Object,
			required: true
		}
	})

	const emit = defineEmits(['edit', 'deleted'])

	/* 仅发布者本人可编辑 */
	const isOwner = computed(() => {
		const uid = userState.userInfo && userState.userInfo.id
		return uid != null && String(uid) === String(props.item.userId)
	})

	const bookTitleFallback = computed(() => props.item.bookTitle || '')

	const typeText = computed(() => {
		const map = { 1: t('card.typeImage'), 2: t('card.typeAudio'), 3: t('card.typeNote') }
		return map[props.item.checkinType] || map[3]
	})
	const typeClass = computed(() => {
		const map = { 1: 'badge-image', 2: 'badge-audio', 3: 'badge-note' }
		return map[props.item.checkinType] || 'badge-note'
	})

	/* 图片 */
	const images = computed(() => {
		if (!props.item.mediaUrl) return []
		return String(props.item.mediaUrl).split(',').filter(Boolean)
	})

	function preview(idx) {
		uni.previewImage({ urls: images.value, current: idx })
	}

	/* 音频 */
	const playing = ref(false)
	let audioCtx = null

	function toggleAudio() {
		if (!audioCtx) {
			audioCtx = uni.createInnerAudioContext()
			audioCtx.src = props.item.audioUrl
			audioCtx.onEnded(() => { playing.value = false })
			audioCtx.onStop(() => { playing.value = false })
			audioCtx.onError(() => {
				playing.value = false
				uni.showToast({ title: t('common.fail'), icon: 'none' })
			})
		}
		if (playing.value) {
			audioCtx.pause()
			playing.value = false
		} else {
			audioCtx.play()
			playing.value = true
		}
	}

	/* 心得展开 */
	const expanded = ref(false)
	const needExpand = computed(() => (props.item.content || '').length > 80)

	/* 点赞 */
	const liked = ref(!!props.item.liked)
	const likeCount = ref(props.item.likeCount || 0)

	async function toggleLike() {
		try {
			if (liked.value) {
				await unlikeCheckin(props.item.id)
				liked.value = false
				likeCount.value = Math.max(0, likeCount.value - 1)
			} else {
				await likeCheckin(props.item.id)
				liked.value = true
				likeCount.value += 1
			}
		} catch (e) {}
	}

	/* 评论 */
	const showComments = ref(false)
	const comments = ref([])
	const commentText = ref('')
	let commentLoaded = false

	async function toggleComments() {
		showComments.value = !showComments.value
		if (showComments.value && !commentLoaded) {
			commentLoaded = true
			try {
				const data = await getComments(props.item.id)
				comments.value = Array.isArray(data) ? data : (data.records || [])
			} catch (e) {}
		}
	}

	async function sendComment() {
		if (!commentText.value.trim()) return
		try {
			await postComment(props.item.id, { content: commentText.value.trim() })
			commentText.value = ''
			const data = await getComments(props.item.id)
			comments.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {}
	}

	function onFav() {
		uni.showToast({ title: t('card.favTip'), icon: 'none' })
	}

	function onShare() {
		uni.showToast({ title: t('card.shareTip'), icon: 'none' })
	}

	function onEdit() {
		emit('edit', props.item)
	}

	function onDelete() {
		showModal({
			title: t('common.tips'),
			content: t('card.deleteConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteCheckin(props.item.id)
					uni.showToast({ title: t('card.deleteSuccess'), icon: 'none' })
					emit('deleted', props.item)
				} catch (e) {}
			}
		})
	}
</script>

<style lang="scss" scoped>
	.checkin-card {
		padding: 30rpx;
		margin-bottom: 24rpx;
	}

	.card-header {
		display: flex;
		align-items: center;
	}

	.avatar-ring {
		width: 84rpx;
		height: 84rpx;
		border-radius: 50%;
		background: var(--brand-gradient);
		padding: 3rpx;
		box-sizing: border-box;
		flex-shrink: 0;
	}

	.avatar {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: var(--bg-secondary);
		border: 2rpx solid var(--bg-secondary);
		box-sizing: border-box;
	}

	.header-main {
		margin-left: 20rpx;
		flex: 1;
		min-width: 0;
	}

	.name-row {
		display: flex;
		align-items: center;
	}

	.nickname {
		font-size: 30rpx;
		font-weight: 600;
		color: var(--text-primary);
		max-width: 300rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.type-badge {
		display: inline-flex;
		align-items: center;
		font-size: 20rpx;
		font-weight: 500;
		padding: 4rpx 14rpx;
		border-radius: var(--radius-pill);
		margin-left: 14rpx;
		flex-shrink: 0;
	}

	.badge-dot {
		width: 8rpx;
		height: 8rpx;
		border-radius: 50%;
		background: currentColor;
		margin-right: 8rpx;
	}

	.badge-audio {
		background: rgba(46, 74, 107, 0.12);
		color: var(--primary);
	}

	.badge-note {
		background: rgba(46, 125, 91, 0.13);
		color: var(--success);
	}

	.badge-image {
		background: rgba(180, 101, 46, 0.15);
		color: var(--warning);
	}

	.sub-row {
		display: flex;
		align-items: center;
		margin-top: 8rpx;
	}

	.date {
		font-size: 22rpx;
		color: var(--text-secondary);
	}

	.dot {
		margin: 0 8rpx;
		color: var(--text-tertiary);
		font-size: 22rpx;
	}

	.sub {
		font-size: 22rpx;
		color: var(--text-secondary);
	}

	.chapter {
		display: flex;
		align-items: center;
		margin-top: 22rpx;
		padding: 16rpx 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-sm);
	}

	.chapter-bar {
		width: 6rpx;
		height: 30rpx;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		margin-right: 16rpx;
		flex-shrink: 0;
	}

	.chapter-text {
		font-size: 27rpx;
		font-weight: 600;
		color: var(--text-primary);
		line-height: 1.5;
	}

	.audio-bar {
		display: flex;
		align-items: center;
		background: var(--surface-2);
		border-radius: var(--radius-md);
		padding: 16rpx 22rpx;
		margin-top: 20rpx;
		border: 1rpx solid var(--border);
	}

	.audio-btn {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 6rpx 14rpx rgba(46, 74, 107, 0.28);
	}

	.audio-btn-icon {
		color: #FFFFFF;
		font-size: 24rpx;
		line-height: 1;
	}

	.audio-info {
		flex: 1;
		margin: 0 20rpx;
	}

	.audio-text {
		font-size: 24rpx;
		color: var(--text-primary);
		font-weight: 500;
	}

	.audio-progress {
		height: 6rpx;
		border-radius: var(--radius-pill);
		background: var(--border-strong);
		margin-top: 10rpx;
		overflow: hidden;
	}

	.audio-progress-inner {
		height: 100%;
		background: var(--brand-gradient);
		border-radius: var(--radius-pill);
		transition: width 0.6s;
	}

	.audio-duration {
		font-size: 22rpx;
		color: var(--text-secondary);
	}

	.note-content {
		margin-top: 20rpx;
	}

	.note-text {
		font-size: 27rpx;
		color: var(--text-primary);
		line-height: 1.75;
	}

	.note-text.collapsed {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 3;
		overflow: hidden;
	}

	.expand-btn {
		font-size: 24rpx;
		font-weight: 500;
		color: var(--primary);
		margin-top: 10rpx;
		display: inline-block;
	}

	.image-grid {
		display: flex;
		flex-wrap: wrap;
		margin-top: 20rpx;
		gap: 12rpx;
	}

	.grid-img {
		width: calc((100% - 24rpx) / 3);
		height: 200rpx;
		border-radius: var(--radius-md);
		background: var(--surface-2);
	}

	.grid-img.single {
		width: 62%;
		height: 320rpx;
	}

	.action-row {
		display: flex;
		justify-content: space-between;
		margin-top: 24rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid var(--border);
	}

	.action {
		display: flex;
		align-items: center;
		padding: 6rpx 10rpx;
		border-radius: var(--radius-pill);
		transition: background 0.2s ease;
	}

	.action:active {
		background: var(--surface-2);
	}

	.action-icon {
			font-size: 30rpx;
			line-height: 1;
			color: var(--text-secondary);
		}

		.action.liked .action-icon {
			color: var(--danger);
		}

	.action-num {
		font-size: 23rpx;
		color: var(--text-secondary);
		margin-left: 8rpx;
	}

	.action.liked .action-num {
		color: var(--danger);
	}

	.comment-box {
		margin-top: 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
		padding: 22rpx;
		border: 1rpx solid var(--border);
	}

	.comment-item {
		font-size: 24rpx;
		line-height: 1.7;
		margin-bottom: 10rpx;
	}

	.comment-user {
		color: var(--primary);
		font-weight: 600;
	}

	.comment-content {
		color: var(--text-primary);
	}

	.comment-empty {
		font-size: 24rpx;
		color: var(--text-tertiary);
		text-align: center;
		padding: 10rpx 0;
	}

	.comment-input-row {
		display: flex;
		align-items: center;
		margin-top: 16rpx;
		gap: 16rpx;
	}

	.comment-input {
		flex: 1;
		height: 68rpx;
		background: var(--bg-secondary);
		border-radius: var(--radius-pill);
		padding: 0 26rpx;
		font-size: 24rpx;
		color: var(--text-primary);
		border: 1rpx solid var(--border);
		box-sizing: border-box;
	}

	.comment-send {
		height: 64rpx;
		padding: 0 28rpx;
		border-radius: var(--radius-pill);
		background: var(--brand-gradient);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 8rpx 16rpx rgba(46, 74, 107, 0.26);
	}

	.comment-send-text {
		font-size: 24rpx;
		font-weight: 600;
		color: #FFFFFF;
	}
</style>
