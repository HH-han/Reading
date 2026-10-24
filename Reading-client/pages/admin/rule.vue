<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="btn-primary create-btn" @click="openForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.rule.create') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="ruler-line" class="empty-icon" />
				<text>{{ $t('admin.rule.empty') }}</text>
			</view>

			<view class="rule-card glass-card" v-for="item in list" :key="item.id">
				<view class="r-head">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="medal-line" /></view>
					<text class="r-position">{{ item.position }}</text>
					<view class="r-ops">
						<view class="r-op" @click="openForm(item)">
							<r-icon name="ball-pen-line" class="r-op-text" />
						</view>
						<view class="r-op danger" @click="onDelete(item)">
							<r-icon name="delete-bin-line" class="r-op-text danger" />
						</view>
					</view>
				</view>
				<view class="r-days">
					<view class="r-day-item">
						<text class="r-day-num">{{ item.weekDays || 0 }}</text>
						<text class="r-day-label">{{ $t('admin.rule.week') }}</text>
					</view>
					<view class="r-day-divider"></view>
					<view class="r-day-item">
						<text class="r-day-num">{{ item.monthDays || 0 }}</text>
						<text class="r-day-label">{{ $t('admin.rule.month') }}</text>
					</view>
					<view class="r-day-divider"></view>
					<view class="r-day-item">
						<text class="r-day-num">{{ item.yearDays || 0 }}</text>
						<text class="r-day-label">{{ $t('admin.rule.year') }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 新建/编辑弹层 -->
		<view class="ui-modal-mask" v-if="showForm" @click="showForm = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ form.id ? $t('admin.rule.edit') : $t('admin.rule.create') }}</text>
					<view class="ui-sheet-close" @click="showForm = false"><r-icon name="close-line" /></view>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.rule.position') }}</text>
					<r-picker
						:range="positionOptions"
						:value="positionIndex"
						:title="$t('admin.rule.position')"
						:placeholder="$t('admin.rule.positionSelectPh')"
						@change="e => form.position = positionOptions[Number(e.detail.value)] || ''"
					/>
				</view>
				<view class="field field__row">
					<view class="field__col">
						<text class="field__label">{{ $t('admin.rule.week') }}</text>
						<input class="input-item" type="number" v-model="form.weekDays" placeholder-class="input-ph" />
					</view>
					<view class="field__col">
						<text class="field__label">{{ $t('admin.rule.month') }}</text>
						<input class="input-item" type="number" v-model="form.monthDays" placeholder-class="input-ph" />
					</view>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.rule.year') }}</text>
					<input class="input-item" type="number" v-model="form.yearDays" placeholder-class="input-ph" />
				</view>
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
	import { ref, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getRuleList, createRule, updateRule, deleteRule, getPositionList } from '@/api/admin.js'

	const list = ref([])
	const loading = ref(false)
	const showForm = ref(false)
	const form = ref({})
	const positionNames = ref([])

	const positionOptions = computed(() => {
		const options = positionNames.value.slice()
		const current = form.value.position
		if (current && options.indexOf(current) < 0) options.unshift(current)
		return options
	})

	const positionIndex = computed(() => positionOptions.value.indexOf(form.value.position))

	async function loadPositions() {
		try {
			const data = await getPositionList({})
			const records = Array.isArray(data) ? data : (data.records || [])
			positionNames.value = records
				.filter(p => p.name && p.status !== 0)
				.sort((a, b) => (a.sort || 0) - (b.sort || 0))
				.map(p => p.name)
		} catch (e) {
			positionNames.value = []
		}
	}

	async function load() {
		loading.value = true
		try {
			const params = {}
			if (userState.userInfo && userState.userInfo.circleId) params.circleId = userState.userInfo.circleId
			const data = await getRuleList(params)
			list.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function openForm(item) {
		form.value = item
			? { ...item }
			: { position: '', weekDays: '', monthDays: '', yearDays: '' }
		showForm.value = true
	}

	async function save() {
		if (!form.value.position) {
			uni.showToast({ title: t('admin.rule.errPosition'), icon: 'none' })
			return
		}
		try {
			if (form.value.id) {
				await updateRule(form.value)
			} else {
				await createRule(form.value)
			}
			showForm.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load()
		} catch (e) {}
	}

	function onDelete(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.rule.delConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteRule(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load()
				} catch (e) {}
			}
		})
	}

	onShow(() => {
		load()
		loadPositions()
	})

	onPullDownRefresh(async () => {
		await Promise.all([load(), loadPositions()])
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

	.rule-card {
		margin-bottom: 20rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
	}

	.r-head {
		display: flex;
		align-items: center;
	}

	.r-position {
		flex: 1;
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		margin-left: 16rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.r-ops {
		display: flex;
		flex-shrink: 0;
	}

	.r-op {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 12rpx;
	}

	.r-op.danger {
		background: rgba(179, 69, 59, 0.08);
	}

	.r-op-text {
		font-size: 24rpx;
		color: var(--text-secondary);
	}

	.r-op-text.danger {
		color: var(--danger);
	}

	.r-days {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
		padding: 20rpx 0;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.r-day-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.r-day-divider {
		width: 1rpx;
		height: 48rpx;
		background: var(--border);
	}

	.r-day-num {
		font-size: 38rpx;
		font-weight: 700;
		color: var(--primary);
		line-height: 1.2;
	}

	.r-day-label {
		font-size: 20rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.form-modal {
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

	.field__row {
		display: flex;
		gap: 20rpx;
	}

	.field__col {
		flex: 1;
		min-width: 0;
	}

	.modal-ops {
		display: flex;
		margin-top: 32rpx;
		gap: 20rpx;
	}

	.modal-btn {
		flex: 1;
	}
</style>
