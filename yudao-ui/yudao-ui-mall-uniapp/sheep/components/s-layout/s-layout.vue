<template>
  <view
    class="page-app"
    :class="['theme-' + sys?.mode, 'main-' + sys?.theme, 'font-' + sys?.fontSize]"
  >
    <view class="page-main" :style="[bgMain]">
      <!-- 顶部导航栏-情况1：默认通用顶部导航栏 -->
      <su-navbar
        v-if="navbar === 'normal'"
        :title="title"
        statusBar
        :color="color"
        :tools="tools"
        :opacityBgUi="opacityBgUi"
        @search="(e) => emits('search', e)"
        :defaultSearch="defaultSearch"
      />

      <!-- 顶部导航栏-情况2：装修组件导航栏-标准 -->
      <s-custom-navbar
        v-else-if="navbar === 'custom' && navbarMode === 'normal'"
        :data="navbarStyle"
        :showLeftButton="showLeftButton"
      />
      <view class="page-body" :style="[bgBody]">
        <!-- 顶部导航栏-情况3：沉浸式头部 -->
        <su-inner-navbar v-if="navbar === 'inner'" :title="title" />
        <view
          v-if="navbar === 'inner'"
          :style="[{ paddingTop: sheep?.$platform?.navbar + 'px' }]"
        ></view>

        <!-- 顶部导航栏-情况4：装修组件导航栏-沉浸式 -->
        <s-custom-navbar
          v-if="navbar === 'custom' && navbarMode === 'inner'"
          :data="navbarStyle"
          :showLeftButton="showLeftButton"
        />

        <!-- 页面内容插槽 -->
        <slot />

        <!-- 底部导航 -->
        <s-tabbar v-if="tabbar !== ''" :path="tabbar" />
      </view>
    </view>

    <view class="page-modal">
      <!-- 全局授权弹窗 -->
      <s-auth-modal />
      <!-- 全局分享弹窗 -->
      <s-share-modal :shareInfo="shareInfo" />
      <!-- 全局快捷入口 -->
      <s-menu-tools />
    </view>
  </view>
</template>

<script setup>
  /**
   * 模板组件 - 提供页面公共组件，属性，方法
   */
  import { computed, onMounted } from 'vue';
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  // #ifdef MP-WEIXIN
  import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app';
  // #endif

  const props = defineProps({
    title: {
      type: String,
      default: '',
    },
    navbar: {
      type: String,
      default: 'normal',
    },
    opacityBgUi: {
      type: String,
      default: 'bg-white',
    },
    color: {
      type: String,
      default: '',
    },
    tools: {
      type: String,
      default: 'title',
    },
    keyword: {
      type: String,
      default: '',
    },
    navbarStyle: {
      type: Object,
      default: () => ({
        styleType: '',
        type: '',
        color: '',
        src: '',
        list: [],
        alwaysShow: 0,
      }),
    },
    bgStyle: {
      type: Object,
      default: () => ({
        src: '',
        color: 'var(--ui-BG-1)',
      }),
    },
    tabbar: {
      type: [String, Boolean],
      default: '',
    },
    onShareAppMessage: {
      type: [Boolean, Object],
      default: true,
    },
    leftWidth: {
      type: [Number, String],
      default: 100,
    },
    rightWidth: {
      type: [Number, String],
      default: 100,
    },
    defaultSearch: {
      type: String,
      default: '',
    },
    //展示返回按钮
    showLeftButton: {
      type: Boolean,
      default: false,
    },
  });
  const emits = defineEmits(['search']);

  const sysStore = sheep.$store('sys');
  const userStore = sheep.$store('user');
  const appStore = sheep.$store('app');
  const modalStore = sheep.$store('modal');
  const sys = computed(() => sysStore);

  // 导航栏模式(因为有自定义导航栏 需要计算)
  const navbarMode = computed(() => {
    if (props.navbar === 'normal' || props.navbarStyle.styleType === 'normal') {
      return 'normal';
    }
    return 'inner';
  });

  // 背景1
  const bgMain = computed(() => {
    if (navbarMode.value === 'inner') {
      return {
        background: `${props.bgStyle.backgroundColor || props.bgStyle.color} url(${sheep.$url.cdn(
          props.bgStyle.backgroundImage,
        )}) no-repeat top center / 100% auto`,
      };
    }
    return {};
  });

  // 背景2
  const bgBody = computed(() => {
    if (navbarMode.value === 'normal') {
      return {
        background: `${props.bgStyle.backgroundColor || props.bgStyle.color} url(${sheep.$url.cdn(
          props.bgStyle.backgroundImage,
        )}) no-repeat top center / 100% auto`,
      };
    }
    return {};
  });

  // 分享信息
  const shareInfo = computed(() => {
    if (props.onShareAppMessage === true) {
      return sheep.$platform.share.getShareInfo();
    } else {
      if (!isEmpty(props.onShareAppMessage)) {
        sheep.$platform.share.updateShareInfo(props.onShareAppMessage);
        return props.onShareAppMessage;
      }
    }
    return {};
  });

  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
  });
  // 微信小程序分享好友
  onShareAppMessage(() => {
    return {
      title: shareInfo.value.title,
      path: shareInfo.value.forward.path,
      imageUrl: shareInfo.value.image,
    };
  });
  // 微信小程序分享朋友圈
  onShareTimeline(() => {
    return {
      title: shareInfo.value.title,
      query: shareInfo.value.forward.path,
      imageUrl: shareInfo.value.image,
    };
  });
  // #endif

  // 组件中使用 onMounted 监听页面加载，不是页面组件不使用 onShow
  onMounted(()=>{
    if (!isEmpty(shareInfo.value)) {
      sheep.$platform.share.updateShareInfo(shareInfo.value);
    }
  })
</script>

<style lang="scss" scoped>
  .page-app {
    position: relative;
    color: var(--ui-TC);
    background-color: var(--ui-BG-1) !important;
    z-index: 2;
    display: flex;
    width: 100%;
    height: 100vh;

    .page-main {
      position: absolute;
      z-index: 1;
      width: 100%;
      min-height: 100%;
      display: flex;
      flex-direction: column;

      .page-body {
        width: 100%;
        position: relative;
        z-index: 1;
        flex: 1;
      }

      .page-img {
        width: 100vw;
        height: 100vh;
        position: absolute;
        top: 0;
        left: 0;
        z-index: 0;
      }
    }
  }
</style>
