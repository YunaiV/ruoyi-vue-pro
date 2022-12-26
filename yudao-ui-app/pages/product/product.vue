<template>
  <view class="container">
    <!-- 商品轮播图 -->
    <u-swiper :list="spu.picUrls" @change="e => (currentNum = e.current)" :autoplay="false"
              height="750rpx" radius="0" indicatorStyle="right: 20px">
      <view slot="indicator" class="indicator-num">
        <text class="indicator-num__text">{{ currentNum + 1 }}/{{ spu.picUrls.length }}</text>
      </view>
    </u-swiper>

    <view class="product-box">
      <!-- TODO @Sfmind：样式讨论，要不要改成类似 likeshop 的样子？第一栏：价格 + 分享；第二栏：商品名；第三栏：库存、销量、浏览量 -->
      <view class="prod-info">
        <view class="info-text">
          <u--text :lines="2" size="14px" color="#333333" :text="spu.name"></u--text>
        </view>
        <view class="price-and-cart">
          <!-- TODO @Sfmind：custom-text-price 会报错 -->
          <custom-text-price color="red" size="16" intSize="26" :price="spu.minPrice"></custom-text-price>
        </view>
      </view>
      <view class="prod-favor">
        <u-icon name="star" color="#2979ff" size="28"></u-icon>
      </view>
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="row-box">
      <view class="row-left">规格</view>
      <view class="row-right" @click="skuPopup = true">
        <view class="row-content">
          <view class="sku-box">
            <view v-if="spu.skus.length > 0" class="sku-item">
              <view class="sku-desc">{{ spu.skus[currentSkuIndex].desc }}</view>
            </view>
          </view>
        </view>
        <view class="row-more">
          <u-icon name="more-dot-fill" color="#939393" size="14"></u-icon>
        </view>
      </view>
    </view>

    <!-- 商品 SKU 选择弹窗 -->
    <u-popup :show="skuPopup" :round="10" :closeable="true" :closeOnClickOverlay="false" @close="skuPopup = false">
      <view class="sku-popup-slot">
        <view class="current-sku-info">
          <u--image class="current-sku-img" :showLoading="true" :src="spu.skus[currentSkuIndex].picUrl"
                    width="120rpx" height="120rpx"></u--image>
          <view class="current-sku-desc">
            <!-- TODO @Sfmind:name 这里的选择规格值的拼接 -->
            <view class="name">{{ spu.skus[currentSkuIndex].desc }}</view>
            <custom-text-price color="red" size="12" intSize="18" :price="spu.skus[currentSkuIndex].price"></custom-text-price>
            <view class="current-sku-stock">库存： {{ 1 }}</view>
          </view>
        </view>
        <view class="sku-selection">
          <!-- TODO @Sfmind:name 这里是规格的具体选项 -->
          <view class="sku-item" :class="{ active: currentSkuIndex === index }" v-for="(item, index) in spu.skus"
                :key="item.id" @click="handleSkuItemClick(index)">{{ item.desc }}</view>
        </view>
        <view class="sku-num-box">
          <view class="text">选择数量</view>
          <u-number-box integer></u-number-box>
        </view>
        <view class="sku-btn-group">
          <view class="btn-item-main">
            <u-button type="warning" shape="circle" size="small" text="加入购物车"></u-button>
          </view>
          <view class="btn-item-main">
            <u-button type="error" shape="circle" size="small" text="立即购买"></u-button>
          </view>
        </view>
      </view>
    </u-popup>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="row-box">
      <view class="row-left">促销</view>
      <view class="row-right" v-if="promotionList.length > 0" @click="promotionPopup = true">
        <view class="row-content">
          <view class="prom-box">
            <view class="prom-item">
              <view class="prom-title">{{ promotionList[0].title }}</view>
              <text class="prom-desc">{{ promotionList[0].desc }}</text>
            </view>
          </view>
        </view>
        <view class="row-more">
          <u-icon name="more-dot-fill" color="#939393" size="14"></u-icon>
        </view>
      </view>
    </view>

    <!-- 促销信息弹窗 -->
    <u-popup :show="promotionPopup" :round="10" :closeable="true" :closeOnClickOverlay="false" @close="promotionPopup = false">
      <view class="prom-popup-slot">
        <view class="prom-info">促销信息</view>
        <view class="prom-list">
          <view v-for="(item, index) in promotionList" :key="index.id" class="prom-item">
            <view class="prom-title">{{ item.title }}</view>
            <text class="prom-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="row-box">
      <view class="row-left">领券</view>
      <view class="row-right" @click="handleCouponClick">
        <view class="row-content">
          <view class="coupon-box">
            <view v-if="couponList.length > 0" class="coupon-list">
              <view v-for="(item, index) in couponList" :key="item.id" class="coupon-item">
                <view v-if="index < 2" class="coupon-desc">{{ item.desc }}</view>
              </view>
            </view>
            <view class="coupon-total">共 {{ couponList.length }} 张</view>
          </view>
        </view>
        <view class="row-more">
          <u-icon name="more-dot-fill" color="#939393" size="14"></u-icon>
        </view>
      </view>
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="evaluation-box-wrap">
      <view class="evaluation-box">
        <view class="evaluation-title">评价</view>
        <view class="evaluation-info">
          <view class="evan-type-list">
            <view class="evan-type-item" :class="{ active: currentEvanIndex === index }" v-for="(item, index) in evanTypeList" :key="item.id" @click="handleEvanTypeClick(index)"> {{ item.name }}({{ item.count }}) </view>
          </view>
          <view class="comment-empty" v-if="true">
            <u-empty mode="comment" width="350rpx" height="350rpx" icon="/static/images/empty/comment.png"></u-empty>
          </view>
          <view v-else class="comment-list" style="min-height: 50px"> </view>
        </view>
      </view>
    </view>

    <!-- TODO @Sfmind:缺个商品详情 -->

    <view class="fixed-btn-box">
      <view class="btn-group">
        <navigator class="btn-item" url="/pages/index/index" open-type="switchTab" hover-class="none">
          <u-icon name="home" :size="24"></u-icon>
          <view class="btn-text">首页</view>
        </navigator>
        <navigator class="btn-item" url="/pages/xxx/xxx" open-type="navigate" hover-class="none">
          <u-icon name="server-man" :size="24"></u-icon>
          <view class="btn-text">客服</view>
        </navigator>
        <!-- TODO @Sfmind:改成收藏 -->
        <navigator class="btn-item" url="/pages/cart/cart" open-type="switchTab" hover-class="none">
          <u-icon name="star" :size="24"></u-icon>
          <view class="btn-text">收藏</view>
        </navigator>
        <view class="btn-item-main">
          <u-button type="warning" shape="circle" size="small" text="加入购物车"></u-button>
        </view>
        <view class="btn-item-main">
          <u-button type="error" color="#ea322b" shape="circle" size="small" text="立即购买"></u-button>
        </view>
      </view>
      <u-safe-bottom customStyle="background: #ffffff"></u-safe-bottom>
    </view>
  </view>
</template>

<script>
import { getSpuDetail } from '../../api/product';

export default {
  data() {
    return {
      current: 0,
      currentNum: 0,
      currentSkuIndex: 0,
      skuPopup: false,
      spu: {
        id: '',
        picUrls: [],
        minPrice: '13.00',
        sku: [
          {
            id: 0,
            picUrl: 'https://cdn.uviewui.com/uview/album/1.jpg',
            price: 13.0,
            desc: '山不在高，有仙则名。'
          },
          {
            id: 1,
            picUrl: 'https://cdn.uviewui.com/uview/album/2.jpg',
            price: 11.0,
            desc: '水不在深，有龙则灵。'
          },
          {
            id: 2,
            picUrl: 'https://cdn.uviewui.com/uview/album/3.jpg',
            price: 10.0,
            desc: '斯是陋室，惟吾德馨。'
          }
        ]
      },
      promotionPopup: false,
      promotionList: [
        {
          id: 0,
          title: '满额减',
          desc: '全场满500减100'
        },
        {
          id: 1,
          title: '满额减',
          desc: '全场满300减50'
        },
        {
          id: 2,
          title: '满额减',
          desc: '全场满200减20'
        },
        {
          id: 3,
          title: '满额减',
          desc: '全场满100减5'
        }
      ],
      couponList: [
        {
          id: 0,
          title: '优惠券',
          desc: '满50减10'
        },
        {
          id: 1,
          title: '优惠券',
          desc: '满30减5'
        },
        {
          id: 2,
          title: '优惠券',
          desc: '满20减2'
        }
      ],
      currentEvanIndex: 0,
      evanTypeList: [
        {
          id: '0',
          name: '全部',
          count: 0
        },
        {
          id: '1',
          name: '好评',
          count: 0
        },
        {
          id: '2',
          name: '中评',
          count: 0
        },
        {
          id: '3',
          name: '差评',
          count: 0
        },
        {
          id: '4',
          name: '有图',
          count: 0
        }
      ]
    }
  },
  onLoad(e) {
    if (!e.id) {
      uni.$u.toast('请求参数错误')
      return;
    }

    // 加载商品详情
    this.spu.id = e.id
    this.loadProductData()
  },
  methods: {
    loadProductData() {
      getSpuDetail(this.spu.id).then(res => {
        // this.spu.desc = res.data.description.replace(/<[^>]*>/g,'');
        // console.log(res)
        this.spu = res.data;
      })
    },
    handleSkuItemClick(index) {
      this.currentSkuIndex = index
    },
    handleCouponClick() {
      // TODO 未登录去登录，登录则跳转优惠券页面
    },
    handleEvanTypeClick(index) {
      this.currentEvanIndex = index
      // TODO 展示评论
    }
  },
  computed: {
    hasLogin() {
      return this.$store.getters.hasLogin
    }
  }
}
</script>

<style lang="scss" scoped>
.indicator-num {
  @include flex-center;
  padding: 2px 0;
  background-color: rgba(0, 0, 0, 0.35);
  border-radius: 100px;
  width: 35px;

  &__text {
    color: #ffffff;
    font-size: 12px;
  }
}

.product-box {
  padding: 40rpx 40rpx 10rpx 40rpx;
  @include flex;
  border-bottom: $custom-border-style;
  .prod-info {
    padding-right: 30rpx;
    .info-text {
      padding-bottom: 10rpx;
    }
    .price-and-cart {
      @include flex-space-between;
    }
  }
  .prod-favor {
    margin-top: 15rpx;
  }
}

.row-box {
  @include flex-left;
  padding: 0 30rpx;
  height: 70rpx;
  .row-left {
    width: 70rpx;
    font-size: 24rpx;
    color: #939393;
  }

  .row-right {
    @include flex-space-between;
    flex: 1;

    .row-content {
      flex: 1;

      .prom-box {
        @include flex-left;
        .prom-item {
          @include flex-left;
          font-size: 22rpx;
          .prom-title {
            padding: 1rpx 10rpx;
            border: 1rpx solid red;
            border-radius: 5rpx;
            color: red;
            transform: scale(0.9);
          }
          .prom-desc {
            margin-left: 15rpx;
          }
        }
      }

      .coupon-box {
        @include flex-space-between;
        .coupon-list {
          @include flex-left;
          .coupon-item {
            @include flex-left;
            font-size: 22rpx;
            .coupon-desc {
              padding: 2rpx 15rpx;
              margin-right: 15rpx;
              background: red;
              color: #ffffff;
            }
          }
        }
        .coupon-total {
          color: #939393;
          font-size: 12rpx;
          padding: 0 15rpx;
        }
      }

      .sku-box {
        @include flex-space-between;
        .sku-item {
          @include flex-left;
          font-size: 22rpx;
          .sku-desc {
            margin-left: 15rpx;
            font-weight: 700;
          }
        }
      }
    }

    .row-more {
      @include flex-right;
      width: 30rpx;
    }
  }
}

.sku-popup-slot {
  width: 750rpx;
  .current-sku-info {
    @include flex;
    padding: 30rpx 100rpx 0 30rpx;
    .current-sku-img {
      border-radius: 10rpx;
      /deep/ * {
        border-radius: 10rpx;
      }
    }
    .current-sku-desc {
      padding: 0 30rpx;
      font-size: 28rpx;
      .current-sku-stock {
        height: 40rpx;
        line-height: 40rpx;
        color: #666666;
        font-size: 24rpx;
      }
    }
  }
  .sku-selection {
    margin: 30rpx;
    font-size: 26rpx;
    color: #939393;
    .sku-item {
      margin-bottom: 20rpx;
      border-radius: 6rpx;
      padding: 5rpx 15rpx;
      border: 1rpx solid #e3e3e3;
      width: fit-content !important;
      &.active {
        color: #666666;
        border: 1rpx solid #666666;
      }
    }
  }
  .sku-num-box {
    @include flex-space-between padding: 30rpx;
    .text {
      font-size: 30rpx;
    }
  }
  .sku-btn-group {
    @include flex-space-around;
    height: 100rpx;
    .btn-item-main {
      width: 350rpx;
    }
  }
}

.prom-popup-slot {
  width: 750rpx;
  min-height: 500rpx;
  .prom-info {
    background: #f3f3f3;
    line-height: 90rpx;
    padding-left: 30rpx;
    font-size: 36rpx;
    border-radius: 10px 10px 0 0;
  }
  .prom-list {
    padding: 30rpx;
    .prom-item {
      @include flex-left;
      font-size: 22rpx;
      margin-bottom: 15rpx;
      .prom-title {
        padding: 1rpx 10rpx;
        border: 1rpx solid red;
        border-radius: 5rpx;
        color: red;
        transform: scale(0.9);
      }
      .prom-desc {
        margin-left: 15rpx;
      }
    }
  }
}

.evaluation-box-wrap {
  background: #f3f3f3;

  .evaluation-box {
    border-radius: 20rpx 20rpx 0 0;
    background: $custom-bg-color;
    padding-bottom: 120rpx;

    .evaluation-title {
      border-radius: 20rpx 20rpx 0 0;
      padding: 20rpx 30rpx;
      border-bottom: $custom-border-style;
      font-size: 30rpx;
    }

    .evaluation-info {
    }

    .evan-type-list {
      padding: 20rpx;
      @include flex-space-around;

      .evan-type-item {
        border-radius: 8rpx;
        padding: 7rpx 12rpx;
        background: #f3f3f3;
        font-size: 22rpx;
        text-align: center;
        &.active {
          background: #ffffff;
          border: 1rpx solid red;
          padding: 5rpx 10rpx;
          color: red;
        }
      }
    }

    .comment-empty {
      margin-bottom: 100rpx;
    }
  }
}

.fixed-btn-box {
  position: fixed;
  bottom: 0;
  left: 0;
  .btn-group {
    background: $custom-bg-color;
    border-top: $custom-border-style;

    width: 750rpx;
    @include flex-space-around;
    height: 100rpx;

    .btn-item {
      width: 80rpx;
      @include flex-center(column);
      .btn-text {
        font-size: 18rpx;
        color: #666666;
      }
    }

    .btn-item-main {
      width: 200rpx;
    }
  }
}
</style>
