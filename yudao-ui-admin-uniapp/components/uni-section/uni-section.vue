<template>
	<view class="uni-section">
		<view class="uni-section-header" @click="onClick">
				<view class="uni-section-header__decoration" v-if="type" :class="type" />
        <slot v-else name="decoration"></slot>

        <view class="uni-section-header__content">
          <text :style="{'font-size':titleFontSize,'color':titleColor}" class="uni-section__content-title" :class="{'distraction':!subTitle}">{{ title }}</text>
          <text v-if="subTitle" :style="{'font-size':subTitleFontSize,'color':subTitleColor}" class="uni-section-header__content-sub">{{ subTitle }}</text>
        </view>

        <view class="uni-section-header__slot-right">
          <slot name="right"></slot>
        </view>
		</view>

		<view class="uni-section-content" :style="{padding: _padding}">
			<slot />
		</view>
	</view>
</template>

<script>

	/**
	 * Section 标题栏
	 * @description 标题栏
	 * @property {String} type = [line|circle|square] 标题装饰类型
	 * 	@value line 竖线
	 * 	@value circle 圆形
	 * 	@value square 正方形
	 * @property {String} title 主标题
	 * @property {String} titleFontSize 主标题字体大小
	 * @property {String} titleColor 主标题字体颜色
	 * @property {String} subTitle 副标题
	 * @property {String} subTitleFontSize 副标题字体大小
	 * @property {String} subTitleColor 副标题字体颜色
	 * @property {String} padding 默认插槽 padding
	 */

	export default {
		name: 'UniSection',
    emits:['click'],
		props: {
			type: {
				type: String,
				default: ''
			},
			title: {
				type: String,
				required: true,
				default: ''
			},
      titleFontSize: {
        type: String,
        default: '14px'
      },
			titleColor:{
				type: String,
				default: '#333'
			},
			subTitle: {
				type: String,
				default: ''
			},
      subTitleFontSize: {
        type: String,
        default: '12px'
      },
      subTitleColor: {
        type: String,
        default: '#999'
      },
			padding: {
				type: [Boolean, String],
				default: false
			}
		},
    computed:{
      _padding(){
        if(typeof this.padding === 'string'){
          return this.padding
        }

        return this.padding?'10px':''
      }
    },
		watch: {
			title(newVal) {
				if (uni.report && newVal !== '') {
					uni.report('title', newVal)
				}
			}
		},
    methods: {
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>
<style lang="scss" >
	$uni-primary: #2979ff !default;

	.uni-section {
		background-color: #fff;
    .uni-section-header {
      position: relative;
      /* #ifndef APP-NVUE */
      display: flex;
      /* #endif */
      flex-direction: row;
      align-items: center;
      padding: 12px 10px;
      font-weight: normal;

      &__decoration{
        margin-right: 6px;
        background-color: $uni-primary;
        &.line {
          width: 4px;
          height: 12px;
          border-radius: 10px;
        }

        &.circle {
          width: 8px;
          height: 8px;
          border-top-right-radius: 50px;
          border-top-left-radius: 50px;
          border-bottom-left-radius: 50px;
          border-bottom-right-radius: 50px;
        }

        &.square {
          width: 8px;
          height: 8px;
        }
      }

      &__content {
        /* #ifndef APP-NVUE */
        display: flex;
        /* #endif */
        flex-direction: column;
        flex: 1;
        color: #333;

        .distraction {
          flex-direction: row;
          align-items: center;
        }
        &-sub {
          margin-top: 2px;
        }
      }

      &__slot-right{
        font-size: 14px;
      }
    }

    .uni-section-content{
      font-size: 14px;
    }
	}
</style>
