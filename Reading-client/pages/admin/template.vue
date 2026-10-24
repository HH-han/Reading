<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="btn-primary create-btn" @click="openForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.template.create') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="puzzle-line" class="empty-icon" />
				<text>{{ $t('admin.template.empty') }}</text>
			</view>

			<view class="tpl-card glass-card" v-for="item in list" :key="item.id">
				<view class="t-head">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="puzzle-line" /></view>
					<text class="t-name">{{ item.name }}</text>
					<view class="t-ops">
						<view class="t-op" @click="openForm(item)">
							<r-icon name="ball-pen-line" class="t-op-text" />
						</view>
						<view class="t-op danger" @click="onDelete(item)">
							<r-icon name="delete-bin-line" class="t-op-text danger" />
						</view>
					</view>
				</view>
				<view class="t-tags">
					<text class="t-tag" v-for="tp in parseTypes(item.checkinTypes)" :key="tp">{{ typeLabel(tp) }}</text>
				</view>
				<view class="t-flags">
					<view class="t-flag" v-for="tp in parseTypes(item.checkinTypes)" :key="tp">
						<text class="t-flag-text">{{ typeLabel(tp) }}：{{ reqSummary(item, tp) }}</text>
					</view>
					<view class="t-flag" v-if="!parseTypes(item.checkinTypes).length">
						<text class="t-flag-text">{{ $t('admin.template.noRequirement') }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 新建/编辑弹层 -->
		<view class="ui-modal-mask" v-if="showForm" @click="showForm = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ form.id ? $t('admin.template.edit') : $t('admin.template.create') }}</text>
					<view class="ui-sheet-close" @click="showForm = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<view class="field">
						<text class="field__label">{{ $t('admin.template.name') }}</text>
						<input class="input-item" v-model="form.name" :placeholder="$t('admin.template.namePh')" placeholder-class="input-ph" />
					</view>

					<view class="field">
						<text class="field__label">{{ $t('admin.template.types') }}</text>
						<view class="type-row">
							<view
								v-for="tp in allTypes"
								:key="tp"
								class="type-tag"
								:class="{ active: selectedTypes.includes(tp) }"
								@click="toggleType(tp)"
							>
								<text class="type-tag-text">{{ typeLabel(tp) }}</text>
							</view>
						</view>
					</view>

					<view class="field" v-for="tp in selectedTypes" :key="tp">
						<text class="field__label">{{ typeLabel(tp) }} · {{ $t('admin.template.reqItems') }}</text>
						<view class="type-row">
							<view
								v-for="op in reqOptions"
								:key="op.key"
								class="req-chip"
								:class="{ active: isReqOn(tp, op.key) }"
								@click="toggleReq(tp, op.key)"
							>
								<text class="req-chip-text">{{ $t(op.label) }}</text>
							</view>
						</view>
					</view>

					<view class="field">
						<text class="field__label">{{ $t('admin.template.minWords') }}</text>
						<input class="input-item" type="number" v-model="form.minWordCount" placeholder-class="input-ph" />
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
	import { getTemplateList, createTemplate, updateTemplate, deleteTemplate } from '@/api/admin.js'

	const allTypes = [1, 2, 3]
	// 每个打卡类型可单独勾选的必填项
	const reqOptions = [
		{ key: 'book', label: 'admin.template.requireBook' },
		{ key: 'chapter', label: 'admin.template.requireChapter' },
		{ key: 'image', label: 'admin.template.requireImage' },
		{ key: 'audio', label: 'admin.template.requireAudio' },
		{ key: 'note', label: 'admin.template.requireContent' }
	]
	const list = ref([])
	const loading = ref(false)
	const showForm = ref(false)
	const form = ref({})
	const selectedTypes = ref([])
	const typeConfig = ref({})

	function typeLabel(tp) {
		return t(tp === 1 ? 'card.typeImage' : tp === 2 ? 'card.typeAudio' : 'card.typeNote')
	}

	function parseTypes(val) {
		if (Array.isArray(val)) return val
		if (!val) return []
		return String(val).split(',').map(Number).filter(Boolean)
	}

	async function load() {
		loading.value = true
		try {
			const data = await getTemplateList()
			list.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function openForm(item) {
		if (item) {
			form.value = { ...item }
			selectedTypes.value = parseTypes(item.checkinTypes)
			typeConfig.value = buildTypeConfig(item)
		} else {
			form.value = { name: '', minWordCount: 0 }
			selectedTypes.value = [3]
			typeConfig.value = buildTypeConfig(null)
		}
		showForm.value = true
	}

	function toggleType(tp) {
		const i = selectedTypes.value.indexOf(tp)
		if (i >= 0) {
			selectedTypes.value.splice(i, 1)
		} else {
			selectedTypes.value.push(tp)
			ensureTypeReq(tp)
		}
	}

	// 每种打卡类型的默认必填项：书籍 + 章节 + 该类型核心内容
	function defaultTypeReq(type) {
		return { book: 1, chapter: 1, image: type === 1 ? 1 : 0, audio: type === 2 ? 1 : 0, note: type === 3 ? 1 : 0 }
	}

	function normalizeTypeReq(cfg) {
		const c = cfg || {}
		return {
			book: c.book ? 1 : 0,
			chapter: c.chapter ? 1 : 0,
			image: c.image ? 1 : 0,
			audio: c.audio ? 1 : 0,
			note: c.note ? 1 : 0
		}
	}

	function parseTypeConfig(val) {
		let obj = val
		if (typeof val === 'string') {
			try {
				obj = JSON.parse(val)
			} catch (e) {
				obj = {}
			}
		}
		const result = {}
		if (obj && typeof obj === 'object') {
			Object.keys(obj).forEach(k => {
				result[k] = normalizeTypeReq(obj[k])
			})
		}
		return result
	}

	// 编辑已有模板时按类型初始化，旧模板无按类型配置则按全局开关推导
	function buildTypeConfig(item) {
		const parsed = item ? parseTypeConfig(item.typeConfig) : {}
		const cfg = {}
		selectedTypes.value.forEach(tp => {
			if (parsed[tp]) {
				cfg[tp] = parsed[tp]
			} else if (item && !item.typeConfig) {
				cfg[tp] = {
					book: 1,
					chapter: 1,
					image: item.requireImage ? 1 : 0,
					audio: item.requireAudio ? 1 : 0,
					note: item.requireContent ? 1 : 0
				}
			} else {
				cfg[tp] = defaultTypeReq(tp)
			}
		})
		return cfg
	}

	function ensureTypeReq(type) {
		const key = String(type)
		if (!typeConfig.value[key]) typeConfig.value[key] = defaultTypeReq(type)
	}

	function isReqOn(type, key) {
		const cfg = typeConfig.value[String(type)]
		return !!(cfg && cfg[key])
	}

	function toggleReq(type, key) {
		ensureTypeReq(type)
		const cfg = typeConfig.value[String(type)]
		cfg[key] = cfg[key] ? 0 : 1
	}

	function reqSummary(item, type) {
		const cfg = parseTypeConfig(item.typeConfig)[type]
		if (!cfg) return t('admin.template.noRequirement')
		const labels = []
		if (cfg.book) labels.push(t('admin.template.requireBook'))
		if (cfg.chapter) labels.push(t('admin.template.requireChapter'))
		if (cfg.image) labels.push(t('admin.template.requireImage'))
		if (cfg.audio) labels.push(t('admin.template.requireAudio'))
		if (cfg.note) labels.push(t('admin.template.requireContent'))
		return labels.length ? labels.join(' / ') : t('admin.template.noRequirement')
	}

	async function save() {
		if (!form.value.name) {
			uni.showToast({ title: t('admin.template.errName'), icon: 'none' })
			return
		}
		const typeReq = {}
		selectedTypes.value.forEach(tp => {
			typeReq[tp] = normalizeTypeReq(typeConfig.value[String(tp)] || defaultTypeReq(tp))
		})
		const payload = {
			...form.value,
			checkinTypes: selectedTypes.value.join(','),
			typeConfig: JSON.stringify(typeReq)
		}
		try {
			if (payload.id) {
				await updateTemplate(payload)
			} else {
				await createTemplate(payload)
			}
			showForm.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load()
		} catch (e) {}
	}

	function onDelete(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.template.delConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteTemplate(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load()
				} catch (e) {}
			}
		})
	}

	onShow(load)

	onPullDownRefresh(async () => {
		await load()
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

	.tpl-card {
		margin-bottom: 20rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
	}

	.t-head {
		display: flex;
		align-items: center;
	}

	.t-name {
		flex: 1;
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		margin-left: 16rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.t-ops {
		display: flex;
		flex-shrink: 0;
	}

	.t-op {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 12rpx;
	}

	.t-op.danger {
		background: rgba(179, 69, 59, 0.08);
	}

	.t-op-text {
		font-size: 24rpx;
		color: var(--text-secondary);
	}

	.t-op-text.danger {
		color: var(--danger);
	}

	.t-tags {
		display: flex;
		flex-wrap: wrap;
		margin-top: 18rpx;
		gap: 12rpx;
	}

	.t-tag {
		font-size: 20rpx;
		font-weight: 500;
		color: var(--primary);
		background: var(--primary-soft);
		border-radius: var(--radius-pill);
		padding: 5rpx 18rpx;
	}

	.t-flags {
		display: flex;
		flex-wrap: wrap;
		margin-top: 16rpx;
		padding-top: 16rpx;
		border-top: 1rpx solid var(--border);
		gap: 10rpx 20rpx;
	}

	.t-flag {
		display: flex;
		align-items: center;
	}

	.t-flag-icon {
		font-size: 20rpx;
		margin-right: 6rpx;
	}

	.t-flag-text {
		font-size: 21rpx;
		color: var(--text-secondary);
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

	.type-row {
		display: flex;
		gap: 16rpx;
	}

	.type-tag {
		padding: 12rpx 32rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		border: 1rpx solid var(--border);
	}

	.type-tag.active {
		background: var(--brand-gradient);
		border-color: transparent;
		box-shadow: 0 8rpx 16rpx rgba(46, 74, 107, 0.24);
	}

	.type-tag-text {
		font-size: 24rpx;
		color: var(--text-primary);
	}

	.type-tag.active .type-tag-text {
		color: #FFFFFF;
		font-weight: 600;
	}

	.req-chip {
		padding: 8rpx 22rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		border: 1rpx solid var(--border);
	}

	.req-chip.active {
		background: var(--primary-fill);
		border-color: transparent;
	}

	.req-chip-text {
		font-size: 21rpx;
		color: var(--text-primary);
	}

	.req-chip.active .req-chip-text {
		color: var(--on-primary);
		font-weight: 600;
	}

	.switch-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.switch-label {
		font-size: 27rpx;
		color: var(--text-primary);
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
