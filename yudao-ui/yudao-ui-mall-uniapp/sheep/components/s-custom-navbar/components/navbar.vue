<template>
  <su-fixed
    :noFixed="props.noFixed"
    :alway="props.alway"
    :bgStyles="props.bgStyles"
    :val="0"
    :index="props.zIndex"
    noNav
    :bg="props.bg"
    :ui="props.ui"
    :opacity="props.opacity"
    :placeholder="props.placeholder"
    :sticky="props.sticky"
  >
    <su-status-bar />
    <!-- 
      :class="[{ 'border-bottom': !props.opacity && props.bg != 'bg-none' }]"
     -->
    <view class="ui-navbar-box">
      <view
        class="ui-bar"
        :class="
          props.status == '' ? `text-a` : props.status == 'light' ? 'text-white' : 'text-black'
        "
        :style="[{ height: sys_navBar - sys_statusBar + 'px' }]"
      >
        <slot name="item"></slot>
        <view class="right">
          <!-- #ifdef MP -->
          <view :style="[state.capsuleStyle]"></view>
          <!-- #endif -->
        </view>
      </view>
    </view>
  </su-fixed>
</template>

<script setup>
  /**
   * 标题栏 - 基础组件navbar
   *
   * @param {Number}  zIndex = 100  							- 层级
   * @param {Boolean}  back = true 							- 是否返回上一页
   * @param {String}  backtext = ''  							- 返回文本
   * @param {String}  bg = 'bg-white'  						- 公共Class
   * @param {String}  status = ''  							- 状态栏颜色
   * @param {Boolean}  alway = true							- 是否常驻
   * @param {Boolean}  opacity = false  						- 是否开启透明渐变
   * @param {Boolean}  opacityBg = false  					- 开启滑动渐变后，返回按钮是否添加背景
   * @param {Boolean}  noFixed = false  						- 是否浮动
   * @param {String}  ui = ''									- 公共Class
   * @param {Boolean}  capsule = false  						- 是否开启胶囊返回
   * @param {Boolean}  stopBack = false 					    - 是否禁用返回
   * @param {Boolean}  placeholder = true 					- 是否开启占位
   * @param {Object}   bgStyles = {} 					    	- 背景样式
   *
   */

  import { computed, reactive, onBeforeMount } from 'vue';
  import sheep from '@/sheep';

  // 本地数据
  const state = reactive({
    statusCur: '',
    capsuleStyle: {},
    capsuleBack: {},
  });

  const sys_statusBar = sheep.$platform.device.statusBarHeight;
  const sys_navBar = sheep.$platform.navbar;

  const props = defineProps({
    sticky: Boolean,
    zIndex: {
      type: Number,
      default: 100,
    },
    back: {
      //是否返回上一页
      type: Boolean,
      default: true,
    },
    backtext: {
      //返回文本
      type: String,
      default: '',
    },
    bg: {
      type: String,
      default: 'bg-white',
    },
    status: {
      //状态栏颜色 可以选择light dark/其他字符串视为黑色
      type: String,
      default: '',
    },
    // 常驻
    alway: {
      type: Boolean,
      default: true,
    },
    opacity: {
      //是否开启滑动渐变
      type: Boolean,
      default: false,
    },
    opacityBg: {
      //开启滑动渐变后 返回按钮是否添加背景
      type: Boolean,
      default: false,
    },
    noFixed: {
      //是否浮动
      type: Boolean,
      default: false,
    },
    ui: {
      type: String,
      default: '',
    },
    capsule: {
      //是否开启胶囊返回
      type: Boolean,
      default: false,
    },
    stopBack: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: [Boolean],
      default: true,
    },
    bgStyles: {
      type: Object,
      default() {},
    },
  });

  const emits = defineEmits(['navback']);

  onBeforeMount(() => {
    init();
  });

  // 返回
  const onNavback = () => {
    sheep.$router.back();
  };

  // 初始化
  const init = () => {
    state.capsuleStyle = {
      width: sheep.$platform.capsule.width + 'px',
      height: sheep.$platform.capsule.height + 'px',
      margin: '0 ' + (sheep.$platform.device.windowWidth - sheep.$platform.capsule.right) + 'px',
    };

    state.capsuleBack = state.capsuleStyle;
  };
</script>

<style lang="scss" scoped>
  .ui-navbar-box {
    background-color: transparent;
    width: 100%;

    .ui-bar {
      position: relative;
      z-index: 2;
      white-space: nowrap;
      display: flex;
      position: relative;
      align-items: center;
      justify-content: space-between;

      .left {
        @include flex-bar;

        .back {
          @include flex-bar;

          .back-icon {
            @include flex-center;
            width: 56rpx;
            height: 56rpx;
            margin: 0 10rpx;
            font-size: 46rpx !important;

            &.opacityIcon {
              position: relative;
              border-radius: 50%;
              background-color: rgba(127, 127, 127, 0.5);

              &::after {
                content: '';
                display: block;
                position: absolute;
                height: 200%;
                width: 200%;
                left: 0;
                top: 0;
                border-radius: inherit;
                transform: scale(0.5);
                transform-origin: 0 0;
                opacity: 0.1;
                border: 1px solid currentColor;
                pointer-events: none;
              }

              &::before {
                transform: scale(0.9);
              }
            }
          }

          /* #ifdef  MP-ALIPAY */
          ._icon-back {
            opacity: 0;
          }

          /* #endif */
        }

        .capsule {
          @include flex-bar;
          border-radius: 100px;
          position: relative;

          &.dark {
            background-color: rgba(255, 255, 255, 0.5);
          }

          &.light {
            background-color: rgba(0, 0, 0, 0.15);
          }

          &::after {
            content: '';
            display: block;
            position: absolute;
            height: 60%;
            width: 1px;
            left: 50%;
            top: 20%;
            background-color: currentColor;
            opacity: 0.1;
            pointer-events: none;
          }

          &::before {
            content: '';
            display: block;
            position: absolute;
            height: 200%;
            width: 200%;
            left: 0;
            top: 0;
            border-radius: inherit;
            transform: scale(0.5);
            transform-origin: 0 0;
            opacity: 0.1;
            border: 1px solid currentColor;
            pointer-events: none;
          }

          .capsule-back,
          .capsule-home {
            @include flex-center;
            flex: 1;
          }

          &.isFristPage {
            .capsule-back,
            &::after {
              display: none;
            }
          }
        }
      }

      .right {
        @include flex-bar;

        .right-content {
          @include flex;
          flex-direction: row-reverse;
        }
      }

      .center {
        @include flex-center;
        text-overflow: ellipsis;
        text-align: center;
        flex: 1;

        .image {
          display: block;
          height: 36px;
          max-width: calc(100vw - 200px);
        }
      }
    }

    .ui-bar-bg {
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      z-index: 1;
      pointer-events: none;
    }
  }
</style>
