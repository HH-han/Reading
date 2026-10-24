<template>
	<text class="r-icon" :style="iconStyle">{{ glyph }}</text>
</template>

<script setup>
	import { computed } from 'vue'
	import { ICONS } from './icons.js'

	const props = defineProps({
		name: {
			type: String,
			required: true
		},
		// 数字按 rpx 处理，字符串原样使用（如 '32rpx' / '21px'）；不传则继承父级 font-size
		size: {
			type: [String, Number],
			default: ''
		},
		color: {
			type: String,
			default: ''
		}
	})

	const glyph = computed(() => ICONS[props.name] || '')

	const iconStyle = computed(() => {
		const style = {}
		if (props.size !== '' && props.size !== null) {
			style.fontSize = typeof props.size === 'number' ? props.size + 'rpx' : props.size
		}
		if (props.color) {
			style.color = props.color
		}
		return style
	})
</script>
