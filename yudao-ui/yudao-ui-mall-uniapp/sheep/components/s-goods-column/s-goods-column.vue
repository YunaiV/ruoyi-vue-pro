<!-- 页面 -->
<template>
  <view class="ss-goods-wrap">
    <!-- xs卡片：横向紧凑型，一行放两个，图片左内容右边  -->
    <view
      v-if="size === 'xs'"
      class="xs-goods-card ss-flex ss-col-stretch"
      :style="[elStyles]"
      @tap="onClick"
    >
      <view v-if="tagStyle.show" class="tag-icon-box">
        <image class="tag-icon" :src="sheep.$url.cdn(tagStyle.src || tagStyle.imgUrl)"></image>
      </view>
      <image class="xs-img-box" :src="sheep.$url.cdn(data.image || data.picUrl)" mode="aspectFit" />
      <view
        v-if="goodsFields.title?.show || goodsFields.name?.show || goodsFields.price?.show"
        class="xs-goods-content ss-flex-col ss-row-around"
      >
        <view
          v-if="goodsFields.title?.show || goodsFields.name?.show"
          class="xs-goods-title ss-line-1"
          :style="[{ color: titleColor, width: titleWidth ? titleWidth + 'rpx' : '' }]"
        >
          {{ data.title || data.name }}
        </view>
        <!-- 活动信息 -->
        <view class="iconBox" v-if="data.promotionType > 0 || data.rewardActivity">
          <view class="card" v-if="discountText">{{ discountText }}</view>
          <view
            class="card2"
            v-for="item in getRewardActivityRuleItemDescriptions(data.rewardActivity).slice(0, 1)"
            :key="item"
          >
            {{ item }}
          </view>
        </view>
        <view
          v-if="goodsFields.price?.show"
          class="xs-goods-price font-OPPOSANS"
          :style="[{ color: goodsFields.price.color }]"
        >
          <!-- 活动价格 -->
          <view
            class="ss-flex"
            v-if="data.activityType && data.activityType === PromotionActivityTypeEnum.POINT.type"
          >
            <image
              :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
              class="point-img"
            ></image>
            <text class="point-text ss-m-r-16">
              {{ data.point }}
              {{
                !data.pointPrice || data.pointPrice === 0
                  ? ''
                  : `+${priceUnit}${fen2yuan(data.pointPrice)}`
              }}
            </text>
          </view>
          <template v-else>
            <text class="price-unit ss-font-24">{{ priceUnit }}</text>
            <text v-if="data.promotionPrice > 0">{{ fen2yuan(data.promotionPrice) }}</text>
            <text v-else>
              {{ isArray(data.price) ? fen2yuan(data.price[0]) : fen2yuan(data.price) }}
            </text>
          </template>
        </view>
      </view>
    </view>

    <!-- sm卡片：竖向紧凑，一行放三个，图上内容下 -->
    <view v-if="size === 'sm'" class="sm-goods-card ss-flex-col" :style="[elStyles]" @tap="onClick">
      <view v-if="tagStyle.show" class="tag-icon-box">
        <image class="tag-icon" :src="sheep.$url.cdn(tagStyle.src || tagStyle.imgUrl)"></image>
      </view>
      <image
        class="sm-img-box"
        :src="sheep.$url.cdn(data.image || data.picUrl)"
        mode="aspectFill"
      ></image>

      <view
        v-if="goodsFields.title?.show || goodsFields.name?.show || goodsFields.price?.show"
        class="sm-goods-content"
        :style="[{ color: titleColor, width: titleWidth ? titleWidth + 'rpx' : '' }]"
      >
        <view
          v-if="goodsFields.title?.show || goodsFields.name?.show"
          class="sm-goods-title ss-line-1 ss-m-b-16"
        >
          {{ data.title || data.name }}
        </view>
        <!-- 活动信息 -->
        <view class="iconBox" v-if="data.promotionType > 0 || data.rewardActivity">
          <view class="card" v-if="discountText">{{ discountText }}</view>
          <view
            class="card2"
            v-for="item in getRewardActivityRuleItemDescriptions(data.rewardActivity).slice(0, 1)"
            :key="item"
          >
            {{ item }}
          </view>
        </view>
        <view
          v-if="goodsFields.price?.show"
          class="sm-goods-price font-OPPOSANS"
          :style="[{ color: goodsFields.price.color }]"
        >
          <!-- 活动价格 -->
          <view
            class="ss-flex"
            v-if="data.activityType && data.activityType === PromotionActivityTypeEnum.POINT.type"
          >
            <image
              :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
              class="point-img"
            ></image>
            <text class="point-text ss-m-r-16">
              {{ data.point }}
              {{
                !data.pointPrice || data.pointPrice === 0
                  ? ''
                  : `+${priceUnit}${fen2yuan(data.pointPrice)}`
              }}
            </text>
          </view>
          <template v-else>
            <text class="price-unit ss-font-24">{{ priceUnit }}</text>
            <text v-if="data.promotionPrice > 0">{{ fen2yuan(data.promotionPrice) }}</text>
            <text v-else>
              {{ isArray(data.price) ? fen2yuan(data.price[0]) : fen2yuan(data.price) }}
            </text>
          </template>
        </view>
      </view>
    </view>

    <!-- md卡片：竖向，一行放两个，图上内容下 -->
    <view v-if="size === 'md'" class="md-goods-card ss-flex-col" :style="[elStyles]" @tap="onClick">
      <view v-if="tagStyle.show" class="tag-icon-box">
        <image class="tag-icon" :src="sheep.$url.cdn(tagStyle.src || tagStyle.imgUrl)" />
      </view>
      <image
        v-if="data.image_wh"
        class="md-img-box"
        :src="sheep.$url.cdn(data.image || data.picUrl)"
        mode="widthFix"
      />
      <image
        v-else
        class="md-img-box"
        :src="sheep.$url.cdn(data.image || data.picUrl)"
        :style="[{ height: defaultImgWidth * 2 + 'rpx' }]"
        mode="aspectFill"
      />
      <view
        class="md-goods-content ss-flex-col ss-row-around ss-p-b-20 ss-p-t-20 ss-p-x-16"
        :id="elId"
      >
        <view
          v-if="goodsFields.title?.show || goodsFields.name?.show"
          class="md-goods-title ss-line-1"
          :style="[{ color: titleColor, width: titleWidth ? titleWidth + 'rpx' : '' }]"
        >
          {{ data.title || data.name }}
        </view>
        <view
          v-if="goodsFields.subtitle?.show || goodsFields.introduction?.show"
          class="md-goods-subtitle ss-m-t-16 ss-line-1"
          :style="[{ color: subTitleColor, background: subTitleBackground }]"
        >
          {{ data.subtitle || data.introduction }}
        </view>
        <slot name="activity">
          <view v-if="data.promos?.length" class="tag-box ss-flex-wrap ss-flex ss-col-center">
            <view
              class="activity-tag ss-m-r-10 ss-m-t-16"
              v-for="item in data.promos"
              :key="item.id"
            >
              {{ item.title }}
            </view>
          </view>
        </slot>
        <!-- 活动信息 -->
        <view class="iconBox" v-if="data.promotionType > 0 || data.rewardActivity">
          <view class="card" v-if="discountText">{{ discountText }}</view>
          <view
            class="card2"
            v-for="item in getRewardActivityRuleItemDescriptions(data.rewardActivity).slice(0, 1)"
            :key="item"
          >
            {{ item }}
          </view>
        </view>
        <view class="ss-flex ss-col-bottom">
          <view
            v-if="goodsFields.price?.show"
            class="md-goods-price ss-m-t-16 font-OPPOSANS ss-m-r-10"
            :style="[{ color: goodsFields.price.color }]"
          >
            <!-- 活动价格 -->
            <view
              class="ss-flex"
              v-if="data.activityType && data.activityType === PromotionActivityTypeEnum.POINT.type"
            >
              <image
                :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
                class="point-img"
              ></image>
              <text class="point-text ss-m-r-16">
                {{ data.point }}
                {{
                  !data.pointPrice || data.pointPrice === 0
                    ? ''
                    : `+${priceUnit}${fen2yuan(data.pointPrice)}`
                }}
              </text>
            </view>
            <template v-else>
              <text class="price-unit ss-font-24">{{ priceUnit }}</text>
              <text v-if="data.promotionPrice > 0">{{ fen2yuan(data.promotionPrice) }}</text>
              <text v-else>
                {{ isArray(data.price) ? fen2yuan(data.price[0]) : fen2yuan(data.price) }}
              </text>
            </template>
          </view>
          <view
            v-if="
              (goodsFields.original_price?.show || goodsFields.marketPrice?.show) &&
              (data.original_price > 0 || data.marketPrice > 0)
            "
            class="goods-origin-price ss-m-t-16 font-OPPOSANS ss-flex"
            :style="[{ color: originPriceColor }]"
          >
            <text class="price-unit ss-font-20">{{ priceUnit }}</text>
            <view class="ss-m-l-8">{{ fen2yuan(data.marketPrice) }}</view>
          </view>
        </view>

        <view class="ss-m-t-16 ss-flex ss-col-center ss-flex-wrap">
          <view class="sales-text">{{ salesAndStock }}</view>
        </view>
      </view>

      <slot name="cart">
        <view class="cart-box ss-flex ss-col-center ss-row-center">
          <image class="cart-icon" src="/static/img/shop/tabbar/category2.png" mode="" />
        </view>
      </slot>
    </view>

    <!-- lg卡片：横向型，一行放一个，图片左内容右边  -->
    <view
      v-if="size === 'lg'"
      class="lg-goods-card ss-flex ss-col-stretch"
      :style="[elStyles]"
      @tap="onClick"
    >
      <view v-if="tagStyle.show" class="tag-icon-box">
        <image class="tag-icon" :src="sheep.$url.cdn(tagStyle.src || tagStyle.imgUrl)"></image>
      </view>
      <view v-if="seckillTag" class="seckill-tag ss-flex ss-row-center">秒杀</view>
      <view v-if="grouponTag" class="groupon-tag ss-flex ss-row-center">
        <view class="tag-icon">拼团</view>
      </view>
      <image
        class="lg-img-box"
        :src="sheep.$url.cdn(data.image || data.picUrl)"
        mode="aspectFill"
      />
      <view class="lg-goods-content ss-flex-1 ss-flex-col ss-row-between ss-p-b-10 ss-p-t-20">
        <view>
          <view
            v-if="goodsFields.title?.show || goodsFields.name?.show"
            class="lg-goods-title ss-line-2"
            :style="[{ color: titleColor }]"
          >
            {{ data.title || data.name }}
          </view>
          <view
            v-if="goodsFields.subtitle?.show || goodsFields.introduction?.show"
            class="lg-goods-subtitle ss-m-t-10 ss-line-1"
            :style="[{ color: subTitleColor, background: subTitleBackground }]"
          >
            {{ data.subtitle || data.introduction }}
          </view>
        </view>
        <view>
          <slot name="activity">
            <view v-if="data.promos?.length" class="tag-box ss-flex ss-col-center">
              <view class="activity-tag ss-m-r-10" v-for="item in data.promos" :key="item.id">
                {{ item.title }}
              </view>
            </view>
          </slot>
          <!-- 活动信息 -->
          <view class="iconBox" v-if="data.promotionType > 0 || data.rewardActivity">
            <view class="card" v-if="discountText">{{ discountText }}</view>
            <view
              class="card2"
              v-for="item in getRewardActivityRuleItemDescriptions(data.rewardActivity).slice(0, 1)"
              :key="item"
            >
              {{ item }}
            </view>
          </view>
          <view v-if="goodsFields.price?.show" class="ss-flex ss-col-bottom font-OPPOSANS">
            <view class="sl-goods-price ss-m-r-12" :style="[{ color: goodsFields.price.color }]">
              <!-- 活动价格 -->
              <view
                class="ss-flex"
                v-if="
                  data.activityType && data.activityType === PromotionActivityTypeEnum.POINT.type
                "
              >
                <image
                  :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
                  class="point-img"
                ></image>
                <text class="point-text ss-m-r-16">
                  {{ data.point }}
                  {{
                    !data.pointPrice || data.pointPrice === 0
                      ? ''
                      : `+${priceUnit}${fen2yuan(data.pointPrice)}`
                  }}
                </text>
              </view>
              <template v-else>
                <text class="price-unit ss-font-24">{{ priceUnit }}</text>
                <text v-if="data.promotionPrice > 0">{{ fen2yuan(data.promotionPrice) }}</text>
                <text v-else>
                  {{ isArray(data.price) ? fen2yuan(data.price[0]) : fen2yuan(data.price) }}
                </text>
              </template>
            </view>
            <view
              v-if="
                (goodsFields.original_price?.show || goodsFields.marketPrice?.show) &&
                (data.original_price > 0 || data.marketPrice > 0)
              "
              class="goods-origin-price ss-m-t-16 font-OPPOSANS ss-flex"
              :style="[{ color: originPriceColor }]"
            >
              <text class="price-unit ss-font-20">{{ priceUnit }}</text>
              <view class="ss-m-l-8">{{ fen2yuan(data.marketPrice) }}</view>
            </view>
          </view>
          <view class="ss-m-t-8 ss-flex ss-col-center ss-flex-wrap">
            <view class="sales-text">{{ salesAndStock }}</view>
          </view>
        </view>
      </view>

      <slot name="cart">
        <view class="buy-box ss-flex ss-col-center ss-row-center" v-if="buttonShow"> 去购买</view>
      </slot>
    </view>

    <!-- sl卡片：竖向型，一行放一个，图片上内容下边 -->
    <view v-if="size === 'sl'" class="sl-goods-card ss-flex-col" :style="[elStyles]" @tap="onClick">
      <view v-if="tagStyle.show" class="tag-icon-box">
        <image class="tag-icon" :src="sheep.$url.cdn(tagStyle.src || tagStyle.imgUrl)" />
      </view>
      <image
        class="sl-img-box"
        :src="sheep.$url.cdn(data.image || data.picUrl)"
        mode="aspectFill"
      />
      <view class="sl-goods-content">
        <view>
          <view
            v-if="goodsFields.title?.show || goodsFields.name?.show"
            class="sl-goods-title ss-line-1"
            :style="[{ color: titleColor }]"
          >
            {{ data.title || data.name }}
          </view>
          <view
            v-if="goodsFields.subtitle?.show || goodsFields.introduction?.show"
            class="sl-goods-subtitle ss-m-t-16"
            :style="[{ color: subTitleColor, background: subTitleBackground }]"
          >
            {{ data.subtitle || data.introduction }}
          </view>
        </view>
        <view>
          <slot name="activity">
            <view v-if="data.promos?.length" class="tag-box ss-flex ss-col-center ss-flex-wrap">
              <view
                class="activity-tag ss-m-r-10 ss-m-t-16"
                v-for="item in data.promos"
                :key="item.id"
              >
                {{ item.title }}
              </view>
            </view>
          </slot>
          <!-- 活动信息 -->
          <view class="iconBox" v-if="data.promotionType > 0 || data.rewardActivity">
            <view class="card" v-if="discountText">{{ discountText }}</view>
            <view
              class="card2"
              v-for="item in getRewardActivityRuleItemDescriptions(data.rewardActivity).slice(0, 1)"
              :key="item"
            >
              {{ item }}
            </view>
          </view>
          <view v-if="goodsFields.price?.show" class="ss-flex ss-col-bottom font-OPPOSANS">
            <view class="sl-goods-price ss-m-r-12" :style="[{ color: goodsFields.price.color }]">
              <!-- 活动价格 -->
              <view
                class="ss-flex"
                v-if="
                  data.activityType && data.activityType === PromotionActivityTypeEnum.POINT.type
                "
              >
                <image
                  :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
                  class="point-img"
                ></image>
                <text class="ss-m-r-16">
                  {{ data.point }}
                  {{
                    !data.pointPrice || data.pointPrice === 0
                      ? ''
                      : `+${priceUnit}${fen2yuan(data.pointPrice)}`
                  }}
                </text>
              </view>
              <template v-else>
                <text class="price-unit ss-font-24">{{ priceUnit }}</text>
                <text v-if="data.promotionPrice > 0">{{ fen2yuan(data.promotionPrice) }}</text>
                <text v-else>
                  {{ isArray(data.price) ? fen2yuan(data.price[0]) : fen2yuan(data.price) }}
                </text>
              </template>
            </view>
            <view
              v-if="
                (goodsFields.original_price?.show || goodsFields.marketPrice?.show) &&
                (data.original_price > 0 || data.marketPrice > 0)
              "
              class="goods-origin-price ss-m-t-16 font-OPPOSANS ss-flex"
              :style="[{ color: originPriceColor }]"
            >
              <text class="price-unit ss-font-20">{{ priceUnit }}</text>
              <view class="ss-m-l-8">{{ fen2yuan(data.marketPrice) }}</view>
            </view>
          </view>
          <view class="ss-m-t-16 ss-flex ss-flex-wrap">
            <view class="sales-text">{{ salesAndStock }}</view>
          </view>
        </view>
      </view>

      <slot name="cart">
        <view class="buy-box ss-flex ss-col-center ss-row-center">去购买</view>
      </slot>
    </view>
  </view>
</template>

<script setup>
  /**
   * 商品卡片
   *
   * @property {Array} size = [xs | sm | md | lg | sl ] 			 	- 列表数据
   * @property {String} tag                      - md及以上才有
   * @property {String} img                      - 图片
   * @property {String} background                  - 背景色
   * @property {String} topRadius                  - 上圆角
   * @property {String} bottomRadius                  - 下圆角
   * @property {String} title                    - 标题
   * @property {String} titleColor                  - 标题颜色
   * @property {Number} titleWidth = 0								- 标题宽度，默认0，单位rpx
   * @property {String} subTitle                    - 副标题
   * @property {String} subTitleColor                  - 副标题颜色
   * @property {String} subTitleBackground              - 副标题背景
   * @property {String | Number} price                - 价格
   * @property {String} priceColor                  - 价格颜色
   * @property {String | Number} originPrice              - 原价/划线价
   * @property {String} originPriceColor                - 原价颜色
   * @property {String | Number} sales                - 销售数量
   * @property {String} salesColor                  - 销售数量颜色
   *
   * @slots activity												 	- 活动插槽
   * @slots cart														- 购物车插槽，默认包含文字，背景色，文字颜色 || 图片 || 行为
   *
   * @event {Function()} click                    - 点击卡片
   *
   */
  import { computed, ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue';
  import sheep from '@/sheep';
  import {
    fen2yuan,
    formatExchange,
    formatSales,
    formatStock,
    getRewardActivityRuleItemDescriptions,
  } from '@/sheep/hooks/useGoods';
  import { isArray } from 'lodash-es';
  import { PromotionActivityTypeEnum } from '@/sheep/helper/const';

  // 数据
  let defaultImgWidth = ref(0);

  // 接收参数
  const props = defineProps({
    goodsFields: {
      type: [Array, Object],
      default() {
        return {
          // 商品价格
          price: {
            show: true,
          },
          // 库存
          stock: {
            show: true,
          },
          // 商品名称
          name: {
            show: true,
          },
          // 商品介绍
          introduction: {
            show: true,
          },
          // 市场价
          marketPrice: {
            show: true,
          },
          // 销量
          salesCount: {
            show: true,
          },
        };
      },
    },
    tagStyle: {
      type: Object,
      default: () => ({}),
    },
    data: {
      type: Object,
      default: () => ({}),
    },
    size: {
      type: String,
      default: 'sl',
    },
    background: {
      type: String,
      default: '',
    },
    topRadius: {
      type: Number,
      default: 0,
    },
    bottomRadius: {
      type: Number,
      default: 0,
    },
    titleWidth: {
      type: Number,
      default: 0,
    },
    titleColor: {
      type: String,
      default: '#333',
    },
    priceColor: {
      type: String,
      default: '',
    },
    originPriceColor: {
      type: String,
      default: '#C4C4C4',
    },
    priceUnit: {
      type: String,
      default: '￥',
    },
    subTitleColor: {
      type: String,
      default: '#999999',
    },
    subTitleBackground: {
      type: String,
      default: '',
    },
    buttonShow: {
      type: Boolean,
      default: true,
    },
    seckillTag: {
      type: Boolean,
      default: false,
    },
    grouponTag: {
      type: Boolean,
      default: false,
    },
  });

  // 优惠文案
  const discountText = computed(() => {
    const promotionType = props.data.promotionType;
    if (promotionType === 4) {
      return '限时优惠';
    } else if (promotionType === 6) {
      return '会员价';
    }
    return undefined;
  });

  // 组件样式
  const elStyles = computed(() => {
    return {
      background: props.background,
      'border-top-left-radius': props.topRadius + 'px',
      'border-top-right-radius': props.topRadius + 'px',
      'border-bottom-left-radius': props.bottomRadius + 'px',
      'border-bottom-right-radius': props.bottomRadius + 'px',
    };
  });

  // 格式化销量、库存信息
  const salesAndStock = computed(() => {
    let text = [];
    if (props.goodsFields.salesCount?.show) {
      if (
        props.data.activityType &&
        props.data.activityType === PromotionActivityTypeEnum.POINT.type
      ) {
        text.push(
          formatExchange(
            props.data.sales_show_type,
            (props.data.pointTotalStock || 0) - (props.data.pointStock || 0),
          ),
        );
      } else {
        text.push(formatSales(props.data.sales_show_type, props.data.salesCount));
      }
    }
    if (props.goodsFields.stock?.show) {
      if (
        props.data.activityType &&
        props.data.activityType === PromotionActivityTypeEnum.POINT.type
      ) {
        text.push(formatStock(props.data.stock_show_type, props.data.pointTotalStock));
      } else {
        text.push(formatStock(props.data.stock_show_type, props.data.stock));
      }
    }
    return text.join(' | ');
  });

  // 返回事件
  const emits = defineEmits(['click', 'getHeight']);

  const onClick = () => {
    emits('click');
  };

  // 获取卡片实时高度
  const { proxy } = getCurrentInstance();
  const elId = `sheep_${Math.ceil(Math.random() * 10e5).toString(36)}`;

  function getGoodsPriceCardWH() {
    if (props.size === 'md') {
      const view = uni.createSelectorQuery().in(proxy);
      view.select(`#${elId}`).fields({
        size: true,
        scrollOffset: true,
      });
      view.exec((data) => {
        console.log(data, 'data');
        let totalHeight = 0;
        const goodsPriceCard = data[0];
        defaultImgWidth.value = data[0].width;

        if (props.data.image_wh && Number(props.data.image_wh.w)) {
          totalHeight =
            (goodsPriceCard.width / props.data.image_wh.w) * props.data.image_wh.h +
            goodsPriceCard.height;
        } else {
          totalHeight = goodsPriceCard.width + goodsPriceCard.height;
        }
        emits('getHeight', totalHeight);
      });
    }
  }

  onMounted(() => {
    nextTick(() => {
      getGoodsPriceCardWH();
    });
  });
</script>

<style lang="scss" scoped>
  .tag-icon-box {
    position: absolute;
    left: 0;
    top: 0;
    z-index: 2;

    .tag-icon {
      width: 72rpx;
      height: 44rpx;
    }
  }

  .seckill-tag {
    position: absolute;
    left: 0;
    top: 0;
    z-index: 2;
    width: 68rpx;
    height: 38rpx;
    background: linear-gradient(90deg, #ff5854 0%, #ff2621 100%);
    border-radius: 10rpx 0px 10rpx 0px;
    font-size: 24rpx;
    font-weight: 500;
    color: #ffffff;
    line-height: 32rpx;
  }

  .point-img {
    width: 30rpx;
    height: 30rpx;
    margin: 0 4rpx;
  }

  .groupon-tag {
    position: absolute;
    left: 0;
    top: 0;
    z-index: 2;
    width: 68rpx;
    height: 38rpx;
    background: linear-gradient(90deg, #fe832a 0%, #ff6600 100%);
    border-radius: 10rpx 0px 10rpx 0px;
    font-size: 24rpx;
    font-weight: 500;
    color: #ffffff;
    line-height: 32rpx;
  }

  .goods-img {
    width: 100%;
    height: 100%;
    background-color: #f5f5f5;
  }

  .price-unit {
    margin-right: -4px;
  }

  .sales-text {
    display: table;
    font-size: 24rpx;
    transform: scale(0.8);
    margin-left: 0rpx;
    color: #c4c4c4;
  }

  .activity-tag {
    font-size: 20rpx;
    color: #ff0000;
    line-height: 30rpx;
    padding: 0 10rpx;
    border: 1px solid rgba(#ff0000, 0.25);
    border-radius: 4px;
    flex-shrink: 0;
  }

  .goods-origin-price {
    font-size: 20rpx;
    color: #c4c4c4;
    line-height: 36rpx;
    text-decoration: line-through;
  }

  // xs
  .xs-goods-card {
    overflow: hidden;
    // max-width: 375rpx;
    background-color: $white;
    position: relative;

    .xs-img-box {
      width: 128rpx;
      height: 128rpx;
      margin-right: 20rpx;
    }

    .xs-goods-title {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }

    .xs-goods-price {
      font-size: 30rpx;
      color: $red;
    }
  }

  // sm
  .sm-goods-card {
    overflow: hidden;
    // width: 223rpx;
    // width: 100%;
    background-color: $white;
    position: relative;

    .sm-img-box {
      // width: 228rpx;
      width: 100%;
      height: 208rpx;
    }

    .sm-goods-content {
      padding: 20rpx 16rpx;
      box-sizing: border-box;
    }

    .sm-goods-title {
      font-size: 26rpx;
      color: #333;
    }

    .sm-goods-price {
      font-size: 30rpx;
      color: $red;
    }
  }

  // md
  .md-goods-card {
    overflow: hidden;
    width: 100%;
    position: relative;
    z-index: 1;
    background-color: $white;
    position: relative;

    .md-img-box {
      width: 100%;
    }

    .md-goods-title {
      font-size: 26rpx;
      color: #333;
      width: 100%;
    }

    .md-goods-subtitle {
      font-size: 24rpx;
      font-weight: 400;
      color: #999999;
    }

    .md-goods-price {
      font-size: 30rpx;
      color: $red;
      line-height: 36rpx;
    }

    .cart-box {
      width: 54rpx;
      height: 54rpx;
      background: linear-gradient(90deg, #fe8900, #ff5e00);
      border-radius: 50%;
      position: absolute;
      bottom: 50rpx;
      right: 20rpx;
      z-index: 2;

      .cart-icon {
        width: 30rpx;
        height: 30rpx;
      }
    }
  }

  // lg
  .lg-goods-card {
    overflow: hidden;
    position: relative;
    z-index: 1;
    background-color: $white;
    height: 280rpx;

    .lg-img-box {
      width: 280rpx;
      height: 280rpx;
      margin-right: 20rpx;
    }

    .lg-goods-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      // line-height: 36rpx;
      // width: 410rpx;
    }

    .lg-goods-subtitle {
      font-size: 24rpx;
      font-weight: 400;
      color: #999999;
      // line-height: 30rpx;
      // width: 410rpx;
    }

    .lg-goods-price {
      font-size: 30rpx;
      color: $red;
      line-height: 36rpx;
    }

    .buy-box {
      position: absolute;
      bottom: 20rpx;
      right: 20rpx;
      z-index: 2;
      width: 120rpx;
      height: 50rpx;
      background: linear-gradient(90deg, #fe8900, #ff5e00);
      border-radius: 25rpx;
      font-size: 24rpx;
      color: #ffffff;
    }

    .tag-box {
      width: 100%;
    }
  }

  // sl

  .sl-goods-card {
    overflow: hidden;
    position: relative;
    z-index: 1;
    width: 100%;
    background-color: $white;

    .sl-goods-content {
      padding: 20rpx 20rpx;
      box-sizing: border-box;
    }

    .sl-img-box {
      width: 100%;
      height: 360rpx;
    }

    .sl-goods-title {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }

    .sl-goods-subtitle {
      font-size: 24rpx;
      font-weight: 400;
      color: #999999;
      line-height: 30rpx;
    }

    .sl-goods-price {
      font-size: 30rpx;
      color: $red;
      line-height: 36rpx;
    }

    .buy-box {
      position: absolute;
      bottom: 20rpx;
      right: 20rpx;
      z-index: 2;
      width: 148rpx;
      height: 50rpx;
      background: linear-gradient(90deg, #fe8900, #ff5e00);
      border-radius: 25rpx;
      font-size: 24rpx;
      color: #ffffff;
    }
  }

  .card {
    width: fit-content;
    height: fit-content;
    padding: 2rpx 10rpx;
    background-color: red;
    color: #ffffff;
    font-size: 24rpx;
    margin-top: 5rpx;
  }

  .card2 {
    width: fit-content;
    height: fit-content;
    padding: 2rpx 10rpx;
    background-color: rgb(255, 242, 241);
    color: #ff2621;
    font-size: 24rpx;
    margin: 5rpx 0 5rpx 5rpx;
  }

  .iconBox {
    width: 100%;
    height: fit-content;
    margin-top: 10rpx;
    display: flex;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
</style>
