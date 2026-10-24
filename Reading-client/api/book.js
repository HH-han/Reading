import request from '@/utils/request.js'

export function getBookPage(params) {
	return request({ url: '/book/page', data: params })
}

export function getCategories() {
	return request({ url: '/book/categories' })
}

export function getBookDetail(id) {
	return request({ url: '/book/' + id })
}

export function addToShelf(data) {
	return request({ url: '/book/shelf', method: 'POST', data })
}

export function removeFromShelf(bookId) {
	return request({ url: '/book/shelf/' + bookId, method: 'DELETE' })
}

export function getShelf(params) {
	return request({ url: '/book/shelf', data: params })
}

export default { getBookPage, getCategories, getBookDetail, addToShelf, removeFromShelf, getShelf }
