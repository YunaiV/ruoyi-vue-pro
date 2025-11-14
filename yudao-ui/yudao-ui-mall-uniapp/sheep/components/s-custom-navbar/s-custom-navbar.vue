<!-- 顶部导航栏 -->
<template>
  <navbar
    :alway="isAlways"
    :back="false"
    bg=""
    :placeholder="isPlaceholder"
    :bgStyles="bgStyles"
    :opacity="isOpacity"
    :sticky="sticky"
  >
    <template #item>
      <view class="nav-box">
        <view class="nav-icon" v-if="showLeftButton">
          <view class="icon-box ss-flex" :class="{ 'inner-icon-box': data.styleType === 'inner' }">
            <view class="icon-button icon-button-left ss-flex ss-row-center" @tap="onClickLeft">
              <text class="sicon-back" v-if="hasHistory" />
              <text class="sicon-home" v-else />
            </view>
            <view class="line"></view>
            <view class="icon-button icon-button-right ss-flex ss-row-center" @tap="onClickRight">
              <text class="sicon-more" />
            </view>
          </view>
        </view>
        <view
          class="nav-item"
          v-for="(item, index) in navList"
          :key="index"
          :style="[parseImgStyle(item)]"
          :class="[{ 'ss-flex ss-col-center ss-row-center': item.type !== 'search' }]"
        >
          <navbar-item :data="item" :width="parseImgStyle(item).width" />
        </view>
      </view>
    </template>
  </navbar>
</template>

<script setup>
  /**
   *  装修组件 - 自定义标题栏
   *
   *
   * @property {Number | String}  alwaysShow = [0,1]			    - 是否常驻
   * @property {Number | String}  styleType = [inner]			   	- 是否沉浸式
   * @property {String | Number} type              - 标题背景模式
   * @property {String} color                  - 页面背景色
   * @property {String} src                    - 页面背景图片
   */
  import { computed, unref } from 'vue';
  import sheep from '@/sheep';
  import Navbar from './components/navbar.vue';
  import NavbarItem from './components/navbar-item.vue';
  import { showMenuTools } from '@/sheep/hooks/useModal';

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    showLeftButton: {
      type: Boolean,
      default: false,
    },
  });
  const hasHistory = sheep.$router.hasHistory();
  const sticky = computed(() => {
    if (props.data.styleType === 'inner') {
      if (props.data.alwaysShow) {
        return false;
      }
    }
    if (props.data.styleType === 'normal') {
      return false;
    }
  });
  const navList = computed(() => {
    // #ifdef MP
    return props.data.mpCells || [];
    // #endif
    return props.data.otherCells || [];
  });
  // 页面宽度
  const windowWidth = sheep.$platform.device.windowWidth;
  // 单元格宽度
  const cell = computed(() => {
    if (unref(navList).length) {
      // 默认宽度为8个格子，微信公众号右上角有胶囊按钮所以是6个格子
      let cell = (windowWidth - 90) / 8;
      // #ifdef MP
      cell = (windowWidth - 80 - unref(sheep.$platform.capsule).width) / 6;
      // #endif
      return cell;
    }
  });
  // 解析位置
  const parseImgStyle = (item) => {
    let obj = {
      width: item.width * cell.value + (item.width - 1) * 10 + 'px',
      left: item.left * cell.value + (item.left + 1) * 10 + 'px',
      'border-radius': item.borderRadius + 'px',
    };
    return obj;
  };
  const isAlways = computed(() =>
    props.data.styleType === 'inner' ? Boolean(props.data.alwaysShow) : true,
  );
  const isOpacity = computed(() =>
    props.data.styleType === 'normal'
      ? false
      : props.showLeftButton
      ? false
      : props.data.styleType === 'inner',
  );
  const isPlaceholder = computed(() => props.data.styleType === 'normal');
  const bgStyles = computed(() => {
    return {
      background:
        props.data.bgType === 'img' && props.data.bgImg
          ? `url(${sheep.$url.cdn(props.data.bgImg)}) no-repeat top center / 100% 100%`
          : props.data.bgColor,
    };
  });

  // 左侧按钮：返回上一页或首页
  function onClickLeft() {
    if (hasHistory) {
      sheep.$router.back();
    } else {
      sheep.$router.go('/pages/index/index');
    }
  }

  // 右侧按钮：打开快捷菜单
  function onClickRight() {
    showMenuTools();
  }
</script>

<style lang="scss" scoped>
  .nav-box {
    width: 750rpx;
    position: relative;
    height: 100%;

    .nav-item {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
    }

    .nav-icon {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      left: 20rpx;

      .inner-icon-box {
        border: 1px solid rgba(#fff, 0.4);
        background: none !important;
      }

      .icon-box {
        background: #ffffff;
        box-shadow: 0px 0px 4rpx rgba(51, 51, 51, 0.08), 0px 4rpx 6rpx 2rpx rgba(102, 102, 102, 0.12);
        border-radius: 30rpx;
        width: 134rpx;
        height: 56rpx;
        margin-left: 8rpx;

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
    }
  }
</style>
