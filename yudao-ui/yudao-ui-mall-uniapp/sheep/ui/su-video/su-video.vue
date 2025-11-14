<template>
  <view class="ui-video-wrap">
    <!-- #ifndef APP-PLUS -->
    <video
      :id="`sVideo${uid}`"
      class="radius"
      :style="[{ height: height + 'rpx' }]"
      :src="src"
      controls
      object-fit="contain"
      :enable-progress-gesture="state.enableProgressGesture"
      :initial-time="initialTime"
      x5-video-player-type="h5"
      x-webkit-airplay="allow"
      webkit-playsinline="true"
      @error="videoErrorCallback"
      @timeupdate="timeupdate"
      @play="play"
      @pause="pause"
      @ended="end"
      :poster="poster"
    >
    </video>
    <!-- #endif -->
    <!-- #ifdef APP-PLUS -->
    <dom-video
      ref="domVideo"
      :id="`sVideo${uid}`"
      class="radius"
      :style="[{ height: height + 'rpx' }]"
      object-fit="contain"
      :controls="true"
      :show-progress="false"
      :show-fullscreen-btn="false"
      :show-play-btn="false"
      :show-bottom-progress="false"
      :autoplay="false"
      :src="src"
      @play="play"
      @pause="pause"
      @ended="end"
      :poster="poster"
    />
    <!-- #endif -->
    <!-- <view
      v-show="!state.isplay"
      class="poster-wrap radius"
      :style="{ height: height + 'px' }"
      @click="beforePlay"
    >
      <image class="poster-image" mode="aspectFill" :src="poster" v-if="poster" />
      <view class="play-icon ss-flex ss-row-center ss-col-center">
        <text class="cicon-play-arrow ss-font-40"></text>
      </view>
    </view> -->
  </view>
</template>
<script setup>
  /**
   * 视频组件
   *
   * @property {Number} uid = 0 						- 当前轮播下标,还用来标记视频Id
   * @property {Number} moveX = 0 					- app端轮播滑动距离
   * @property {String} height = 300 					- 高度（rpx)
   * @property {String} width = 750 					- 宽度（rpx)
   * @property {Number} initialTime = 0 				- 指定视频播放位置
   * @property {String} videoSize						- 视频大小
   * @property {String} src 							- 视频播放地址
   * @property {String} poster 						- 视频封面
   *
   *
   */

  import { reactive, nextTick, getCurrentInstance } from 'vue';
  import DomVideo from './components/dom-video.vue';
  import sheep from '@/sheep';
  const vm = getCurrentInstance();

  // 数据
  const state = reactive({
    // #ifdef APP-PLUS
    enableProgressGesture: true, // 手势滑动
    // #endif
    // #ifndef APP-PLUS
    enableProgressGesture: false, // 手势滑动
    // #endif
    showModal: false, // 弹框
  });

  // 接收参数
  const props = defineProps({
    moveX: {
      type: [Number],
      default: 0,
    },
    // 下标索引
    uid: {
      type: [Number, String],
      default: 0,
    },
    // 视频高度
    height: {
      type: Number,
      default: 300,
    },
    // 视频宽度
    width: {
      type: Number,
      default: 750,
    },
    // 指定视频初始播放位置，单位为秒（s）
    initialTime: {
      type: Number,
      default: 0,
    },
    src: {
      type: String,
      default: '',
    },
    poster: {
      type: String,
      default: 'https://img1.baidu.com/it/u=1601695551,235775011&fm=26&fmt=auto',
    },
  });

  // 事件
  const emits = defineEmits(['videoTimeupdate']);

  // 播放进度变化时触发,播放进度传给父组件
  const timeupdate = (e) => {
    emits('videoTimeupdate', e);
  };
  const videoErrorCallback = (e) => {
    console.log('视频错误信息:', e.target.errMsg);
  };
  // 当开始/继续播放时触发play事件
  const play = () => {
    console.log('视频开始');
  };
  // 当暂停播放时触发 pause 事件
  const pause = () => {
    console.log('视频暂停');
  };
  // 视频结束触发end 时间
  const end = () => {
    console.log('视频结束');
  };
  // 开始播放
  const startPlay = () => {
    nextTick(() => {
      const video = uni.createVideoContext(`sVideo${props.index}`, vm);
      video.play();
    });
  };

  //暂停播放
  const pausePlay = () => {
    const video = uni.createVideoContext(`sVideo${props.index}`, vm);
    video.pause();
  };

  // 播放前拦截
  const beforePlay = () => {
    uni.getNetworkType({
      success: (res) => {
        console.log(res.networkType, 'res.networkType');
        const networkType = res.networkType;
        // if (networkType === 'wifi' || networkType === 'ethernet') {
        //   startPlay();
        // } else {
        //   uni.showModal({
        //     title: '提示',
        //     content: `当前为移动网络，播放视频需消耗手机流量，是否继续播放？${networkType}`,
        //     success: (res) => {
        //       if (res.confirm) {
        //         startPlay();
        //       } else {
        //         state.isplay = false;
        //       }
        //     },
        //   });
        //   sheep.$helper.toast('正在消耗流量播放');
        //   startPlay();
        // }
        startPlay();
      },
    });
  };

  // 抛出方法供父组件调用
  defineExpose({
    pausePlay,
  });
</script>
<style lang="scss" scoped>
  .radius {
    width: 100%;
  }

  .ui-video-wrap {
    display: flex;
    align-items: center;
    justify-content: center;

    .poster-wrap {
      position: relative;
      width: 100%;
      height: 100%;

      .poster-image {
        width: 100%;
        height: 100%;
      }

      .play-icon {
        position: absolute;
        left: 50%;
        top: 50%;
        width: 80rpx;
        height: 80rpx;
        transform: translate(-50%, -50%);
        background-color: rgba($color: #000000, $alpha: 0.1);
        border-radius: 50%;
      }
    }
  }
</style>
