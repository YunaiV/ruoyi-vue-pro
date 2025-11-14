<template>
  <su-popup
    :show="showTools"
    @close="handleClose"
  >
    <view class="ss-modal-box ss-flex-col">
      <slot></slot>
      <view class="content ss-flex ss-flex-1">
        <template v-if="toolsMode === 'emoji'">
          <swiper
            class="emoji-swiper"
            :indicator-dots="true"
            circular
            indicator-active-color="#7063D2"
            indicator-color="rgba(235, 231, 255, 1)"
            :autoplay="false"
            :interval="3000"
            :duration="1000"
          >
            <swiper-item v-for="emoji in emojiPage" :key="emoji">
              <view class="ss-flex ss-flex-wrap">
                <image
                  v-for="item in emoji" :key="item"
                  class="emoji-img"
                  :src="sheep.$url.cdn(`/static/img/chat/emoji/${item.file}`)"
                  @tap="onEmoji(item)"
                >
                </image>
              </view>
            </swiper-item>
          </swiper>
        </template>
        <template v-else>
          <view class="image">
            <s-uploader
              file-mediatype="image"
              :imageStyles="{ width: 50, height: 50, border: false }"
              @select="imageSelect({ type: 'image', data: $event })"
            >
              <image
                class="icon"
                :src="sheep.$url.static('/static/img/shop/chat/image.png')"
                mode="aspectFill"
              ></image>
            </s-uploader>
            <view>图片</view>
          </view>
          <view class="goods" @tap="onShowSelect('goods')">
            <image
              class="icon"
              :src="sheep.$url.static('/static/img/shop/chat/goods.png')"
              mode="aspectFill"
            ></image>
            <view>商品</view>
          </view>
          <view class="order" @tap="onShowSelect('order')">
            <image
              class="icon"
              :src="sheep.$url.static('/static/img/shop/chat/order.png')"
              mode="aspectFill"
            ></image>
            <view>订单</view>
          </view>
        </template>
      </view>
    </view>
  </su-popup>
</template>

<script setup>
  /**
   * 聊天工具
   */
  import { emojiPage } from '@/pages/chat/util/emoji';
  import sheep from '@/sheep';

  const props = defineProps({
    // 工具模式
    toolsMode: {
      type: String,
      default: '',
    },
    // 控制工具菜单弹出
    showTools: {
      type: Boolean,
      default: () => false,
    },
  });
  const emits = defineEmits(['onEmoji', 'imageSelect', 'onShowSelect', 'close']);

  // 关闭弹出工具菜单
  function handleClose() {
    emits('close');
  }

  // 选择表情
  function onEmoji(emoji) {
    emits('onEmoji', emoji);
  }

  // 选择图片
  function imageSelect(val) {
    emits('imageSelect', val);
  }

  // 选择商品或订单
  function onShowSelect(mode) {
    emits('onShowSelect', mode);
  }
</script>

<style scoped lang="scss">
  .content {
    width: 100%;
    align-content: space-around;
    border-top: 1px solid #dfdfdf;
    padding: 20rpx 0 0;

    .emoji-swiper {
      width: 100%;
      height: 280rpx;
      padding: 0 20rpx;

      .emoji-img {
        width: 50rpx;
        height: 50rpx;
        display: inline-block;
        margin: 10rpx;
      }
    }

    .image,
    .goods,
    .order {
      width: 33.3%;
      height: 280rpx;
      text-align: center;
      font-size: 24rpx;
      color: #333;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .icon {
        width: 50rpx;
        height: 50rpx;
        margin-bottom: 21rpx;
      }
    }

    :deep() {
      .uni-file-picker__container {
        justify-content: center;
      }

      .file-picker__box {
        display: none;

        &:last-of-type {
          display: flex;
        }
      }
    }
  }
</style>
