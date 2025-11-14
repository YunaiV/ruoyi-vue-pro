<template>
  <view>
    <s-layout :onShareAppMessage="shareInfo" navbar="goods">
      <!-- 标题栏 -->
      <detailNavbar />

      <!-- 骨架屏 -->
      <detailSkeleton v-if="state.skeletonLoading" />
      <!-- 下架/售罄提醒 -->
      <s-empty
        v-else-if="state.goodsInfo === null"
        text="商品不存在或已下架"
        icon="/static/soldout-empty.png"
        showAction
        actionText="再逛逛"
        actionUrl="/pages/goods/list"
      />
      <block v-else>
        <view class="detail-swiper-selector">
          <!-- 商品轮播图  -->
          <su-swiper
            class="ss-m-b-14"
            isPreview
            :list="formatGoodsSwiper(state.goodsInfo.sliderPicUrls)"
            otStyle="tag"
            imageMode="widthFix"
            dotCur="bg-mask-40"
            :seizeHeight="750"
          />
          <!-- 限时折扣/会员价的优惠信息 -->
          <view
            class="discount"
            v-if="
              state.settlementSku && state.settlementSku.id && state.settlementSku.promotionPrice
            "
          >
            <image class="disImg" :src="sheep.$url.static('/static/img/shop/goods/dis.png')" />
            <view class="discountCont">
              <view class="disContT">
                <view class="disContT1">
                  <view class="disContT1P">
                    ￥{{ fen2yuan(state.settlementSku.promotionPrice) }}
                  </view>
                  <view class="disContT1End">
                    直降￥
                    {{ fen2yuan(state.settlementSku.price - state.settlementSku.promotionPrice) }}
                  </view>
                </view>
                <view class="disContT2" v-if="state.settlementSku.promotionType === 4">
                  限时折扣
                </view>
                <view class="disContT2" v-else-if="state.settlementSku.promotionType === 6">
                  会员折扣
                </view>
              </view>
              <view class="disContB">
                <view class="disContB1">
                  价格：￥{{ fen2yuan(state.settlementSku.price) }} 丨 剩余：
                  {{ state.settlementSku.stock }}
                </view>
                <view class="disContB2" v-if="state.settlementSku.promotionEndTime > 0">
                  距结束仅剩
                  <s-count-down
                    :tipText="' '"
                    :bgColor="bgColor"
                    :dayText="':'"
                    :hourText="':'"
                    :minuteText="':'"
                    :secondText="' '"
                    :datatime="state.settlementSku.promotionEndTime / 1000"
                    :isDay="false"
                  />
                </view>
              </view>
            </view>
          </view>
          <!-- 价格+标题 -->
          <view class="title-card detail-card ss-p-y-30 ss-p-x-20">
            <!-- 没有限时折扣/会员价的优惠信息时，展示的价格信息 -->
            <view
              class="ss-flex ss-row-between ss-col-center ss-m-b-26"
              v-if="!state.settlementSku.promotionPrice"
            >
              <view class="price-box ss-flex ss-col-bottom">
                <view class="price-text ss-m-r-16">
                  {{ fen2yuan(state.selectedSku.price || state.goodsInfo.price) }}
                </view>
                <view
                  class="origin-price-text"
                  v-if="state.goodsInfo.marketPrice > state.goodsInfo.price"
                >
                  {{ fen2yuan(state.selectedSku.marketPrice || state.goodsInfo.marketPrice) }}
                </view>
              </view>
              <view class="sales-text">
                {{ formatSales('exact', state.goodsInfo.salesCount) }}
              </view>
            </view>
            <view class="discounts-box ss-flex ss-row-between ss-m-b-28">
              <!-- 查看优惠劵的描述 -->
              <view
                class="tag ss-m-r-10"
                v-for="coupon in state.couponInfo.slice(0, 1)"
                :key="coupon.id"
                @tap="onOpenActivity"
              >
                [劵]满{{ fen2yuanSimple(coupon.usePrice) }}元{{
                  coupon.discountType === 1
                    ? '减' + fen2yuanSimple(coupon.discountPrice) + '元'
                    : '打' + formatDiscountPercent(coupon.discountPercent) + '折'
                }}
              </view>
              <!-- 查看满减送的描述 -->
              <div class="tag-content">
                <view class="tag-box ss-flex">
                  <!-- 最多打印 3 条，所以需要扣除优惠劵已打印的 -->
                  <view
                    v-for="item in getRewardActivityRuleItemDescriptions(
                      state.rewardActivity,
                    ).slice(0, 3 - state.couponInfo.slice(0, 1).length)"
                    :key="item"
                    class="tag ss-m-r-10"
                    @tap="onOpenActivity"
                  >
                    <text>{{ item }}</text>
                  </view>
                </view>
              </div>
              <!-- 领取优惠劵的按钮 -->
              <view
                class="get-coupon-box ss-flex ss-col-center ss-m-l-20"
                @tap="onOpenActivity"
                v-if="state.couponInfo.length"
              >
                <view class="discounts-title ss-m-r-8">领券</view>
                <text class="cicon-forward"></text>
              </view>
            </view>
            <view class="title-text ss-line-2 ss-m-b-6">{{ state.goodsInfo.name }}</view>
            <view class="subtitle-text ss-line-1">{{ state.goodsInfo.introduction }}</view>
          </view>

          <!-- 功能卡片 -->
          <view class="detail-cell-card detail-card ss-flex-col">
            <detail-cell-sku
              v-model="state.selectedSku.goods_sku_text"
              :sku="state.selectedSku"
              @tap="state.showSelectSku = true"
            />
          </view>

          <!-- 规格与数量弹框 -->
          <s-select-sku
            :goodsInfo="state.goodsInfo"
            :show="state.showSelectSku"
            @addCart="onAddCart"
            @buy="onBuy"
            @change="onSkuChange"
            @close="state.showSelectSku = false"
          />
        </view>

        <!-- 评价 -->
        <detail-comment-card class="detail-comment-selector" :goodsId="state.goodsId" />
        <!-- 详情 -->
        <detail-content-card
          class="detail-content-selector"
          :content="state.goodsInfo.description"
        />

        <!-- 活动跳转：拼团/秒杀/砍价活动 -->
        <detail-activity-tip
          v-if="state.activityList.length > 0"
          :activity-list="state.activityList"
        />

        <!-- 详情 tabbar -->
        <detail-tabbar v-model="state.goodsInfo">
          <view class="buy-box ss-flex ss-col-center ss-p-r-20" v-if="state.goodsInfo.stock > 0">
            <button
              class="ss-reset-button add-btn ui-Shadow-Main"
              @tap="state.showSelectSku = true"
            >
              加入购物车
            </button>
            <button
              class="ss-reset-button buy-btn ui-Shadow-Main"
              @tap="state.showSelectSku = true"
            >
              立即购买
            </button>
          </view>
          <view class="buy-box ss-flex ss-col-center ss-p-r-20" v-else>
            <button class="ss-reset-button disabled-btn" disabled> 已售罄 </button>
          </view>
        </detail-tabbar>

        <!-- 满减送/限时折扣活动弹窗 -->
        <s-activity-pop
          v-model="state"
          :show="state.showActivityModel"
          @close="state.showActivityModel = false"
          @get="onTakeCoupon"
        />
      </block>
    </s-layout>
  </view>
</template>

<script setup>
  import { reactive, computed } from 'vue';
  import { onLoad, onPageScroll } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CouponApi from '@/sheep/api/promotion/coupon';
  import ActivityApi from '@/sheep/api/promotion/activity';
  import FavoriteApi from '@/sheep/api/product/favorite';
  import RewardActivityApi from '@/sheep/api/promotion/rewardActivity';
  import {
    formatSales,
    formatGoodsSwiper,
    fen2yuan,
    fen2yuanSimple,
    formatDiscountPercent,
    getRewardActivityRuleItemDescriptions,
  } from '@/sheep/hooks/useGoods';
  import detailNavbar from './components/detail/detail-navbar.vue';
  import detailCellSku from './components/detail/detail-cell-sku.vue';
  import detailTabbar from './components/detail/detail-tabbar.vue';
  import detailSkeleton from './components/detail/detail-skeleton.vue';
  import detailCommentCard from './components/detail/detail-comment-card.vue';
  import detailContentCard from './components/detail/detail-content-card.vue';
  import detailActivityTip from './components/detail/detail-activity-tip.vue';
  import { isEmpty } from 'lodash-es';
  import SpuApi from '@/sheep/api/product/spu';

  onPageScroll(() => {});
  import OrderApi from '@/sheep/api/trade/order';
  import { SharePageEnum } from '@/sheep/helper/const';

  const bgColor = {
    bgColor: '#E93323',
    Color: '#fff',
    width: '44rpx',
    timeTxtwidth: '16rpx',
    isDay: true,
  };
  const isLogin = computed(() => sheep.$store('user').isLogin);
  const state = reactive({
    goodsId: 0,
    skeletonLoading: true, // SPU 加载中
    goodsInfo: {}, // SPU 信息
    showSelectSku: false, // 是否展示 SKU 选择弹窗
    selectedSku: {}, // 选中的 SKU
    settlementSku: {}, // 结算的 SKU：由于 selectedSku 不进行默认选中，所以初始使用结算价格最低的 SKU 作为基础展示
    showModel: false, // 是否展示 Coupon 优惠劵的弹窗
    couponInfo: [], // 可领取的 Coupon 优惠劵的列表
    showActivityModel: false, // 【满减送/限时折扣】是否展示 Activity 营销活动的弹窗
    rewardActivity: {}, // 【满减送】活动
    activityList: [], // 【秒杀/拼团/砍价】可参与的 Activity 营销活动的列表
  });

  // 规格变更
  function onSkuChange(e) {
    state.selectedSku = e;
    state.settlementSku = e;
  }

  // 添加购物车
  function onAddCart(e) {
    if (!e.id) {
      sheep.$helper.toast('请选择商品规格');
      return;
    }
    sheep.$store('cart').add(e);
  }

  // 立即购买
  function onBuy(e) {
    if (!e.id) {
      sheep.$helper.toast('请选择商品规格');
      return;
    }
    sheep.$router.go('/pages/order/confirm', {
      data: JSON.stringify({
        items: [
          {
            skuId: e.id,
            count: e.goods_num,
            categoryId: state.goodsInfo.categoryId,
          },
        ],
      }),
    });
  }

  // 打开营销弹窗
  function onOpenActivity() {
    state.showActivityModel = true;
  }

  // 立即领取优惠劵
  async function onTakeCoupon(id) {
    const { code } = await CouponApi.takeCoupon(id);
    if (code !== 0) {
      return;
    }
    uni.showToast({
      title: '领取成功',
    });
    setTimeout(() => {
      getCoupon();
    }, 1000);
  }

  const shareInfo = computed(() => {
    if (isEmpty(state.goodsInfo)) return {};
    return sheep.$platform.share.getShareInfo(
      {
        title: state.goodsInfo.name,
        image: sheep.$url.cdn(state.goodsInfo.picUrl),
        desc: state.goodsInfo.introduction,
        params: {
          page: SharePageEnum.GOODS.value,
          query: state.goodsInfo.id,
        },
      },
      {
        type: 'goods', // 商品海报
        title: state.goodsInfo.name, // 商品名称
        image: sheep.$url.cdn(state.goodsInfo.picUrl), // 商品主图
        price: fen2yuan(state.goodsInfo.price), // 商品价格
        original_price: fen2yuan(state.goodsInfo.marketPrice), // 商品原价
      },
    );
  });

  async function getCoupon() {
    const { code, data } = await CouponApi.getCouponTemplateList(state.goodsId, 2, 10);
    if (code === 0) {
      state.couponInfo = data;
    }
  }

  async function getSettlementByIds(ids) {
    let { data, code } = await OrderApi.getSettlementProduct(ids);
    if (code !== 0 || data.length !== 1) {
      return;
    }
    data = data[0];

    // 补充 SKU 的价格信息
    state.goodsInfo.skus.forEach((sku) => {
      data.skus.forEach((item) => {
        if (sku.id === item.id) {
          sku.promotionType = item.promotionType;
          sku.promotionPrice = item.promotionPrice;
          sku.promotionId = item.promotionId;
          sku.promotionEndTime = item.promotionEndTime;
        }
      });
    });

    // 选择有 promotionPrice 且最小的
    state.settlementSku = state.goodsInfo.skus
      .filter((sku) => sku.stock > 0 && sku.promotionPrice > 0)
      .reduce((prev, curr) => (prev.promotionPrice < curr.promotionPrice ? prev : curr), []);

    // 设置满减送活动
    if (data.rewardActivity) {
      state.rewardActivity = data.rewardActivity;
      //获取活动时间
      getActivityTime(state.rewardActivity.id);
    }
  }

  //获取活动时间
  async function getActivityTime(id) {
    const { code, data } = await RewardActivityApi.getRewardActivity(id);
    if (code === 0) {
      // console.log('获取到的活动 数据', data)
      state.rewardActivity.startTime = data.startTime;
      state.rewardActivity.endTime = data.endTime;
    }
  }

  onLoad((options) => {
    // 非法参数
    if (!options.id) {
      state.goodsInfo = null;
      state.skeletonLoading = false;
      return;
    }
    state.goodsId = options.id;
    // 1. 加载商品信息
    SpuApi.getSpuDetail(state.goodsId).then((res) => {
      if (res.code !== 0 || !res.data) {
        state.goodsInfo = null;
        state.skeletonLoading = false;
        return;
      }
      // 加载到商品
      state.skeletonLoading = false;
      state.goodsInfo = res.data;
      // 获取结算信息
      getSettlementByIds(state.goodsId);
      // 加载是否收藏
      if (isLogin.value) {
        FavoriteApi.isFavoriteExists(state.goodsId, 'goods').then((res) => {
          if (res.code !== 0) {
            return;
          }
          state.goodsInfo.favorite = res.data;
        });
      }
    });

    // 2. 加载优惠劵信息
    getCoupon();

    // 3. 加载营销活动信息
    ActivityApi.getActivityListBySpuId(state.goodsId).then((res) => {
      if (res.code !== 0) {
        return;
      }
      state.activityList = res.data;
    });
  });
</script>

<style lang="scss" scoped>
  .detail-card {
    background-color: #ffff;
    margin: 14rpx 20rpx;
    border-radius: 10rpx;
    overflow: hidden;
  }

  // 价格标题卡片
  .title-card {
    .price-box {
      .price-text {
        font-size: 42rpx;
        font-weight: 500;
        color: #ff3000;
        line-height: 30rpx;
        font-family: OPPOSANS;

        &::before {
          content: '￥';
          font-size: 30rpx;
        }
      }

      .origin-price-text {
        font-size: 26rpx;
        font-weight: 400;
        text-decoration: line-through;
        color: $gray-c;
        font-family: OPPOSANS;

        &::before {
          content: '￥';
        }
      }
    }

    .sales-text {
      font-size: 26rpx;
      font-weight: 500;
      color: $gray-c;
    }

    .discounts-box {
      .tag-content {
        flex: 1;
        min-width: 0;
        white-space: nowrap;
      }

      .tag-box {
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .tag {
        flex-shrink: 0;
        padding: 4rpx 10rpx;
        font-size: 24rpx;
        font-weight: 500;
        border-radius: 4rpx;
        color: var(--ui-BG-Main);
        background: var(--ui-BG-Main-tag);
      }

      .discounts-title {
        font-size: 24rpx;
        font-weight: 500;
        color: var(--ui-BG-Main);
        line-height: normal;
      }

      .cicon-forward {
        color: var(--ui-BG-Main);
        font-size: 24rpx;
        line-height: normal;
        margin-top: 4rpx;
      }
    }

    .title-text {
      font-size: 30rpx;
      font-weight: bold;
      line-height: 42rpx;
    }

    .subtitle-text {
      font-size: 26rpx;
      font-weight: 400;
      color: $dark-9;
      line-height: 42rpx;
    }
  }

  // 购买
  .buy-box {
    .add-btn {
      width: 214rpx;
      height: 72rpx;
      font-weight: 500;
      font-size: 28rpx;
      border-radius: 40rpx 0 0 40rpx;
      background-color: var(--ui-BG-Main-light);
      color: var(--ui-BG-Main);
    }

    .buy-btn {
      width: 214rpx;
      height: 72rpx;
      font-weight: 500;
      font-size: 28rpx;

      border-radius: 0 40rpx 40rpx 0;
      background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
      color: $white;
    }

    .disabled-btn {
      width: 428rpx;
      height: 72rpx;
      border-radius: 40rpx;
      background: #999999;
      color: $white;
    }
  }

  .model-box {
    height: 60vh;

    .model-content {
      height: 56vh;
    }

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

  // 限时折扣
  .discount {
    width: 750rpx;
    height: 100rpx;
    // background-color: red;
    overflow: hidden;
    position: relative;
  }

  .disImg {
    width: 750rpx;
    height: 100rpx;
    position: absolute;
    top: 0;
    z-index: -1;
  }

  .discountCont {
    width: 680rpx;
    height: 90rpx;
    margin: 10rpx auto 0 auto;
    // background-color: gold;
  }

  .disContT {
    width: 680rpx;
    height: 50rpx;
    display: flex;
    justify-content: space-between;
  }

  .disContT1 {
    width: 400rpx;
    height: 50rpx;
    // background-color: green;
    display: flex;
    justify-content: flex-start;
    align-items: center;
  }

  .disContT2 {
    width: 200rpx;
    height: 50rpx;
    line-height: 50rpx;
    // background-color: gold;
    font-size: 30rpx;
    text-align: end;
    color: white;
    font-weight: bolder;
    font-style: oblique 20deg;
    letter-spacing: 0.1rem;
  }

  .disContT1P {
    color: white;
    font-weight: bold;
    font-size: 28rpx;
  }

  .disContT1End {
    // width: 180rpx;
    padding: 0 10rpx;
    height: 30rpx;
    line-height: 28rpx;
    text-align: center;
    font-weight: bold;
    background-color: white;
    color: #ff3000;
    font-size: 23rpx;
    border-radius: 20rpx;
    margin-left: 10rpx;
  }

  .disContB {
    width: 680rpx;
    height: 40rpx;
    display: flex;
    justify-content: space-between;
    font-size: 20rpx;
    color: white;
    align-items: center;
  }

  .disContB1 {
    width: 300rpx;
    height: 40rpx;
    line-height: 40rpx;
  }

  .disContB2 {
    width: 300rpx;
    height: 40rpx;
    line-height: 40rpx;
    display: flex;
    justify-content: flex-end;
  }
</style>
