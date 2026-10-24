<template>
	<view v-if="modalState.visible" class="r-modal" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="r-modal-mask" @click="onMask"></view>
		<view class="r-modal-card" :class="'is-' + modalState.type">
			<view class="r-modal-badge">
				<r-icon :name="iconName" class="r-modal-badge-icon" />
			</view>

			<text class="r-modal-title">{{ modalState.title }}</text>

			<view class="r-modal-body" v-if="modalState.content">
				<text class="r-modal-content" :class="{ 'is-long': isLong }">{{ modalState.content }}</text>
			</view>

			<view class="r-modal-actions">
				<view class="r-modal-btn plain" v-if="modalState.showCancel" @click="onCancel">
					<text>{{ modalState.cancelText }}</text>
				</view>
				<view class="r-modal-btn primary" @click="onConfirm">
					<text>{{ modalState.confirmText }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { modalState, closeModal } from '@/utils/modal.js'

	const ICON_MAP = {
		info: 'question-answer-line',
		danger: 'delete-bin-line',
		warn: 'notification-3-line',
		success: 'check-line'
	}

	const iconName = computed(() => ICON_MAP[modalState.type] || ICON_MAP.info)
	// 长文案（协议、说明）改为左对齐并允许滚动
	const isLong = computed(() => (modalState.content || '').length > 56)

	function onConfirm() {
		closeModal(true)
	}

	function onCancel() {
		closeModal(false)
	}

	function onMask() {
		if (modalState.showCancel) closeModal(false)
	}
</script>

<style lang="scss" scoped>
	.r-modal {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 9999;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 72rpx 56rpx;
		box-sizing: border-box;
	}

	.r-modal-mask {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: var(--mask);
		animation: rModalFade 0.2s ease;
	}

	.r-modal-card {
		position: relative;
		width: 100%;
		max-width: 600rpx;
		padding: 52rpx 40rpx 36rpx;
		box-sizing: border-box;
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-xl);
		box-shadow: var(--shadow-lg);
		text-align: center;
		animation: rModalPop 0.26s cubic-bezier(0.22, 0.61, 0.36, 1);
	}

	/* 顶部图标：纸墨风格的小印章 */
	.r-modal-badge {
		width: 96rpx;
		height: 96rpx;
		margin: 0 auto;
		border-radius: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--primary-soft);
		color: var(--primary);
	}

	.r-modal-badge-icon {
		font-size: 46rpx;
		line-height: 1;
	}

	.r-modal-card.is-danger .r-modal-badge {
		background: rgba(179, 69, 59, 0.11);
		color: var(--danger);
	}

	.r-modal-card.is-warn .r-modal-badge {
		background: rgba(180, 101, 46, 0.12);
		color: var(--warning);
	}

	.r-modal-card.is-success .r-modal-badge {
		background: rgba(46, 125, 91, 0.12);
		color: var(--success);
	}

	.theme-dark .r-modal-card.is-danger .r-modal-badge {
		background: rgba(224, 113, 104, 0.14);
	}

	.theme-dark .r-modal-card.is-warn .r-modal-badge {
		background: rgba(208, 138, 92, 0.16);
	}

	.theme-dark .r-modal-card.is-success .r-modal-badge {
		background: rgba(79, 191, 146, 0.14);
	}

	.r-modal-title {
		display: block;
		margin-top: 26rpx;
		font-size: 34rpx;
		font-weight: 700;
		line-height: 1.4;
		letter-spacing: 0.5rpx;
		color: var(--text-primary);
	}

	.r-modal-body {
		margin-top: 16rpx;
		max-height: 44vh;
		overflow-y: auto;
	}

	.r-modal-body::-webkit-scrollbar {
		display: none;
		width: 0;
		height: 0;
	}

	.r-modal-content {
		display: block;
		font-size: 27rpx;
		line-height: 1.75;
		color: var(--text-secondary);
		text-align: center;
		white-space: pre-wrap;
		word-break: break-word;
	}

	.r-modal-content.is-long {
		text-align: left;
		font-size: 26rpx;
		line-height: 1.85;
	}

	.r-modal-actions {
		display: flex;
		margin-top: 44rpx;
		gap: 20rpx;
	}

	.r-modal-btn {
		flex: 1;
		min-width: 0;
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: var(--radius-pill);
		font-size: 29rpx;
		transition: transform 0.16s ease, opacity 0.16s ease;
	}

	.r-modal-btn:active {
		transform: scale(0.97);
		opacity: 0.92;
	}

	.r-modal-btn.plain {
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		color: var(--text-secondary);
		font-weight: 500;
	}

	.r-modal-btn.primary {
		background: var(--primary-fill);
		color: var(--on-primary);
		font-weight: 600;
		letter-spacing: 1rpx;
		box-shadow: 0 8rpx 20rpx rgba(46, 74, 107, 0.2);
	}

	.r-modal-card.is-danger .r-modal-btn.primary {
		background: var(--danger);
		color: #FFFFFF;
		box-shadow: 0 8rpx 20rpx rgba(179, 69, 59, 0.22);
	}

	@keyframes rModalFade {
		from { opacity: 0; }
		to { opacity: 1; }
	}

	@keyframes rModalPop {
		from { opacity: 0; transform: scale(0.92) translateY(16rpx); }
		to { opacity: 1; transform: scale(1) translateY(0); }
	}
</style>
