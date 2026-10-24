<template>
	<view class="theme-switch" @click="toggleTheme">
		<view class="track" :class="{ dark: themeState.isDark }">
			<view class="stars" v-if="themeState.isDark">
				<text class="star s1">·</text>
				<text class="star s2">·</text>
				<text class="star s3">·</text>
			</view>
			<view class="thumb">
					<r-icon
						:name="themeState.isDark ? 'moon-line' : 'sun-line'"
						class="thumb-icon"
						:color="themeState.isDark ? '#2C3448' : '#D9A02B'"
					/>
				</view>
		</view>
	</view>
</template>

<script setup>
	import { themeState, toggleTheme } from '@/store/theme.js'
</script>

<style lang="scss" scoped>
	.theme-switch {
		display: inline-flex;
	}

	.track {
		position: relative;
		width: 108rpx;
		height: 60rpx;
		border-radius: 999rpx;
		background: #E4C77E;
		padding: 6rpx;
		box-sizing: border-box;
		transition: background 0.35s ease;
		box-shadow: inset 0 2rpx 5rpx rgba(120, 94, 30, 0.18);
		overflow: hidden;
	}

	.track.dark {
		background: #2C3448;
		box-shadow: inset 0 2rpx 5rpx rgba(0, 0, 0, 0.4);
	}

	.stars {
		position: absolute;
		inset: 0;
	}

	.star {
		position: absolute;
		color: rgba(255, 255, 255, 0.85);
		font-size: 26rpx;
		line-height: 1;
	}

	.s1 { top: 10rpx; left: 18rpx; }
	.s2 { top: 26rpx; left: 34rpx; font-size: 20rpx; }
	.s3 { top: 14rpx; left: 46rpx; font-size: 18rpx; }

	.thumb {
		position: relative;
		z-index: 1;
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 10rpx rgba(27, 32, 48, 0.22);
		transition: transform 0.32s cubic-bezier(0.22, 0.61, 0.36, 1);
	}

	.track.dark .thumb {
		transform: translateX(48rpx);
	}

	.thumb-icon {
		font-size: 26rpx;
		line-height: 1;
	}
</style>
