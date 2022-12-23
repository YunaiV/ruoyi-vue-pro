<template>
	<view class="uni-card" :class="{ 'uni-card--full': isFull, 'uni-card--shadow': isShadow,'uni-card--border':border}"
		:style="{'margin':isFull?0:margin,'padding':spacing,'box-shadow':isShadow?shadow:''}">
		<!-- 封面 -->
		<slot name="cover">
			<view v-if="cover" class="uni-card__cover">
				<image class="uni-card__cover-image" mode="widthFix" @click="onClick('cover')" :src="cover"></image>
			</view>
		</slot>
		<slot name="title">
			<view v-if="title || extra" class="uni-card__header">
				<!-- 卡片标题 -->
				<view class="uni-card__header-box" @click="onClick('title')">
					<view v-if="thumbnail" class="uni-card__header-avatar">
						<image class="uni-card__header-avatar-image" :src="thumbnail" mode="aspectFit" />
					</view>
					<view class="uni-card__header-content">
						<text class="uni-card__header-content-title uni-ellipsis">{{ title }}</text>
						<text v-if="title&&subTitle"
							class="uni-card__header-content-subtitle uni-ellipsis">{{ subTitle }}</text>
					</view>
				</view>
				<view class="uni-card__header-extra" @click="onClick('extra')">
					<text class="uni-card__header-extra-text">{{ extra }}</text>
				</view>
			</view>
		</slot>
		<!-- 卡片内容 -->
		<view class="uni-card__content" :style="{padding:padding}" @click="onClick('content')">
			<slot></slot>
		</view>
		<view class="uni-card__actions" @click="onClick('actions')">
			<slot name="actions"></slot>
		</view>
	</view>
</template>

<script>
	/**
	 * Card 卡片
	 * @description 卡片视图组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=22
	 * @property {String} title 标题文字
	 * @property {String} subTitle 副标题
	 * @property {Number} padding 内容内边距
	 * @property {Number} margin 卡片外边距
	 * @property {Number} spacing 卡片内边距
	 * @property {String} extra 标题额外信息
	 * @property {String} cover 封面图（本地路径需要引入）
	 * @property {String} thumbnail 标题左侧缩略图
	 * @property {Boolean} is-full = [true | false] 卡片内容是否通栏，为 true 时将去除padding值
	 * @property {Boolean} is-shadow = [true | false] 卡片内容是否开启阴影
	 * @property {String} shadow 卡片阴影
	 * @property {Boolean} border 卡片边框
	 * @event {Function} click 点击 Card 触发事件
	 */
	export default {
		name: 'UniCard',
		emits: ['click'],
		props: {
			title: {
				type: String,
				default: ''
			},
			subTitle: {
				type: String,
				default: ''
			},
			padding: {
				type: String,
				default: '10px'
			},
			margin: {
				type: String,
				default: '15px'
			},
			spacing: {
				type: String,
				default: '0 10px'
			},
			extra: {
				type: String,
				default: ''
			},
			cover: {
				type: String,
				default: ''
			},
			thumbnail: {
				type: String,
				default: ''
			},
			isFull: {
				// 内容区域是否通栏
				type: Boolean,
				default: false
			},
			isShadow: {
				// 是否开启阴影
				type: Boolean,
				default: true
			},
			shadow: {
				type: String,
				default: '0px 0px 3px 1px rgba(0, 0, 0, 0.08)'
			},
			border: {
				type: Boolean,
				default: true
			}
		},
		methods: {
			onClick(type) {
				this.$emit('click', type)
			}
		}
	}
</script>

<style lang="scss">
	$uni-border-3: #EBEEF5 !default;
	$uni-shadow-base:0 0px 6px 1px rgba($color: #a5a5a5, $alpha: 0.2) !default;
	$uni-main-color: #3a3a3a !default;
	$uni-base-color: #6a6a6a !default;
	$uni-secondary-color: #909399 !default;
	$uni-spacing-sm: 8px !default;
	$uni-border-color:$uni-border-3;
	$uni-shadow: $uni-shadow-base;
	$uni-card-title: 15px;
	$uni-cart-title-color:$uni-main-color;
	$uni-card-subtitle: 12px;
	$uni-cart-subtitle-color:$uni-secondary-color;
	$uni-card-spacing: 10px;
	$uni-card-content-color: $uni-base-color;

	.uni-card {
		margin: $uni-card-spacing;
		padding: 0 $uni-spacing-sm;
		border-radius: 4px;
		overflow: hidden;
		font-family: Helvetica Neue, Helvetica, PingFang SC, Hiragino Sans GB, Microsoft YaHei, SimSun, sans-serif;
		background-color: #fff;
		flex: 1;

		.uni-card__cover {
			position: relative;
			margin-top: $uni-card-spacing;
			flex-direction: row;
			overflow: hidden;
			border-radius: 4px;
			.uni-card__cover-image {
				flex: 1;
				// width: 100%;
				/* #ifndef APP-PLUS */
				vertical-align: middle;
				/* #endif */
			}
		}

		.uni-card__header {
			display: flex;
			border-bottom: 1px $uni-border-color solid;
			flex-direction: row;
			align-items: center;
			padding: $uni-card-spacing;
			overflow: hidden;

			.uni-card__header-box {
				/* #ifndef APP-NVUE */
				display: flex;
				/* #endif */
				flex: 1;
				flex-direction: row;
				align-items: center;
				overflow: hidden;
			}

			.uni-card__header-avatar {
				width: 40px;
				height: 40px;
				overflow: hidden;
				border-radius: 5px;
				margin-right: $uni-card-spacing;
				.uni-card__header-avatar-image {
					flex: 1;
					width: 40px;
					height: 40px;
				}
			}

			.uni-card__header-content {
				/* #ifndef APP-NVUE */
				display: flex;
				/* #endif */
				flex-direction: column;
				justify-content: center;
				flex: 1;
				// height: 40px;
				overflow: hidden;

				.uni-card__header-content-title {
					font-size: $uni-card-title;
					color: $uni-cart-title-color;
					// line-height: 22px;
				}

				.uni-card__header-content-subtitle {
					font-size: $uni-card-subtitle;
					margin-top: 5px;
					color: $uni-cart-subtitle-color;
				}
			}

			.uni-card__header-extra {
				line-height: 12px;

				.uni-card__header-extra-text {
					font-size: 12px;
					color: $uni-cart-subtitle-color;
				}
			}
		}

		.uni-card__content {
			padding: $uni-card-spacing;
			font-size: 14px;
			color: $uni-card-content-color;
			line-height: 22px;
		}

		.uni-card__actions {
			font-size: 12px;
		}
	}

	.uni-card--border {
		border: 1px solid $uni-border-color;
	}

	.uni-card--shadow {
		position: relative;
		/* #ifndef APP-NVUE */
		box-shadow: $uni-shadow;
		/* #endif */
	}

	.uni-card--full {
		margin: 0;
		border-left-width: 0;
		border-left-width: 0;
		border-radius: 0;
	}

	/* #ifndef APP-NVUE */
	.uni-card--full:after {
		border-radius: 0;
	}

	/* #endif */
	.uni-ellipsis {
		/* #ifndef APP-NVUE */
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
		/* #endif */
		/* #ifdef APP-NVUE */
		lines: 1;
		/* #endif */
	}
</style>
