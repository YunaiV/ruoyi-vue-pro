<!-- 海报弹窗 -->
<template>
  <su-popup :show="show" round="10" @close="onClosePoster" type="center" class="popup-box">
    <view class="ss-flex-col ss-col-center ss-row-center">
      <image
        v-if="!!painterImageUrl"
        class="poster-img"
        :src="painterImageUrl"
        :style="{
          height: poster.css.height+ 'px',
          width: poster.css.width + 'px',
        }"
        :show-menu-by-longpress="true"
      />
    </view>
    <view
      class="poster-btn-box ss-m-t-20 ss-flex ss-row-between ss-col-center"
      v-if="!!painterImageUrl"
    >
      <button class="cancel-btn ss-reset-button" @tap="onClosePoster">取消</button>
      <button class="save-btn ss-reset-button ui-BG-Main" @tap="onSavePoster">
        {{
          ['wechatOfficialAccount', 'H5'].includes(sheep.$platform.name)
            ? '长按图片保存'
            : '保存图片'
        }}
      </button>
    </view>
    <!--  海报画板：默认隐藏只用来生成海报。生成方式为主动调用  -->
    <l-painter
      isCanvasToTempFilePath
      pathType="url"
      @success="setPainterImageUrl"
      hidden
      ref="painterRef"
    />
  </su-popup>
</template>

<script setup>
  /**
   * 海报生成和展示
   * 提示：小程序码默认跳转首页，由首页进行 spm 参数解析后跳转到对应的分享页面
   * @description 用于生成分享海报，如：分享商品海报。
   * @tutorial https://ext.dcloud.net.cn/plugin?id=2389
   * @property {Boolean} show   弹出层控制
   * @property {Object}  shareInfo 分享信息
   */
  import { reactive, ref, unref } from 'vue';
  import sheep from '@/sheep';
  import { getPosterData } from '@/sheep/components/s-share-modal/canvas-poster/poster';

  const props = defineProps({
    show: {
      type: Boolean,
      default: false,
    },
    shareInfo: {
      type: Object,
      default: () => {
      },
    },
  });

  const poster = reactive({
    css: {
      // 根节点若无尺寸，自动获取父级节点
      width: sheep.$platform.device.windowWidth * 0.9,
      height: 600,
    },
    views: [],
  });

  const emits = defineEmits(['success', 'close']);

  const onClosePoster = () => {
    emits('close');
  };

  const painterRef = ref(); // 海报画板
  const painterImageUrl = ref(); // 海报 url
  // 渲染海报
  const renderPoster = async () => {
    await painterRef.value.render(unref(poster));
  };
  // 获得生成的图片
  const setPainterImageUrl = (path) => {
    painterImageUrl.value = path;
  };
  // 保存海报图片
  const onSavePoster = () => {
    if (['WechatOfficialAccount', 'H5'].includes(sheep.$platform.name)) {
      sheep.$helper.toast('请长按图片保存');
      return;
    }

    // 非H5 保存到相册
    uni.saveImageToPhotosAlbum({
      filePath: painterImageUrl.value,
      success: (res) => {
        onClosePoster();
        sheep.$helper.toast('保存成功');
      },
      fail: (err) => {
        sheep.$helper.toast('保存失败');
        console.log('图片保存失败:', err);
      },
    });
  };

  // 获得海报数据
  async function getPoster() {
    painterImageUrl.value = undefined
    poster.views = await getPosterData({
      width: poster.css.width,
      shareInfo: props.shareInfo,
    });
    await renderPoster();
  }

  defineExpose({
    getPoster,
  });
</script>

<style lang="scss" scoped>
  .popup-box {
    position: relative;
  }

  .poster-title {
    color: #999;
  }

  // 分享海报
  .poster-btn-box {
    width: 600rpx;
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    bottom: -80rpx;

    .cancel-btn {
      width: 240rpx;
      height: 70rpx;
      line-height: 70rpx;
      background: $white;
      border-radius: 35rpx;
      font-size: 28rpx;
      font-weight: 500;
      color: $dark-9;
    }

    .save-btn {
      width: 240rpx;
      height: 70rpx;
      line-height: 70rpx;
      border-radius: 35rpx;
      font-size: 28rpx;
      font-weight: 500;
    }
  }

  .poster-img {
    border-radius: 20rpx;
  }

</style>
