<template>
	<view class="page manage-page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<view class="content">
			<!-- 数据表切换 -->
			<view class="segmented segmented--brand table-tabs">
				<view
					class="segmented__item"
					:class="{ 'segmented__item--active': table === 'users' }"
					@click="switchTable('users')"
				>
					<r-icon name="user-3-line" class="seg-ico" />
					<text>{{ $t('admin.manage.tabUsers') }}</text>
				</view>
				<view
					class="segmented__item"
					:class="{ 'segmented__item--active': table === 'books' }"
					@click="switchTable('books')"
				>
					<r-icon name="book-2-line" class="seg-ico" />
					<text>{{ $t('admin.manage.tabBooks') }}</text>
				</view>
				<view
					class="segmented__item"
					:class="{ 'segmented__item--active': table === 'checkins' }"
					@click="switchTable('checkins')"
				>
					<r-icon name="file-list-3-line" class="seg-ico" />
					<text>{{ $t('admin.manage.tabCheckins') }}</text>
				</view>
			</view>

			<!-- 查询筛选 -->
			<view class="filter-card glass-card">
				<view class="search-box">
					<r-icon name="search-line" class="search-icon" />
					<input
						class="search-input"
						v-model="keyword"
						:placeholder="searchPlaceholder"
						placeholder-class="input-ph"
						confirm-type="search"
						@confirm="search"
					/>
					<view class="search-btn" @click="search">{{ $t('common.search') }}</view>
				</view>
				<view class="filter-row" v-if="table !== 'books'">
					<view class="filter-col">
						<r-picker
							:range="circleOptions"
							range-key="name"
							:value="circleIndex"
							:title="$t('admin.manage.circle')"
							:placeholder="$t('admin.manage.allCircles')"
							@change="onCircleChange"
						/>
					</view>
					<view class="filter-col" v-if="table === 'checkins'">
						<r-picker
							:range="checkinStatusFilterLabels"
							:value="statusIndex"
							:title="$t('admin.manage.status')"
							:placeholder="$t('admin.manage.allStatus')"
							@change="onStatusFilterChange"
						/>
					</view>
				</view>
			</view>

			<view class="btn-primary create-btn" @click="openCreate">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ createLabel }}</text>
			</view>

			<view class="total-line">
				<text class="total-text">{{ $t('admin.manage.total', { n: total }) }}</text>
			</view>

			<!-- 用户列表 -->
			<template v-if="table === 'users'">
				<view class="data-card glass-card" v-for="u in list" :key="'u' + u.id">
					<view class="card-head">
						<image class="avatar" :src="u.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="card-main">
							<view class="title-row">
								<text class="card-title">{{ u.nickname || u.username || '-' }}</text>
								<text class="chip" :class="roleChip(u.role)">{{ roleLabel(u.role) }}</text>
								<text class="chip" :class="u.status === 0 ? 'chip-red' : 'chip-green'">
									{{ u.status === 0 ? $t('admin.manage.disabled') : $t('admin.manage.enabled') }}
								</text>
							</view>
							<text class="card-sub">{{ u.username }} · {{ u.phone || $t('common.none') }}</text>
							<text class="card-meta">ID {{ u.id }} · {{ u.position || '-' }} · {{ circleName(u.circleId) }}</text>
						</view>
					</view>
					<view class="card-ops">
						<view class="op-btn" @click="openUserForm(u)">
							<r-icon name="ball-pen-line" class="op-ico" />
							<text>{{ $t('admin.manage.edit') }}</text>
						</view>
						<view class="op-btn danger" v-if="u.id !== me.id" @click="removeUser(u)">
							<r-icon name="delete-bin-line" class="op-ico" />
							<text>{{ $t('admin.manage.delete') }}</text>
						</view>
						<text class="op-tip" v-else>{{ $t('admin.manage.selfTip') }}</text>
					</view>
				</view>
			</template>

			<!-- 书籍列表 -->
			<template v-if="table === 'books'">
				<view class="data-card glass-card" v-for="b in list" :key="'b' + b.id">
					<view class="card-head">
						<image v-if="b.cover" class="cover" :src="b.cover" mode="aspectFill" />
						<view v-else class="icon-chip cover-ph"><r-icon name="book-open-line" /></view>
						<view class="card-main">
							<view class="title-row">
								<text class="card-title">{{ b.title }}</text>
								<text class="chip" :class="b.status === 0 ? 'chip-gray' : 'chip-green'">
									{{ b.status === 0 ? $t('admin.manage.offline') : $t('admin.manage.online') }}
								</text>
							</view>
							<text class="card-sub">{{ b.author || '-' }} · {{ b.category || '-' }}</text>
							<text class="card-meta">
								ID {{ b.id }} · {{ $t('admin.manage.chapterCount', { n: b.chapterCount || 0 }) }}
								· {{ b.isPublic === 0 ? $t('admin.manage.privateBook') : $t('admin.manage.publicBook') }}
							</text>
						</view>
					</view>
					<view class="card-ops">
						<view class="op-btn" @click="openChapter(b)">
							<r-icon name="file-list-3-line" class="op-ico" />
							<text>{{ $t('admin.manage.chapterManage') }}</text>
						</view>
						<view class="op-btn" @click="openBookForm(b)">
							<r-icon name="ball-pen-line" class="op-ico" />
							<text>{{ $t('admin.manage.edit') }}</text>
						</view>
						<view class="op-btn danger" @click="removeBook(b)">
							<r-icon name="delete-bin-line" class="op-ico" />
							<text>{{ $t('admin.manage.delete') }}</text>
						</view>
					</view>
				</view>
			</template>

			<!-- 打卡记录列表 -->
			<template v-if="table === 'checkins'">
				<view class="data-card glass-card" v-for="r in list" :key="'c' + r.id">
					<view class="card-head">
						<view class="icon-chip icon-chip--sm" :class="typeColor(r.checkinType)">
							<r-icon :name="typeIcon(r.checkinType)" />
						</view>
						<view class="card-main">
							<view class="title-row">
								<text class="card-title">{{ r.nickname || ('#' + r.userId) }}</text>
								<text class="chip chip-gray">{{ typeLabel(r.checkinType) }}</text>
								<text class="chip" :class="checkinStatusChip(r.status)">{{ checkinStatusLabel(r.status) }}</text>
							</view>
							<text class="card-sub">
								{{ r.bookTitle || $t('common.none') }}<text v-if="r.chapterTitle"> · {{ r.chapterTitle }}</text>
							</text>
							<text class="card-meta">ID {{ r.id }} · {{ r.checkinDate || '-' }}</text>
							<text class="card-content" v-if="r.content">{{ r.content }}</text>
						</view>
					</view>
					<view class="card-ops">
						<view class="op-btn" @click="openCheckinForm(r)">
							<r-icon name="ball-pen-line" class="op-ico" />
							<text>{{ $t('admin.manage.edit') }}</text>
						</view>
						<view class="op-btn danger" @click="removeCheckin(r)">
							<r-icon name="delete-bin-line" class="op-ico" />
							<text>{{ $t('admin.manage.delete') }}</text>
						</view>
					</view>
				</view>
			</template>

			<view class="row-empty" v-if="!loading && !list.length">{{ $t('admin.manage.empty') }}</view>
			<view class="load-more" v-if="list.length && list.length < total" @click="loadMore">
				<text class="load-more-text">{{ loading ? $t('common.loading') : $t('admin.manage.loadMore') }}</text>
			</view>
		</view>

		<!-- 用户表单 -->
		<view class="ui-modal-mask form-mask" v-if="formVisible && table === 'users'" @click="formVisible = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ userForm.id ? $t('admin.manage.editUser') : $t('admin.manage.createUser') }}</text>
					<view class="ui-sheet-close" @click="formVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.username') }}</text>
						<input class="input-item" v-model="userForm.username" :placeholder="$t('admin.manage.usernamePh')" placeholder-class="input-ph" />
					</view>
					<view class="field" v-if="!userForm.id">
						<text class="field__label">{{ $t('admin.manage.password') }}</text>
						<input class="input-item" v-model="userForm.password" :placeholder="$t('admin.manage.passwordPh')" placeholder-class="input-ph" />
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.nickname') }}</text>
						<input class="input-item" v-model="userForm.nickname" :placeholder="$t('admin.manage.nicknamePh')" placeholder-class="input-ph" />
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.phone') }}</text>
							<input class="input-item" type="number" v-model="userForm.phone" :placeholder="$t('admin.manage.phonePh')" placeholder-class="input-ph" />
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.gender') }}</text>
							<r-picker
								:range="genderLabels"
								:value="userForm.gender || 0"
								:title="$t('admin.manage.gender')"
								@change="e => userForm.gender = Number(e.detail.value)"
							/>
						</view>
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.role') }}</text>
							<r-picker
								:range="roleLabels"
								:value="roleIndex(userForm.role)"
								:title="$t('admin.manage.role')"
								@change="onRoleChange"
							/>
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.status') }}</text>
							<r-picker
								:range="enableLabels"
								:value="userForm.status === 0 ? 1 : 0"
								:title="$t('admin.manage.status')"
								@change="e => userForm.status = Number(e.detail.value) === 0 ? 1 : 0"
							/>
						</view>
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.position') }}</text>
						<input class="input-item" v-model="userForm.position" :placeholder="$t('admin.manage.positionPh')" placeholder-class="input-ph" />
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.circle') }}</text>
						<r-picker
							:range="userCircleOptions"
							range-key="name"
							:value="userCircleIndex()"
							:title="$t('admin.manage.circle')"
							:placeholder="$t('admin.manage.noCircle')"
							@change="onUserCircleChange"
						/>
					</view>
					<view class="modal-ops">
						<view class="btn-plain modal-btn" @click="formVisible = false">{{ $t('common.cancel') }}</view>
						<view class="btn-primary modal-btn" @click="saveUser">{{ $t('common.save') }}</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 书籍表单 -->
		<view class="ui-modal-mask form-mask" v-if="formVisible && table === 'books'" @click="formVisible = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ bookForm.id ? $t('admin.manage.editBook') : $t('admin.manage.createBook') }}</text>
					<view class="ui-sheet-close" @click="formVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.bookTitle') }}</text>
						<input class="input-item" v-model="bookForm.title" :placeholder="$t('admin.manage.bookTitlePh')" placeholder-class="input-ph" />
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.author') }}</text>
							<input class="input-item" v-model="bookForm.author" :placeholder="$t('admin.manage.authorPh')" placeholder-class="input-ph" />
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.publisher') }}</text>
							<input class="input-item" v-model="bookForm.publisher" :placeholder="$t('admin.manage.publisherPh')" placeholder-class="input-ph" />
						</view>
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.category') }}</text>
							<input class="input-item" v-model="bookForm.category" :placeholder="$t('admin.manage.categoryPh')" placeholder-class="input-ph" />
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.bookType') }}</text>
							<r-picker
								:range="bookTypeLabels"
								:value="bookForm.bookType === 2 ? 1 : 0"
								:title="$t('admin.manage.bookType')"
								@change="e => bookForm.bookType = Number(e.detail.value) === 1 ? 2 : 1"
							/>
						</view>
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.status') }}</text>
							<r-picker
								:range="bookStatusLabels"
								:value="bookForm.status === 0 ? 1 : 0"
								:title="$t('admin.manage.status')"
								@change="e => bookForm.status = Number(e.detail.value) === 1 ? 0 : 1"
							/>
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.publicBook') }}</text>
							<r-picker
								:range="publicLabels"
								:value="bookForm.isPublic === 0 ? 1 : 0"
								:title="$t('admin.manage.publicBook')"
								@change="e => bookForm.isPublic = Number(e.detail.value) === 1 ? 0 : 1"
							/>
						</view>
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.cover') }}</text>
						<input class="input-item" v-model="bookForm.cover" :placeholder="$t('admin.manage.coverPh')" placeholder-class="input-ph" />
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.description') }}</text>
						<textarea class="input-item f-area" v-model="bookForm.description" :placeholder="$t('admin.manage.descriptionPh')" placeholder-class="input-ph" />
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.recommend') }}</text>
						<textarea class="input-item f-area" v-model="bookForm.recommendReason" :placeholder="$t('admin.manage.recommendPh')" placeholder-class="input-ph" />
					</view>
					<view class="chapter-entry" v-if="bookForm.id" @click="openChapter(bookForm)">
						<r-icon name="file-list-3-line" class="chapter-entry-ico" />
						<text class="chapter-entry-text">{{ $t('admin.manage.chapterManage') }}</text>
						<r-icon name="arrow-right-s-line" class="chapter-entry-arrow" />
					</view>
					<view class="modal-ops">
						<view class="btn-plain modal-btn" @click="formVisible = false">{{ $t('common.cancel') }}</view>
						<view class="btn-primary modal-btn" @click="saveBook">{{ $t('common.save') }}</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 打卡记录表单 -->
		<view class="ui-modal-mask form-mask" v-if="formVisible && table === 'checkins'" @click="formVisible = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ checkinForm.id ? $t('admin.manage.editCheckin') : $t('admin.manage.createCheckin') }}</text>
					<view class="ui-sheet-close" @click="formVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.member') }}</text>
						<input class="input-item" type="number" v-model="checkinForm.userId" :placeholder="$t('admin.manage.memberPh')" placeholder-class="input-ph" />
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.book') }}</text>
						<r-picker
							:range="bookOptions"
							range-key="title"
							:value="checkinForm.bookId ? bookIndex() : -1"
							:title="$t('admin.manage.book')"
							:placeholder="$t('admin.manage.selectBook')"
							@change="onFormBookChange"
						/>
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.chapterName') }}</text>
						<r-picker
							:range="formChapterOptions"
							range-key="title"
							:value="formChapterIndex()"
							:title="$t('admin.manage.chapterName')"
							:placeholder="$t('common.none')"
							@change="onFormChapterChange"
						/>
					</view>
					<view class="field field__row">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.type') }}</text>
							<r-picker
								:range="checkinTypeLabels"
								:value="typeIndex(checkinForm.checkinType)"
								:title="$t('admin.manage.type')"
								@change="onFormTypeChange"
							/>
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.checkinDate') }}</text>
							<r-picker
								mode="date"
								:value="checkinForm.checkinDate"
								:title="$t('admin.manage.checkinDate')"
								:placeholder="$t('admin.manage.checkinDate')"
								@change="e => checkinForm.checkinDate = e.detail.value"
							/>
						</view>
					</view>
					<view class="field field__row" v-if="checkinForm.id">
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.status') }}</text>
							<r-picker
								:range="checkinStatusLabels"
								:value="checkinForm.status || 0"
								:title="$t('admin.manage.status')"
								@change="e => checkinForm.status = Number(e.detail.value)"
							/>
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.isPublic') }}</text>
							<r-picker
								:range="publicLabels"
								:value="checkinForm.isPublic === 0 ? 1 : 0"
								:title="$t('admin.manage.isPublic')"
								@change="e => checkinForm.isPublic = Number(e.detail.value) === 1 ? 0 : 1"
							/>
						</view>
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.content') }}</text>
						<textarea class="input-item f-area" v-model="checkinForm.content" :placeholder="$t('admin.manage.contentPh')" placeholder-class="input-ph" />
					</view>
					<view class="field" v-if="checkinForm.id">
						<text class="field__label">{{ $t('admin.manage.remark') }}</text>
						<input class="input-item" v-model="checkinForm.auditRemark" :placeholder="$t('admin.manage.remarkPh')" placeholder-class="input-ph" />
					</view>
					<view class="modal-ops">
						<view class="btn-plain modal-btn" @click="formVisible = false">{{ $t('common.cancel') }}</view>
						<view class="btn-primary modal-btn" @click="saveCheckin">{{ $t('common.save') }}</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 章节管理 -->
		<view class="ui-modal-mask chapter-mask" v-if="chapterVisible" @click="closeChapter">
			<view class="ui-modal-card chapter-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.manage.chapterTitle') }} · {{ chapterBook ? chapterBook.title : '' }}</text>
					<view class="ui-sheet-close" @click="closeChapter"><r-icon name="close-line" /></view>
				</view>
				<view class="chapter-form">
					<view class="field field__row">
						<view class="field__col chapter-no-col">
							<text class="field__label">{{ $t('admin.manage.chapterNo') }}</text>
							<input class="input-item" type="number" v-model="chapterForm.chapterNo" placeholder-class="input-ph" />
						</view>
						<view class="field__col">
							<text class="field__label">{{ $t('admin.manage.chapterName') }}</text>
							<input class="input-item" v-model="chapterForm.title" :placeholder="$t('admin.manage.chapterNamePh')" placeholder-class="input-ph" />
						</view>
					</view>
					<view class="field">
						<text class="field__label">{{ $t('admin.manage.chapterContent') }}</text>
						<textarea class="input-item chapter-area" v-model="chapterForm.content" :placeholder="$t('admin.manage.chapterContentPh')" placeholder-class="input-ph" />
					</view>
					<view class="chapter-form-ops">
						<view class="btn-plain mini-btn" v-if="chapterForm.id" @click="resetChapterForm">{{ $t('common.cancel') }}</view>
						<view class="btn-primary mini-btn" @click="saveChapter">{{ $t('admin.manage.chapterSave') }}</view>
					</view>
				</view>
				<scroll-view scroll-y class="chapter-list">
					<view class="chapter-row" v-for="c in chapters" :key="c.id">
						<text class="chapter-no">{{ c.chapterNo }}</text>
						<text class="chapter-name">{{ c.title }}</text>
						<view class="chapter-ops">
							<view class="op-btn" @click="editChapter(c)">
								<r-icon name="ball-pen-line" class="op-ico" />
							</view>
							<view class="op-btn danger" @click="removeChapter(c)">
								<r-icon name="delete-bin-line" class="op-ico" />
							</view>
						</view>
					</view>
					<view class="row-empty" v-if="!chapters.length">{{ $t('admin.manage.chapterEmpty') }}</view>
				</scroll-view>
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
	import { onShow, onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
	import {
		getCircleList,
		getDataUserPage, createDataUser, updateDataUser, deleteDataUser,
		getDataBookPage, createDataBook, updateDataBook, deleteDataBook,
		getChapterList, createChapter, updateChapter, deleteChapter,
		getDataCheckinPage, createDataCheckin, updateDataCheckin, deleteDataCheckin
	} from '@/api/admin.js'

	const PAGE_SIZE = 10
	const me = computed(() => userState.userInfo || {})

	const table = ref('users')
	const keyword = ref('')
	const list = ref([])
	const total = ref(0)
	const pageNo = ref(1)
	const loading = ref(false)

	const circles = ref([])
	const circleIndex = ref(0)
	const statusIndex = ref(0)

	const formVisible = ref(false)
	const userForm = ref({})
	const bookForm = ref({})
	const checkinForm = ref({})
	const bookOptions = ref([])
	const formChapters = ref([])

	const chapterVisible = ref(false)
	const chapterBook = ref(null)
	const chapters = ref([])
	const chapterForm = ref({})

	const roleValues = ['MEMBER', 'TEAM_ADMIN', 'SUPER_ADMIN']
	const genderValues = [0, 1, 2]
	const bookTypeValues = [1, 2]
	const bookStatusValues = [1, 0]
	const publicValues = [1, 0]
	const checkinTypeValues = [1, 2, 3, 4]
	const checkinStatusValues = [0, 1, 2]
	const checkinStatusFilterValues = ['', 0, 1, 2]

	const roleLabels = computed(() => [
		t('admin.manage.roleMember'),
		t('admin.manage.roleTeam'),
		t('admin.manage.roleSuper')
	])
	const genderLabels = computed(() => [
		t('admin.manage.unknown'),
		t('admin.manage.male'),
		t('admin.manage.female')
	])
	const enableLabels = computed(() => [t('admin.manage.enabled'), t('admin.manage.disabled')])
	const bookTypeLabels = computed(() => [t('admin.manage.ebook'), t('admin.manage.paper')])
	const bookStatusLabels = computed(() => [t('admin.manage.online'), t('admin.manage.offline')])
	const publicLabels = computed(() => [t('admin.manage.publicBook'), t('admin.manage.privateBook')])
	const checkinTypeLabels = computed(() => [
		t('admin.manage.typeImage'),
		t('admin.manage.typeAudio'),
		t('admin.manage.typeNote'),
		t('admin.manage.typeVideo')
	])
	const checkinStatusLabels = computed(() => [
		t('admin.manage.checkinPending'),
		t('admin.manage.checkinPass'),
		t('admin.manage.checkinReject')
	])
	const checkinStatusFilterLabels = computed(() => [t('admin.manage.allStatus')].concat(checkinStatusLabels.value))

	const circleOptions = computed(() => {
		return [{ id: null, name: t('admin.manage.allCircles') }].concat(circles.value.map(c => ({ id: c.id, name: c.name })))
	})
	const userCircleOptions = computed(() => {
		return [{ id: null, name: t('admin.manage.noCircle') }].concat(circles.value.map(c => ({ id: c.id, name: c.name })))
	})
	const formChapterOptions = computed(() => {
		return [{ id: null, title: t('common.none') }].concat(formChapters.value.map(c => ({ id: c.id, title: c.chapterNo + '. ' + c.title, chapterTitle: c.title })))
	})

	const searchPlaceholder = computed(() => {
		if (table.value === 'books') return t('admin.manage.searchBookPh')
		return t('admin.manage.searchUserPh')
	})
	const createLabel = computed(() => {
		if (table.value === 'books') return t('admin.manage.createBook')
		if (table.value === 'checkins') return t('admin.manage.createCheckin')
		return t('admin.manage.createUser')
	})

	function roleIndex(role) {
		const i = roleValues.indexOf(role)
		return i < 0 ? 0 : i
	}

	function roleLabel(role) {
		return roleLabels.value[roleIndex(role)]
	}

	function roleChip(role) {
		if (role === 'SUPER_ADMIN') return 'chip-violet'
		if (role === 'TEAM_ADMIN') return 'chip-blue'
		return 'chip-gray'
	}

	function circleName(id) {
		if (!id) return t('admin.manage.noCircle')
		const c = circles.value.find(item => item.id === id)
		return c ? c.name : '#' + id
	}

	function typeIndex(type) {
		const i = checkinTypeValues.indexOf(type)
		return i < 0 ? 2 : i
	}

	function typeLabel(type) {
		return checkinTypeLabels.value[typeIndex(type)]
	}

	function typeIcon(type) {
		if (type === 1) return 'image-line'
		if (type === 2) return 'mic-line'
		if (type === 4) return 'play-fill'
		return 'file-text-line'
	}

	function typeColor(type) {
		if (type === 1) return 'icon-blue'
		if (type === 2) return 'icon-violet'
		if (type === 4) return 'icon-green'
		return 'icon-amber'
	}

	function checkinStatusLabel(status) {
		const i = checkinStatusValues.indexOf(status)
		return checkinStatusLabels.value[i < 0 ? 1 : i]
	}

	function checkinStatusChip(status) {
		if (status === 0) return 'chip-amber'
		if (status === 2) return 'chip-red'
		return 'chip-green'
	}

	async function loadCircles() {
		try {
			const data = await getCircleList()
			circles.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			circles.value = []
		}
	}

	async function loadBooks() {
		if (bookOptions.value.length) return
		try {
			const data = await getDataBookPage({ current: 1, size: 200 })
			bookOptions.value = data.records || []
		} catch (e) {
			bookOptions.value = []
		}
	}

	function currentCircleId() {
		const opt = circleOptions.value[circleIndex.value]
		return opt && opt.id ? opt.id : null
	}

	async function load(reset) {
		if (reset) {
			pageNo.value = 1
			list.value = []
		}
		loading.value = true
		try {
			let data
			if (table.value === 'users') {
				data = await getDataUserPage({
					keyword: keyword.value,
					circleId: currentCircleId(),
					current: pageNo.value,
					size: PAGE_SIZE
				})
			} else if (table.value === 'books') {
				data = await getDataBookPage({
					keyword: keyword.value,
					current: pageNo.value,
					size: PAGE_SIZE
				})
			} else {
				const status = checkinStatusFilterValues[statusIndex.value]
				data = await getDataCheckinPage({
					keyword: keyword.value,
					circleId: currentCircleId(),
					status: status === '' ? null : status,
					current: pageNo.value,
					size: PAGE_SIZE
				})
			}
			const records = data.records || []
			list.value = reset ? records : list.value.concat(records)
			total.value = data.total || 0
		} catch (e) {
			if (reset) {
				list.value = []
				total.value = 0
			}
		} finally {
			loading.value = false
		}
	}

	function loadMore() {
		if (loading.value) return
		pageNo.value = pageNo.value + 1
		load(false)
	}

	function search() {
		load(true)
	}

	function switchTable(key) {
		if (table.value === key) return
		table.value = key
		keyword.value = ''
		circleIndex.value = 0
		statusIndex.value = 0
		load(true)
	}

	function onCircleChange(e) {
		circleIndex.value = Number(e.detail.value)
		load(true)
	}

	function onStatusFilterChange(e) {
		statusIndex.value = Number(e.detail.value)
		load(true)
	}

	function today() {
		const d = new Date()
		const p = n => (n < 10 ? '0' + n : '' + n)
		return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
	}

	function openCreate() {
		if (table.value === 'users') {
			openUserForm(null)
		} else if (table.value === 'books') {
			openBookForm(null)
		} else {
			openCheckinForm(null)
		}
	}

	/* ================= 用户 ================= */

	function openUserForm(u) {
		userForm.value = u
			? {
				id: u.id,
				username: u.username,
				nickname: u.nickname,
				phone: u.phone,
				gender: u.gender == null ? 0 : u.gender,
				role: u.role || 'MEMBER',
				position: u.position,
				circleId: u.circleId || null,
				groupId: u.groupId || null,
				status: u.status == null ? 1 : u.status
			}
			: {
				username: '',
				password: '',
				nickname: '',
				phone: '',
				gender: 0,
				role: 'MEMBER',
				position: '',
				circleId: null,
				groupId: null,
				status: 1
			}
		formVisible.value = true
	}

	function userCircleIndex() {
		const i = userCircleOptions.value.findIndex(o => o.id === userForm.value.circleId)
		return i < 0 ? 0 : i
	}

	function onUserCircleChange(e) {
		const opt = userCircleOptions.value[Number(e.detail.value)]
		userForm.value.circleId = opt ? opt.id : null
		userForm.value.groupId = null
	}

	function onRoleChange(e) {
		userForm.value.role = roleValues[Number(e.detail.value)]
	}

	async function saveUser() {
		if (!userForm.value.username) {
			uni.showToast({ title: t('admin.manage.errUsername'), icon: 'none' })
			return
		}
		const payload = {
			id: userForm.value.id,
			username: userForm.value.username,
			password: userForm.value.password || null,
			nickname: userForm.value.nickname,
			phone: userForm.value.phone,
			gender: Number(userForm.value.gender) || 0,
			role: userForm.value.role,
			position: userForm.value.position,
			circleId: userForm.value.circleId,
			groupId: userForm.value.groupId,
			status: Number(userForm.value.status)
		}
		try {
			if (payload.id) {
				await updateDataUser(payload)
			} else {
				await createDataUser(payload)
			}
			formVisible.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load(true)
		} catch (e) {}
	}

	function removeUser(u) {
		showModal({
			title: t('common.tips'),
			content: t('admin.manage.delUserConfirm', { name: u.nickname || u.username }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteDataUser(u.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load(true)
				} catch (e) {}
			}
		})
	}

	/* ================= 书籍 ================= */

	function openBookForm(b) {
		bookForm.value = b
			? {
				id: b.id,
				title: b.title,
				author: b.author,
				publisher: b.publisher,
				category: b.category,
				bookType: b.bookType == null ? 1 : b.bookType,
				cover: b.cover,
				description: b.description,
				recommendReason: b.recommendReason,
				isPublic: b.isPublic == null ? 1 : b.isPublic,
				status: b.status == null ? 1 : b.status,
				sort: b.sort == null ? 0 : b.sort
			}
			: {
				title: '',
				author: '',
				publisher: '',
				category: '',
				bookType: 1,
				cover: '',
				description: '',
				recommendReason: '',
				isPublic: 1,
				status: 1,
				sort: 0
			}
		formVisible.value = true
	}

	async function saveBook() {
		if (!bookForm.value.title) {
			uni.showToast({ title: t('admin.manage.errTitle'), icon: 'none' })
			return
		}
		const payload = {
			id: bookForm.value.id,
			title: bookForm.value.title,
			author: bookForm.value.author,
			publisher: bookForm.value.publisher,
			category: bookForm.value.category,
			bookType: Number(bookForm.value.bookType) || 1,
			cover: bookForm.value.cover,
			description: bookForm.value.description,
			recommendReason: bookForm.value.recommendReason,
			isPublic: Number(bookForm.value.isPublic),
			status: Number(bookForm.value.status),
			sort: Number(bookForm.value.sort) || 0
		}
		try {
			if (payload.id) {
				await updateDataBook(payload)
			} else {
				await createDataBook(payload)
			}
			formVisible.value = false
			bookOptions.value = []
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load(true)
		} catch (e) {}
	}

	function removeBook(b) {
		showModal({
			title: t('common.tips'),
			content: t('admin.manage.delBookConfirm', { name: b.title }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteDataBook(b.id)
					bookOptions.value = []
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load(true)
				} catch (e) {}
			}
		})
	}

	/* ================= 章节 ================= */

	async function openChapter(book) {
		chapterBook.value = { id: book.id, title: book.title }
		chapterForm.value = { bookId: book.id, chapterNo: '', title: '', content: '' }
		chapters.value = []
		chapterVisible.value = true
		await loadChapters()
	}

	async function loadChapters() {
		if (!chapterBook.value) return
		try {
			const data = await getChapterList(chapterBook.value.id)
			chapters.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			chapters.value = []
		}
	}

	function resetChapterForm() {
		chapterForm.value = { bookId: chapterBook.value.id, chapterNo: '', title: '', content: '' }
	}

	function editChapter(c) {
		chapterForm.value = {
			id: c.id,
			bookId: c.bookId,
			chapterNo: c.chapterNo,
			title: c.title,
			content: c.content
		}
	}

	async function saveChapter() {
		if (!chapterForm.value.title) {
			uni.showToast({ title: t('admin.manage.errChapterTitle'), icon: 'none' })
			return
		}
		const payload = {
			id: chapterForm.value.id,
			bookId: chapterForm.value.bookId,
			chapterNo: chapterForm.value.chapterNo === '' ? null : Number(chapterForm.value.chapterNo),
			title: chapterForm.value.title,
			content: chapterForm.value.content
		}
		try {
			if (payload.id) {
				await updateChapter(payload)
			} else {
				await createChapter(payload)
			}
			uni.showToast({ title: t('common.success'), icon: 'none' })
			resetChapterForm()
			await loadChapters()
			load(true)
		} catch (e) {}
	}

	function removeChapter(c) {
		showModal({
			title: t('common.tips'),
			content: t('admin.manage.delChapterConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteChapter(c.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await loadChapters()
					load(true)
				} catch (e) {}
			}
		})
	}

	function closeChapter() {
		chapterVisible.value = false
		chapterBook.value = null
		chapters.value = []
	}

	/* ================= 打卡记录 ================= */

	function bookIndex() {
		const i = bookOptions.value.findIndex(b => b.id === checkinForm.value.bookId)
		return i < 0 ? 0 : i
	}

	function formChapterIndex() {
		const i = formChapterOptions.value.findIndex(c => c.id === checkinForm.value.chapterId)
		return i < 0 ? 0 : i
	}

	async function openCheckinForm(r) {
		await loadBooks()
		checkinForm.value = r
			? {
				id: r.id,
				userId: r.userId,
				bookId: r.bookId,
				chapterId: r.chapterId || null,
				chapterTitle: r.chapterTitle || '',
				checkinType: r.checkinType || 3,
				content: r.content,
				checkinDate: r.checkinDate,
				status: r.status == null ? 1 : r.status,
				isPublic: r.isPublic == null ? 1 : r.isPublic,
				auditRemark: r.auditRemark || ''
			}
			: {
				userId: '',
				bookId: null,
				chapterId: null,
				chapterTitle: '',
				checkinType: 3,
				content: '',
				checkinDate: today(),
				status: 1,
				isPublic: 1,
				auditRemark: ''
			}
		formChapters.value = []
		if (checkinForm.value.bookId) {
			await loadFormChapters(checkinForm.value.bookId)
		}
		formVisible.value = true
	}

	async function loadFormChapters(bookId) {
		try {
			const data = await getChapterList(bookId)
			formChapters.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			formChapters.value = []
		}
	}

	async function onFormBookChange(e) {
		const opt = bookOptions.value[Number(e.detail.value)]
		if (!opt) return
		checkinForm.value.bookId = opt.id
		checkinForm.value.chapterId = null
		checkinForm.value.chapterTitle = ''
		await loadFormChapters(opt.id)
	}

	function onFormChapterChange(e) {
		const opt = formChapterOptions.value[Number(e.detail.value)]
		checkinForm.value.chapterId = opt ? opt.id : null
		checkinForm.value.chapterTitle = opt && opt.id ? opt.chapterTitle : ''
	}

	function onFormTypeChange(e) {
		checkinForm.value.checkinType = checkinTypeValues[Number(e.detail.value)]
	}

	async function saveCheckin() {
		const form = checkinForm.value
		if (!form.userId) {
			uni.showToast({ title: t('admin.manage.errMember'), icon: 'none' })
			return
		}
		if (!form.bookId) {
			uni.showToast({ title: t('admin.manage.errBook'), icon: 'none' })
			return
		}
		if (!form.checkinDate) {
			uni.showToast({ title: t('admin.manage.errDate'), icon: 'none' })
			return
		}
		const payload = {
			id: form.id,
			userId: Number(form.userId),
			bookId: form.bookId,
			chapterId: form.chapterId,
			chapterTitle: form.chapterTitle,
			checkinType: form.checkinType,
			content: form.content,
			checkinDate: form.checkinDate,
			status: form.status,
			isPublic: form.isPublic,
			auditRemark: form.auditRemark
		}
		try {
			if (payload.id) {
				await updateDataCheckin(payload)
			} else {
				await createDataCheckin({
					userId: payload.userId,
					bookId: payload.bookId,
					chapterId: payload.chapterId,
					checkinType: payload.checkinType,
					content: payload.content,
					checkinDate: payload.checkinDate
				})
			}
			formVisible.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			load(true)
		} catch (e) {}
	}

	function removeCheckin(r) {
		showModal({
			title: t('common.tips'),
			content: t('admin.manage.delCheckinConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteDataCheckin(r.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					load(true)
				} catch (e) {}
			}
		})
	}

	onLoad((query) => {
		const key = query && query.table
		if (key === 'books' || key === 'checkins') {
			table.value = key
		}
	})

	onShow(async () => {
		await loadCircles()
		load(true)
	})

	onPullDownRefresh(async () => {
		await loadCircles()
		await load(true)
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.manage-page {
		min-height: 100vh;
	}

	.content {
		padding: 24rpx;
	}

	.table-tabs {
		margin-bottom: 20rpx;
	}

	.seg-ico {
		font-size: 26rpx;
		margin-right: 8rpx;
	}

	.filter-card {
		padding: 24rpx;
		border-radius: var(--radius-lg);
		margin-bottom: 20rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
		padding: 0 8rpx 0 20rpx;
		height: 84rpx;
		box-sizing: border-box;
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

	.filter-row {
		display: flex;
		gap: 16rpx;
		margin-top: 16rpx;
	}

	.filter-col {
		flex: 1;
		min-width: 0;
	}

	.create-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 16rpx;
	}

	.create-icon {
		font-size: 30rpx;
		margin-right: 8rpx;
	}

	.total-line {
		display: flex;
		justify-content: flex-end;
		margin-bottom: 14rpx;
	}

	.total-text {
		font-size: 23rpx;
		color: var(--text-tertiary);
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

	.avatar {
		width: 84rpx;
		height: 84rpx;
		border-radius: 26rpx;
		background: var(--surface-2);
		flex-shrink: 0;
		margin-right: 18rpx;
	}

	.cover {
		width: 84rpx;
		height: 112rpx;
		border-radius: var(--radius-sm);
		background: var(--surface-2);
		flex-shrink: 0;
		margin-right: 18rpx;
	}

	.cover-ph {
		width: 84rpx;
		height: 112rpx;
		border-radius: var(--radius-sm);
		flex-shrink: 0;
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

	.card-content {
		font-size: 23rpx;
		line-height: 1.6;
		color: var(--text-secondary);
		margin-top: 10rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
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

	.op-tip {
		font-size: 22rpx;
		color: var(--text-tertiary);
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

	.chip-red {
		background: rgba(179, 69, 59, 0.11);
		color: var(--danger);
	}

	.chip-amber {
		background: rgba(180, 101, 46, 0.12);
		color: var(--warning);
	}

	.chip-blue {
		background: rgba(46, 74, 107, 0.1);
		color: var(--primary);
	}

	.chip-violet {
		background: rgba(107, 91, 123, 0.12);
		color: #6B5B7B;
	}

	.theme-dark .chip-violet {
		background: rgba(180, 165, 200, 0.14);
		color: #C2B4D6;
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

	.load-more {
		text-align: center;
		padding: 24rpx 0 40rpx;
	}

	.load-more-text {
		font-size: 25rpx;
		color: var(--primary);
	}

	.form-modal {
		padding: 32rpx 28rpx 28rpx;
		max-height: 82vh;
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
		flex: 1;
		min-width: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.modal-scroll {
		max-height: 64vh;
	}

	.f-area {
		min-height: 150rpx;
	}

	.modal-ops {
		display: flex;
		margin-top: 32rpx;
		gap: 20rpx;
	}

	.modal-btn {
		flex: 1;
	}

	.chapter-entry {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
		padding: 24rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
	}

	.chapter-entry-ico {
		font-size: 28rpx;
		color: var(--primary);
		margin-right: 12rpx;
	}

	.chapter-entry-text {
		flex: 1;
		font-size: 26rpx;
		color: var(--text-primary);
	}

	.chapter-entry-arrow {
		font-size: 24rpx;
		color: var(--text-tertiary);
	}

	.chapter-mask {
		z-index: 1100;
	}

	.chapter-modal {
		padding: 32rpx 28rpx 28rpx;
		max-height: 84vh;
	}

	.chapter-form {
		padding-bottom: 8rpx;
		border-bottom: 1rpx solid var(--border);
	}

	.chapter-no-col {
		max-width: 180rpx;
	}

	.chapter-area {
		min-height: 120rpx;
	}

	.chapter-form-ops {
		display: flex;
		justify-content: flex-end;
		gap: 16rpx;
		margin-top: 20rpx;
	}

	.mini-btn {
		padding: 16rpx 36rpx;
		font-size: 26rpx;
	}

	.chapter-list {
		max-height: 40vh;
		margin-top: 8rpx;
	}

	.chapter-row {
		display: flex;
		align-items: center;
		padding: 18rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.chapter-row:last-child {
		border-bottom: none;
	}

	.chapter-no {
		width: 64rpx;
		flex-shrink: 0;
		font-size: 24rpx;
		font-weight: 700;
		color: var(--primary);
	}

	.chapter-name {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.chapter-ops {
		display: flex;
		gap: 10rpx;
		flex-shrink: 0;
	}
</style>
