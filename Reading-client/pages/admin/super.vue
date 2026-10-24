<template>
	<view class="page super-page" :class="themeState.isDark ? 'theme-dark' : 'theme-light'">
		<!-- 控制台头部 -->
		<view class="hero glass-card">
			<view class="hero-stripe"></view>
			<view class="hero-head">
				<view class="hero-id">
					<image class="hero-avatar" :src="me.avatar || '/static/logo.png'" mode="aspectFill" />
					<view class="hero-id-main">
						<view class="hero-name-row">
							<text class="hero-name">{{ me.nickname || me.username || '-' }}</text>
							<view class="role-badge">
								<r-icon name="shield-check-line" class="role-icon" />
								<text class="role-text">{{ $t('admin.super.role') }}</text>
							</view>
						</view>
						<text class="hero-sub">{{ $t('admin.super.subtitle') }}</text>
					</view>
				</view>
				<view class="hero-app" @click="goApp">
					<r-icon name="home-5-line" class="hero-app-icon" />
					<text class="hero-app-text">{{ $t('admin.super.backApp') }}</text>
				</view>
			</view>
			<view class="hero-foot">
				<text class="hero-date">{{ $t('admin.super.today') }} · {{ todayLabel }}</text>
				<text class="hero-refresh" @click="refreshAll">{{ $t('admin.super.refresh') }}</text>
			</view>
		</view>

		<!-- 系统数据 -->
		<view class="stat-grid">
			<view class="stat-card" v-for="card in statCards" :key="card.key">
				<view class="stat-card-top">
					<text class="stat-card-label">{{ $t(card.label) }}</text>
					<view class="icon-chip icon-chip--sm" :class="card.color">
						<r-icon :name="card.icon" />
					</view>
				</view>
				<text class="stat-card-num">{{ loading.stats ? '--' : card.value }}</text>
			</view>
		</view>

		<!-- 模块切换 -->
		<scroll-view scroll-x class="tab-scroll" :show-scrollbar="false">
			<view class="tab-row">
				<view
					v-for="tab in tabs"
					:key="tab.key"
					class="tab-item"
					:class="{ active: activeTab === tab.key }"
					@click="switchTab(tab.key)"
				>
					<r-icon :name="tab.icon" class="tab-icon" />
					<text class="tab-text">{{ $t(tab.label) }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 总览 -->
		<view v-if="activeTab === 'overview'">
			<view class="section glass-card">
				<view class="section-head">
					<view class="section-bar"></view>
					<text class="section-title">{{ $t('admin.super.quickTitle') }}</text>
				</view>
				<view class="quick-grid">
					<view class="quick-tile" v-for="q in quickActions" :key="q.key" @click="onQuickAction(q)">
						<view class="icon-chip icon-chip--sm" :class="q.color"><r-icon :name="q.icon" /></view>
						<view class="quick-main">
							<text class="quick-title">{{ $t(q.label) }}</text>
							<text class="quick-desc">{{ $t(q.desc) }}</text>
						</view>
						<r-icon name="arrow-right-s-line" class="quick-arrow" />
					</view>
				</view>
			</view>

			<view class="section glass-card">
				<view class="section-head">
					<view class="section-bar"></view>
					<text class="section-title">{{ $t('admin.super.recentUsers') }}</text>
					<text class="section-link" @click="switchTab('users')">{{ $t('admin.super.allUsers') }}</text>
				</view>
				<view class="user-row compact" v-for="u in recentUsers" :key="u.id">
					<image class="user-avatar" :src="u.avatar || '/static/logo.png'" mode="aspectFill" />
					<view class="user-main">
						<view class="user-name-row">
							<text class="user-name">{{ u.nickname || u.username || '-' }}</text>
							<text class="chip" :class="roleChipClass(u.role)">{{ roleLabel(u.role) }}</text>
						</view>
						<text class="user-sub">{{ circleName(u.circleId) }} · {{ $t('admin.super.userCheckin', { n: u.checkinDays || 0 }) }}</text>
					</view>
					<text class="chip" :class="u.status === 0 ? 'chip-red' : 'chip-green'">{{ statusLabel(u.status) }}</text>
				</view>
				<view class="row-empty" v-if="!recentUsers.length">{{ $t('common.empty') }}</view>
			</view>

			<view class="section glass-card">
				<view class="section-head">
					<view class="section-bar"></view>
					<text class="section-title">{{ $t('admin.super.systemInfo') }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">{{ $t('admin.super.service') }}</text>
					<text class="info-value">{{ baseUrl }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">{{ $t('admin.super.currentRole') }}</text>
					<text class="info-value">{{ roleLabel(me.role) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">{{ $t('admin.super.accountId') }}</text>
					<text class="info-value">#{{ me.id || '-' }}</text>
				</view>
			</view>
		</view>

		<!-- 用户管理 -->
		<view v-else-if="activeTab === 'users'" class="section glass-card">
			<view class="section-head">
				<view class="section-bar"></view>
				<text class="section-title">{{ $t('admin.super.userTitle') }}</text>
				<text class="section-count">{{ $t('admin.super.userTotal', { n: userTotal }) }}</text>
			</view>
			<view class="search-box">
				<r-icon name="search-line" class="search-icon" />
				<input
					class="search-input"
					v-model="userKeyword"
					:placeholder="$t('admin.super.searchUserPh')"
					placeholder-class="input-ph"
					confirm-type="search"
					@confirm="searchUsers"
				/>
				<view class="search-btn" @click="searchUsers">{{ $t('common.search') }}</view>
			</view>

			<view class="user-row" v-for="u in users" :key="u.id">
				<image class="user-avatar" :src="u.avatar || '/static/logo.png'" mode="aspectFill" />
				<view class="user-main">
					<view class="user-name-row">
						<text class="user-name">{{ u.nickname || u.username || '-' }}</text>
						<text class="chip" :class="roleChipClass(u.role)">{{ roleLabel(u.role) }}</text>
						<text class="chip" :class="u.status === 0 ? 'chip-red' : 'chip-green'">{{ statusLabel(u.status) }}</text>
					</view>
					<text class="user-sub">{{ u.position || '-' }} · {{ u.phone || $t('common.none') }}</text>
					<text class="user-meta">ID {{ u.id }} · {{ $t('admin.super.userCircle') }} {{ circleName(u.circleId) }} · {{ $t('admin.super.userCheckin', { n: u.checkinDays || 0 }) }}</text>
				</view>
				<view class="user-ops">
					<view class="row-action" @click="openPositionForm(u)">
						<r-icon name="team-line" class="row-action-icon" />
						<text>{{ $t('admin.super.assignPosition') }}</text>
					</view>
					<view v-if="u.id !== me.id" class="row-action" :class="{ danger: u.status === 1 }" @click="toggleUser(u)">
						{{ u.status === 1 ? $t('admin.super.disable') : $t('admin.super.enable') }}
					</view>
					<text v-else class="row-self">{{ $t('admin.super.selfTip') }}</text>
				</view>
			</view>
			<view class="row-empty" v-if="!users.length && !loading.users">{{ $t('common.empty') }}</view>
			<view class="load-more" v-if="users.length && !userNoMore" @click="loadUsers(false)">
				<text class="load-more-text">{{ loading.users ? $t('common.loading') : $t('admin.super.loadMore') }}</text>
			</view>
		</view>

		<!-- 读书圈 -->
		<view v-else-if="activeTab === 'circles'">
			<view class="btn-primary create-btn" @click="openCircleForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.super.createCircle') }}</text>
			</view>
			<view class="item-card glass-card" v-for="c in circles" :key="c.id">
				<view class="item-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="book-2-line" /></view>
					<view class="item-main">
						<text class="item-title">{{ c.name }}</text>
						<text class="item-sub">{{ c.description || $t('common.none') }}</text>
					</view>
					<view class="row-ops">
						<view class="row-op" @click="openCircleForm(c)"><r-icon name="edit-2-line" class="row-op-icon" /></view>
						<view class="row-op danger" @click="removeCircle(c)"><r-icon name="delete-bin-line" class="row-op-icon danger" /></view>
					</view>
				</view>
				<view class="item-meta">
					<text class="meta-pill">{{ $t('admin.super.memberCount', { n: c.memberCount || 0 }) }}</text>
					<text class="meta-pill" @click="copyText(c.inviteCode)">{{ $t('admin.super.inviteCode') }}：{{ c.inviteCode || '-' }}</text>
					<text class="meta-pill action" @click="openMembers(c)">{{ $t('admin.super.members') }}</text>
					<text class="meta-pill action" @click="openGroups(c)">{{ $t('admin.super.groupManage') }}</text>
				</view>
			</view>
			<view class="row-empty" v-if="!circles.length">{{ $t('common.empty') }}</view>
		</view>

		<!-- 作业 -->
		<view v-else-if="activeTab === 'jobs'">
			<view class="btn-primary create-btn" @click="openJobForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.super.createJob') }}</text>
			</view>
			<view class="item-card glass-card" v-for="j in jobs" :key="j.id">
				<view class="item-head">
					<view class="icon-chip icon-chip--sm icon-amber"><r-icon name="file-edit-line" /></view>
					<view class="item-main">
						<text class="item-title">{{ j.title }}</text>
						<text class="item-sub">{{ circleName(j.circleId) }} · {{ bookName(j.bookId) }}</text>
					</view>
					<text class="chip" :class="'job-s' + j.status">{{ jobStatusLabel(j.status) }}</text>
				</view>
				<view class="item-lines">
					<text class="item-line">{{ $t('admin.super.startDate') }}：{{ j.startDate || '-' }} · {{ $t('admin.super.deadline') }}：{{ j.deadline || '-' }}</text>
					<text class="item-line" v-if="j.chapterStart || j.chapterEnd">{{ $t('admin.job.chapterRange', { s: j.chapterStart || '-', e: j.chapterEnd || '-' }) }}</text>
					<text class="item-line" v-if="j.content">{{ j.content }}</text>
				</view>
				<view class="row-ops bottom">
					<view class="row-op" @click="openJobForm(j)"><r-icon name="ball-pen-line" class="row-op-icon" /></view>
					<view class="row-op danger" @click="removeJob(j)"><r-icon name="delete-bin-line" class="row-op-icon danger" /></view>
				</view>
			</view>
			<view class="row-empty" v-if="!jobs.length">{{ $t('common.empty') }}</view>
		</view>

		<!-- 打卡规则 -->
		<view v-else-if="activeTab === 'rules'">
			<view class="btn-primary create-btn" @click="openRuleForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.super.createRule') }}</text>
			</view>
			<view class="item-card glass-card" v-for="r in rules" :key="r.id">
				<view class="item-head">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="ruler-line" /></view>
					<view class="item-main">
						<text class="item-title">{{ r.position }}</text>
						<text class="item-sub">{{ circleName(r.circleId) }}</text>
					</view>
					<view class="row-ops">
						<view class="row-op" @click="openRuleForm(r)"><r-icon name="ball-pen-line" class="row-op-icon" /></view>
						<view class="row-op danger" @click="removeRule(r)"><r-icon name="delete-bin-line" class="row-op-icon danger" /></view>
					</view>
				</view>
				<view class="tri-stat">
					<view class="tri-cell">
						<text class="tri-num">{{ r.requiredDaysPerWeek || 0 }}</text>
						<text class="tri-label">{{ $t('admin.super.weekDays') }}</text>
					</view>
					<view class="tri-divider"></view>
					<view class="tri-cell">
						<text class="tri-num">{{ r.requiredDaysPerMonth || 0 }}</text>
						<text class="tri-label">{{ $t('admin.super.monthDays') }}</text>
					</view>
					<view class="tri-divider"></view>
					<view class="tri-cell">
						<text class="tri-num">{{ r.requiredDaysPerYear || 0 }}</text>
						<text class="tri-label">{{ $t('admin.super.yearDays') }}</text>
					</view>
				</view>
			</view>
			<view class="row-empty" v-if="!rules.length">{{ $t('common.empty') }}</view>
		</view>

		<!-- 打卡模板 -->
		<view v-else-if="activeTab === 'templates'">
			<view class="btn-primary create-btn" @click="openTemplateForm()">
				<r-icon name="add-line" class="create-icon" />
				<text>{{ $t('admin.super.createTemplate') }}</text>
			</view>
			<view class="item-card glass-card" v-for="tp in templates" :key="tp.id">
				<view class="item-head">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="puzzle-line" /></view>
					<view class="item-main">
						<text class="item-title">{{ tp.name }}</text>
						<text class="item-sub">
							{{ circleName(tp.circleId) }}
							<text class="tpl-status" :class="{ off: tp.status !== 1 }">{{ tp.status === 1 ? $t('admin.super.enabled') : $t('admin.super.disabled') }}</text>
						</text>
					</view>
					<view class="row-ops">
						<view class="row-op" @click="openTemplateForm(tp)"><r-icon name="ball-pen-line" class="row-op-icon" /></view>
						<view class="row-op danger" @click="removeTemplate(tp)"><r-icon name="delete-bin-line" class="row-op-icon danger" /></view>
					</view>
				</view>
				<view class="tag-row">
					<text class="tag" v-for="t in parseTypes(tp.checkinTypes)" :key="t">{{ typeLabel(t) }}</text>
				</view>
				<view class="flag-row">
					<text class="flag" v-for="t in parseTypes(tp.checkinTypes)" :key="t">{{ typeLabel(t) }}：{{ reqSummary(tp, t) }}</text>
					<text class="flag" v-if="!parseTypes(tp.checkinTypes).length">{{ $t('admin.super.noRequirement') }}</text>
				</view>
			</view>
			<view class="row-empty" v-if="!templates.length">{{ $t('common.empty') }}</view>
		</view>

		<!-- 数据管理 -->
		<view v-else-if="activeTab === 'data'">
			<view class="section glass-card">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-cyan"><r-icon name="file-list-3-line" /></view>
					<text class="section-title">{{ $t('admin.super.dataManageTitle') }}</text>
				</view>
				<text class="section-desc">{{ $t('admin.super.dataManageDesc') }}</text>
				<view class="quick-grid">
					<view class="quick-tile" v-for="m in dataEntries" :key="m.key" @click="openDataManage(m.key)">
						<view class="icon-chip icon-chip--sm" :class="m.color"><r-icon :name="m.icon" /></view>
						<view class="quick-main">
							<text class="quick-title">{{ $t(m.label) }}</text>
							<text class="quick-desc">{{ $t(m.desc) }}</text>
						</view>
						<r-icon name="arrow-right-s-line" class="quick-arrow" />
					</view>
				</view>
			</view>

			<view class="section glass-card">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-green"><r-icon name="upload-2-line" /></view>
					<text class="section-title">{{ $t('admin.super.exportTitle') }}</text>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.super.exportCircle') }}</text>
					<r-picker
						:range="exportCircleOptions"
						range-key="name"
						:value="exportCircleIndex"
						:title="$t('admin.super.exportCircle')"
						:placeholder="$t('admin.super.allCircles')"
						@change="e => exportCircleIndex = Number(e.detail.value)"
					/>
				</view>
				<view class="field field__row">
					<view class="field__col">
						<text class="field__label">{{ $t('admin.super.exportStart') }}</text>
						<r-picker
							mode="date"
							:value="exportStart"
							:title="$t('admin.super.exportStart')"
							:placeholder="$t('admin.super.exportStart')"
							@change="e => exportStart = e.detail.value"
						/>
					</view>
					<view class="field__col">
						<text class="field__label">{{ $t('admin.super.exportEnd') }}</text>
						<r-picker
							mode="date"
							:value="exportEnd"
							:title="$t('admin.super.exportEnd')"
							:placeholder="$t('admin.super.exportEnd')"
							@change="e => exportEnd = e.detail.value"
						/>
					</view>
				</view>
				<view class="btn-primary sec-btn" @click="genExportLink">{{ $t('admin.super.genLink') }}</view>
				<view class="link-box" v-if="exportLink">
					<text class="link-text" user-select>{{ exportLink }}</text>
					<view class="btn-plain sec-btn" @click="copyText(exportLink)">{{ $t('admin.super.copyLink') }}</view>
					<text class="link-tip">{{ $t('admin.super.linkTip') }}</text>
				</view>
			</view>

			<view class="section glass-card">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-blue"><r-icon name="download-2-line" /></view>
					<text class="section-title">{{ $t('admin.super.importTitle') }}</text>
				</view>
				<text class="section-desc">{{ $t('admin.super.importDesc') }}</text>
				<view class="btn-plain sec-btn" @click="chooseImportFile">{{ $t('admin.super.chooseFile') }}</view>
			</view>

			<view class="section glass-card">
				<view class="section-head">
					<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="ball-pen-line" /></view>
					<text class="section-title">{{ $t('admin.super.proxyTitle') }}</text>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.super.memberId') }}</text>
					<input class="input-item" type="number" v-model="proxy.userId" :placeholder="$t('admin.super.memberIdPh')" placeholder-class="input-ph" />
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.super.selectBook') }}</text>
					<r-picker
						:range="bookList"
						range-key="title"
						:value="proxyBookIndex"
						:title="$t('admin.super.selectBook')"
						:placeholder="$t('admin.super.selectBookPh')"
						@change="onProxyBookChange"
					/>
				</view>
				<view class="field field__row">
					<view class="field__col">
						<text class="field__label">{{ $t('admin.super.checkinType') }}</text>
						<r-picker
							:range="checkinTypeLabels"
							:value="proxyTypeIndex"
							:title="$t('admin.super.checkinType')"
							@change="onProxyTypeChange"
						/>
					</view>
					<view class="field__col">
						<text class="field__label">{{ $t('admin.super.checkinDate') }}</text>
						<r-picker
							mode="date"
							:value="proxy.checkinDate"
							:title="$t('admin.super.checkinDate')"
							:placeholder="$t('admin.super.checkinDate')"
							@change="e => proxy.checkinDate = e.detail.value"
						/>
					</view>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.super.checkinRemark') }}</text>
					<input class="input-item" v-model="proxy.remark" :placeholder="$t('admin.super.checkinRemarkPh')" placeholder-class="input-ph" />
				</view>
				<view class="btn-primary sec-btn" @click="submitProxy">{{ $t('admin.super.submitCheckin') }}</view>
			</view>
		</view>

		<!-- 表单弹层 -->
		<view class="ui-modal-mask form-mask" v-if="formVisible" @click="formVisible = false">
			<view class="ui-modal-card form-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ formTitle }}</text>
					<view class="ui-sheet-close" @click="formVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="modal-scroll">
					<!-- 读书圈 -->
					<template v-if="formType === 'circle'">
						<view class="field">
							<text class="field__label">{{ $t('admin.super.circleName') }}</text>
							<input class="input-item" v-model="form.name" :placeholder="$t('admin.super.circleNamePh')" placeholder-class="input-ph" />
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.circleDesc') }}</text>
							<textarea class="input-item f-area" v-model="form.description" :placeholder="$t('admin.super.circleDescPh')" placeholder-class="input-ph" />
						</view>
					</template>

					<!-- 作业 -->
					<template v-else-if="formType === 'job'">
						<view class="field">
							<text class="field__label">{{ $t('admin.super.jobTitle') }}</text>
							<input class="input-item" v-model="form.title" :placeholder="$t('admin.super.jobTitlePh')" placeholder-class="input-ph" />
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.selectCircle') }}</text>
							<r-picker
								:range="circles"
								range-key="name"
								:value="formCircleIndex"
								:title="$t('admin.super.selectCircle')"
								:placeholder="$t('admin.super.selectCirclePh')"
								@change="onFormCircleChange"
							/>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.selectBook') }}</text>
							<r-picker
								:range="bookList"
								range-key="title"
								:value="formBookIndex"
								:title="$t('admin.super.selectBook')"
								:placeholder="$t('admin.super.selectBookPh')"
								@change="onFormBookChange"
							/>
						</view>
						<view class="field field__row">
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.chapterStart') }}</text>
								<input class="input-item" type="number" v-model="form.chapterStart" placeholder-class="input-ph" />
							</view>
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.chapterEnd') }}</text>
								<input class="input-item" type="number" v-model="form.chapterEnd" placeholder-class="input-ph" />
							</view>
						</view>
						<view class="field field__row">
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.startDate') }}</text>
								<r-picker
									mode="date"
									:value="form.startDate"
									:title="$t('admin.super.startDate')"
									:placeholder="$t('admin.super.startDate')"
									@change="e => form.startDate = e.detail.value"
								/>
							</view>
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.deadline') }}</text>
								<r-picker
									mode="date"
									:value="form.deadline"
									:title="$t('admin.super.deadline')"
									:placeholder="$t('admin.super.deadline')"
									@change="e => form.deadline = e.detail.value"
								/>
							</view>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.jobStatus') }}</text>
							<r-picker
								:range="jobStatusLabels"
								:value="form.status"
								:title="$t('admin.super.jobStatus')"
								@change="e => form.status = Number(e.detail.value)"
							/>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.jobContent') }}</text>
							<textarea class="input-item f-area" v-model="form.content" :placeholder="$t('admin.super.jobContentPh')" placeholder-class="input-ph" />
						</view>
					</template>

					<!-- 打卡规则 -->
					<template v-else-if="formType === 'rule'">
						<view class="field">
							<text class="field__label">{{ $t('admin.super.selectCircle') }}</text>
							<r-picker
								:range="circles"
								range-key="name"
								:value="formCircleIndex"
								:title="$t('admin.super.selectCircle')"
								:placeholder="$t('admin.super.selectCirclePh')"
								@change="onFormCircleChange"
							/>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.position') }}</text>
							<r-picker
								:range="formPositionOptions"
								:value="formPositionIndex"
								:title="$t('admin.super.position')"
								:placeholder="$t('admin.super.positionSelectPh')"
								@change="e => form.position = formPositionOptions[Number(e.detail.value)] || ''"
							/>
						</view>
						<view class="field field__row">
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.weekDays') }}</text>
								<input class="input-item" type="number" v-model="form.requiredDaysPerWeek" placeholder-class="input-ph" />
							</view>
							<view class="field__col">
								<text class="field__label">{{ $t('admin.super.monthDays') }}</text>
								<input class="input-item" type="number" v-model="form.requiredDaysPerMonth" placeholder-class="input-ph" />
							</view>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.yearDays') }}</text>
							<input class="input-item" type="number" v-model="form.requiredDaysPerYear" placeholder-class="input-ph" />
						</view>
						<view class="switch-row">
							<text class="switch-label">{{ $t('admin.super.statusEnabled') }}</text>
							<switch color="#2E4A6B" :checked="form.status === 1" @change="e => form.status = e.detail.value ? 1 : 0" />
						</view>
					</template>

					<!-- 打卡模板 -->
					<template v-else-if="formType === 'template'">
						<view class="field">
							<text class="field__label">{{ $t('admin.super.selectCircle') }}</text>
							<r-picker
								:range="circles"
								range-key="name"
								:value="formCircleIndex"
								:title="$t('admin.super.selectCircle')"
								:placeholder="$t('admin.super.selectCirclePh')"
								@change="onFormCircleChange"
							/>
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.templateName') }}</text>
							<input class="input-item" v-model="form.name" :placeholder="$t('admin.super.templateNamePh')" placeholder-class="input-ph" />
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.allowTypes') }}</text>
							<view class="type-row">
								<view
									v-for="tp in allTypes"
									:key="tp"
									class="type-tag"
									:class="{ active: formTypes.indexOf(tp) >= 0 }"
									@click="toggleFormType(tp)"
								>
									<text class="type-tag-text">{{ typeLabel(tp) }}</text>
								</view>
							</view>
						</view>
						<view class="field" v-for="tp in formTypes" :key="tp">
							<text class="field__label">{{ typeLabel(tp) }} · {{ $t('admin.super.reqItems') }}</text>
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
							<text class="field__label">{{ $t('admin.super.minWords') }}</text>
							<input class="input-item" type="number" v-model="form.minWordCount" placeholder-class="input-ph" />
						</view>
						<view class="switch-row">
							<text class="switch-label">{{ $t('admin.super.statusEnabled') }}</text>
							<switch color="#2E4A6B" :checked="form.status === 1" @change="e => form.status = e.detail.value ? 1 : 0" />
						</view>
					</template>

					<!-- 读书圈分组 -->
					<template v-else-if="formType === 'group'">
						<view class="field">
							<text class="field__label">{{ $t('admin.super.groupName') }}</text>
							<input class="input-item" v-model="form.name" :placeholder="$t('admin.super.groupNamePh')" placeholder-class="input-ph" />
						</view>
						<view class="field">
							<text class="field__label">{{ $t('admin.super.groupDesc') }}</text>
							<textarea class="input-item f-area" v-model="form.description" :placeholder="$t('admin.super.groupDescPh')" placeholder-class="input-ph" />
						</view>
					</template>
				</scroll-view>
				<view class="modal-ops">
					<view class="btn-plain modal-btn" @click="formVisible = false">{{ $t('common.cancel') }}</view>
					<view class="btn-primary modal-btn" @click="saveForm">{{ $t('common.save') }}</view>
				</view>
			</view>
		</view>

		<!-- 分配职位弹层 -->
		<view class="ui-modal-mask" v-if="positionVisible" @click="positionVisible = false">
			<view class="ui-modal-card position-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.super.assignPosition') }}</text>
					<view class="ui-sheet-close" @click="positionVisible = false"><r-icon name="close-line" /></view>
				</view>
				<view class="position-user">
					<image class="member-avatar" :src="positionUser.avatar || '/static/logo.png'" mode="aspectFill" />
					<view class="member-main">
						<text class="member-name">{{ positionUser.nickname || positionUser.username || '-' }}</text>
						<text class="member-sub">{{ $t('admin.super.currentPosition') }}：{{ positionUser.position || $t('common.none') }}</text>
					</view>
				</view>
				<view class="position-circles">
					<text class="field__label">{{ $t('admin.super.joinedCircles') }}</text>
					<view class="tag-row" v-if="positionCircles.length">
						<text class="circle-tag" v-for="c in positionCircles" :key="c.id">{{ c.name }}</text>
					</view>
					<text v-else class="position-empty">{{ $t('common.none') }}</text>
				</view>
				<view class="field">
					<text class="field__label">{{ $t('admin.super.position') }}</text>
					<input class="input-item" v-model="positionDraft" :placeholder="$t('admin.super.positionPh')" placeholder-class="input-ph" />
				</view>
				<view class="field" v-if="positionOptions.length">
					<text class="field__label">{{ $t('admin.super.positionSuggest') }}</text>
					<view class="tag-row">
						<text
							v-for="p in positionOptions"
							:key="p"
							class="pos-tag"
							:class="{ active: positionDraft === p }"
							@click="positionDraft = p"
						>{{ p }}</text>
					</view>
				</view>
				<view class="modal-ops">
					<view class="btn-plain modal-btn" @click="positionVisible = false">{{ $t('common.cancel') }}</view>
					<view class="btn-primary modal-btn" @click="savePosition">{{ $t('common.save') }}</view>
				</view>
			</view>
		</view>

		<!-- 成员弹层 -->
		<view class="ui-modal-mask" v-if="membersVisible" @click="membersVisible = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ memberCircle.name }} · {{ $t('admin.super.members') }}</text>
					<view class="ui-sheet-close" @click="membersVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="member-row" v-for="m in members" :key="m.id">
						<image class="member-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="member-main">
							<text class="member-name">{{ m.nickname || m.username || '-' }}</text>
							<text class="member-sub">{{ m.position || '-' }} · {{ m.phone || '-' }}</text>
						</view>
						<view class="row-action danger" @click="removeMember(m)">{{ $t('admin.super.remove') }}</view>
					</view>
					<view class="row-empty" v-if="!members.length && !memberLoading">{{ $t('common.empty') }}</view>
				</scroll-view>
				<text class="member-tip">{{ $t('admin.super.inviteTip') }}</text>
				<view class="btn-primary member-btn" @click="goCircleAdmin">{{ $t('admin.super.inviteMember') }}</view>
			</view>
		</view>

		<!-- 分组管理弹层 -->
		<view class="ui-modal-mask" v-if="groupsVisible" @click="groupsVisible = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ groupCircle.name }} · {{ $t('admin.super.groupManage') }}</text>
					<view class="ui-sheet-close" @click="groupsVisible = false"><r-icon name="close-line" /></view>
				</view>
				<view class="btn-primary group-create-btn" @click="openGroupForm()">
					<r-icon name="add-line" class="create-icon" />
					<text>{{ $t('admin.super.createGroup') }}</text>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="group-row" v-for="g in groups" :key="g.id">
						<view class="group-main" @click="openGroupMembers(g)">
							<view class="icon-chip icon-chip--sm icon-violet"><r-icon name="group-2-line" /></view>
							<view class="group-info">
								<text class="group-name">{{ g.name }}</text>
								<text class="group-sub">{{ g.description || $t('common.none') }} · {{ $t('admin.super.groupMemberCount', { n: g.memberCount || 0 }) }}</text>
							</view>
						</view>
						<view class="row-ops">
							<view class="row-op" @click.stop="openGroupForm(g)"><r-icon name="ball-pen-line" class="row-op-icon" /></view>
							<view class="row-op danger" @click.stop="removeGroup(g)"><r-icon name="delete-bin-line" class="row-op-icon danger" /></view>
						</view>
					</view>
					<view class="row-empty" v-if="groupLoading">{{ $t('common.loading') }}</view>
					<view class="row-empty" v-else-if="!groups.length">{{ $t('admin.super.groupEmpty') }}</view>
				</scroll-view>
				<text class="member-tip">{{ $t('admin.super.groupTip') }}</text>
			</view>
		</view>

		<!-- 分组成员弹层 -->
		<view class="ui-modal-mask" v-if="groupMembersVisible" @click="groupMembersVisible = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ activeGroup.name }} · {{ $t('admin.super.groupMembers') }}</text>
					<view class="ui-sheet-close" @click="groupMembersVisible = false"><r-icon name="close-line" /></view>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="member-row" v-for="m in groupMembers" :key="m.id">
						<image class="member-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="member-main">
							<text class="member-name">{{ m.nickname || m.username || '-' }}</text>
							<text class="member-sub">{{ m.position || '-' }} · {{ m.phone || '-' }}</text>
						</view>
						<view class="row-action danger" @click="removeFromGroup(m)">{{ $t('admin.super.removeFromGroup') }}</view>
					</view>
					<view class="row-empty" v-if="groupMembersLoading">{{ $t('common.loading') }}</view>
					<view class="row-empty" v-else-if="!groupMembers.length">{{ $t('common.empty') }}</view>
				</scroll-view>
				<view class="btn-primary member-btn" @click="openMemberPicker">{{ $t('admin.super.addMember') }}</view>
			</view>
		</view>

		<!-- 添加分组成员弹层 -->
		<view class="ui-modal-mask picker-mask" v-if="memberPickerVisible" @click="memberPickerVisible = false">
			<view class="ui-modal-card member-modal" @click.stop>
				<view class="modal-head">
					<text class="ui-modal-title">{{ $t('admin.super.addMemberTitle', { name: activeGroup.name }) }}</text>
					<view class="ui-sheet-close" @click="memberPickerVisible = false"><r-icon name="close-line" /></view>
				</view>
				<view class="search-box">
					<r-icon name="search-line" class="search-icon" />
					<input
						class="search-input"
						v-model="memberKeyword"
						:placeholder="$t('admin.super.memberSearchPh')"
						placeholder-class="input-ph"
						confirm-type="search"
						@confirm="loadCircleMembers"
					/>
					<view class="search-btn" @click="loadCircleMembers">{{ $t('common.search') }}</view>
				</view>
				<scroll-view scroll-y class="member-scroll">
					<view class="member-row" v-for="m in availableMembers" :key="m.id">
						<image class="member-avatar" :src="m.avatar || '/static/logo.png'" mode="aspectFill" />
						<view class="member-main">
							<text class="member-name">{{ m.nickname || m.username || '-' }}</text>
							<text class="member-sub">{{ m.position || '-' }} · {{ m.phone || '-' }}</text>
						</view>
						<view class="row-action" @click="addToGroup(m)">{{ $t('admin.super.moveToGroup') }}</view>
					</view>
					<view class="row-empty" v-if="circleMemberLoading">{{ $t('common.loading') }}</view>
					<view class="row-empty" v-else-if="!availableMembers.length">{{ $t('admin.super.noAvailableMember') }}</view>
				</scroll-view>
			</view>
		</view>

		<r-modal />
	</view>
</template>

<script setup>
	import { ref, reactive, computed } from 'vue'
	import { themeState } from '@/store/theme.js'
	import { userState, fetchUserInfo } from '@/store/user.js'
	import { t } from '@/utils/i18n.js'
	import { showModal } from '@/utils/modal.js'
	import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
	import { BASE_URL } from '@/utils/request.js'
	import {
		getUserPage, updateUserStatus, updateUserPosition, getUserCircles,
		getCircleList, createCircle, updateCircle, deleteCircle,
		getCircleMembers, removeCircleMember,
		getGroupList, createGroup, updateGroup, deleteGroup, getGroupMembers, assignMemberGroup,
		getJobPage, createJob, updateJob, deleteJob,
		getRuleList, createRule, updateRule, deleteRule,
		getTemplateList, createTemplate, updateTemplate, deleteTemplate,
		getPositionList,
		proxyCheckin
	} from '@/api/admin.js'
	import { getBookPage } from '@/api/book.js'
	import { uploadFile } from '@/api/file.js'

	const me = computed(() => userState.userInfo || {})
	const allowed = computed(() => me.value.role === 'SUPER_ADMIN')
	const baseUrl = BASE_URL

	const tabs = [
		{ key: 'overview', label: 'admin.super.overview', icon: 'bar-chart-2-line' },
		{ key: 'users', label: 'admin.super.users', icon: 'user-3-line' },
		{ key: 'circles', label: 'admin.super.circles', icon: 'book-2-line' },
		{ key: 'jobs', label: 'admin.super.jobs', icon: 'file-edit-line' },
		{ key: 'rules', label: 'admin.super.rules', icon: 'ruler-line' },
		{ key: 'templates', label: 'admin.super.templates', icon: 'puzzle-line' },
		{ key: 'data', label: 'admin.super.data', icon: 'upload-2-line' }
	]
	const activeTab = ref('overview')

	const loading = reactive({ stats: true, users: false })
	const stats = reactive({ users: 0, circles: 0, jobs: 0, rules: 0, templates: 0, books: 0 })

	const statCards = computed(() => [
		{ key: 'users', label: 'admin.super.statUsers', value: stats.users, icon: 'user-3-line', color: 'icon-blue' },
		{ key: 'circles', label: 'admin.super.statCircles', value: stats.circles, icon: 'book-2-line', color: 'icon-violet' },
		{ key: 'jobs', label: 'admin.super.statJobs', value: stats.jobs, icon: 'file-edit-line', color: 'icon-amber' },
		{ key: 'rules', label: 'admin.super.statRules', value: stats.rules, icon: 'ruler-line', color: 'icon-green' },
		{ key: 'templates', label: 'admin.super.statTemplates', value: stats.templates, icon: 'puzzle-line', color: 'icon-cyan' },
		{ key: 'books', label: 'admin.super.statBooks', value: stats.books, icon: 'book-open-line', color: 'icon-pink' }
	])

	const quickActions = [
		{ key: 'users', icon: 'user-3-line', color: 'icon-blue', label: 'admin.super.quickUser', desc: 'admin.super.quickUserDesc' },
		{ key: 'circles', icon: 'book-2-line', color: 'icon-violet', label: 'admin.super.quickCircle', desc: 'admin.super.quickCircleDesc' },
		{ key: 'jobs', icon: 'file-edit-line', color: 'icon-amber', label: 'admin.super.quickJob', desc: 'admin.super.quickJobDesc' },
		{ key: 'rules', icon: 'ruler-line', color: 'icon-green', label: 'admin.super.quickRule', desc: 'admin.super.quickRuleDesc' },
		{ key: 'templates', icon: 'puzzle-line', color: 'icon-cyan', label: 'admin.super.quickTemplate', desc: 'admin.super.quickTemplateDesc' },
		{ key: 'data', icon: 'upload-2-line', color: 'icon-pink', label: 'admin.super.quickData', desc: 'admin.super.quickDataDesc' },
		{ key: 'positions', icon: 'medal-line', color: 'icon-gray', label: 'admin.super.quickPosition', desc: 'admin.super.quickPositionDesc', url: '/pages/admin/position' }
	]

	const dataEntries = [
		{ key: 'users', icon: 'user-3-line', color: 'icon-blue', label: 'admin.super.dataManageUsers', desc: 'admin.super.dataManageUsersDesc' },
		{ key: 'books', icon: 'book-open-line', color: 'icon-violet', label: 'admin.super.dataManageBooks', desc: 'admin.super.dataManageBooksDesc' },
		{ key: 'checkins', icon: 'file-list-3-line', color: 'icon-green', label: 'admin.super.dataManageCheckins', desc: 'admin.super.dataManageCheckinsDesc' }
	]

	function openDataManage(key) {
		uni.navigateTo({ url: '/pages/admin/manage?table=' + key })
	}

	const todayLabel = computed(() => {
		const d = new Date()
		const pad = n => (n < 10 ? '0' + n : '' + n)
		return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
	})

	/* ================= 基础数据 ================= */

	const circles = ref([])
	const bookList = ref([])
	const positionNames = ref([])
	const recentUsers = ref([])

	function safe(promise, fallback) {
		return promise.then(res => res).catch(() => fallback)
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
		try {
			const data = await getBookPage({ current: 1, size: 200 })
			bookList.value = data.records || []
		} catch (e) {
			bookList.value = []
		}
	}

	async function loadPositionNames() {
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

	async function loadStats() {
		loading.stats = true
		const [u, j, r, tp, b] = await Promise.all([
			safe(getUserPage({ current: 1, size: 1 }), null),
			safe(getJobPage({ current: 1, size: 1 }), null),
			safe(getRuleList({}), null),
			safe(getTemplateList({}), null),
			safe(getBookPage({ current: 1, size: 1 }), null)
		])
		stats.users = (u && u.total) || 0
		stats.circles = circles.value.length
		stats.jobs = (j && j.total) || 0
		stats.rules = Array.isArray(r) ? r.length : 0
		stats.templates = Array.isArray(tp) ? tp.length : 0
		stats.books = (b && b.total) || 0
		loading.stats = false
	}

	async function loadRecentUsers() {
		try {
			const data = await getUserPage({ current: 1, size: 5 })
			recentUsers.value = data.records || []
		} catch (e) {
			recentUsers.value = []
		}
	}

	async function ensureCircles() {
		if (!circles.value.length) await loadCircles()
	}

	async function ensureBooks() {
		if (!bookList.value.length) await loadBooks()
	}

	async function ensurePositions() {
		if (!positionNames.value.length) await loadPositionNames()
	}

	/* ================= 用户 ================= */

	const users = ref([])
	const userKeyword = ref('')
	const userCurrent = ref(1)
	const userSize = 10
	const userTotal = ref(0)
	const userNoMore = ref(false)

	async function loadUsers(reset) {
		if (loading.users) return
		loading.users = true
		if (reset) {
			userCurrent.value = 1
			userNoMore.value = false
		}
		try {
			const data = await getUserPage({ keyword: userKeyword.value.trim(), current: userCurrent.value, size: userSize })
			const records = data.records || []
			users.value = reset ? records : users.value.concat(records)
			userTotal.value = data.total || 0
			if (records.length < userSize) {
				userNoMore.value = true
			} else {
				userCurrent.value += 1
			}
		} catch (e) {
			if (reset) users.value = []
		} finally {
			loading.users = false
		}
	}

	function searchUsers() {
		loadUsers(true)
	}

	function toggleUser(u) {
		const next = u.status === 1 ? 0 : 1
		const name = u.nickname || u.username || ''
		showModal({
			title: t('common.tips'),
			content: next === 0 ? t('admin.super.disableConfirm', { name }) : t('admin.super.enableConfirm', { name }),
			type: next === 0 ? 'danger' : 'info',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await updateUserStatus(u.id, next)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					loadUsers(true)
					loadRecentUsers()
				} catch (e) {}
			}
		})
	}

	/* 分配职位 */
	const positionVisible = ref(false)
	const positionUser = ref({})
	const positionDraft = ref('')
	const positionOptions = ref([])
	const positionCircles = ref([])

	async function openPositionForm(u) {
		positionUser.value = u
		positionDraft.value = u.position || ''
		positionOptions.value = []
		positionCircles.value = []
		positionVisible.value = true
		const [ruleData, dictData, circleData] = await Promise.all([
			safe(getRuleList(u.circleId ? { circleId: u.circleId } : {}), []),
			safe(getPositionList({}), []),
			safe(getUserCircles(u.id), [])
		])
		const ruleList = Array.isArray(ruleData) ? ruleData : (ruleData.records || [])
		const dictList = Array.isArray(dictData) ? dictData : (dictData.records || [])
		const options = []
		dictList.forEach(p => {
			if (p.name && p.status !== 0 && options.indexOf(p.name) < 0) options.push(p.name)
		})
		ruleList.forEach(r => {
			if (r.position && options.indexOf(r.position) < 0) options.push(r.position)
		})
		positionOptions.value = options
		positionCircles.value = Array.isArray(circleData) ? circleData : (circleData.records || [])
		if (!positionCircles.value.length && u.circleId) {
			positionCircles.value = [{ id: u.circleId, name: circleName(u.circleId) }]
		}
	}

	async function savePosition() {
		try {
			await updateUserPosition(positionUser.value.id, positionDraft.value.trim())
			positionVisible.value = false
			uni.showToast({ title: t('common.success'), icon: 'none' })
			loadUsers(true)
			loadRecentUsers()
		} catch (e) {}
	}

	function roleLabel(role) {
		if (role === 'SUPER_ADMIN') return t('admin.super.roleSuper')
		if (role === 'TEAM_ADMIN') return t('admin.super.roleTeam')
		return t('admin.super.roleMember')
	}

	function roleChipClass(role) {
		if (role === 'SUPER_ADMIN') return 'chip-red'
		if (role === 'TEAM_ADMIN') return 'chip-blue'
		return 'chip-gray'
	}

	function statusLabel(status) {
		return t(status === 0 ? 'admin.super.statusDisabled' : 'admin.super.statusNormal')
	}

	function circleName(id) {
		if (!id) return t('common.none')
		const found = circles.value.find(c => c.id === id)
		return found ? found.name : '#' + id
	}

	function bookName(id) {
		if (!id) return t('common.none')
		const found = bookList.value.find(b => b.id === id)
		return found ? found.title : '#' + id
	}

	/* ================= 列表数据 ================= */

	const jobs = ref([])
	const rules = ref([])
	const templates = ref([])
	const memberCircle = ref({})
	const members = ref([])
	const memberLoading = ref(false)

	/* 读书圈分组 */
	const groupsVisible = ref(false)
	const groupLoading = ref(false)
	const groupCircle = ref({})
	const groups = ref([])
	const activeGroup = ref({})
	const groupMembersVisible = ref(false)
	const groupMembersLoading = ref(false)
	const groupMembers = ref([])
	const memberPickerVisible = ref(false)
	const circleMemberList = ref([])
	const circleMemberLoading = ref(false)
	const memberKeyword = ref('')

	async function loadJobs() {
		try {
			const data = await getJobPage({ current: 1, size: 100 })
			jobs.value = data.records || []
		} catch (e) {
			jobs.value = []
		}
	}

	async function loadRules() {
		try {
			const data = await getRuleList({})
			rules.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			rules.value = []
		}
	}

	async function loadTemplates() {
		try {
			const data = await getTemplateList({})
			templates.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			templates.value = []
		}
	}

	function onQuickAction(q) {
		if (q.url) {
			uni.navigateTo({ url: q.url })
			return
		}
		switchTab(q.key)
	}

	function switchTab(key) {
		activeTab.value = key
		if (key === 'users') loadUsers(true)
		else if (key === 'circles') ensureCircles()
		else if (key === 'jobs') {
			ensureCircles()
			ensureBooks()
			loadJobs()
		} else if (key === 'rules') {
			ensureCircles()
			loadRules()
		} else if (key === 'templates') {
			ensureCircles()
			loadTemplates()
		}
	}

	/* ================= 表单 ================= */

	const formVisible = ref(false)
	const formType = ref('')
	const form = ref({})
	const formCircleIndex = ref(-1)
	const formBookIndex = ref(-1)
	const formTypes = ref([])
	const typeConfig = ref({})
	const allTypes = [1, 2, 3]
	// 每个打卡类型可单独勾选的必填项
	const reqOptions = [
		{ key: 'book', label: 'admin.super.requireBook' },
		{ key: 'chapter', label: 'admin.super.requireChapter' },
		{ key: 'image', label: 'admin.super.requireImage' },
		{ key: 'audio', label: 'admin.super.requireAudio' },
		{ key: 'note', label: 'admin.super.requireContent' }
	]
	const jobStatusLabels = computed(() => [t('admin.super.statusDraft'), t('admin.super.statusPublished'), t('admin.super.statusFinished')])

	const formPositionOptions = computed(() => {
		const options = positionNames.value.slice()
		const current = form.value.position
		if (current && options.indexOf(current) < 0) options.unshift(current)
		return options
	})

	const formPositionIndex = computed(() => formPositionOptions.value.indexOf(form.value.position))

	const formTitle = computed(() => {
		const edit = !!form.value.id
		if (formType.value === 'circle') return t(edit ? 'admin.super.editCircle' : 'admin.super.createCircle')
		if (formType.value === 'job') return t(edit ? 'admin.super.editJob' : 'admin.super.createJob')
		if (formType.value === 'rule') return t(edit ? 'admin.super.editRule' : 'admin.super.createRule')
		if (formType.value === 'group') return t(edit ? 'admin.super.editGroup' : 'admin.super.createGroup')
		return t(edit ? 'admin.super.editTemplate' : 'admin.super.createTemplate')
	})

	function openCircleForm(item) {
		formType.value = 'circle'
		form.value = item
			? { id: item.id, name: item.name, description: item.description || '' }
			: { name: '', description: '' }
		formVisible.value = true
	}

	async function openJobForm(item) {
		await Promise.all([ensureCircles(), ensureBooks()])
		formType.value = 'job'
		if (item) {
			form.value = {
				id: item.id,
				title: item.title,
				circleId: item.circleId,
				bookId: item.bookId,
				chapterStart: item.chapterStart == null ? '' : item.chapterStart,
				chapterEnd: item.chapterEnd == null ? '' : item.chapterEnd,
				startDate: item.startDate || '',
				deadline: item.deadline || '',
				content: item.content || '',
				status: item.status == null ? 1 : item.status
			}
			formCircleIndex.value = circles.value.findIndex(c => c.id === item.circleId)
			formBookIndex.value = bookList.value.findIndex(b => b.id === item.bookId)
		} else {
			form.value = {
				title: '', circleId: null, bookId: null, chapterStart: '', chapterEnd: '',
				startDate: '', deadline: '', content: '', status: 1
			}
			formCircleIndex.value = circles.value.length === 1 ? 0 : -1
			if (formCircleIndex.value === 0) form.value.circleId = circles.value[0].id
			formBookIndex.value = -1
		}
		formVisible.value = true
	}

	async function openRuleForm(item) {
		await Promise.all([ensureCircles(), ensurePositions()])
		formType.value = 'rule'
		if (item) {
			form.value = {
				id: item.id,
				circleId: item.circleId,
				position: item.position,
				requiredDaysPerWeek: item.requiredDaysPerWeek == null ? '' : item.requiredDaysPerWeek,
				requiredDaysPerMonth: item.requiredDaysPerMonth == null ? '' : item.requiredDaysPerMonth,
				requiredDaysPerYear: item.requiredDaysPerYear == null ? '' : item.requiredDaysPerYear,
				status: item.status == null ? 1 : item.status
			}
			formCircleIndex.value = circles.value.findIndex(c => c.id === item.circleId)
		} else {
			form.value = {
				circleId: null, position: '', requiredDaysPerWeek: '', requiredDaysPerMonth: '',
				requiredDaysPerYear: '', status: 1
			}
			formCircleIndex.value = circles.value.length === 1 ? 0 : -1
			if (formCircleIndex.value === 0) form.value.circleId = circles.value[0].id
		}
		formVisible.value = true
	}

	async function openTemplateForm(item) {
		await ensureCircles()
		formType.value = 'template'
		if (item) {
			form.value = {
				id: item.id,
				circleId: item.circleId,
				name: item.name,
				checkinTypes: item.checkinTypes || '1,2,3',
				requireAudio: item.requireAudio || 0,
				requireContent: item.requireContent || 0,
				requireImage: item.requireImage || 0,
				requireBook: item.requireBook || 0,
				requireChapter: item.requireChapter || 0,
				minWordCount: item.minWordCount == null ? 0 : item.minWordCount,
				status: item.status == null ? 1 : item.status
			}
			formTypes.value = parseTypes(item.checkinTypes)
			typeConfig.value = buildTypeConfig(item)
			formCircleIndex.value = circles.value.findIndex(c => c.id === item.circleId)
		} else {
			form.value = {
				circleId: null, name: '', checkinTypes: '1,2,3', requireAudio: 0,
				requireContent: 0, requireImage: 0, requireBook: 0, requireChapter: 0, minWordCount: 0, status: 1
			}
			formTypes.value = [1, 2, 3]
			typeConfig.value = buildTypeConfig(null)
			formCircleIndex.value = circles.value.length === 1 ? 0 : -1
			if (formCircleIndex.value === 0) form.value.circleId = circles.value[0].id
		}
		formVisible.value = true
	}

	function openGroupForm(item) {
		formType.value = 'group'
		form.value = item
			? {
				id: item.id,
				circleId: groupCircle.value.id,
				name: item.name,
				description: item.description || '',
				status: item.status == null ? 1 : item.status
			}
			: { circleId: groupCircle.value.id, name: '', description: '', status: 1 }
		formVisible.value = true
	}

	function onFormCircleChange(e) {
		formCircleIndex.value = Number(e.detail.value)
		const c = circles.value[formCircleIndex.value]
		form.value.circleId = c ? c.id : null
	}

	function onFormBookChange(e) {
		formBookIndex.value = Number(e.detail.value)
		const b = bookList.value[formBookIndex.value]
		form.value.bookId = b ? b.id : null
	}

	function toggleFormType(tp) {
		const i = formTypes.value.indexOf(tp)
		if (i >= 0) {
			formTypes.value.splice(i, 1)
		} else {
			formTypes.value.push(tp)
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

	// 编辑已有模板时，按类型配置初始化；旧模板无按类型配置则按全局开关推导
	function buildTypeConfig(item) {
		const parsed = item ? parseTypeConfig(item.typeConfig) : {}
		const cfg = {}
		formTypes.value.forEach(tp => {
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
		if (!cfg) return t('admin.super.noRequirement')
		const labels = []
		if (cfg.book) labels.push(t('admin.super.requireBook'))
		if (cfg.chapter) labels.push(t('admin.super.requireChapter'))
		if (cfg.image) labels.push(t('admin.super.requireImage'))
		if (cfg.audio) labels.push(t('admin.super.requireAudio'))
		if (cfg.note) labels.push(t('admin.super.requireContent'))
		return labels.length ? labels.join(' / ') : t('admin.super.noRequirement')
	}

	function parseTypes(val) {
		if (Array.isArray(val)) return val.map(Number)
		if (!val) return []
		return String(val).split(',').map(Number).filter(Boolean)
	}

	function typeLabel(tp) {
		return t(tp === 1 ? 'card.typeImage' : tp === 2 ? 'card.typeAudio' : 'card.typeNote')
	}

	function jobStatusLabel(status) {
		if (status === 0) return t('admin.super.statusDraft')
		if (status === 2) return t('admin.super.statusFinished')
		return t('admin.super.statusPublished')
	}

	function toNumberOrZero(val) {
		return val === '' || val == null ? 0 : Number(val)
	}

	function toNumberOrNull(val) {
		return val === '' || val == null ? null : Number(val)
	}

	async function saveForm() {
		try {
			if (formType.value === 'circle') {
				if (!form.value.name || !form.value.name.trim()) {
					uni.showToast({ title: t('admin.super.errRequired'), icon: 'none' })
					return
				}
				const payload = { id: form.value.id, name: form.value.name.trim(), description: form.value.description || '' }
				if (payload.id) await updateCircle(payload)
				else await createCircle(payload)
				formVisible.value = false
				uni.showToast({ title: t('common.success'), icon: 'none' })
				await loadCircles()
				await loadStats()
			} else if (formType.value === 'job') {
				if (!form.value.title || !form.value.title.trim() || !form.value.circleId || !form.value.bookId || !form.value.startDate || !form.value.deadline) {
					uni.showToast({ title: t('admin.super.errRequired'), icon: 'none' })
					return
				}
				const payload = {
					id: form.value.id,
					circleId: form.value.circleId,
					title: form.value.title.trim(),
					bookId: form.value.bookId,
					chapterStart: toNumberOrNull(form.value.chapterStart),
					chapterEnd: toNumberOrNull(form.value.chapterEnd),
					content: form.value.content || '',
					startDate: form.value.startDate,
					deadline: form.value.deadline,
					status: form.value.status
				}
				if (payload.id) await updateJob(payload)
				else await createJob(payload)
				formVisible.value = false
				uni.showToast({ title: t('common.success'), icon: 'none' })
				await Promise.all([loadJobs(), loadStats()])
			} else if (formType.value === 'rule') {
				if (!form.value.circleId || !form.value.position || !form.value.position.trim() || form.value.requiredDaysPerWeek === '') {
					uni.showToast({ title: t('admin.super.errRequired'), icon: 'none' })
					return
				}
				const payload = {
					id: form.value.id,
					circleId: form.value.circleId,
					position: form.value.position.trim(),
					requiredDaysPerWeek: toNumberOrZero(form.value.requiredDaysPerWeek),
					requiredDaysPerMonth: toNumberOrZero(form.value.requiredDaysPerMonth),
					requiredDaysPerYear: toNumberOrZero(form.value.requiredDaysPerYear),
					status: form.value.status
				}
				if (payload.id) await updateRule(payload)
				else await createRule(payload)
				formVisible.value = false
				uni.showToast({ title: t('common.success'), icon: 'none' })
				await Promise.all([loadRules(), loadStats()])
			} else if (formType.value === 'template') {
				if (!form.value.circleId || !form.value.name || !form.value.name.trim()) {
					uni.showToast({ title: t('admin.super.errRequired'), icon: 'none' })
					return
				}
				const typeReq = {}
				formTypes.value.forEach(tp => {
					typeReq[tp] = normalizeTypeReq(typeConfig.value[String(tp)] || defaultTypeReq(tp))
				})
				const payload = {
					id: form.value.id,
					circleId: form.value.circleId,
					name: form.value.name.trim(),
					checkinTypes: formTypes.value.length ? formTypes.value.join(',') : '1,2,3',
					requireAudio: form.value.requireAudio || 0,
					requireContent: form.value.requireContent || 0,
					requireImage: form.value.requireImage || 0,
					requireBook: form.value.requireBook || 0,
					requireChapter: form.value.requireChapter || 0,
					typeConfig: JSON.stringify(typeReq),
					minWordCount: toNumberOrZero(form.value.minWordCount),
					status: form.value.status
				}
				if (payload.id) await updateTemplate(payload)
				else await createTemplate(payload)
				formVisible.value = false
				uni.showToast({ title: t('common.success'), icon: 'none' })
				await Promise.all([loadTemplates(), loadStats()])
			} else if (formType.value === 'group') {
				if (!form.value.circleId || !form.value.name || !form.value.name.trim()) {
					uni.showToast({ title: t('admin.super.errGroupName'), icon: 'none' })
					return
				}
				const payload = {
					id: form.value.id,
					circleId: form.value.circleId,
					name: form.value.name.trim(),
					description: form.value.description || '',
					status: form.value.status == null ? 1 : form.value.status
				}
				if (payload.id) await updateGroup(payload)
				else await createGroup(payload)
				formVisible.value = false
				uni.showToast({ title: t('common.success'), icon: 'none' })
				await loadGroups(form.value.circleId)
			}
		} catch (e) {}
	}

	/* ================= 删除 ================= */

	function removeCircle(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.super.delCircleConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteCircle(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await loadCircles()
					await loadStats()
				} catch (e) {}
			}
		})
	}

	function removeJob(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.super.delJobConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteJob(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await Promise.all([loadJobs(), loadStats()])
				} catch (e) {}
			}
		})
	}

	function removeRule(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.super.delRuleConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteRule(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await Promise.all([loadRules(), loadStats()])
				} catch (e) {}
			}
		})
	}

	function removeTemplate(item) {
		showModal({
			title: t('common.tips'),
			content: t('admin.super.delTemplateConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteTemplate(item.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await Promise.all([loadTemplates(), loadStats()])
				} catch (e) {}
			}
		})
	}

	/* ================= 成员 ================= */

	const membersVisible = ref(false)

	async function openMembers(c) {
		memberCircle.value = c
		members.value = []
		membersVisible.value = true
		memberLoading.value = true
		try {
			const data = await getCircleMembers(c.id, { current: 1, size: 100 })
			members.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			members.value = []
		} finally {
			memberLoading.value = false
		}
	}

	function removeMember(m) {
		const name = m.nickname || m.username || ''
		showModal({
			title: t('common.tips'),
			content: t('admin.circle.removeConfirm', { name }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await removeCircleMember(memberCircle.value.id, m.id)
					uni.showToast({ title: t('admin.circle.removeSuccess'), icon: 'none' })
					openMembers(memberCircle.value)
					await loadCircles()
					await loadStats()
				} catch (e) {}
			}
		})
	}

	function goCircleAdmin() {
		membersVisible.value = false
		uni.navigateTo({ url: '/pages/admin/circle' })
	}

	/* ================= 分组管理 ================= */

	async function loadGroups(circleId) {
		groupLoading.value = true
		try {
			const data = await getGroupList(circleId)
			groups.value = Array.isArray(data) ? data : (data.records || [])
		} catch (e) {
			groups.value = []
		} finally {
			groupLoading.value = false
		}
	}

	function openGroups(c) {
		groupCircle.value = c
		groupsVisible.value = true
		groupMembersVisible.value = false
		memberPickerVisible.value = false
		activeGroup.value = {}
		groups.value = []
		groupMembers.value = []
		memberKeyword.value = ''
		loadGroups(c.id)
	}

	async function loadGroupMembers() {
		if (!activeGroup.value.id) return
		groupMembersLoading.value = true
		try {
			const data = await getGroupMembers(activeGroup.value.id, { current: 1, size: 200 })
			groupMembers.value = data.records || []
		} catch (e) {
			groupMembers.value = []
		} finally {
			groupMembersLoading.value = false
		}
	}

	async function openGroupMembers(g) {
		activeGroup.value = g
		groupMembers.value = []
		groupMembersVisible.value = true
		await loadGroupMembers()
	}

	async function loadCircleMembers() {
		if (!groupCircle.value.id) return
		circleMemberLoading.value = true
		try {
			const data = await getCircleMembers(groupCircle.value.id, {
				keyword: memberKeyword.value.trim(),
				current: 1,
				size: 200
			})
			circleMemberList.value = data.records || []
		} catch (e) {
			circleMemberList.value = []
		} finally {
			circleMemberLoading.value = false
		}
	}

	const availableMembers = computed(() => {
		return circleMemberList.value.filter(m => !groupMembers.value.some(gm => gm.id === m.id))
	})

	function openMemberPicker() {
		memberPickerVisible.value = true
		memberKeyword.value = ''
		loadCircleMembers()
	}

	async function addToGroup(m) {
		try {
			await assignMemberGroup(groupCircle.value.id, m.id, activeGroup.value.id)
			uni.showToast({ title: t('common.success'), icon: 'none' })
			await Promise.all([loadGroupMembers(), loadGroups(groupCircle.value.id), loadCircleMembers()])
		} catch (e) {}
	}

	function removeFromGroup(m) {
		const name = m.nickname || m.username || ''
		showModal({
			title: t('common.tips'),
			content: t('admin.super.removeFromGroupConfirm', { name }),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await assignMemberGroup(groupCircle.value.id, m.id, null)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					await Promise.all([loadGroupMembers(), loadGroups(groupCircle.value.id)])
				} catch (e) {}
			}
		})
	}

	function removeGroup(g) {
		showModal({
			title: t('common.tips'),
			content: t('admin.super.groupDelConfirm'),
			type: 'danger',
			confirmText: t('common.confirm'),
			cancelText: t('common.cancel'),
			success: async (res) => {
				if (!res.confirm) return
				try {
					await deleteGroup(g.id)
					uni.showToast({ title: t('common.success'), icon: 'none' })
					if (activeGroup.value.id === g.id) {
						groupMembersVisible.value = false
						memberPickerVisible.value = false
						activeGroup.value = {}
					}
					await loadGroups(groupCircle.value.id)
				} catch (e) {}
			}
		})
	}

	/* ================= 数据管理 ================= */

	const exportStart = ref('')
	const exportEnd = ref('')
	const exportLink = ref('')
	const exportCircleIndex = ref(0)
	const exportCircleOptions = computed(() => [{ id: null, name: t('admin.super.allCircles') }].concat(circles.value))

	function genExportLink() {
		const option = exportCircleOptions.value[exportCircleIndex.value] || {}
		const circleId = option.id || ''
		exportLink.value = BASE_URL + '/admin/export?circleId=' + circleId + '&startDate=' + exportStart.value + '&endDate=' + exportEnd.value
	}

	function copyText(text) {
		if (!text) return
		uni.setClipboardData({
			data: String(text),
			success: () => uni.showToast({ title: t('common.copied'), icon: 'none' })
		})
	}

	function chooseImportFile() {
		// #ifdef MP-WEIXIN
		uni.chooseMessageFile({
			count: 1,
			type: 'file',
			extension: ['xlsx'],
			success: async (res) => {
				const file = res.tempFiles[0]
				try {
					const count = await uploadFile(file.path, '/admin/import')
					uni.showToast({ title: t('admin.super.importSuccess', { n: count || 0 }), icon: 'none' })
				} catch (e) {}
			}
		})
		// #endif
		// #ifndef MP-WEIXIN
		uni.showToast({ title: t('admin.super.importNotSupport'), icon: 'none' })
		// #endif
	}

	const proxy = ref({ userId: '', bookId: null, checkinType: 1, checkinDate: '', remark: '' })
	const proxyBookIndex = ref(-1)
	const proxyTypeIndex = ref(0)
	const checkinTypeValues = [1, 2, 3]
	const checkinTypeLabels = computed(() => [t('card.typeImage'), t('card.typeAudio'), t('card.typeNote')])

	function onProxyBookChange(e) {
		proxyBookIndex.value = Number(e.detail.value)
		const b = bookList.value[proxyBookIndex.value]
		proxy.value.bookId = b ? b.id : null
	}

	function onProxyTypeChange(e) {
		proxyTypeIndex.value = Number(e.detail.value)
		proxy.value.checkinType = checkinTypeValues[proxyTypeIndex.value]
	}

	async function submitProxy() {
		if (!proxy.value.userId) {
			uni.showToast({ title: t('admin.super.errUser'), icon: 'none' })
			return
		}
		if (!proxy.value.bookId) {
			uni.showToast({ title: t('admin.super.errBook'), icon: 'none' })
			return
		}
		try {
			const payload = {
				userId: Number(proxy.value.userId),
				bookId: proxy.value.bookId,
				checkinType: proxy.value.checkinType,
				remark: proxy.value.remark || ''
			}
			if (proxy.value.checkinDate) payload.checkinDate = proxy.value.checkinDate
			await proxyCheckin(payload)
			uni.showToast({ title: t('admin.super.proxySuccess'), icon: 'none' })
		} catch (e) {}
	}

	/* ================= 生命周期 ================= */

	function goApp() {
		uni.reLaunch({ url: '/pages/index/index' })
	}

	async function refreshAll() {
		await Promise.all([loadCircles(), loadBooks(), loadPositionNames(), loadRecentUsers()])
		await loadStats()
		if (activeTab.value === 'users') await loadUsers(true)
		else if (activeTab.value === 'jobs') await loadJobs()
		else if (activeTab.value === 'rules') await loadRules()
		else if (activeTab.value === 'templates') await loadTemplates()
	}

	onShow(async () => {
		try {
			await fetchUserInfo()
		} catch (e) {
			if (!uni.getStorageSync('satoken')) return
		}
		if (!allowed.value) {
			uni.showToast({ title: t('admin.super.noPermission'), icon: 'none' })
			setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 800)
			return
		}
		refreshAll()
	})

	onPullDownRefresh(async () => {
		await refreshAll()
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	.super-page {
		padding: 24rpx 24rpx 80rpx;
	}

	/* ---------- 头部 ---------- */
	.hero {
		position: relative;
		padding: 32rpx 30rpx 24rpx 40rpx;
		border-radius: var(--radius-xl);
		overflow: hidden;
	}

	.hero-stripe {
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		width: 10rpx;
		background: var(--primary-fill);
	}

	.hero-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
	}

	.hero-id {
		display: flex;
		align-items: center;
		flex: 1;
		min-width: 0;
	}

	.hero-avatar {
		width: 92rpx;
		height: 92rpx;
		border-radius: 28rpx;
		background: var(--surface-2);
		flex-shrink: 0;
	}

	.hero-id-main {
		flex: 1;
		min-width: 0;
		margin-left: 20rpx;
	}

	.hero-name-row {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
	}

	.hero-name {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--text-primary);
		letter-spacing: 1rpx;
		margin-right: 14rpx;
	}

	.role-badge {
		display: inline-flex;
		align-items: center;
		padding: 4rpx 14rpx;
		border-radius: var(--radius-pill);
		border: 1rpx solid var(--border-strong);
		background: var(--surface-2);
	}

	.role-icon {
		font-size: 20rpx;
		color: var(--primary);
		margin-right: 6rpx;
	}

	.role-text {
		font-size: 19rpx;
		font-weight: 600;
		color: var(--text-secondary);
		letter-spacing: 1rpx;
	}

	.hero-sub {
		display: block;
		font-size: 23rpx;
		color: var(--text-secondary);
		margin-top: 10rpx;
	}

	.hero-app {
		display: flex;
		align-items: center;
		flex-shrink: 0;
		margin-left: 16rpx;
		padding: 12rpx 20rpx;
		border-radius: var(--radius-pill);
		border: 1rpx solid var(--border-strong);
		background: var(--bg-secondary);
	}

	.hero-app:active {
		opacity: 0.7;
	}

	.hero-app-icon {
		font-size: 24rpx;
		color: var(--primary);
		margin-right: 8rpx;
	}

	.hero-app-text {
		font-size: 21rpx;
		color: var(--text-primary);
	}

	.hero-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 26rpx;
		padding-top: 20rpx;
		border-top: 1rpx dashed var(--border-strong);
	}

	.hero-date {
		font-size: 22rpx;
		color: var(--text-tertiary);
		letter-spacing: 1rpx;
	}

	.hero-refresh {
		font-size: 22rpx;
		color: var(--primary);
		padding: 4rpx 10rpx;
	}

	/* ---------- 数据卡 ---------- */
	.stat-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
		margin-top: 20rpx;
	}

	.stat-card {
		width: calc(50% - 8rpx);
		box-sizing: border-box;
		padding: 22rpx 24rpx 20rpx;
		background: var(--card-bg);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-lg);
		box-shadow: var(--shadow-sm);
	}

	.stat-card-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.stat-card-label {
		font-size: 22rpx;
		color: var(--text-secondary);
		letter-spacing: 0.5rpx;
	}

	.stat-card-num {
		display: block;
		margin-top: 14rpx;
		font-size: 48rpx;
		font-weight: 700;
		line-height: 1.1;
		color: var(--text-primary);
		font-variant-numeric: tabular-nums;
		letter-spacing: 1rpx;
	}

	/* ---------- 模块切换 ---------- */
	.tab-scroll {
		margin: 24rpx 0 20rpx;
		white-space: nowrap;
	}

	.tab-row {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx;
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
	}

	.tab-item {
		display: inline-flex;
		align-items: center;
		flex-shrink: 0;
		padding: 12rpx 24rpx;
		border-radius: var(--radius-pill);
	}

	.tab-item.active {
		background: var(--bg-secondary);
		box-shadow: var(--shadow-sm);
	}

	.tab-icon {
		font-size: 24rpx;
		color: var(--text-tertiary);
		margin-right: 8rpx;
	}

	.tab-item.active .tab-icon {
		color: var(--primary);
	}

	.tab-text {
		font-size: 24rpx;
		color: var(--text-secondary);
		white-space: nowrap;
	}

	.tab-item.active .tab-text {
		color: var(--text-primary);
		font-weight: 600;
	}

	/* ---------- 区块 ---------- */
	.section {
		margin-bottom: 20rpx;
		padding: 28rpx;
		border-radius: var(--radius-lg);
	}

	.section-head {
		display: flex;
		align-items: center;
		margin-bottom: 18rpx;
	}

	.section-bar {
		width: 6rpx;
		height: 26rpx;
		border-radius: var(--radius-pill);
		background: var(--primary-fill);
		margin-right: 12rpx;
	}

	.section-title {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
		letter-spacing: 0.5rpx;
	}

	.section-link {
		font-size: 22rpx;
		color: var(--primary);
		flex-shrink: 0;
	}

	.section-count {
		font-size: 22rpx;
		color: var(--text-tertiary);
		flex-shrink: 0;
	}

	.section-desc {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		line-height: 1.6;
	}

	/* 头部图标与标题并排的区块 */
	.section-head .icon-chip {
		margin-right: 14rpx;
	}

	.section-head .section-title {
		flex: 1;
	}

	/* ---------- 快捷操作 ---------- */
	.quick-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.quick-tile {
		width: calc(50% - 8rpx);
		box-sizing: border-box;
		display: flex;
		align-items: center;
		padding: 18rpx;
		background: var(--bg-secondary);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-md);
	}

	.quick-tile:active {
		background: var(--surface-2);
	}

	.quick-main {
		flex: 1;
		min-width: 0;
		margin-left: 14rpx;
		display: flex;
		flex-direction: column;
	}

	.quick-title {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--text-primary);
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.quick-desc {
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 4rpx;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.quick-arrow {
		font-size: 24rpx;
		color: var(--text-tertiary);
		margin-left: 6rpx;
		flex-shrink: 0;
	}

	/* ---------- 用户行 ---------- */
	.user-row {
		display: flex;
		align-items: center;
		padding: 20rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.user-row:last-child {
		border-bottom: none;
	}

	.user-row.compact {
		padding: 16rpx 0;
	}

	.user-avatar {
		width: 76rpx;
		height: 76rpx;
		border-radius: 22rpx;
		background: var(--surface-2);
		flex-shrink: 0;
	}

	.user-main {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
	}

	.user-name-row {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
	}

	.user-name {
		font-size: 27rpx;
		font-weight: 600;
		color: var(--text-primary);
		margin-right: 8rpx;
		max-width: 260rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.user-sub {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.user-meta {
		display: block;
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 4rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.chip {
		display: inline-flex;
		align-items: center;
		font-size: 19rpx;
		line-height: 1.4;
		padding: 3rpx 14rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		color: var(--text-secondary);
		margin-left: 8rpx;
		flex-shrink: 0;
	}

	.chip-gray {
		background: var(--surface-2);
		color: var(--text-secondary);
	}

	.chip-blue {
		background: rgba(46, 74, 107, 0.10);
		color: #2E4A6B;
	}

	.chip-red {
		background: rgba(179, 69, 59, 0.11);
		color: #B3453B;
	}

	.chip-green {
		background: rgba(47, 125, 91, 0.12);
		color: #2F7D5B;
	}

	.theme-dark .chip-blue {
		background: rgba(147, 175, 203, 0.14);
		color: #A8C0DA;
	}

	.theme-dark .chip-red {
		background: rgba(224, 113, 104, 0.14);
		color: #EE938C;
	}

	.theme-dark .chip-green {
		background: rgba(79, 191, 146, 0.14);
		color: #6FD3AB;
	}

	.user-ops {
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 8rpx;
		margin-left: 14rpx;
	}

	.user-ops .row-action {
		display: inline-flex;
		align-items: center;
		margin-left: 0;
		padding: 7rpx 16rpx;
		font-size: 20rpx;
	}

	.user-ops .row-self {
		margin-left: 0;
	}

	.row-action-icon {
		font-size: 20rpx;
		margin-right: 6rpx;
	}

	.row-action {
		flex-shrink: 0;
		margin-left: 14rpx;
		padding: 10rpx 22rpx;
		border-radius: var(--radius-pill);
		font-size: 22rpx;
		color: var(--primary);
		background: var(--primary-soft);
	}

	.row-action.danger {
		color: var(--danger);
		background: rgba(179, 69, 59, 0.09);
	}

	.row-self {
		flex-shrink: 0;
		margin-left: 14rpx;
		font-size: 20rpx;
		color: var(--text-tertiary);
	}

	/* ---------- 搜索 ---------- */
	.search-box {
		display: flex;
		align-items: center;
		height: 78rpx;
		padding: 0 10rpx 0 22rpx;
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
		font-size: 25rpx;
		color: var(--text-primary);
		margin-left: 12rpx;
	}

	.search-btn {
		flex-shrink: 0;
		padding: 10rpx 24rpx;
		border-radius: var(--radius-pill);
		background: var(--primary-fill);
		color: var(--on-primary);
		font-size: 22rpx;
	}

	/* ---------- 列表卡片 ---------- */
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

	.item-card {
		margin-bottom: 18rpx;
		padding: 26rpx;
		border-radius: var(--radius-lg);
	}

	.item-head {
		display: flex;
		align-items: center;
	}

	.item-main {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
	}

	.item-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.item-sub {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.tpl-status {
		display: inline-block;
		margin-left: 12rpx;
		padding: 2rpx 12rpx;
		border-radius: var(--radius-pill);
		font-size: 20rpx;
		color: var(--primary);
		background: var(--primary-soft);
	}

	.tpl-status.off {
		color: var(--text-tertiary);
		background: var(--surface-2);
	}

	.job-s0 {
		background: var(--surface-2);
		color: var(--text-secondary);
	}

	.job-s1 {
		background: rgba(47, 125, 91, 0.12);
		color: var(--success);
	}

	.job-s2 {
		background: rgba(180, 101, 46, 0.15);
		color: var(--warning);
	}

	.row-ops {
		display: flex;
		flex-shrink: 0;
	}

	.row-ops.bottom {
		justify-content: flex-end;
		margin-top: 18rpx;
		padding-top: 16rpx;
		border-top: 1rpx solid var(--border);
	}

	.row-op {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: var(--surface-2);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 12rpx;
	}

	.row-op.danger {
		background: rgba(179, 69, 59, 0.08);
	}

	.row-op-icon {
		font-size: 24rpx;
		color: var(--text-secondary);
	}

	.row-op-icon.danger {
		color: var(--danger);
	}

	.item-meta {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx 12rpx;
		margin-top: 18rpx;
	}

	.meta-pill {
		font-size: 20rpx;
		color: var(--text-secondary);
		background: var(--surface-2);
		border-radius: var(--radius-pill);
		padding: 6rpx 16rpx;
	}

	.meta-pill.action {
		color: var(--primary);
		background: var(--primary-soft);
	}

	.item-lines {
		margin-top: 16rpx;
		padding: 16rpx 18rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.item-line {
		display: block;
		font-size: 22rpx;
		color: var(--text-secondary);
		line-height: 1.6;
	}

	/* ---------- 三列数据 ---------- */
	.tri-stat {
		display: flex;
		align-items: center;
		margin-top: 18rpx;
		padding: 16rpx 0;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.tri-cell {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.tri-num {
		font-size: 34rpx;
		font-weight: 700;
		color: var(--primary);
		line-height: 1.2;
		font-variant-numeric: tabular-nums;
	}

	.tri-label {
		font-size: 19rpx;
		color: var(--text-secondary);
		margin-top: 4rpx;
	}

	.tri-divider {
		width: 1rpx;
		height: 44rpx;
		background: var(--border);
	}

	/* ---------- 标签 ---------- */
	.tag-row {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 16rpx;
	}

	.tag {
		font-size: 20rpx;
		color: var(--primary);
		background: var(--primary-soft);
		border-radius: var(--radius-pill);
		padding: 5rpx 16rpx;
	}

	.flag-row {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx 14rpx;
		margin-top: 12rpx;
	}

	.flag {
		font-size: 20rpx;
		color: var(--text-secondary);
	}

	/* ---------- 数据管理 ---------- */
	.sec-btn {
		margin-top: 24rpx;
	}

	.link-box {
		margin-top: 22rpx;
		padding: 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.link-text {
		display: block;
		font-size: 21rpx;
		color: var(--primary);
		line-height: 1.6;
		word-break: break-all;
	}

	.link-tip {
		display: block;
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 12rpx;
	}

	/* ---------- 空态与加载 ---------- */
	.row-empty {
		padding: 36rpx 0;
		text-align: center;
		font-size: 23rpx;
		color: var(--text-tertiary);
	}

	.load-more {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 26rpx 0 6rpx;
	}

	.load-more-text {
		font-size: 23rpx;
		color: var(--primary);
	}

	/* ---------- 系统信息 ---------- */
	.info-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 14rpx 0;
		border-bottom: 1rpx dashed var(--border);
	}

	.info-row:last-child {
		border-bottom: none;
	}

	.info-label {
		font-size: 23rpx;
		color: var(--text-secondary);
	}

	.info-value {
		font-size: 23rpx;
		color: var(--text-primary);
		max-width: 440rpx;
		text-align: right;
		word-break: break-all;
	}

	/* ---------- 弹层表单 ---------- */
	.form-modal {
		max-height: 86vh;
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
		max-height: 58vh;
	}

	.f-area {
		height: 170rpx;
		min-height: 170rpx;
	}

	.switch-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 22rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.switch-label {
		font-size: 26rpx;
		color: var(--text-primary);
	}

	.type-row {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
	}

	.type-tag {
		padding: 12rpx 30rpx;
		border-radius: var(--radius-pill);
		background: var(--surface-2);
		border: 1rpx solid var(--border);
	}

	.type-tag.active {
		background: var(--primary-fill);
		border-color: transparent;
	}

	.type-tag-text {
		font-size: 23rpx;
		color: var(--text-primary);
	}

	.type-tag.active .type-tag-text {
		color: var(--on-primary);
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

	.modal-ops {
		display: flex;
		gap: 20rpx;
		margin-top: 26rpx;
	}

	.modal-btn {
		flex: 1;
	}

	/* ---------- 分配职位弹层 ---------- */
	.position-modal {
		padding: 32rpx 28rpx 28rpx;
	}

	.position-user {
		display: flex;
		align-items: center;
		margin: 18rpx 0 8rpx;
		padding: 16rpx 20rpx;
		background: var(--surface-2);
		border-radius: var(--radius-md);
	}

	.position-circles {
		margin-top: 20rpx;
	}

	.position-circles .tag-row {
		margin-top: 0;
	}

	.circle-tag {
		font-size: 22rpx;
		color: var(--text-primary);
		background: var(--surface-2);
		border: 1rpx solid var(--border);
		border-radius: var(--radius-pill);
		padding: 8rpx 22rpx;
	}

	.position-empty {
		font-size: 22rpx;
		color: var(--text-tertiary);
	}

	.pos-tag {
		font-size: 22rpx;
		color: var(--primary);
		background: var(--primary-soft);
		border-radius: var(--radius-pill);
		padding: 8rpx 22rpx;
	}

	.pos-tag.active {
		background: var(--primary-fill);
		color: var(--on-primary);
		font-weight: 600;
	}

	/* ---------- 弹层层级 ---------- */
	.form-mask {
		z-index: 1300;
	}

	.picker-mask {
		z-index: 1400;
	}

	/* ---------- 分组管理 ---------- */
	.group-create-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 18rpx;
		padding: 18rpx 0;
		font-size: 26rpx;
	}

	.group-row {
		display: flex;
		align-items: center;
		padding: 16rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.group-row:last-child {
		border-bottom: none;
	}

	.group-main {
		display: flex;
		align-items: center;
		flex: 1;
		min-width: 0;
	}

	.group-info {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
	}

	.group-name {
		display: block;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.group-sub {
		display: block;
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 4rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/* ---------- 成员弹层 ---------- */
	.member-modal {
		max-height: 84vh;
		display: flex;
		flex-direction: column;
		padding: 32rpx 28rpx 28rpx;
	}

	.member-scroll {
		max-height: 52vh;
		margin-top: 12rpx;
	}

	.member-row {
		display: flex;
		align-items: center;
		padding: 16rpx 0;
		border-bottom: 1rpx solid var(--border);
	}

	.member-row:last-child {
		border-bottom: none;
	}

	.member-avatar {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: var(--surface-2);
		flex-shrink: 0;
	}

	.member-main {
		flex: 1;
		min-width: 0;
		margin-left: 16rpx;
	}

	.member-name {
		display: block;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--text-primary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.member-sub {
		display: block;
		font-size: 21rpx;
		color: var(--text-secondary);
		margin-top: 4rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.member-tip {
		display: block;
		font-size: 21rpx;
		color: var(--text-tertiary);
		line-height: 1.6;
		margin-top: 20rpx;
	}

	.member-btn {
		margin-top: 20rpx;
	}
</style>
