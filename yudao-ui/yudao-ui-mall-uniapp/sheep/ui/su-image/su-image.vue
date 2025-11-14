<template>
  <image
    v-if="!state.isError"
    class="su-img"
    :style="customStyle"
    :draggable="false"
    :mode="mode"
    :src="sheep.$url.cdn(src)"
    @tap="onImgPreview"
    @load="onImgLoad"
    @error="onImgError"
  ></image>
</template>

<script setup>
  /**
   * 图片组件
   *
   * @property {String} src 						- 图片地址
   * @property {Number} mode 						- 裁剪方式
   * @property {String} isPreview 		  		- 是否开启预览
   * @property {Number} previewList 				- 预览列表
   * @property {String} current 		  			- 预览首张下标
   *
   * @event {Function} load 						- 图片加载完毕触发
   * @event {Function} error 						- 图片加载错误触发
   *
   */

  import { reactive, computed } from 'vue';
  import sheep from '@/sheep';

  // 组件数据
  const state = reactive({
    isError: false,
    imgHeight: 600,
  });

  // 接收参数
  const props = defineProps({
    src: {
      type: String,
      default: '',
    },
    errorSrc: {
      type: String,
      default: '/static/img/shop/empty_network.png',
    },
    mode: {
      type: String,
      default: 'widthFix',
    },
    isPreview: {
      type: Boolean,
      default: false,
    },
    previewList: {
      type: Array,
      default() {
        return [];
      },
    },
    current: {
      type: Number,
      default: -1,
    },
    height: {
      type: Number,
      default: 0,
    },
    width: {
      type: Number,
      default: 0,
    },
    radius: {
      type: Number,
      default: 0,
    },
  });

  const emits = defineEmits(['load', 'error']);

  const customStyle = computed(() => {
    return {
      height: (props.height || state.imgHeight) + 'rpx',
      width: props.width ? props.width + 'rpx' : '100%',
      borderRadius: props.radius ? props.radius + 'rpx' : '',
    };
  });

  // 图片加载完成
  function onImgLoad(e) {
    if (props.height === 0) {
      state.imgHeight = (e.detail.height / e.detail.width) * 750;
    }
  }

  // 图片加载错误
  function onImgError(e) {
    state.isError = true;
    emits('error', e);
  }

  // 预览图片
  function onImgPreview() {
    if (!props.isPreview) return;
    uni.previewImage({
      urls: props.previewList.length < 1 ? [props.src] : props.previewList,
      current: props.current,
      longPressActions: {
        itemList: ['发送给朋友', '保存图片', '收藏'],
        success: function (data) {
          console.log('选中了第' + (data.tapIndex + 1) + '个按钮,第' + (data.index + 1) + '张图片');
        },
        fail: function (err) {
          console.log(err.errMsg);
        },
      },
    });
  }
</script>

<style lang="scss" scoped>
  .su-img {
    position: relative;
    width: 100%;
    height: 100%;
    display: block;
  }
</style>
