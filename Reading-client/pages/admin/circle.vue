<template>
	<view class="page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<view class="btn-primary create-btn" @click="openForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.circle.create') }}</text>
			</view>

			<view class="empty-box" v-if="!loading && !list.length">
				<r-icon name="book-2-line" class="empty-icon" />
				<text>{{ $t('admin.circle.empty') }}</text>
			</view>

			<view class="circle-card glass-card" v-for="item in list" :key="item.id">
				<view class="c-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="book-2-line" /></view>
					<view class="c-main">
						<text class="c-name">{{ item.name }}</text>
						<text class="c-desc" v-if="item.description">{{ item.description }}</text>
					</view>
					<view class="r-ops">
						<view class="r-op" @click="openForm(item)">
							<r-icon name="edit-2-line" class="r-op-text" />
						</view>
						<view class="r-op danger" @click="onDelete(item)">
							<r-icon name="delete-bin-line" class="r-op-text danger" />
						</view>
					</view>
				</view>
				<view class="c-meta">
					<view class="c-meta-item">
						<text class="c-meta-num">{{ item.memberCount || 0 }}</text>
						<text class="c-meta-label">{{ $t('admin.circle.memberLabel') }}</text>
					</view>
					<view class="c-meta-divider"></view>
					<view class="c-code" @click="copyCode(item)">
						<text class="c-code-label">{{ $t('admin.circle.inviteCode') }}</text>
						<text class="c-code-value">{{ item.inviteCode || '-' }}</text>
						<text class="c-code-copy">{{ $t('admin.circle.copy') }}</text>
					</view>
				</view>
				<view class="member-entry" @click="openMembers(item)">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="group-2-line" /></view>
					<text class="member-entry__text">{{ $t('admin.circle.members') }}</text>
					<r-icon name="arrow-right-s-line" class="member-entry__arrow" />
				</view>
			</view>
		</view>

		<!-- 新建/编辑弹层 -->
		<view class="ui-modal-mask" v-if="showForm" @click="showForm = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ form.id ? $t('admin.circle.edit') : $t('admin.circle.create') }}</text>
					<view class="ui-sheet-close" @click="showForm = false"><r-icon name="close-line" /></view>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.circle.name') }}</text>
					<input class="input-item" v-model="form.name" :placeholder="$t('admin.circle.namePh')" placeholder-class="input-ph" />
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.circle.desc') }}</text>
					<textarea class="input-item" v-model="form.description" :placeholder="$t('admin.circle.descPh')" placeholder-class="input-ph" />
				</view>
				<view class="modal-ops">
					<view class="btn-plain modal-btn" @click="showForm = false">{{ $t('common.cancel') }}</view>
					<view class="btn-primary modal-btn" @click="save">{{ $t('common.save') }}</view>
				</view>
			</view>
		</view>

		<!-- 成员管理弹层 -->
		<view class="ui-modal-mask" v-if="showMembers" @click="showMembers = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.circle.membersOf', { name: currentCircle.name }) }}</text>
					<view class="ui-sheet-close" @click="showMembers = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="member-row" v-for="m in members" :key="m.id">
						<image class="member-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="member-info">
							<text class="member-name">{{ m.nickname || m.username || '-' }}</text>
							<text class="member-sub">{{ m.phone || m.position || '-' }}</text>
						</view>
						<view class="member-del" @click="onRemoveMember(m)">{{ $t('admin.circle.remove') }}</view>
					</view>
					<view class="row-empty" v-if="!members.length">
						<text>{{ memberLoading ? $t('common.loading') : $t('admin.circle.noMembers') }}</text>
					</view>
				</scroll-view>
				<view class="btn-primary modal-btn member-invite-btn" @click="openInvite">{{ $t('admin.circle.inviteMember') }}</view>
			</view>
		</view>

		<!-- 邀请成员弹层 -->
		<view class="ui-modal-mask" v-if="showInvite" @click="showInvite = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.circle.inviteTitle') }}</text>
					<view class="ui-sheet-close" @click="showInvite = false"><r-icon name="close-line" /></view>
				</view>
				<view class="search-box">
					<r-icon name="search-line" class="search-icon" />
					<input
						class="search-input"
						v-model="keyword"
						:placeholder="$t('admin.circle.invitePh')"
						placeholder-class="input-ph"
						@input="onSearchInput"
					/>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="member-row" v-for="u in users" :key="u.id" @click="invite(u)">
						<image class="member-avatar" :src="u.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="member-info">
							<text class="member-name">{{ u.nickname || u.username || '-' }}</text>
							<text class="member-sub">{{ u.phone || u.position || '-' }}</text>
						</view>
						<view class="member-state" v-if="u.circleId === currentCircle.id">
							{{ $t('admin.circle.inCircle') }}
						</view>
						<view class="member-invite" v-else>{{ $t('admin.circle.inviteAction') }}</view>
					</view>
					<view class="row-empty" v-if="!users.length">
						<text>{{ userLoading ? $t('common.loading') : $t('admin.circle.noUser') }}</text>
					</view>
				</scroll-view>
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
	import {
		getCircleList, createCircle, updateCircle, deleteCircle,
		getCircleMembers, inviteCircleMember, removeCircleMember, getUserPage
	} from '@/api/admin.js'

	const list = ref([])
	const loading = ref(false)
	const showForm = ref(false)
	const form = ref({})

	/* 成员管理 */
	const showMembers = ref(false)
	const showInvite = ref(false)
	const currentCircle = ref({})
	const members = ref([])
	const memberLoading = ref(false)
	const users = ref([])
	const userLoading = ref(false)
	const keyword = ref('')
	let searchTimer = null

	async function load() {
		loading.value = true
		try {
			const data = await getCircleList()
			list.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			list.value = []
		} finally {
			loading.value = false
		}
	}

	function openForm(item) {
		form.value = item
			? { id: item.id, name: item.name, description: item.description || '' }
			: { name: '', description: '' }
		showForm.value = true
	}

	async function save() {
		if (!form.value.name || !form.value.name.trim()) {
			uni.showToast({ title: t('admin.circle.errName'), icon: 'none' })
			return
		}
		try {
			if (form.value.id) {
				await updateCircle(form.value)
			} else {
				await createCircle(form.value)
			}
			showForm.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load()
		} catch (e) {}
	}

	function copyCode(item) {
		if (!item.inviteCode) return
		uni.setClipboardData({
			data: item.inviteCode,
			success: () => {
				uni.showToast({ title: t('admin.circle.copied'), icon: 'none' })
			}
		})
	}

	function onDelete(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.circle.delConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteCircle(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load()
				} catch (e) {}
			}
		})
	}

	/* ================= 成员管理 ================= */

	function openMembers(item) {
		currentCircle.value = item
		members.value = []
		showMembers.value = true
		loadMembers()
	}

	async function loadMembers() {
		memberLoading.value = true
		try {
			const data = await getCircleMembers(currentCircle.value.id, { current: 1, size: 100 })
			members.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			members.value = []
		} finally {
			memberLoading.value = false
		}
	}

	function onRemoveMember(m) {
		showModal({
			title: t('common.tips'),
			content: t('admin.circle.removeConfirm', { name: m.nickname || m.username || '' }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await removeCircleMember(currentCircle.value.id, m.id)
					uni.showToast({ title: t('admin.circle.removeSuccess'), icon: 'none' })
					await loadMembers()
					load()
				} catch (e) {}
			}
		})
	}

	function openInvite() {
		showInvite.value = true
		keyword.value = ''
		loadUsers()
	}

	function onSearchInput() {
		clearTimeout(searchTimer)
		searchTimer = setTimeout(loadUsers, 300)
	}

	async function loadUsers() {
		userLoading.value = true
		try {
			const data = await getUserPage({ keyword: keyword.value.trim(), current: 1, size: 100 })
			users.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			users.value = []
		} finally {
			userLoading.value = false
		}
	}

	async function invite(u) {
		if (u.circleId === currentCircle.value.id) return
		try {
			await inviteCircleMember(currentCircle.value.id, u.id)
			uni.showToast({ title: t('admin.circle.inviteSuccess'), icon: 'none' })
			loadUsers()
			loadMembers()
			load()
		} catch (e) {}
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

	.circle-card {
		margin-bottom: 20rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
	}

	.c-head {
		display: flex;
		align-items: flex-start;
	}

	.c-main {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
	}

	.c-name {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.c-desc {
		display: block;
		font-size: 23rpx;
		color: var(--text-secondary);
		margin-top: 8rpx;
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

	.c-meta {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
		padding: 20rpx 24rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.c-meta-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		min-width: 120rpx;
	}

	.c-meta-num {
		font-size: 34rpx;
		font-weight: 700;
		color: var(--primary);
		line-height: 1.2;
	}

	.c-meta-label {
		font-size: 20rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.c-meta-divider {
		width: 1rpx;
		height: 52rpx;
		background: var(--border);
		margin: 0 24rpx;
	}

	.c-code {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		flex-wrap: wrap;
	}

	.c-code-label {
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-right: 12rpx;
	}

	.c-code-value {
		font-size: 26rpx;
		font-weight: 600;
		letter-spacing: 2rpx;
		color: var(--text-primary);
	}

	.c-code-copy {
		font-size: 20rpx;
		color: var(--primary);
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 4rpx 14rpx;
		margin-left: 12rpx;
	}

	.form-modal {
		padding: 32rpx 28rpx 28rpx;
	}

	.member-entry {
		display: flex;
		align-items: center;
		margin-top: 20rpx;
		padding: 20rpx 24rpx;
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
	}

	.member-entry__text {
		flex: 1;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--text-primary);
		margin-left: 14rpx;
	}

	.member-entry__arrow {
		font-size: 28rpx;
		color: var(--text-tertiary);
	}

	.member-modal {
		padding: 32rpx 28rpx 28rpx;
	}

	.member-scroll {
		max-height: 56vh;
		margin-top: 16rpx;
	}

	.member-row {
		display: flex;
		align-items: center;
		padding: 18rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.member-row:last-child {
		border-bottom: none;
	}

	.member-avatar {
		width: 76rpx;
		height: 76rpx;
		border-radius: 50%;
		background: var(--surface-2);
		flex-shrink: 0;
	}

	.member-info {
		flex: 1;
		min-width: 0;
		margin-left: 18rpx;
	}

	.member-name {
		display: block;
		font-size: 27rpx;
		font-weight: 600;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.member-sub {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
	}

	.member-del {
		flex-shrink: 0;
		font-size: 23rpx;
		color: var(--danger);
		background: rgba(179, 69, 59, 0.08);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 16rpx;
	}

	.member-invite {
		flex-shrink: 0;
		font-size: 23rpx;
		color: var(--primary);
		background: var(--surface-2);
		border: 1rpx solid var(--border-strong);
		border-radius: var(--radius-pill);
		padding: 8rpx 20rpx;
		margin-left: 16rpx;
	}

	.member-state {
		flex-shrink: 0;
		font-size: 22rpx;
		color: var(--text-tertiary);
		margin-left: 16rpx;
	}

	.member-invite-btn {
		margin-top: 24rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		margin-top: 20rpx;
		padding: 0 20rpx;
		height: 76rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
	}

	.search-icon {
		font-size: 26rpx;
		color: var(--text-tertiary);
	}

	.search-input {
		flex: 1;
		height: 100%;
		font-size: 26rpx;
		color: var(--text-primary);
		margin-left: 12rpx;
	}

	.row-empty {
		padding: 40rpx 0;
		text-align: center;
		font-size: 24rpx;
		color: var(--text-tertiary);
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

	.modal-ops {
		display: flex;
		margin-top: 32rpx;
		gap: 20rpx;
	}

	.modal-btn {
		flex: 1;
	}
</style>
