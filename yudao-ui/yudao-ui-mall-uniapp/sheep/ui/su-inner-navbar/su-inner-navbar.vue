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
  >
    <su-status-bar />
    <!-- 
      :class="[{ 'border-bottom': !props.opacity && props.bg != 'bg-none' }]"
     -->
    <view class="ui-navbar-box">
      <view
        class="ui-bar ss-p-x-20"
        :class="state.isDark ? 'text-white' : 'text-black'"
        :style="[{ height: sys_navBar - sys_statusBar + 'px' }]"
      >
        <view class="icon-box ss-flex">
          <view class="icon-button icon-button-left ss-flex ss-row-center" @tap="onClickLeft">
            <text class="sicon-back" v-if="hasHistory" />
            <text class="sicon-home" v-else />
          </view>
          <view class="line"></view>
          <view class="icon-button icon-button-right ss-flex ss-row-center" @tap="onClickRight">
            <text class="sicon-more" />
          </view>
        </view>
        <slot name="center">
          <view class="center navbar-title">{{ title }}</view>
        </slot>
        <!-- #ifdef MP -->
        <view :style="[state.capsuleStyle]"></view>
        <!-- #endif -->
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
   * @param {Boolean}  noFixed = false  						- 是否浮动
   * @param {String}  ui = ''									- 公共Class
   * @param {Boolean}  capsule = false  						- 是否开启胶囊返回
   * @param {Boolean}  stopBack = false 					    - 是否禁用返回
   * @param {Boolean}  placeholder = true 					- 是否开启占位
   * @param {Object}   bgStyles = {} 					    	- 背景样式
   *
   */

  import { computed, reactive, onBeforeMount, ref } from 'vue';
  import sheep from '@/sheep';
  import { onPageScroll } from '@dcloudio/uni-app';
  import { showMenuTools, closeMenuTools } from '@/sheep/hooks/useModal';

  // 本地数据
  const state = reactive({
    statusCur: '',
    capsuleStyle: {},
    capsuleBack: {},
    isDark: true,
  });

  const sys_statusBar = sheep.$platform.device.statusBarHeight;
  const sys_navBar = sheep.$platform.navbar;

  const props = defineProps({
    zIndex: {
      type: Number,
      default: 100,
    },

    title: {
      //返回文本
      type: String,
      default: '',
    },
    bg: {
      type: String,
      default: 'bg-white',
    },
    // 常驻
    alway: {
      type: Boolean,
      default: true,
    },
    opacity: {
      //是否开启滑动渐变
      type: Boolean,
      default: true,
    },
    noFixed: {
      //是否浮动
      type: Boolean,
      default: true,
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
      default: false,
    },
    bgStyles: {
      type: Object,
      default() {},
    },
  });

  const emits = defineEmits(['navback', 'clickLeft']);
  const hasHistory = sheep.$router.hasHistory();

  onBeforeMount(() => {
    init();
  });

  onPageScroll((e) => {
    let top = e.scrollTop;
    state.isDark = top < sheep.$platform.navbar;
  });

  function onClickLeft() {
    if (hasHistory) {
      sheep.$router.back();
    } else {
      sheep.$router.go('/pages/index/index');
    }
    emits('clickLeft');
  }
  function onClickRight() {
    showMenuTools();
  }

  // 初始化
  const init = () => {
    // #ifdef MP-ALIPAY
    my.hideAllFavoriteMenu();
    // #endif
    state.capsuleStyle = {
      width: sheep.$platform.capsule.width + 'px',
      height: sheep.$platform.capsule.height + 'px',
    };

    state.capsuleBack = state.capsuleStyle;
  };
</script>

<style lang="scss" scoped>
  .icon-box {
    box-shadow: 0px 0px 4rpx rgba(51, 51, 51, 0.08), 0px 4rpx 6rpx 2rpx rgba(102, 102, 102, 0.12);
    border-radius: 30rpx;
    width: 134rpx;
    height: 56rpx;
    margin-left: 8rpx;
    border: 1px solid rgba(#fff, 0.4);
    .line {
      width: 2rpx;
      height: 24rpx;
      background: #e5e5e7;
    }
    .sicon-back {
      font-size: 32rpx;
    }
    .sicon-home {
      font-size: 32rpx;
    }
    .sicon-more {
      font-size: 32rpx;
    }
    .icon-button {
      width: 67rpx;
      height: 56rpx;
      &-left:hover {
        background: rgba(0, 0, 0, 0.16);
        border-radius: 30rpx 0px 0px 30rpx;
      }
      &-right:hover {
        background: rgba(0, 0, 0, 0.16);
        border-radius: 0px 30rpx 30rpx 0px;
      }
    }
  }
  .navbar-title {
    font-size: 36rpx;
  }
  .tools-icon {
    font-size: 40rpx;
  }
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
        // text-align: center;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);

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
