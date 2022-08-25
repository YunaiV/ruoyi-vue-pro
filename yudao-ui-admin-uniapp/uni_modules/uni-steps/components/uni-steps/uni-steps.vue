<template>
	<view class="uni-steps">
		<view :class="[direction==='column'?'uni-steps__column':'uni-steps__row']">
			<view :class="[direction==='column'?'uni-steps__column-text-container':'uni-steps__row-text-container']">
				<view v-for="(item,index) in options" :key="index"
					:class="[direction==='column'?'uni-steps__column-text':'uni-steps__row-text']">
					<text :style="{color:index === active?activeColor:deactiveColor}"
						:class="[direction==='column'?'uni-steps__column-title':'uni-steps__row-title']">{{item.title}}</text>
					<text :style="{color: deactiveColor}"
						:class="[direction==='column'?'uni-steps__column-desc':'uni-steps__row-desc']">{{item.desc}}</text>
				</view>
			</view>
			<view :class="[direction==='column'?'uni-steps__column-container':'uni-steps__row-container']">
				<view :class="[direction==='column'?'uni-steps__column-line-item':'uni-steps__row-line-item']"
					v-for="(item,index) in options" :key="index">
					<view
						:class="[direction==='column'?'uni-steps__column-line':'uni-steps__row-line',direction==='column'?'uni-steps__column-line--before':'uni-steps__row-line--before']"
						:style="{backgroundColor:index<=active&&index!==0?activeColor:index===0?'transparent':deactiveColor}">
					</view>
					<view :class="[direction==='column'?'uni-steps__column-check':'uni-steps__row-check']"
						v-if="index === active">
						<uni-icons :color="activeColor" :type="activeIcon" size="14"></uni-icons>
					</view>
					<view v-else :class="[direction==='column'?'uni-steps__column-circle':'uni-steps__row-circle']"
						:style="{backgroundColor:index<active?activeColor:deactiveColor}"></view>
					<view
						:class="[direction==='column'?'uni-steps__column-line':'uni-steps__row-line',direction==='column'?'uni-steps__column-line--after':'uni-steps__row-line--after']"
						:style="{backgroundColor:index<active&&index!==options.length-1?activeColor:index===options.length-1?'transparent':deactiveColor}">
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	/**
	 * Steps 步骤条
	 * @description 评分组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=34
	 * @property {Number} active 当前步骤
	 * @property {String} direction = [row|column] 当前步骤
	 * 	@value row 横向
	 * 	@value column 纵向
	 * @property {String} activeColor 选中状态的颜色
	 * @property {Array} options 数据源，格式为：[{title:'xxx',desc:'xxx'},{title:'xxx',desc:'xxx'}]
	 */

	export default {
		name: 'UniSteps',
		props: {
			direction: {
				// 排列方向 row column
				type: String,
				default: 'row'
			},
			activeColor: {
				// 激活状态颜色
				type: String,
				default: '#2979FF'
			},
			deactiveColor: {
				// 未激活状态颜色
				type: String,
				default: '#B7BDC6'
			},
			active: {
				// 当前步骤
				type: Number,
				default: 0
			},
			activeIcon: {
				// 当前步骤
				type: String,
				default: 'checkbox-filled'
			},
			options: {
				type: Array,
				default () {
					return []
				}
			} // 数据
		},
		data() {
			return {}
		}
	}
</script>

<style lang="scss">
	$uni-primary: #2979ff !default;
	$uni-border-color:#EDEDED;
	.uni-steps {
		/* #ifndef APP-NVUE */
		display: flex;
		width: 100%;
		/* #endif */
		/* #ifdef APP-NVUE */
		flex: 1;
		/* #endif */
		flex-direction: column;
	}

	.uni-steps__row {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
	}

	.uni-steps__column {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row-reverse;
	}

	.uni-steps__row-text-container {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: flex-end;
		margin-bottom: 8px;
	}

	.uni-steps__column-text-container {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		flex: 1;
	}

	.uni-steps__row-text {
		/* #ifndef APP-NVUE */
		display: inline-flex;
		/* #endif */
		flex: 1;
		flex-direction: column;
	}

	.uni-steps__column-text {
		padding: 6px 0px;
		border-bottom-style: solid;
		border-bottom-width: 1px;
		border-bottom-color: $uni-border-color;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
	}

	.uni-steps__row-title {
		font-size: 14px;
		line-height: 16px;
		text-align: center;
	}

	.uni-steps__column-title {
		font-size: 14px;
		text-align: left;
		line-height: 18px;
	}

	.uni-steps__row-desc {
		font-size: 12px;
		line-height: 14px;
		text-align: center;
	}

	.uni-steps__column-desc {
		font-size: 12px;
		text-align: left;
		line-height: 18px;
	}

	.uni-steps__row-container {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
	}

	.uni-steps__column-container {
		/* #ifndef APP-NVUE */
		display: inline-flex;
		/* #endif */
		width: 30px;
		flex-direction: column;
	}

	.uni-steps__row-line-item {
		/* #ifndef APP-NVUE */
		display: inline-flex;
		/* #endif */
		flex-direction: row;
		flex: 1;
		height: 14px;
		line-height: 14px;
		align-items: center;
		justify-content: center;
	}

	.uni-steps__column-line-item {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		flex: 1;
		align-items: center;
		justify-content: center;
	}

	.uni-steps__row-line {
		flex: 1;
		height: 1px;
		background-color: #B7BDC6;
	}

	.uni-steps__column-line {
		width: 1px;
		background-color: #B7BDC6;
	}

	.uni-steps__row-line--after {
		transform: translateX(1px);
	}

	.uni-steps__column-line--after {
		flex: 1;
		transform: translate(0px, 1px);
	}

	.uni-steps__row-line--before {
		transform: translateX(-1px);
	}

	.uni-steps__column-line--before {
		height: 6px;
		transform: translate(0px, -13px);
	}

	.uni-steps__row-circle {
		width: 5px;
		height: 5px;
		border-radius: 50%;
		background-color: #B7BDC6;
		margin: 0px 3px;
	}

	.uni-steps__column-circle {
		width: 5px;
		height: 5px;
		border-radius: 50%;
		background-color: #B7BDC6;
		margin: 4px 0px 5px 0px;
	}

	.uni-steps__row-check {
		margin: 0px 6px;
	}

	.uni-steps__column-check {
		height: 14px;
		line-height: 14px;
		margin: 2px 0px;
	}
</style>
