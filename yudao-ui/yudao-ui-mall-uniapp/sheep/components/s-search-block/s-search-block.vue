<template>
  <view
    class="search-content ss-flex ss-col-center ss-row-between"
    @tap="click"
    :style="[
      {
        borderRadius: radius + 'px',
        background: elBackground,
        height: height + 'px',
        width: width,
      },
    ]"
    :class="[{ 'border-content': navbar }]"
  >
    <view
      class="ss-flex ss-col-center"
      :class="[placeholderPosition === 'center' ? 'ss-row-center' : 'ss-row-left']"
      v-if="navbar"
      style="width: 100%"
    >
      <view
        class="search-icon _icon-search"
        :style="{ color: fontColor, margin: '0 10rpx' }"
      ></view>
      <view class="search-input ss-line-1" :style="{ color: fontColor }">
        {{ placeholder }}
      </view>
      <!-- 右侧扫一扫图标 -->
      <view
        v-if="showScan"
        class="scan-icon _icon-scan"
        :style="{ color: fontColor }"
        @tap.stop="onScan"
        style="margin-left: auto"
      >
      </view>
    </view>
    <uni-search-bar
      v-if="!navbar"
      class="ss-flex-1"
      :radius="data.borderRadius"
      :placeholder="data.placeholder"
      cancelButton="none"
      clearButton="none"
      @confirm="onSearch"
      v-model="state.searchVal"
    />
    <view class="keyword-link ss-flex">
      <view v-for="(item, index) in data.hotKeywords" :key="index">
        <view
          class="ss-m-r-16"
          :style="[{ color: data.textColor }]"
          @tap.stop="sheep.$router.go('/pages/goods/list', { keyword: item })"
        >
          {{ item }}
        </view>
      </view>
    </view>
    <view v-if="data.hotKeywords && data.hotKeywords.length && navbar" class="ss-flex">
      <button
        class="ss-reset-button keyword-btn"
        v-for="(item, index) in data.hotKeywords"
        :key="index"
        :style="[{ color: data.textColor, marginRight: '10rpx' }]"
      >
        {{ item }}
      </button>
    </view>
  </view>
</template>

<script setup>
  /**
   * 基础组件 - 搜索栏
   *
   * @property {String} elBackground      - 输入框背景色
   * @property {String} iconColor      - 图标颜色
   * @property {String} fontColor        - 字体颜色
   * @property {Number} placeholder      - 默认placeholder
   * @property {Number} topRadius      - 组件上圆角
   * @property {Number} bottomRadius      - 组件下圆角
   *
   * @slot keywords							- 关键字
   * @event {Function} click          - 点击组件时触发
   */

  import { computed, reactive } from 'vue';
  import sheep from '@/sheep';

  // 组件数据
  const state = reactive({
    searchVal: '',
  });

  // 事件页面
  const emits = defineEmits(['click']);

  // 接收参数
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    // 输入框背景色
    elBackground: {
      type: String,
      default: '',
    },
    height: {
      type: Number,
      default: 36,
    },
    // 图标颜色
    iconColor: {
      type: String,
      default: '#b0b3bf',
    },
    // 字体颜色
    fontColor: {
      type: String,
      default: '#b0b3bf',
    },
    // placeholder
    placeholder: {
      type: String,
      default: '这是一个搜索框',
    },
    // placeholder位置（left | center）
    placeholderPosition: {
      type: String,
      default: 'left',
    },
    // 是否显示扫一扫图标
    showScan: {
      type: Boolean,
      default: false,
    },
    radius: {
      type: Number,
      default: 10,
    },
    width: {
      type: String,
      default: '100%',
    },
    navbar: {
      type: Boolean,
      default: true,
    },
  });

  // 点击
  const click = () => {
    emits('click');
  };

  function onSearch(e) {
    if (e.value) {
      sheep.$router.go('/pages/goods/list', { keyword: e.value });
      setTimeout(() => {
        state.searchVal = '';
      }, 100);
    }
  }

  /**
   * 扫一扫功能
   */
  function onScan() {
    uni.scanCode({
      onlyFromCamera: false,
      sound: 'default',
      scanType: ['qrCode', 'barCode'],
      success: (res) => {
        showScanResult(res.result);
      },
      fail: (err) => {
        console.error(err);
        uni.showToast({
          title: err.errMsg === 'scanCode:fail cancel' ? '操作已取消' : '扫码失败',
          icon: 'error',
        });
      },
    });
  }

  /**
   * 检测是否为有效URL
   * @param {string} str 待检测字符串
   * @returns {boolean} 是否为有效URL
   */
  function isValidUrl(str) {
    try {
      // 微信环境专用验证方式
      const url = str.trim();
      return (
        (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('ftp://')) &&
        // 简单长度验证
        url.length >= 10
      );
    } catch {
      return false;
    }
  }

  /**
   * 展示扫码结果并处理用户操作
   * @param {string} text 扫码得到的内容
   */
  function showScanResult(text) {
    const isUrl = isValidUrl(text);
    // 显示结果弹窗
    uni.showModal({
      title: '扫描结果',
      content: text,
      confirmText: isUrl ? '访问' : '复制',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          if (isUrl) {
            handleUrl(text);
          } else {
            handleCopy(text);
          }
        }
      },
    });
  }

  /**
   * 处理URL跳转
   * @param {string} url 要访问的URL
   */
  function handleUrl(url) {
    // 方案1：跳转到webview页面（推荐）
    uni.navigateTo({
      url: `/pages/public/webview?url=${encodeURIComponent(url)}`,
    });

    // 方案2：直接复制链接（备选方案）
    /*
    uni.setClipboardData({
      data: url,
      success: () => {
        uni.showToast({
          title: '链接已复制，请在浏览器打开',
          icon: 'success',
        });
      },
    });
    */
  }

  /**
   * 处理文本复制
   * @param {string} text 要复制的文本
   */
  function handleCopy(text) {
    uni.setClipboardData({
      data: text,
      success: () => {
        uni.showToast({
          title: '已复制到剪贴板',
          icon: 'success',
        });
      },
      fail: () => {
        uni.showToast({
          title: '复制失败，请重试',
          icon: 'error',
        });
      },
    });
  }
</script>

<style lang="scss" scoped>
  .border-content {
    border: 2rpx solid #eee;
  }

  .search-content {
    flex: 1;
    // height: 80rpx;
    position: relative;

    .search-icon {
      font-size: 38rpx;
      margin-right: 20rpx;
    }

    .scan-icon {
      font-size: 38rpx;
      margin-right: 20rpx;
    }

    .keyword-link {
      position: absolute;
      right: 16rpx;
      top: 18rpx;
    }

    .search-input {
      font-size: 28rpx;
    }
  }
</style>
