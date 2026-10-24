<template>
	<view class="float-tabbar glass">
		<view
			v-for="(item, i) in items"
			:key="i"
			class="tab-item"
			:class="{ active: current === i }"
			@click="go(item, i)"
		>
			<template v-if="i === 2">
					<view class="center-wrap">
						<view class="center-btn">
							<r-icon name="quill-pen-fill" class="center-icon" />
						</view>
						<text class="tab-label center-label">{{ $t(item.key) }}</text>
					</view>
				</template>
				<template v-else>
					<view class="tab-icon-wrap">
						<r-icon :name="item.icon" class="tab-icon" />
					</view>
					<text class="tab-label">{{ $t(item.key) }}</text>
				</template>
		</view>
	</view>
</template>

<script setup>
	const props = defineProps({
		current: {
			type: Number,
			default: 0
		}
	})

	const items = [
			{ key: 'tabbar.home', icon: 'home-5-line', url: '/pages/index/index' },
			{ key: 'tabbar.contacts', icon: 'group-2-line', url: '/pages/contacts/contacts' },
			{ key: 'tabbar.checkin', icon: '', url: '/pages/checkin/checkin' },
			{ key: 'tabbar.bookcity', icon: 'book-2-line', url: '/pages/bookcity/bookcity' },
			{ key: 'tabbar.mine', icon: 'user-3-line', url: '/pages/mine/mine' }
		]

	function go(item, i) {
		if (i === props.current) return
		uni.reLaunch({ url: item.url })
	}
</script>

<style lang="scss" scoped>
	.float-tabbar {
		position: fixed;
		bottom: calc(18px + constant(safe-area-inset-bottom));
		bottom: calc(18px + env(safe-area-inset-bottom));
		left: 16px;
		right: 16px;
		height: 64px;
		border-radius: 24px;
		display: flex;
		align-items: stretch;
		z-index: 999;
		background: var(--tabbar-bg);
		border: 1px solid var(--glass-border);
		box-shadow: 0 12px 34px rgba(27, 32, 48, 0.16);
		overflow: visible;
	}

	.tab-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		position: relative;
	}

	.tab-icon-wrap {
		width: 40px;
		height: 28px;
		border-radius: 10px;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: background 0.2s ease, transform 0.2s ease;
	}

	.tab-icon {
		font-size: 21px;
		line-height: 1;
		opacity: 0.5;
		transition: opacity 0.2s ease, transform 0.2s ease;
	}

	.tab-label {
		font-size: 10px;
		color: var(--text-secondary);
		margin-top: 2px;
		line-height: 1.4;
		white-space: nowrap;
		transition: color 0.2s ease;
	}

	.tab-item.active .tab-icon-wrap {
		background: var(--primary-soft);
	}

	.tab-item.active .tab-icon {
		opacity: 1;
		transform: translateY(-1px);
	}

	.tab-item.active .tab-label {
		color: var(--primary);
		font-weight: 600;
	}

	.center-wrap {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: -28px;
	}

	.center-btn {
		position: relative;
		width: 58px;
		height: 58px;
		border-radius: 50%;
		background: var(--primary-fill);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8px 20px rgba(29, 30, 34, 0.22);
		border: 3px solid var(--bg-primary);
		transition: transform 0.2s ease;
	}

	.center-btn::after {
		content: '';
		position: absolute;
		inset: 5px;
		border-radius: 50%;
		border: 1px solid rgba(255, 255, 255, 0.35);
		pointer-events: none;
	}

	.tab-item:active .center-btn {
		transform: scale(0.94);
	}

	.center-icon {
		font-size: 24px;
		color: #FFFFFF;
		line-height: 1;
	}

	.center-label {
		margin-top: 4px;
		color: var(--primary);
		font-weight: 600;
	}
</style>
