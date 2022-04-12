<template>
  <view class="container">
    <u-swiper :list="product.images" @change="e => currentNum = e.current" :autoplay="false" height="750rpx" radius="0" indicatorStyle="right: 20px">
      <view slot="indicator" class="indicator-num">
        <text class="indicator-num__text">{{ currentNum + 1 }}/{{ product.images.length }}</text>
      </view>
    </u-swiper>

    <view class="product-box">
      <view class="prod-info">
        <view class="info-text">
          <u--text :lines="2" size="14px" color="#333333" :text="product.title"></u--text>
          <u-gap height="5px"></u-gap>
          <u--text :lines="3" size="12px" color="#939393" :text="product.desc"></u--text>
        </view>
        <view class="price-and-cart">
          <u--text-price color="red" size="16" intSize="26" :text="product.price"></u--text-price>
        </view>
      </view>
      <view class="prod-favor">
        <u-icon name="star" color="#2979ff" size="28"></u-icon>
      </view>

    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="info-box">
      <text class="info-title">配送</text>
<!--      <text>快递配送</text>
      <text>到店自提</text>-->
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="info-box" @click="showPromPopup">
      <text class="info-title">促销</text>
    </view>

    <view>
      <u-popup :show="promotionPopup" :round="10" :closeable="true" :closeOnClickOverlay="false" @close="closePromPopup">
        <view class="prom-popup-slot">
          <view class="prom-title">促销信息</view>
          <view class="prom-list">
            <view class="prom-item">全场满500减100</view>
            <view class="prom-item">全场满300减50</view>
            <view class="prom-item">全场满200减20</view>
            <view class="prom-item">全场满100减5</view>
          </view>
        </view>
      </u-popup>
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="info-box">
      <text class="info-title">领券</text>
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="info-box">
      <text class="info-title">已选</text>
    </view>

    <u-gap height="8" bgColor="#f3f3f3"></u-gap>
    <view class="evaluation-box-wrap">
      <view class="evaluation-box">
        <view class="evaluation-title">评价</view>
        <view class="evaluation-info">
          <view class="evan-type-list">
            <view class="evan-type-item" v-for="(item, index) in evanTypeList" :key="index" @click="handleEvanTypeClick(index)">
              {{item.name}}({{item.count}})
            </view>
          </view>
          <view class="comment-empty" v-if="true">
            <u-empty mode="comment" icon="/static/images/empty/comment.png"></u-empty>
          </view>
          <view v-else class="comment-list" style="min-height: 50px">

          </view>
        </view>
      </view>
    </view>

    <view class="fixed-btn-box">
      <view class="btn-group">
        <view class="btn-item">
          <u-icon name="home" :size="24"></u-icon>
          <view class="btn-text">首页</view>
        </view>
        <view class="btn-item">
          <u-icon name="server-man" :size="24"></u-icon>
          <view class="btn-text">客服</view>
        </view>
        <view class="btn-item">
          <u-icon name="bag" :size="24"></u-icon>
          <view class="btn-text">购物车</view>
        </view>
        <view style="width: 200rpx">
          <u-button type="warning" shape="circle" size="small" text="加入购物车"></u-button>
        </view>
        <view style="width: 200rpx">
          <u-button type="error" shape="circle" size="small" text="立即购买"></u-button>
        </view>
      </view>
    </view>


  </view>

</template>

<script>
export default {
  data() {
    return {
      current: 0,
      currentNum: 0,
      product: {
        id: '',
        images: ['https://cdn.uviewui.com/uview/album/1.jpg', 'https://cdn.uviewui.com/uview/album/2.jpg', 'https://cdn.uviewui.com/uview/album/3.jpg'],
        title: '山不在高，有仙则名。水不在深，有龙则灵。斯是陋室，惟吾德馨。',
        desc: '山不在于高，有了神仙就会有名气。水不在于深，有了龙就会有灵气。这是简陋的房子，只是我品德好就感觉不到简陋了。',
        price: '13.00'
      },
      promotionPopup: false,
      currentEvanType: 0,
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
        },
      ]
    }
  },
  onLoad(e) {
    if (!e.productId) {
      uni.$u.toast('请求参数错误');
    } else {
      this.product.id = e.productId;
      this.loadProductData();
      // TODO 请求接口获取商品详情数据
    }
    console.log(e);
  },
  methods: {
    loadProductData(){

    },
    showPromPopup() {
      this.promotionPopup = true
    },
    closePromPopup(){
      this.promotionPopup = false
    },
    handleEvanTypeClick(e) {
      this.currentEvanType = e.index;
    },

  },
  computed: {

  }
}
</script>

<style lang="scss" scoped>

.indicator-num {
  padding: 2px 0;
  background-color: rgba(0, 0, 0, 0.35);
  border-radius: 100px;
  width: 35px;
  @include flex;
  justify-content: center;

  &__text {
    color: #FFFFFF;
    font-size: 12px;
  }
}

.product-box {
  padding: 40rpx 40rpx 10rpx 40rpx;
  display: flex;
  border-bottom: $custom-border-style;
  .prod-info {
    padding-right: 30rpx;
    .info-text {
      padding-bottom: 10rpx;
    }
    .price-and-cart {
      display: flex;
      justify-content: space-between;
    }
  }
  .prod-favor {
    margin-top: 15rpx;
  }
}

.info-box {
  padding: 15rpx 30rpx;
  .info-title {
    font-size: 30rpx;
  }
}

.prom-popup-slot {
  width: 750rpx;
  min-height: 500rpx;
  .prom-title {
    background: #f3f3f3;
    line-height: 100rpx;
    padding-left: 30rpx;
    font-size: 36rpx;
    border-radius: 10px 10px 0 0;
  }
  .prom-list{
    padding: 30rpx;
    .prom-item{
      line-height: 40rpx;
      font-size: 24rpx;
    }
  }
}

.evaluation-box-wrap {
  background: #f3f3f3;

  .evaluation-box {
    border-radius: 20rpx 20rpx 0 0;
    background: $custom-bg-color;
    padding-bottom: 120rpx;

    .evaluation-title{
      border-radius: 20rpx 20rpx 0 0;
      padding: 20rpx 30rpx;
      border-bottom: $custom-border-style;
      font-size: 30rpx;
    }

    .evaluation-info{

    }

    .evan-type-list{
      padding: 20rpx;
      display: flex;
      align-items: center;
      justify-content: space-around;

      .evan-type-item {
        border-radius: 8rpx;
        padding: 8rpx 12rpx;
        background: #f3f3f3;
        font-size: 12rpx;
        text-align: center;
      }
    }

    .comment-empty {

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
    display: flex;
    align-items: center;
    justify-content: space-around;
    height: 100rpx;

    .btn-item {
      width: 80rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      .btn-text {
        font-size: 12rpx;
        color: #666666;
      }
    }
  }
}


</style>
