<template>
  <view
    v-if="showPopup"
    class="uni-popup"
    :class="[popupstyle, isDesktop ? 'fixforpc-z-index' : '']"
    :style="[{ zIndex: zIndex }]"
    @touchmove.stop.prevent="clear"
  >
    <view @touchstart="touchstart">
      <uni-transition
        key="1"
        v-if="maskShow"
        name="mask"
        mode-class="fade"
        :styles="maskClass"
        :duration="duration"
        :show="showTrans"
        @click="onTap"
      />
      <uni-transition
        key="2"
        :mode-class="ani"
        name="content"
        :styles="{ ...transClass, ...borderRadius }"
        :duration="duration"
        :show="showTrans"
        @click="onTap"
      >
        <view
          v-if="showPopup"
          class="uni-popup__wrapper"
          :style="[{ backgroundColor: bg }, borderRadius]"
          :class="[popupstyle]"
          @click="clear"
        >
          <uni-icons
            v-if="showClose"
            class="close-icon"
            color="#F6F6F6"
            type="closeempty"
            size="32"
            @click="close"
          ></uni-icons>
          <slot />
        </view>
      </uni-transition>
    </view>
    <!-- #ifdef H5 -->
    <keypress v-if="maskShow" @esc="onTap" />
    <!-- #endif -->
  </view>
  <!-- #ifdef MP -->
  <view v-else style="display: none">
    <slot></slot>
  </view>
  <!-- #endif -->
</template>

<script>
  // #ifdef H5
  import keypress from './keypress.js';
  // #endif

  /**
   * PopUp 弹出层
   * @description 弹出层组件，为了解决遮罩弹层的问题
   * @tutorial https://ext.dcloud.net.cn/plugin?id=329
   * @property {String} type = [top|center|bottom|left|right|message|dialog|share] 弹出方式
   * 	@value top 顶部弹出
   * 	@value center 中间弹出
   * 	@value bottom 底部弹出
   * 	@value left		左侧弹出
   * 	@value right  右侧弹出
   * 	@value message 消息提示
   * 	@value dialog 对话框
   * 	@value share 底部分享示例
   * @property {Boolean} animation = [true|false] 是否开启动画
   * @property {Boolean} maskClick = [true|false] 蒙版点击是否关闭弹窗(废弃)
   * @property {Boolean} isMaskClick = [true|false] 蒙版点击是否关闭弹窗
   * @property {String}  backgroundColor 主窗口背景色
   * @property {String}  maskBackgroundColor 蒙版颜色
   * @property {Boolean} safeArea		   是否适配底部安全区
   * @event {Function} change 打开关闭弹窗触发，e={show: false}
   * @event {Function} maskClick 点击遮罩触发
   */
  import sheep from '@/sheep';

  export default {
    name: 'SuPopup',
    components: {
      // #ifdef H5
      keypress,
      // #endif
    },
    emits: ['change', 'maskClick', 'close'],
    props: {
      // 开启状态
      show: {
        type: Boolean,
        default: false,
      },
      // 顶部，底部时有效
      space: {
        type: Number,
        default: 0,
      },
      // 默认圆角
      round: {
        type: [String, Number],
        default: 0,
      },
      // 是否显示关闭
      showClose: {
        type: Boolean,
        default: false,
      },
      // 开启动画
      animation: {
        type: Boolean,
        default: true,
      },
      // 弹出层类型，可选值，top: 顶部弹出层；bottom：底部弹出层；center：全屏弹出层
      // message: 消息提示 ; dialog : 对话框
      type: {
        type: String,
        default: 'bottom',
      },
      // maskClick
      isMaskClick: {
        type: Boolean,
        default: null,
      },
      // TODO 2 个版本后废弃属性 ，使用 isMaskClick
      maskClick: {
        type: Boolean,
        default: null,
      },
      // 可设置none
      backgroundColor: {
        type: String,
        default: '#ffffff',
      },
      backgroundImage: {
        type: String,
        default: '',
      },
      safeArea: {
        type: Boolean,
        default: true,
      },
      maskBackgroundColor: {
        type: String,
        default: 'rgba(0, 0, 0, 0.4)',
      },
      zIndex: {
        type: [String, Number],
        default: 10075,
      },
    },

    watch: {
      show: {
        handler: function (newValue, oldValue) {
          if (typeof oldValue === 'undefined' && !newValue) {
            return;
          }
          if (newValue) {
            this.open();
          } else {
            this.close();
          }
        },
        immediate: true,
      },
      /**
       * 监听type类型
       */
      type: {
        handler: function (type) {
          if (!this.config[type]) return;
          this[this.config[type]](true);
        },
        immediate: true,
      },
      isDesktop: {
        handler: function (newVal) {
          if (!this.config[newVal]) return;
          this[this.config[this.type]](true);
        },
        immediate: true,
      },
      /**
       * 监听遮罩是否可点击
       * @param {Object} val
       */
      maskClick: {
        handler: function (val) {
          this.mkclick = val;
        },
        immediate: true,
      },
      isMaskClick: {
        handler: function (val) {
          this.mkclick = val;
        },
        immediate: true,
      },
      // H5 下禁止底部滚动
      showPopup(show) {
        // #ifdef H5
        // fix by mehaotian 处理 h5 滚动穿透的问题
        document.getElementsByTagName('body')[0].style.overflow = show ? 'hidden' : 'visible';
        // #endif
      },
    },
    data() {
      return {
        sheep,
        duration: 300,
        ani: [],
        showPopup: false,
        showTrans: false,
        popupWidth: 0,
        popupHeight: 0,
        config: {
          top: 'top',
          bottom: 'bottom',
          center: 'center',
          left: 'left',
          right: 'right',
          message: 'top',
          dialog: 'center',
          share: 'bottom',
        },
        maskClass: {
          position: 'fixed',
          bottom: 0,
          top: 0,
          left: 0,
          right: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
        },
        transClass: {
          position: 'fixed',
          left: 0,
          right: 0,
        },
        maskShow: true,
        mkclick: true,
        popupstyle: this.isDesktop ? 'fixforpc-top' : 'top',
      };
    },
    computed: {
      isDesktop() {
        return this.popupWidth >= 500 && this.popupHeight >= 500;
      },
      bg() {
        if (this.backgroundColor === '' || this.backgroundColor === 'none') {
          return 'transparent';
        }
        return this.backgroundColor;
      },
      borderRadius() {
        if (this.round) {
          if (this.type === 'bottom') {
            return {
              'border-top-left-radius': parseFloat(this.round) + 'px',
              'border-top-right-radius': parseFloat(this.round) + 'px',
            };
          }
          if (this.type === 'center') {
            return {
              'border-top-left-radius': parseFloat(this.round) + 'px',
              'border-top-right-radius': parseFloat(this.round) + 'px',
              'border-bottom-left-radius': parseFloat(this.round) + 'px',
              'border-bottom-right-radius': parseFloat(this.round) + 'px',
            };
          }
          if (this.type === 'top') {
            return {
              'border-bottom-left-radius': parseFloat(this.round) + 'px',
              'border-bottom-right-radius': parseFloat(this.round) + 'px',
            };
          }
        }
      },
    },
    mounted() {
      const fixSize = () => {
        const { windowWidth, windowHeight, windowTop, safeArea, screenHeight, safeAreaInsets } =
          sheep.$platform.device;
        this.popupWidth = windowWidth;
        this.popupHeight = windowHeight + (windowTop || 0);
        // TODO fix by mehaotian 是否适配底部安全区 ,目前微信ios 、和 app ios 计算有差异，需要框架修复
        if (safeArea && this.safeArea) {
          // #ifdef MP-WEIXIN
          this.safeAreaInsets = screenHeight - safeArea.bottom;
          // #endif
          // #ifndef MP-WEIXIN
          this.safeAreaInsets = safeAreaInsets.bottom;
          // #endif
        } else {
          this.safeAreaInsets = 0;
        }
      };
      fixSize();
      // #ifdef H5
      // window.addEventListener('resize', fixSize)
      // this.$once('hook:beforeDestroy', () => {
      // 	window.removeEventListener('resize', fixSize)
      // })
      // #endif
    },
    // #ifndef VUE3
    // TODO vue2
    destroyed() {
      this.setH5Visible();
    },
    // #endif
    // #ifdef VUE3
    // TODO vue3
    unmounted() {
      this.setH5Visible();
    },
    // #endif
    created() {
      // this.mkclick =  this.isMaskClick || this.maskClick
      if (this.isMaskClick === null && this.maskClick === null) {
        this.mkclick = true;
      } else {
        this.mkclick = this.isMaskClick !== null ? this.isMaskClick : this.maskClick;
      }
      if (this.animation) {
        this.duration = 300;
      } else {
        this.duration = 0;
      }
      // TODO 处理 message 组件生命周期异常的问题
      this.messageChild = null;
      // TODO 解决头条冒泡的问题
      this.clearPropagation = false;
      this.maskClass.backgroundColor = this.maskBackgroundColor;
    },
    methods: {
      setH5Visible() {
        // #ifdef H5
        // fix by mehaotian 处理 h5 滚动穿透的问题
        document.getElementsByTagName('body')[0].style.overflow = 'visible';
        // #endif
      },
      /**
       * 公用方法，不显示遮罩层
       */
      closeMask() {
        this.maskShow = false;
      },
      /**
       * 公用方法，遮罩层禁止点击
       */
      disableMask() {
        this.mkclick = false;
      },
      // TODO nvue 取消冒泡
      clear(e) {
        // #ifndef APP-NVUE
        e.stopPropagation();
        // #endif
        this.clearPropagation = true;
      },

      open(direction) {
        // fix by mehaotian 处理快速打开关闭的情况
        if (this.showPopup) {
          clearTimeout(this.timer);
          this.showPopup = false;
        }
        let innerType = ['top', 'center', 'bottom', 'left', 'right', 'message', 'dialog', 'share'];
        if (!(direction && innerType.indexOf(direction) !== -1)) {
          direction = this.type;
        }
        if (!this.config[direction]) {
          console.error('缺少类型：', direction);
          return;
        }
        this[this.config[direction]]();
        this.$emit('change', {
          show: true,
          type: direction,
        });
      },
      close(type) {
        this.showTrans = false;
        this.$emit('change', {
          show: false,
          type: this.type,
        });
        this.$emit('close');
        clearTimeout(this.timer);
        // // 自定义关闭事件
        // this.customOpen && this.customClose()
        this.timer = setTimeout(() => {
          this.showPopup = false;
        }, 300);
      },
      // TODO 处理冒泡事件，头条的冒泡事件有问题 ，先这样兼容
      touchstart() {
        this.clearPropagation = false;
      },

      onTap() {
        if (this.clearPropagation) {
          // fix by mehaotian 兼容 nvue
          this.clearPropagation = false;
          return;
        }
        this.$emit('maskClick');
        if (!this.mkclick) return;
        this.close();
      },
      /**
       * 顶部弹出样式处理
       */
      top(type) {
        this.popupstyle = this.isDesktop ? 'fixforpc-top' : 'top';
        this.ani = ['slide-top'];
        this.transClass = {
          position: 'fixed',
          left: 0,
          right: 0,
          top: this.space + 'px',
          backgroundColor: this.bg,
        };
        // TODO 兼容 type 属性 ，后续会废弃
        if (type) return;
        this.showPopup = true;
        this.showTrans = true;
        this.$nextTick(() => {
          if (this.messageChild && this.type === 'message') {
            this.messageChild.timerClose();
          }
        });
      },
      /**
       * 底部弹出样式处理
       */
      bottom(type) {
        this.popupstyle = 'bottom';
        this.ani = ['slide-bottom'];
        this.transClass = {
          position: 'fixed',
          left: 0,
          right: 0,
          bottom: 0,
          paddingBottom: this.safeAreaInsets + this.space + 'px',
          backgroundColor: this.bg,
        };
        // TODO 兼容 type 属性 ，后续会废弃
        if (type) return;
        this.showPopup = true;
        this.showTrans = true;
      },
      /**
       * 中间弹出样式处理
       */
      center(type) {
        this.popupstyle = 'center';
        this.ani = ['zoom-out', 'fade'];
        this.transClass = {
          position: 'fixed',
          /* #ifndef APP-NVUE */
          display: 'flex',
          flexDirection: 'column',
          /* #endif */
          bottom: 0,
          left: 0,
          right: 0,
          top: 0,
          justifyContent: 'center',
          alignItems: 'center',
        };
        // TODO 兼容 type 属性 ，后续会废弃
        if (type) return;
        this.showPopup = true;
        this.showTrans = true;
      },
      left(type) {
        this.popupstyle = 'left';
        this.ani = ['slide-left'];
        this.transClass = {
          position: 'fixed',
          left: 0,
          bottom: 0,
          top: 0,
          backgroundColor: this.bg,
          /* #ifndef APP-NVUE */
          display: 'flex',
          flexDirection: 'column',
          /* #endif */
        };
        // TODO 兼容 type 属性 ，后续会废弃
        if (type) return;
        this.showPopup = true;
        this.showTrans = true;
      },
      right(type) {
        this.popupstyle = 'right';
        this.ani = ['slide-right'];
        this.transClass = {
          position: 'fixed',
          bottom: 0,
          right: 0,
          top: 0,
          backgroundColor: this.bg,
          /* #ifndef APP-NVUE */
          display: 'flex',
          flexDirection: 'column',
          /* #endif */
        };
        // TODO 兼容 type 属性 ，后续会废弃
        if (type) return;
        this.showPopup = true;
        this.showTrans = true;
      },
    },
  };
</script>
<style lang="scss">
  // 关闭icon
  .close-icon {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    bottom: -80rpx;
    z-index: 100;
  }

  .uni-popup {
    position: fixed;
    /* #ifndef APP-NVUE */
    z-index: 99;

    /* #endif */
    &.top,
    &.left,
    &.right {
      /* #ifdef H5 */
      top: var(--window-top);
      /* #endif */
      /* #ifndef H5 */
      top: 0;
      /* #endif */
    }

    .uni-popup__wrapper {
      /* #ifndef APP-NVUE */
      display: block;
      /* #endif */
      position: relative;
      background: v-bind(backgroundImage) no-repeat;
      background-size: 100% 100%;

      /* iphonex 等安全区设置，底部安全区适配 */
      /* #ifndef APP-NVUE */
      // padding-bottom: constant(safe-area-inset-bottom);
      // padding-bottom: env(safe-area-inset-bottom);
      /* #endif */
      &.left,
      &.right {
        /* #ifdef H5 */
        padding-top: var(--window-top);
        /* #endif */
        /* #ifndef H5 */
        padding-top: 0;
        /* #endif */
        flex: 1;
      }
    }
  }

  .fixforpc-z-index {
    /* #ifndef APP-NVUE */
    z-index: 999;
    /* #endif */
  }

  .fixforpc-top {
    top: 0;
  }
</style>
