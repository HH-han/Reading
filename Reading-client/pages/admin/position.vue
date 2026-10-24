<template>
	<view class="page position-page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 概览 -->
			<view class="overview-card glass-card">
				<view class="overview-main">
					<text class="overview-label">{{ $t('admin.position.title') }}</text>
					<text class="overview-num">{{ list.length }}</text>
					<text class="overview-desc">{{ $t('admin.position.subtitle') }}</text>
				</view>
				<view class="icon-chip icon-chip--lg icon-amber"><r-icon name="medal-line" /></view>
			</view>

			<!-- 搜索 -->
			<view class="search-box">
				<r-icon name="search-line" class="search-icon" />
				<input
					class="search-input"
					v-model="keyword"
					:placeholder="$t('admin.position.searchPh')"
					placeholder-class="input-ph"
					confirm-type="search"
					@confirm="load"
				/>
				<view class="search-btn" @click="load">{{ $t('common.search') }}</view>
			</view>

			<view class="btn-primary create-btn" @click="openForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.position.create') }}</text>
			</view>

			<!-- 职位列表 -->
			<view class="data-card glass-card" v-for="p in list" :key="p.id">
				<view class="card-head">
					<view class="icon-chip icon-chip--sm" :class="p.status === 0 ? 'icon-gray' : 'icon-amber'">
						<r-icon name="medal-line" />
					</view>
					<view class="card-main">
						<view class="title-row">
							<text class="card-title">{{ p.name }}</text>
							<text class="chip" :class="p.status === 0 ? 'chip-gray' : 'chip-green'">
								{{ p.status === 0 ? $t('admin.position.disabled') : $t('admin.position.enabled') }}
							</text>
						</view>
						<text class="card-sub">{{ p.description || $t('common.none') }}</text>
						<text class="card-meta">
							{{ $t('admin.position.userCount', { n: p.userCount || 0 }) }} ·
							{{ $t('admin.position.ruleCount', { n: p.ruleCount || 0 }) }} ·
							{{ $t('admin.position.sort') }} {{ p.sort || 0 }}
						</text>
					</view>
				</view>
				<view class="card-ops">
					<view class="op-btn" @click="openForm(p)">
						<r-icon name="ball-pen-line" class="op-ico" />
						<text>{{ $t('admin.position.edit') }}</text>
					</view>
					<view class="op-btn danger" @click="onDelete(p)">
						<r-icon name="delete-bin-line" class="op-ico" />
						<text>{{ $t('admin.position.delete') }}</text>
					</view>
				</view>
			</view>

			<view class="row-empty" v-if="!loading && !list.length">{{ $t('admin.position.empty') }}</view>
		</view>

		<!-- 新建/编辑 -->
		<view class="ui-modal-mask" v-if="showForm" @click="showForm = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ form.id ? $t('admin.position.edit') : $t('admin.position.create') }}</text>
					<view class="ui-sheet-close" @click="showForm = false"><r-icon name="close-line" /></view>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.position.name') }}</text>
					<input class="input-item" v-model="form.name" :placeholder="$t('admin.position.namePh')" placeholder-class="input-ph" />
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.position.description') }}</text>
					<textarea class="input-item f-area" v-model="form.description" :placeholder="$t('admin.position.descriptionPh')" placeholder-class="input-ph" />
				</view>
				<view class="field field__row">
					<view class="field__col">
						<text class="field__label">{{ $t('admin.position.sort') }}</text>
						<input class="input-item" type="number" v-model="form.sort" placeholder-class="input-ph" />
					</view>
					<view class="field__col">
						<text class="field__label">{{ $t('admin.position.status') }}</text>
						<r-picker
							:range="statusLabels"
							:value="form.status === 0 ? 1 : 0"
							:title="$t('admin.position.status')"
							@change="e => form.status = Number(e.detail.value) === 1 ? 0 : 1"
						/>
					</view>
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
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import { getPositionList, createPosition, updatePosition, deletePosition } from '@/api/admin.js'

	const list = ref([])
	const loading = ref(false)
	const keyword = ref('')
	const showForm = ref(false)
	const form = ref({})

	const statusLabels = computed(() => [t('admin.position.enabled'), t('admin.position.disabled')])

	async function load() {
		loading.value = true
		try {
			const data = await getPositionList({ keyword: keyword.value })
			list.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function openForm(p) {
		form.value = p
			? {
				id: p.id,
				name: p.name,
				description: p.description,
				sort: p.sort == null ? 0 : p.sort,
				status: p.status == null ? 1 : p.status
			}
			: { name: '', description: '', sort: 0, status: 1 }
		showForm.value = true
	}

	async function save() {
		if (!form.value.name || !form.value.name.trim()) {
			uni.showToast({ title: t('admin.position.errName'), icon: 'none' })
			return
		}
		const payload = {
			id: form.value.id,
			name: form.value.name.trim(),
			description: form.value.description,
			sort: Number(form.value.sort) || 0,
			status: Number(form.value.status)
		}
		try {
			if (payload.id) {
				await updatePosition(payload)
			} else {
				await createPosition(payload)
			}
			showForm.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load()
		} catch (e) {}
	}

	function onDelete(p) {
		if ((p.userCount || 0) > 0 || (p.ruleCount || 0) > 0) {
			uni.showToast({ title: t('admin.position.inUse'), icon: 'none' })
			return
		}
		showModal({
			title: t('common.tips'),
			content: t('admin.position.delConfirm', { name: p.name }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deletePosition(p.id)
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

	.overview-card {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 28rpx;
		border-radius: var(--radius-lg);
		margin-bottom: 20rpx;
	}

	.overview-main {
		flex: 1;
		min-width: 0;
	}

	.overview-label {
		display: block;
		font-size: 24rpx;
		color: var(--text-secondary);
	}

	.overview-num {
		display: block;
		font-size: 52rpx;
		font-weight: 700;
		line-height: 1.2;
		color: var(--text-primary);
		margin-top: 4rpx;
	}

	.overview-desc {
		display: block;
		font-size: 22rpx;
		color: var(--text-tertiary);
		margin-top: 6rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		padding: 0 8rpx 0 20rpx;
		height: 84rpx;
		box-sizing: border-box;
		margin-bottom: 20rpx;
	}

	.search-icon {
		font-size: 28rpx;
		color: var(--text-tertiary);
		margin-right: 12rpx;
	}

	.search-input {
		flex: 1;
		min-width: 0;
		height: 84rpx;
		font-size: 27rpx;
		color: var(--text-primary);
	}

	.search-btn {
		flex-shrink: 0;
		padding: 0 24rpx;
		height: 64rpx;
		line-height: 64rpx;
		border-radius: var(--radius-pill);
		background: var(--primary-fill);
		color: var(--on-primary);
		font-size: 25rpx;
		font-weight: 600;
	}

	.create-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20rpx;
	}

	.create-icon {
		font-size: 30rpx;
		margin-right: 8rpx;
	}

	.data-card {
		padding: 26rpx 24rpx;
		border-radius: var(--radius-lg);
		margin-bottom: 20rpx;
	}

	.card-head {
		display: flex;
		align-items: flex-start;
	}

	.card-head .icon-chip {
		margin-right: 18rpx;
	}

	.card-main {
		flex: 1;
		min-width: 0;
	}

	.title-row {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.card-title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		max-width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.card-sub {
		display: block;
		font-size: 24rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
		word-break: break-all;
	}

	.card-meta {
		display: block;
		font-size: 22rpx;
		color: var(--text-tertiary);
		margin-top: 6rpx;
	}

	.card-ops {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		margin-top: 18rpx;
		padding-top: 18rpx;
		border-top: 1rpx solid var(--border);
		gap: 14rpx;
	}

	.op-btn {
		display: flex;
		align-items: center;
		height: 60rpx;
		padding: 0 22rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		color: var(--text-secondary);
		font-size: 23rpx;
	}

	.op-btn.danger {
		background: rgba(179, 69, 59, 0.08);
		color: var(--danger);
	}

	.op-ico {
		font-size: 24rpx;
		margin-right: 6rpx;
	}

	.chip {
		display: inline-flex;
		align-items: center;
		font-size: 20rpx;
		font-weight: 500;
		padding: 2rpx 14rpx;
		border-radius: var(--radius-pill);
		line-height: 1.6;
		white-space: nowrap;
	}

	.chip-green {
		background: rgba(46, 125, 91, 0.12);
		color: var(--success);
	}

	.chip-gray {
		background: var(--surface-2);
		color: var(--text-secondary);
	}

	.row-empty {
		text-align: center;
		font-size: 25rpx;
		color: var(--text-tertiary);
		padding: 90rpx 0;
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

	.f-area {
		min-height: 140rpx;
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
