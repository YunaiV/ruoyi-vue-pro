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
      <view class="prod-collect">
        <u-icon name="heart" color="#2979ff" size="28"></u-icon>
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
      }
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

    }

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
  .prod-collect {
    margin-top: 15rpx;
  }
}



</style>
