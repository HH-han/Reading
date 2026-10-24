<template>
	<view class="r-picker">
		<view class="rp-field" @click="open">
			<text class="rp-field__text" :class="{ 'rp-field__text--ph': !hasValue }">{{ displayText }}</text>
			<r-icon :name="mode === 'date' ? 'calendar-line' : 'arrow-right-s-line'" class="rp-field__arrow" />
		</view>

		<view class="rp-mask" v-if="visible" @click="close">
			<view class="rp-mask__bg" @touchmove.stop.prevent="noop"></view>
			<view class="rp-sheet" @click.stop>
				<view class="rp-head">
					<text class="rp-title">{{ title }}</text>
					<view class="rp-close" @click="close"><r-icon name="close-line" /></view>
				</view>

				<scroll-view
					v-if="mode === 'selector'"
					scroll-y
					class="rp-list"
					:style="{ height: listHeight }"
					:scroll-into-view="scrollTarget"
					scroll-with-animation
				>
					<view
						v-for="(item, index) in range"
						:key="index"
						:id="'rp-i-' + index"
						class="rp-item"
						:class="{ 'rp-item--active': index === innerIndex }"
						@click="choose(index)"
					>
						<text class="rp-item__text">{{ itemText(item) }}</text>
						<r-icon v-if="index === innerIndex" name="check-line" class="rp-item__check" />
					</view>
					<view class="rp-empty" v-if="!range.length">{{ $t('common.empty') }}</view>
				</scroll-view>

				<template v-else>
					<text class="rp-preview">{{ dateText }}</text>
					<picker-view class="rp-date" :value="dateIndexes" :indicator-style="indicatorStyle" @change="onDateChange">
						<picker-view-column>
							<view class="rp-date__item" v-for="y in years" :key="y">{{ y }}</view>
						</picker-view-column>
						<picker-view-column>
							<view class="rp-date__item" v-for="m in months" :key="m">{{ pad(m) }}</view>
						</picker-view-column>
						<picker-view-column>
							<view class="rp-date__item" v-for="d in dayCount" :key="d">{{ pad(d) }}</view>
						</picker-view-column>
					</picker-view>
					<view class="rp-actions">
						<view class="rp-btn rp-btn--plain" @click="close">{{ $t('common.cancel') }}</view>
						<view class="rp-btn rp-btn--primary" @click="confirmDate">{{ $t('common.confirm') }}</view>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed, nextTick } from 'vue'

	const props = defineProps({
		range: { type: Array, default: () => [] },
		rangeKey: { type: String, default: '' },
		value: { type: [Number, String], default: null },
		mode: { type: String, default: 'selector' },
		title: { type: String, default: '' },
		placeholder: { type: String, default: '' }
	})

	const emit = defineEmits(['change'])

	const visible = ref(false)
	const scrollTarget = ref('')

	const innerIndex = computed(() => {
		const i = Number(props.value)
		return Number.isInteger(i) && i >= 0 && i < props.range.length ? i : -1
	})

	const hasValue = computed(() => {
		if (props.mode === 'date') return !!props.value
		return innerIndex.value >= 0
	})

	const displayText = computed(() => {
		if (props.mode === 'date') return props.value || props.placeholder
		return hasValue.value ? itemText(props.range[innerIndex.value]) : props.placeholder
	})

	function itemText(item) {
		if (item === null || item === undefined) return ''
		return props.rangeKey ? item[props.rangeKey] : item
	}

	// 列表高度随选项数量自适应，最多展示 8 条
	const listHeight = computed(() => {
		if (!props.range.length) return '220rpx'
		return Math.min(props.range.length, 8) * 96 + 16 + 'rpx'
	})

	/* ---------- 日期选择 ---------- */
	const currentYear = new Date().getFullYear()
	const years = []
	for (let y = currentYear - 20; y <= currentYear + 10; y++) years.push(y)
	const months = Array.from({ length: 12 }, (_, i) => i + 1)
	const dateIndexes = ref([0, 0, 0])

	const dayCount = computed(() => {
		const [yi, mi] = dateIndexes.value
		return new Date(years[yi], months[mi], 0).getDate()
	})

	const dateText = computed(() => {
		const [yi, mi, di] = dateIndexes.value
		return `${years[yi]}-${pad(months[mi])}-${pad(di + 1)}`
	})

	// picker-view 的选中框高度需用 px，与 80rpx 的选项行高保持一致
	const indicatorStyle = computed(() => {
		let width = 375
		try {
			width = uni.getSystemInfoSync().windowWidth || 375
		} catch (e) {}
		return `height: ${Math.round(80 * width / 750)}px;`
	})

	function pad(n) {
		return n < 10 ? '0' + n : String(n)
	}

	function syncDate() {
		const match = /^(\d{4})-(\d{1,2})-(\d{1,2})/.exec(props.value || '')
		const today = new Date()
		const year = match ? Number(match[1]) : today.getFullYear()
		const month = match ? Number(match[2]) : today.getMonth() + 1
		const day = match ? Number(match[3]) : today.getDate()
		let yi = years.indexOf(year)
		if (yi < 0) yi = year < years[0] ? 0 : years.length - 1
		const mi = Math.min(Math.max(month - 1, 0), months.length - 1)
		const maxDay = new Date(years[yi], months[mi], 0).getDate()
		dateIndexes.value = [yi, mi, Math.min(Math.max(day - 1, 0), maxDay - 1)]
	}

	function onDateChange(e) {
		const val = e.detail.value.map(n => Number(n))
		const maxDay = new Date(years[val[0]], months[val[1]], 0).getDate()
		dateIndexes.value = [val[0], val[1], Math.min(val[2], maxDay - 1)]
	}

	function confirmDate() {
		emit('change', { detail: { value: dateText.value } })
		close()
	}

	/* ---------- 交互 ---------- */
	function open() {
		if (props.mode === 'date') syncDate()
		visible.value = true
		scrollTarget.value = ''
		nextTick(() => {
			if (props.mode === 'selector' && innerIndex.value >= 0) {
				scrollTarget.value = 'rp-i-' + innerIndex.value
			}
		})
	}

	function close() {
		visible.value = false
	}

	function choose(index) {
		emit('change', { detail: { value: index } })
		close()
	}

	function noop() {}
</script>

<style lang="scss" scoped>
	.r-picker {
		width: 100%;
	}

	/* ---------- 触发框 ---------- */
	.rp-field {
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 96rpx;
		padding: 0 28rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		box-sizing: border-box;
		transition: border-color 0.2s ease;
	}

	.rp-field:active {
		border-color: rgba(46, 74, 107, 0.45);
	}

	.rp-field__text {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.rp-field__text--ph {
		color: var(--text-tertiary);
	}

	.rp-field__arrow {
		font-size: 26rpx;
		color: var(--text-tertiary);
		margin-left: 16rpx;
		flex-shrink: 0;
	}

	/* ---------- 底部弹窗 ---------- */
	.rp-mask {
		position: fixed;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		z-index: 2000;
		background: var(--mask);
		display: flex;
		align-items: flex-end;
		animation: rpFadeIn 0.2s ease;
	}

	.rp-mask__bg {
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
	}

	.rp-sheet {
		position: relative;
		width: 100%;
		max-height: 78vh;
		padding: 28rpx;
		display: flex;
		flex-direction: column;
		background: var(--bg-secondary);
		border-top: 1rpx solid var(--border);
		border-radius: var(--radius-xl) var(--radius-xl) 0 0;
		padding: 28rpx 28rpx calc(28rpx + constant(safe-area-inset-bottom));
		padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
		box-sizing: border-box;
		box-shadow: 0 -20rpx 60rpx rgba(0, 0, 0, 0.16);
		animation: rpSlideUp 0.26s cubic-bezier(0.22, 0.61, 0.36, 1);
	}

	.rp-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-bottom: 16rpx;
	}

	.rp-title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
	}

	.rp-close {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: var(--text-secondary);
	}

	/* ---------- 列表选择 ---------- */
	.rp-list {
		width: 100%;
	}

	.rp-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 26rpx 20rpx;
		border-bottom: 1rpx solid var(--border);
	}

	.rp-item:last-child {
		border-bottom: none;
	}

	.rp-item:active {
		background: var(--surface-2);
	}

	.rp-item__text {
		flex: 1;
		min-width: 0;
		font-size: 29rpx;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.rp-item--active .rp-item__text {
		color: var(--primary);
		font-weight: 600;
	}

	.rp-item__check {
		font-size: 30rpx;
		color: var(--primary);
		margin-left: 16rpx;
	}

	.rp-empty {
		padding: 80rpx 0;
		text-align: center;
		font-size: 26rpx;
		color: var(--text-tertiary);
	}

	/* ---------- 日期选择 ---------- */
	.rp-preview {
		display: block;
		text-align: center;
		font-size: 26rpx;
		color: var(--primary);
		letter-spacing: 1rpx;
		margin: 4rpx 0 12rpx;
	}

	.rp-date {
		width: 100%;
		height: 400rpx;
	}

	.rp-date__item {
		height: 80rpx;
		line-height: 80rpx;
		text-align: center;
		font-size: 30rpx;
		color: var(--text-primary);
	}

	.rp-actions {
		display: flex;
		gap: 20rpx;
		margin-top: 24rpx;
	}

	.rp-btn {
		flex: 1;
		text-align: center;
		font-size: 30rpx;
		padding: 22rpx 0;
		border-radius: var(--radius-pill);
		box-sizing: border-box;
		transition: transform 0.16s ease, opacity 0.16s ease;
	}

	.rp-btn:active {
		transform: scale(0.98);
		opacity: 0.92;
	}

	.rp-btn--plain {
		background: var(--bg-secondary);
		border: 1rpx solid var(--border-strong);
		color: var(--text-primary);
	}

	.rp-btn--primary {
		background: var(--primary-fill);
		color: var(--on-primary);
		font-weight: 600;
		letter-spacing: 1rpx;
		box-shadow: 0 8rpx 20rpx rgba(46, 74, 107, 0.2);
	}

	@keyframes rpFadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}

	@keyframes rpSlideUp {
		from { transform: translateY(100%); }
		to { transform: translateY(0); }
	}
</style>
