<!-- 装修图文组件：广告魔方 -->
<template>
  <view class="ss-cube-wrap" :style="[parseAdWrap]">
    <view v-for="(item, index) in data.list" :key="index">
      <view
        class="cube-img-wrap"
        :style="[parseImgStyle(item), { margin: data.space + 'px' }]"
        @tap="sheep.$router.go(item.url)"
      >
        <image class="cube-img" :src="sheep.$url.cdn(item.imgUrl)" mode="aspectFill"></image>
      </view>
    </view>
  </view>
</template>
<script setup>
  /**
/**
 * 广告魔方
 *
 * @property {Array<Object>} list 			- 魔方列表
 * @property {Object} styles 				- 组件样式
 * @property {String} background 			- 组件背景色
 * @property {Number} topSpace 				- 组件顶部间距
 * @property {Number} bottomSpace 			- 组件底部间距
 * @property {Number} leftSpace 			- 容器左间距
 * @property {Number} rightSpace 			- 容器右间距
 * @property {Number} imgSpace 				- 图片间距
 * @property {Number} imgTopRadius 			- 图片上圆角
 * @property {Number} imgBottomRadius 		- 图片下圆角
 *
 */

  import { computed, inject, unref } from 'vue';
  import sheep from '@/sheep';

  // 参数
  const props = defineProps({
    data: {
      type: Object,
      default() {},
    },
    styles: {
      type: Object,
      default() {},
    },
  });

  // 单元格大小
  const windowWidth = sheep.$platform.device.windowWidth;
  const cell = computed(() => {
    return (
      (windowWidth -
        ((props.styles.marginLeft || 0) + (props.styles.marginRight || 0) + (props.styles.padding || 0) * 2)) /
      4
    );
  });

  //包裹容器高度
  const parseAdWrap = computed(() => {
    let heightArr = props.data.list.reduce(
      (prev, cur) => (prev.includes(cur.height + cur.top) ? prev : [...prev, cur.height + cur.top]),
      [],
    );
    let heightMax = Math.max(...heightArr);
    return {
      height: heightMax * cell.value + 'px',
      width:
        windowWidth -
        (props.data?.style?.marginLeft +
          props.data?.style?.marginRight +
          props.styles.padding * 2) *
          2 +
        'px',
    };
  });

  // 解析图片大小位置
  const parseImgStyle = (item) => {
    let obj = {
      width: item.width * cell.value - props.data.space + 'px',
      height: item.height * cell.value - props.data.space + 'px',
      left: item.left * cell.value + 'px',
      top: item.top * cell.value + 'px',
      'border-top-left-radius': props.data.borderRadiusTop + 'px',
      'border-top-right-radius': props.data.borderRadiusTop + 'px',
      'border-bottom-left-radius': props.data.borderRadiusBottom + 'px',
      'border-bottom-right-radius': props.data.borderRadiusBottom + 'px',
    };
    return obj;
  };
</script>

<style lang="scss" scoped>
  .ss-cube-wrap {
    position: relative;
    z-index: 2;
    width: 750rpx;
  }

  .cube-img-wrap {
    position: absolute;
    z-index: 3;
    overflow: hidden;
  }

  .cube-img {
    width: 100%;
    height: 100%;
  }
</style>
