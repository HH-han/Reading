<template>
	<view class="feed-panel">
		<!-- 二级 Tab：按当前读书圈模板允许的打卡类型渲染 -->
		<view class="segmented segmented--brand feed-tabs" v-if="tabs.length > 1">
			<view
				v-for="t in tabs"
				:key="t.key"
				class="segmented__item"
				:class="{ 'segmented__item--active': tab === t.key }"
				@click="switchTab(t.key)"
			>{{ $t(t.label) }}</view>
		</view>

		<view class="empty-box" v-if="!loading && !list.length">
			<r-icon name="book-open-line" class="empty-icon" />
			<text>{{ $t('index.feedEmpty') }}</text>
		</view>

		<checkin-card v-for="item in list" :key="item.id" :item="item" @edit="onEdit" @deleted="onDeleted" />

		<view class="load-more" v-if="list.length">
			<view class="load-dot"></view>
			<text class="load-text">{{ noMore ? $t('common.noMore') : $t('common.loading') }}</text>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue'
	import { getFeed, getCheckinTemplate } from '@/api/checkin.js'
	import CheckinCard from '@/components/checkin-card/checkin-card.vue'

	// 动态分类与打卡类型的映射
	const allTabs = [
		{ key: 'audio', type: 2, label: 'index.feedAudio' },
		{ key: 'note', type: 3, label: 'index.feedNote' },
		{ key: 'image', type: 1, label: 'index.feedImage' }
	]

	const tab = ref('audio')
	const template = ref(null)
	const list = ref([])
	const current = ref(1)
	const size = 10
	const noMore = ref(false)
	const loading = ref(false)

	// 当前读书圈模板允许的打卡类型，无模板时展示全部
	const allowedTypes = computed(() => {
		const raw = template.value ? template.value.checkinTypes : ''
		if (!raw) return [1, 2, 3]
		const types = String(raw).split(',').map(Number).filter(v => v >= 1 && v <= 3)
		return types.length ? types : [1, 2, 3]
	})

	const tabs = computed(() => allTabs.filter(t => allowedTypes.value.includes(t.type)))

	async function loadTemplate() {
		try {
			template.value = await getCheckinTemplate()
		} catch (e) {
			template.value = null
		}
		// 当前分类不在模板允许范围内时切到第一个可用分类
		const available = tabs.value
		if (available.length && !available.some(t => t.key === tab.value)) {
			tab.value = available[0].key
		}
	}

	function switchTab(v) {
		if (tab.value === v) return
		tab.value = v
		load(true)
	}

	async function load(reset) {
		if (loading.value) return
		if (reset) {
			current.value = 1
			noMore.value = false
		}
		loading.value = true
		try {
			const data = await getFeed({ current: current.value, size, tab: tab.value })
			const records = data.records || []
			if (reset) {
				list.value = records
			} else {
				list.value = list.value.concat(records)
			}
			if (records.length < size) noMore.value = true
			else current.value += 1
		} catch (e) {} finally {
			loading.value = false
		}
	}

	// 拉取模板后再加载动态，保证分类按钮与模板一致
	async function refresh() {
		await loadTemplate()
		await load(true)
	}

	onMounted(() => {
		refresh()
	})

	// 本人编辑：进入打卡页的编辑模式
	function onEdit(item) {
		uni.navigateTo({ url: '/pages/checkin/checkin?id=' + item.id })
	}

	// 本人删除：本地移除该卡片
	function onDeleted(item) {
		list.value = list.value.filter(c => c.id !== item.id)
	}

	defineExpose({
		refresh,
		loadMore: () => {
			if (!noMore.value) load(false)
		}
	})
</script>

<style lang="scss" scoped>
	.feed-tabs {
		margin-bottom: 24rpx;
	}

	.load-more {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24rpx 0;
	}

	.load-dot {
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: var(--text-tertiary);
		margin-right: 12rpx;
		opacity: 0.7;
	}

	.load-text {
		font-size: 22rpx;
		color: var(--text-tertiary);
	}
</style>
