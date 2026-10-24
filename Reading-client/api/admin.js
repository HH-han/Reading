import request from '@/utils/request.js'

/* 作业 */
export function getJobPage(params) {
	return request({ url: '/admin/job/page', data: params })
}

export function createJob(data) {
	return request({ url: '/admin/job', method: 'POST', data })
}

export function updateJob(data) {
	return request({ url: '/admin/job', method: 'PUT', data })
}

export function deleteJob(id) {
	return request({ url: '/admin/job/' + id, method: 'DELETE' })
}

/* 读书圈 */
export function getCircleList() {
	return request({ url: '/admin/circle/list' })
}

export function createCircle(data) {
	return request({ url: '/admin/circle', method: 'POST', data })
}

export function updateCircle(data) {
	return request({ url: '/admin/circle', method: 'PUT', data })
}

export function deleteCircle(id) {
	return request({ url: '/admin/circle/' + id, method: 'DELETE' })
}

/* 读书圈成员 */
export function getCircleMembers(circleId, params) {
	return request({ url: '/admin/circle/' + circleId + '/member/page', data: params })
}

export function inviteCircleMember(circleId, userId) {
	return request({ url: '/admin/circle/' + circleId + '/member/' + userId, method: 'POST' })
}

export function removeCircleMember(circleId, userId) {
	return request({ url: '/admin/circle/' + circleId + '/member/' + userId, method: 'DELETE' })
}

/* 读书圈小组 */
export function getGroupList(circleId) {
	return request({ url: '/admin/circle/' + circleId + '/group/list' })
}

export function createGroup(data) {
	return request({ url: '/admin/group', method: 'POST', data })
}

export function updateGroup(data) {
	return request({ url: '/admin/group', method: 'PUT', data })
}

export function deleteGroup(id) {
	return request({ url: '/admin/group/' + id, method: 'DELETE' })
}

export function getGroupMembers(groupId, params) {
	return request({ url: '/admin/group/' + groupId + '/member/page', data: params })
}

export function assignMemberGroup(circleId, userId, groupId) {
	const query = groupId ? '?groupId=' + groupId : ''
	return request({ url: '/admin/circle/' + circleId + '/member/' + userId + '/group' + query, method: 'PUT' })
}

export function getUserPage(params) {
	return request({ url: '/admin/user/page', data: params })
}

/* 规则 */
export function getRuleList(params) {
	return request({ url: '/admin/rule/list', data: params })
}

export function createRule(data) {
	return request({ url: '/admin/rule', method: 'POST', data })
}

export function updateRule(data) {
	return request({ url: '/admin/rule', method: 'PUT', data })
}

export function deleteRule(id) {
	return request({ url: '/admin/rule/' + id, method: 'DELETE' })
}

/* 模板 */
export function getTemplateList() {
	return request({ url: '/admin/template/list' })
}

export function createTemplate(data) {
	return request({ url: '/admin/template', method: 'POST', data })
}

export function updateTemplate(data) {
	return request({ url: '/admin/template', method: 'PUT', data })
}

export function deleteTemplate(id) {
	return request({ url: '/admin/template/' + id, method: 'DELETE' })
}

/* 数据 */
export function proxyCheckin(data) {
	return request({ url: '/admin/checkin', method: 'POST', data })
}

export function updateUserStatus(id, status) {
	return request({ url: '/admin/user/' + id + '/status?status=' + status, method: 'PUT' })
}

export function updateUserPosition(id, position) {
	return request({ url: '/admin/user/' + id + '/position?position=' + encodeURIComponent(position || ''), method: 'PUT' })
}

export function getUserCircles(id) {
	return request({ url: '/admin/user/' + id + '/circle/list' })
}

export function cancelAccount() {
	return request({ url: '/admin/account', method: 'DELETE' })
}

/* 数据管理 - 用户 */
export function getDataUserPage(params) {
	return request({ url: '/admin/data/user/page', data: params })
}

export function createDataUser(data) {
	return request({ url: '/admin/data/user', method: 'POST', data })
}

export function updateDataUser(data) {
	return request({ url: '/admin/data/user', method: 'PUT', data })
}

export function deleteDataUser(id) {
	return request({ url: '/admin/data/user/' + id, method: 'DELETE' })
}

/* 数据管理 - 书籍 */
export function getDataBookPage(params) {
	return request({ url: '/admin/data/book/page', data: params })
}

export function createDataBook(data) {
	return request({ url: '/admin/data/book', method: 'POST', data })
}

export function updateDataBook(data) {
	return request({ url: '/admin/data/book', method: 'PUT', data })
}

export function deleteDataBook(id) {
	return request({ url: '/admin/data/book/' + id, method: 'DELETE' })
}

/* 数据管理 - 章节 */
export function getChapterList(bookId) {
	return request({ url: '/admin/data/book/' + bookId + '/chapter/list' })
}

export function createChapter(data) {
	return request({ url: '/admin/data/chapter', method: 'POST', data })
}

export function updateChapter(data) {
	return request({ url: '/admin/data/chapter', method: 'PUT', data })
}

export function deleteChapter(id) {
	return request({ url: '/admin/data/chapter/' + id, method: 'DELETE' })
}

/* 数据管理 - 打卡记录 */
export function getDataCheckinPage(params) {
	return request({ url: '/admin/data/checkin/page', data: params })
}

export function createDataCheckin(data) {
	return request({ url: '/admin/data/checkin', method: 'POST', data })
}

export function updateDataCheckin(data) {
	return request({ url: '/admin/data/checkin', method: 'PUT', data })
}

export function deleteDataCheckin(id) {
	return request({ url: '/admin/data/checkin/' + id, method: 'DELETE' })
}

/* 职位 */
export function getPositionList(params) {
	return request({ url: '/admin/position/list', data: params })
}

export function createPosition(data) {
	return request({ url: '/admin/position', method: 'POST', data })
}

export function updatePosition(data) {
	return request({ url: '/admin/position', method: 'PUT', data })
}

export function deletePosition(id) {
	return request({ url: '/admin/position/' + id, method: 'DELETE' })
}

export default {
	getJobPage, createJob, updateJob, deleteJob,
	getCircleList, createCircle, updateCircle, deleteCircle,
	getCircleMembers, inviteCircleMember, removeCircleMember,
	getGroupList, createGroup, updateGroup, deleteGroup, getGroupMembers, assignMemberGroup,
	getUserPage,
	getRuleList, createRule, updateRule, deleteRule,
	getTemplateList, createTemplate, updateTemplate, deleteTemplate,
	proxyCheckin, updateUserStatus, updateUserPosition, getUserCircles, cancelAccount,
	getDataUserPage, createDataUser, updateDataUser, deleteDataUser,
	getDataBookPage, createDataBook, updateDataBook, deleteDataBook,
	getChapterList, createChapter, updateChapter, deleteChapter,
	getDataCheckinPage, createDataCheckin, updateDataCheckin, deleteDataCheckin,
	getPositionList, createPosition, updatePosition, deletePosition
}
