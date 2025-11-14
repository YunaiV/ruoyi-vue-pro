<template>
  <view>
    <view class="ui-swiper" :class="[props.mode, props.bg, props.ui]">
      <swiper
        :circular="props.circular"
        :current="state.cur"
        :autoplay="props.autoplay && !state.videoPlaySataus"
        :interval="props.interval"
        :duration="props.duration"
        @transition="transition"
        @animationfinish="animationfinish"
        :style="customStyle"
        @change="swiperChange"
      >
        <swiper-item
          class="swiper-item"
          v-for="(item, index) in props.list"
          :key="index"
          :class="{ cur: state.cur == index }"
          @tap="onSwiperItem(item)"
        >
          <view class="ui-swiper-main">
            <image
              v-if="item.type === 'image'"
              class="swiper-image"
              :mode="props.imageMode"
              :src="item.src"
              width="100%"
              height="100%"
              @load="onImgLoad"
            ></image>
            <su-video
              v-else
              :ref="(el) => (refs.videoRef[`video_${index}`] = el)"
              :poster="sheep.$url.cdn(item.poster)"
              :src="sheep.$url.cdn(item.src)"
              :index="index"
              :moveX="state.moveX"
              :initialTime="item.currentTime || 0"
              :height="seizeHeight"
              @videoTimeupdate="videoTimeupdate"
            ></su-video>
          </view>
        </swiper-item>
      </swiper>
      <template v-if="!state.videoPlaySataus">
        <view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle != 'tag'">
          <view
            class="line-box"
            v-for="(item, index) in props.list"
            :key="index"
            :class="[state.cur == index ? 'cur' : '', props.dotCur]"
          ></view>
        </view>
        <view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle == 'tag'">
          <view
            class="ui-tag radius-lg"
            :class="[props.dotCur]"
            style="pointer-events: none; padding: 0 10rpx"
          >
            <view style="transform: scale(0.7)">{{ state.cur + 1 }} / {{ props.list.length }}</view>
          </view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
  /**
   * 轮播组件
   *
   * @property {Boolean} circular = false  	- 是否采用衔接滑动，即播放到末尾后重新回到开头
   * @property {Boolean} autoplay = true  	- 是否自动切换
   * @property {Number} interval = 5000  		- 自动切换时间间隔
   * @property {Number} duration = 500  		- 滑动动画时长,app-nvue不支持
   * @property {Array} list = [] 				- 轮播数据
   * @property {String} ui = ''  				- 样式class
   * @property {String} mode  				- 模式
   * @property {String} dotStyle  			- 指示点样式
   * @property {String} dotCur= 'ui-BG-Main' 	- 当前指示点样式,默认主题色
   * @property {String} bg  					- 背景
   * @property {String} height = 300  		- 组件高度
   * @property {String} imgHeight = 300   	- 图片高度
   *
   * @example list = [{url:'跳转路径',urlType:'跳转方式',type:'轮播类型',src:'轮播内容地址',poster:'视频必传'}]
   */

  import { reactive, computed } from 'vue';
  import sheep from '@/sheep';
  import { clone } from 'lodash-es';

  // 数据
  const state = reactive({
    imgHeight: 0,
    cur: 0,
    moveX: 0,
    videoPlaySataus: false,
    heightList: [],
  });

  const refs = reactive({
    videoRef: {},
  });

  // 接收参数

  const props = defineProps({
    circular: {
      type: Boolean,
      default: true,
    },
    autoplay: {
      type: Boolean,
      default: false,
    },
    interval: {
      type: Number,
      default: 3000,
    },
    duration: {
      type: Number,
      default: 500,
    },
    mode: {
      type: String,
      default: 'default',
    },
    imageMode: {
      type: String,
      default: 'scaleToFill',
    },
    list: {
      type: Array,
      default() {
        return [];
      },
    },
    dotStyle: {
      type: String,
      default: 'long', //default long tag
    },
    dotCur: {
      type: String,
      default: 'ss-bg-opactity-block',
    },
    bg: {
      type: String,
      default: 'bg-none',
    },
    height: {
      type: Number,
      default: 0,
    },
    imgHeight: {
      type: Number,
      default: 0,
    },
    imgTopRadius: {
      type: Number,
      default: 0,
    },
    imgBottomRadius: {
      type: Number,
      default: 0,
    },
    isPreview: {
      type: Boolean,
      default: false,
    },
    seizeHeight: {
      type: Number,
      default: 200,
    },
  });

  // current 改变时会触发 change 事件
  const swiperChange = (e) => {
    if (e.detail.source !== 'touch' && e.detail.source !== 'autoplay') return;
    state.cur = e.detail.current;
    state.videoPlaySataus = false;
    if (props.list[state.cur].type === 'video') {
      refs.videoRef[`video_${state.cur}`].pausePlay();
    }
  };
  // 点击轮播组件
  const onSwiperItem = (item) => {
    if (item.type === 'video') {
      state.videoPlaySataus = true;
    } else {
      sheep.$router.go(item.url);
      onPreview();
    }
  };

  const onPreview = () => {
    if (!props.isPreview) return;
    let previewImage = clone(props.list);
    previewImage.forEach((item, index) => {
      if (item.type === 'video') {
        previewImage.splice(index, 1);
      }
    });
    uni.previewImage({
      urls:
        previewImage.length < 1
          ? [props.src]
          : previewImage.reduce((pre, cur) => {
              pre.push(cur.src);
              return pre;
            }, []),
      current: state.cur,
      // longPressActions: {
      //   itemList: ['发送给朋友', '保存图片', '收藏'],
      //   success: function (data) {
      //     console.log('选中了第' + (data.tapIndex + 1) + '个按钮,第' + (data.index + 1) + '张图片');
      //   },
      //   fail: function (err) {
      //     console.log(err.errMsg);
      //   },
      // },
    });
  };
  //

  // swiper-item 的位置发生改变时会触发 transition
  const transition = (e) => {
    // #ifdef APP-PLUS
    state.moveX = e.detail.dx;
    // #endif
  };

  // 动画结束时会触发 animationfinish
  const animationfinish = (e) => {
    state.moveX = 0;
  };

  const videoTimeupdate = (e) => {
    props.list[state.cur].currentTime = e.detail.currentTime;
  };

  // 自动计算高度
  const customStyle = computed(() => {
    let height;

    // 固定高度情况
    if (props.height !== 0) {
      height = props.height;
    }

    // 自动高度情况
    if (props.height === 0) {
      // 图片预加载占位高度
      if (state.imgHeight !== 0) {
        height = state.imgHeight;
      } else if (props.seizeHeight !== 0) {
        height = props.seizeHeight;
      }
    }

    return {
      height: height + 'rpx',
    };
  });

  // 计算轮播图片最大高度
  function onImgLoad(e) {
    if (props.height === 0) {
      let newHeight = (e.detail.height / e.detail.width) * 750;
      if (state.imgHeight < newHeight) {
        state.imgHeight = newHeight;
      }
    }
  }
</script>

<style lang="scss" scoped>
  .ui-swiper {
    position: relative;

    .ui-swiper-main {
      width: 100%;
      height: 100%;
    }

    .ui-swiper-main .swiper-image {
      width: 100%;
      height: 100%;
    }

    .ui-swiper-dot {
      position: absolute;
      width: 100%;
      bottom: 20rpx;
      height: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.default .line-box {
        display: inline-flex;
        border-radius: 50rpx;
        width: 6px;
        height: 6px;
        border: 2px solid transparent;
        margin: 0 10rpx;
        opacity: 0.3;
        position: relative;
        justify-content: center;
        align-items: center;

        &.cur {
          width: 8px;
          height: 8px;
          opacity: 1;
          border: 0px solid transparent;
        }

        &.cur::after {
          content: '';
          border-radius: 50rpx;
          width: 4px;
          height: 4px;
          background-color: #fff;
        }
      }

      &.long .line-box {
        display: inline-block;
        border-radius: 100rpx;
        width: 6px;
        height: 6px;
        margin: 0 10rpx;
        opacity: 0.3;
        position: relative;

        &.cur {
          width: 24rpx;
          opacity: 1;
        }

        &.cur::after {
        }
      }

      &.line {
        bottom: 20rpx;

        .line-box {
          display: inline-block;
          width: 30px;
          height: 3px;
          opacity: 0.3;
          position: relative;

          &.cur {
            opacity: 1;
          }
        }
      }

      &.tag {
        justify-content: flex-end;
        position: absolute;
        bottom: 20rpx;
        right: 20rpx;
      }
    }

    &.card {
      .swiper-item {
        width: 610rpx !important;
        left: 70rpx;
        box-sizing: border-box;
        padding: 20rpx 0rpx 60rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.9);
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          height: 100%;
        }
      }

      .swiper-item .ui-swiper-main::before {
        content: '';
        display: block;
        background: inherit;
        filter: blur(5px);
        position: absolute;
        width: 100%;
        height: 100%;
        top: 10rpx;
        left: 10rpx;
        z-index: -1;
        opacity: 0.3;
        transform-origin: 0 0;
        transform: scale(1, 1);
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(1);
        transition: all 0.2s ease-in 0s;
      }

      .ui-swiper-dot.tag {
        position: absolute;
        bottom: 85rpx;
        right: 75rpx;
      }
    }

    &.hotelCard {
      .swiper-item {
        width: 650rpx !important;
        left: 30rpx;
        box-sizing: border-box;
        padding: 0rpx 0rpx 50rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.9);
        opacity: 0.8;
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          width: 100%;
          height: 400rpx;
        }
      }

      .swiper-item .ui-swiper-main::before {
        content: '';
        display: block;
        background: inherit;
        filter: blur(5px);
        position: absolute;
        width: 100%;
        height: 100%;
        top: 10rpx;
        left: 10rpx;
        z-index: -1;
        opacity: 0.3;
        transform-origin: 0 0;
        transform: scale(1, 1);
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(1);
        transition: all 0.2s ease-in 0s;
        opacity: 1;
      }

      .ui-swiper-dot {
        display: none;
      }
    }

    &.hotelDetail {
      .swiper-item {
        width: 690rpx !important;
        left: 30rpx;
        box-sizing: border-box;
        padding: 20rpx 0rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.96);
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          height: 100%;
        }
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(0.96);
        transition: all 0.2s ease-in 0s;
      }
    }
  }
</style>
