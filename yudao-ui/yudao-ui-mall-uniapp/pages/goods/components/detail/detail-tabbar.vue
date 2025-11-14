<!-- 商品详情的底部导航 -->
<template>
  <su-fixed bottom placeholder bg="bg-white">
    <view class="ui-tabbar-box">
      <view class="ui-tabbar ss-flex ss-col-center ss-row-between">
        <view
          v-if="collectIcon"
          class="detail-tabbar-item ss-flex ss-flex-col ss-row-center ss-col-center"
          @tap="onFavorite"
        >
          <block v-if="modelValue.favorite">
            <image
              class="item-icon"
              :src="sheep.$url.static('/static/img/shop/goods/collect_1.gif')"
              mode="aspectFit"
            />
            <view class="item-title">已收藏</view>
          </block>
          <block v-else>
            <image
              class="item-icon"
              :src="sheep.$url.static('/static/img/shop/goods/collect_0.png')"
              mode="aspectFit"
            />
            <view class="item-title">收藏</view>
          </block>
        </view>
        <view
          v-if="serviceIcon"
          class="detail-tabbar-item ss-flex ss-flex-col ss-row-center ss-col-center"
          @tap="onChat"
        >
          <image
            class="item-icon"
            :src="sheep.$url.static('/static/img/shop/goods/message.png')"
            mode="aspectFit"
          />
          <view class="item-title">客服</view>
        </view>
        <view
          v-if="shareIcon"
          class="detail-tabbar-item ss-flex ss-flex-col ss-row-center ss-col-center"
          @tap="showShareModal"
        >
          <image
            class="item-icon"
            :src="sheep.$url.static('/static/img/shop/goods/share.png')"
            mode="aspectFit"
          />
          <view class="item-title">分享</view>
        </view>
        <slot></slot>
      </view>
    </view>
  </su-fixed>
</template>

<script setup>
  /**
   *
   * 底部导航
   *
   * @property {String} bg 			 			- 背景颜色Class
   * @property {String} ui 			 			- 自定义样式Class
   * @property {Boolean} noFixed 		 			- 是否定位
   * @property {Boolean} topRadius 		 		- 上圆角
   */
  import { reactive } from 'vue';
  import sheep from '@/sheep';
  import { showShareModal } from '@/sheep/hooks/useModal';
  import FavoriteApi from '@/sheep/api/product/favorite';

  // 数据
  const state = reactive({});

  // 接收参数
  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
    bg: {
      type: String,
      default: 'bg-white',
    },
    bgStyles: {
      type: Object,
      default() {},
    },
    ui: {
      type: String,
      default: '',
    },

    noFixed: {
      type: Boolean,
      default: false,
    },
    topRadius: {
      type: Number,
      default: 0,
    },
    collectIcon: {
      type: Boolean,
      default: true,
    },
    serviceIcon: {
      type: Boolean,
      default: true,
    },
    shareIcon: {
      type: Boolean,
      default: true,
    },
  });

  async function onFavorite() {
    // 情况一：取消收藏
    if (props.modelValue.favorite) {
      const { code } = await FavoriteApi.deleteFavorite(props.modelValue.id);
      if (code !== 0) {
        return
      }
      sheep.$helper.toast('取消收藏');
      props.modelValue.favorite = false;
    // 情况二：添加收藏
    } else {
      const { code } = await FavoriteApi.createFavorite(props.modelValue.id);
      if (code !== 0) {
        return
      }
      sheep.$helper.toast('收藏成功');
      props.modelValue.favorite = true;
    }
  }

  const onChat = () => {
    sheep.$router.go('/pages/chat/index', {
      id: props.modelValue.id,
    });
  };
</script>

<style lang="scss" scoped>
  .ui-tabbar-box {
    box-shadow: 0px -6px 10px 0px rgba(51, 51, 51, 0.2);
  }
  .ui-tabbar {
    display: flex;
    height: 50px;
    background: #fff;

    .detail-tabbar-item {
      width: 100rpx;

      .item-icon {
        width: 40rpx;
        height: 40rpx;
      }

      .item-title {
        font-size: 20rpx;
        font-weight: 500;
        line-height: 20rpx;
        margin-top: 12rpx;
      }
    }
  }
</style>
