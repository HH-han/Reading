/* 全局自定义弹窗：替代 uni.showModal，统一 UI 与主题 */
import { reactive } from 'vue'
import { t } from '@/utils/i18n.js'

export const modalState = reactive({
	visible: false,
	title: '',
	content: '',
	showCancel: true,
	confirmText: '',
	cancelText: '',
	type: 'info',
	success: null
})

/**
 * 显示弹窗
 * @param {Object|String} options 传入字符串时作为 content
 * @param {String} options.title 标题
 * @param {String} options.content 内容（支持 \n 换行）
 * @param {Boolean} options.showCancel 是否显示取消按钮，默认 true
 * @param {String} options.confirmText 确认按钮文案
 * @param {String} options.cancelText 取消按钮文案
 * @param {String} options.type 样式类型：info | danger | warn | success
 * @param {Function} options.success 回调，参数 { confirm, cancel }
 */
export function showModal(options = {}) {
	const opts = typeof options === 'string' ? { content: options } : options
	modalState.title = opts.title || t('common.tips')
	modalState.content = opts.content || ''
	modalState.showCancel = opts.showCancel !== false
	modalState.confirmText = opts.confirmText || t('common.confirm')
	modalState.cancelText = opts.cancelText || t('common.cancel')
	modalState.type = opts.type || 'info'
	modalState.success = typeof opts.success === 'function' ? opts.success : null
	modalState.visible = true
}

export function closeModal(confirm = false) {
	const cb = modalState.success
	modalState.visible = false
	modalState.success = null
	if (cb) cb({ confirm, cancel: !confirm })
}

export default { modalState, showModal, closeModal }
