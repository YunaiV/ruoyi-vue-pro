<!-- 拼团商品详情 -->
<template>
  <s-layout :onShareAppMessage="shareInfo" navbar="goods">
    <!-- 标题栏 -->
    <detailNavbar />
    <!-- 骨架屏 -->
    <detailSkeleton v-if="state.skeletonLoading" />
    <!-- 下架/售罄提醒 -->
    <s-empty
      v-else-if="
        state.goodsInfo === null ||
        state.activity.status !== 0 ||
        state.activity.endTime < new Date().getTime()
      "
      text="活动不存在或已结束"
      icon="/static/soldout-empty.png"
      showAction
      actionText="返回上一页"
      @clickAction="sheep.$router.back()"
    />
    <block v-else>
      <view class="detail-swiper-selector">
        <!-- 商品图轮播 -->
        <su-swiper
          class="ss-m-b-14"
          isPreview
          :list="state.goodsSwiper"
          dotStyle="tag"
          imageMode="widthFix"
          dotCur="bg-mask-40"
          :seizeHeight="750"
        />

        <!-- 价格+标题 -->
        <view class="title-card detail-card ss-m-y-14 ss-m-x-20 ss-p-x-20 ss-p-y-34">
          <view class="ss-flex ss-row-between ss-m-b-60">
            <view>
              <view class="price-box ss-flex ss-col-bottom ss-m-b-18">
                <view class="price-text ss-m-r-16">
                  {{ fen2yuan(state.activity.price || state.goodsInfo.price) }}
                </view>
                <view class="tig ss-flex ss-col-center">
                  <view class="tig-icon ss-flex ss-col-center ss-row-center">
                    <view class="groupon-tag">
                      <image
                        :src="sheep.$url.static('/static/img/shop/goods/groupon-tag.png')"
                      ></image>
                    </view>
                  </view>
                  <view class="tig-title">拼团价</view>
                </view>
              </view>
              <view class="ss-flex ss-row-between">
                <view class="origin-price ss-flex ss-col-center" v-if="state.goodsInfo.price">
                  单买价：
                  <view class="origin-price-text">
                    {{ fen2yuan(state.goodsInfo.marketPrice) }}
                  </view>
                </view>
              </view>
            </view>

            <view class="countdown-box" v-if="endTime.ms > 0">
              <view class="countdown-title ss-m-b-20">距结束仅剩</view>
              <view class="ss-flex countdown-time">
                <view class="ss-flex countdown-h">{{ endTime.h }}</view>
                <view class="ss-m-x-4">:</view>
                <view class="countdown-num ss-flex ss-row-center">{{ endTime.m }}</view>
                <view class="ss-m-x-4">:</view>
                <view class="countdown-num ss-flex ss-row-center">{{ endTime.s }}</view>
              </view>
            </view>
            <view class="countdown-title" v-else> 活动已结束 </view>
          </view>

          <view class="title-text ss-line-2 ss-m-b-6">{{ state.goodsInfo.name }}</view>
          <view class="subtitle-text ss-line-1">{{ state.goodsInfo.introduction }}</view>
        </view>

        <!-- 功能卡片 -->
        <view class="detail-cell-card detail-card ss-flex-col">
          <!-- 规格 -->
          <detail-cell-sku :sku="state.selectedSku" @tap="state.showSelectSku = true" />
        </view>

        <!-- 参团列表 -->
        <groupon-card-list v-model="state.activity" @join="onJoinGroupon" />

        <!-- 规格与数量弹框 -->
        <s-select-groupon-sku
          :show="state.showSelectSku"
          :goodsInfo="state.goodsInfo"
          :grouponAction="state.grouponAction"
          :grouponNum="state.grouponNum"
          @buy="onBuy"
          @change="onSkuChange"
          @close="onSkuClose"
        />
      </view>

      <!-- 评价 -->
      <detail-comment-card class="detail-comment-selector" :goodsId="state.goodsId" />
      <!-- 详情 -->
      <detail-content-card class="detail-content-selector" :content="state.goodsInfo.description" />

      <!-- 商品tabbar -->
      <detail-tabbar v-model="state.goodsInfo">
        <view class="buy-box ss-flex ss-col-center ss-p-r-20">
          <button
            class="ss-reset-button origin-price-btn ss-flex-col"
            @tap="sheep.$router.go('/pages/goods/index', { id: state.goodsInfo.id })"
          >
            <view class="btn-price">{{ fen2yuan(state.goodsInfo.marketPrice) }}</view>
            <view>原价购买</view>
          </button>
          <button
            class="ss-reset-button btn-tox ss-flex-col"
            @tap="onCreateGroupon"
            :class="
              state.activity.status === 0 && state.goodsInfo.stock !== 0
                ? 'check-btn-box'
                : 'disabled-btn-box'
            "
            :disabled="state.goodsInfo.stock === 0 || state.activity.status !== 0"
          >
            <view class="btn-price">{{
              fen2yuan(
                state.selectedSku.price * state.selectedSku.count ||
                  state.activity.price * state.selectedSku.count ||
                  state.goodsInfo.price * state.selectedSku.count ||
                  state.goodsInfo.price,
              )
            }}</view>
            <view v-if="state.activity.startTime > new Date().getTime()">未开始</view>
            <view v-else-if="state.activity.endTime <= new Date().getTime()">已结束</view>
            <view v-else>
              <view v-if="state.goodsInfo.stock === 0">已售罄</view>
              <view v-else>立即开团</view>
            </view>
          </button>
        </view>
      </detail-tabbar>
    </block>
  </s-layout>
</template>

<script setup>
  import { reactive, computed } from 'vue';
  import { onLoad, onPageScroll } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  import detailNavbar from './components/detail/detail-navbar.vue';
  import detailCellSku from './components/detail/detail-cell-sku.vue';
  import detailTabbar from './components/detail/detail-tabbar.vue';
  import detailSkeleton from './components/detail/detail-skeleton.vue';
  import detailCommentCard from './components/detail/detail-comment-card.vue';
  import detailContentCard from './components/detail/detail-content-card.vue';
  import grouponCardList from './components/groupon/groupon-card-list.vue';
  import { useDurationTime, formatGoodsSwiper, fen2yuan } from '@/sheep/hooks/useGoods';
  import CombinationApi from '@/sheep/api/promotion/combination';
  import SpuApi from '@/sheep/api/product/spu';
  import { SharePageEnum } from '@/sheep/helper/const';

  const headerBg = sheep.$url.css('/static/img/shop/goods/groupon-bg.png');
  const btnBg = sheep.$url.css('/static/img/shop/goods/groupon-btn.png');
  const disabledBtnBg = sheep.$url.css('/static/img/shop/goods/activity-btn-disabled.png');
  const grouponBg = sheep.$url.css('/static/img/shop/goods/groupon-tip-bg.png');

  onPageScroll(() => {});
  const state = reactive({
    skeletonLoading: true, // 骨架屏
    goodsId: 0, // 商品ID
    goodsInfo: {}, // 商品信息
    goodsSwiper: [], // 商品轮播图
    showSelectSku: false, // 显示规格弹框
    selectedSku: {}, // 选中的规格属性
    activity: {}, // 团购活动
    grouponId: 0, // 团购ID
    grouponNum: 0, // 团购人数
    grouponAction: 'create', // 团购操作
    combinationHeadId: null, // 拼团团长编号
  });

  // 倒计时
  const endTime = computed(() => {
    return useDurationTime(state.activity.endTime);
  });

  // 规格变更
  function onSkuChange(e) {
    state.selectedSku = e;
  }

  function onSkuClose() {
    state.showSelectSku = false;
  }

  // 发起拼团
  function onCreateGroupon() {
    state.grouponAction = 'create';
    state.grouponId = 0;
    state.showSelectSku = true;
  }

  /**
   * 去参团
   *
   * @param record 团长的团购记录
   */
  function onJoinGroupon(record) {
    state.grouponAction = 'join';
    state.grouponId = record.activityId;
    state.combinationHeadId = record.id;
    state.grouponNum = record.userSize;
    state.showSelectSku = true;
  }

  // 立即购买
  function onBuy(sku) {
    sheep.$router.go('/pages/order/confirm', {
      data: JSON.stringify({
        order_type: 'goods',
        combinationActivityId: state.activity.id,
        combinationHeadId: state.combinationHeadId,
        items: [
          {
            skuId: sku.id,
            count: sku.count,
          },
        ],
      }),
    });
  }

  // 分享信息
  const shareInfo = computed(() => {
    if (isEmpty(state.activity)) return {};
    return sheep.$platform.share.getShareInfo(
      {
        title: state.activity.name,
        image: sheep.$url.cdn(state.goodsInfo.picUrl),
        params: {
          page: SharePageEnum.GROUPON.value,
          query: state.activity.id,
        },
      },
      {
        type: 'goods', // 商品海报
        title: state.activity.name, // 商品标题
        image: sheep.$url.cdn(state.goodsInfo.picUrl), // 商品主图
        price: fen2yuan(state.goodsInfo.price), // 商品价格
        marketPrice: fen2yuan(state.goodsInfo.marketPrice), // 商品原价
      },
    );
  });

  onLoad(async (options) => {
    // 非法参数
    if (!options.id) {
      state.goodsInfo = null;
      state.skeletonLoading = false;
      return;
    }
    state.grouponId = options.id;
    // 加载活动信息
    const { code, data: activity } = await CombinationApi.getCombinationActivity(state.grouponId);
    if (code !== 0) {
      state.goodsInfo = null;
      state.skeletonLoading = false;
      return;
    }
    state.activity = activity;
    // 加载商品信息
    const { data: spu } = await SpuApi.getSpuDetail(activity.spuId);
    if (code !== 0) {
      state.goodsInfo = null;
      state.skeletonLoading = false;
      return;
    }
    state.goodsId = spu.id;

    // 默认显示最低价
    spu.price = activity.products.reduce((min, product) => {
      return Math.min(min, product.combinationPrice || Infinity);
    }, Infinity);

    // 价格、库存使用活动的
    spu.skus.forEach((sku) => {
      const product = activity.products.find((product) => product.skuId === sku.id);
      if (product) {
        sku.price = product.combinationPrice;
      } else {
        // 找不到可能是没配置，则不能发起秒杀
        sku.stock = 0;
      }
    });

    // 关闭骨架屏
    state.skeletonLoading = false;
    if (code === 0) {
      state.goodsInfo = spu;
      state.grouponNum = activity.userSize;
      state.goodsSwiper = formatGoodsSwiper(state.goodsInfo.sliderPicUrls);
    } else {
      // 未找到商品
      state.goodsInfo = null;
    }
  });
</script>

<style lang="scss" scoped>
  .detail-card {
    background-color: $white;
    margin: 14rpx 20rpx;
    border-radius: 10rpx;
    overflow: hidden;
  }

  // 价格标题卡片
  .title-card {
    width: 710rpx;
    box-sizing: border-box;
    // height: 320rpx;
    background-size: 100% 100%;
    border-radius: 10rpx;
    background-image: v-bind(headerBg);
    background-repeat: no-repeat;

    .price-box {
      .price-text {
        font-size: 30rpx;
        font-weight: 500;
        color: #fff;
        line-height: normal;
        font-family: OPPOSANS;

        &::before {
          content: '￥';
          font-size: 30rpx;
        }
      }
    }
    .origin-price {
      font-size: 24rpx;
      font-weight: 400;
      color: #fff;
      opacity: 0.7;

      .origin-price-text {
        text-decoration: line-through;

        font-family: OPPOSANS;

        &::before {
          content: '￥';
        }
      }
    }

    .tig {
      border: 2rpx solid #ffffff;
      border-radius: 4rpx;
      width: 126rpx;
      height: 38rpx;

      .tig-icon {
        margin-left: -2rpx;
        width: 40rpx;
        height: 40rpx;
        background: #ffffff;
        border-radius: 4rpx 0 0 4rpx;

        .groupon-tag {
          width: 32rpx;
          height: 32rpx;
        }
      }

      .tig-title {
        font-size: 24rpx;
        font-weight: 500;
        line-height: normal;
        color: #ffffff;
        width: 86rpx;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }

    .countdown-title {
      font-size: 26rpx;
      font-weight: 500;
      color: #ffffff;
    }

    .countdown-time {
      font-size: 26rpx;
      font-weight: 500;
      color: #ffffff;
      .countdown-h {
        font-size: 24rpx;
        font-family: OPPOSANS;
        font-weight: 500;
        color: #ffffff;
        padding: 0 4rpx;
        height: 40rpx;
        background: rgba(#000000, 0.1);
        border-radius: 6rpx;
      }
      .countdown-num {
        font-size: 24rpx;
        font-family: OPPOSANS;
        font-weight: 500;
        color: #ffffff;
        width: 40rpx;
        height: 40rpx;
        background: rgba(#000000, 0.1);
        border-radius: 6rpx;
      }
    }

    .title-text {
      font-size: 30rpx;
      font-weight: bold;
      line-height: 42rpx;
      color: #fff;
    }

    .subtitle-text {
      font-size: 26rpx;
      font-weight: 400;
      color: #ffffff;
      line-height: 42rpx;
      opacity: 0.9;
    }
  }

  // 购买
  .buy-box {
    .disabled-btn-box[disabled] {
      background-color: transparent;
    }
    .check-btn-box {
      width: 248rpx;
      height: 80rpx;
      font-size: 24rpx;
      font-weight: 600;
      margin-left: -36rpx;
      background-image: v-bind(btnBg);
      background-repeat: no-repeat;
      background-size: 100% 100%;
      color: #ffffff;
      line-height: normal;
      border-radius: 0px 40rpx 40rpx 0px;
    }
    .disabled-btn-box {
      width: 248rpx;
      height: 80rpx;
      font-size: 24rpx;
      font-weight: 600;
      margin-left: -36rpx;
      background-image: v-bind(disabledBtnBg);
      background-repeat: no-repeat;
      background-size: 100% 100%;
      color: #999999;
      line-height: normal;
      border-radius: 0px 40rpx 40rpx 0px;
    }

    .origin-price-btn {
      width: 236rpx;
      height: 80rpx;
      background: rgba(#ff5651, 0.1);
      color: #ff6000;
      border-radius: 40rpx 0px 0px 40rpx;
      line-height: normal;
      font-size: 24rpx;
      font-weight: 500;

      .btn-title {
        font-size: 28rpx;
      }
    }
    .btn-price {
      font-family: OPPOSANS;

      &::before {
        content: '￥';
      }
    }
    .more-item-box {
      .more-item {
        width: 156rpx;
        height: 58rpx;
        font-size: 26rpx;
        font-weight: 500;
        color: #999999;
        border-radius: 10rpx;
      }
      .more-item-hover {
        background: rgba(#ffefe5, 0.32);
        color: #ff6000;
      }
    }
  }

  .groupon-box {
    background: v-bind(grouponBg) no-repeat;
    background-size: 100% 100%;
  }

  //活动卡片
  .activity-box {
    width: 100%;
    height: 80rpx;
    box-sizing: border-box;
    margin-bottom: 10rpx;

    .activity-title {
      font-size: 26rpx;
      font-weight: 500;
      color: #ffffff;
      line-height: 42rpx;

      .activity-icon {
        width: 38rpx;
        height: 38rpx;
      }
    }

    .activity-go {
      width: 70rpx;
      height: 32rpx;
      background: #ffffff;
      border-radius: 16rpx;
      font-weight: 500;
      color: #ff6000;
      font-size: 24rpx;
      line-height: normal;
    }
  }

  .model-box {
    .title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333333;
    }

    .subtitle {
      font-size: 26rpx;
      font-weight: 500;
      color: #333333;
    }
  }

  image {
    width: 100%;
    height: 100%;
  }
</style>
