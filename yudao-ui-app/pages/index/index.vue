<template>
  <view class="container">
    <!--搜索栏-->
    <u-sticky style="top: 0" offset-top="0">
      <view class="search-wrap">
        <u-search placeholder="搜索" disabled height="32" :show-action="false" @click="handleSearchClick"></u-search>
      </view>
    </u-sticky>

    <!--轮播图-->
    <u-swiper :list="swiperList" previousMargin="20" nextMargin="20" circular height="200" @change="e => (current = e.current)" :autoplay="true" @click="handleSwiperClick">
      <view slot="indicator" class="indicator">
        <view class="indicator__dot" v-for="(item, index) in swiperList" :key="index" :class="[index === current && 'indicator__dot--active']"> </view>
      </view>
    </u-swiper>

    <u-gap height="20px"></u-gap>

    <!--宫格菜单按钮-->
    <u-grid :border="false" col="4"
      ><u-grid-item v-for="(item, index) in menuList" :key="index">
        <u-icon :name="item.icon" :size="40"></u-icon>
        <text class="grid-title">{{ item.title }}</text>
      </u-grid-item>
    </u-grid>

    <u-gap height="15px"></u-gap>

    <!--消息滚动栏-->
    <u-notice-bar style="padding: 13px 12px" :text="noticeList" mode="link" direction="column" @click="click"></u-notice-bar>

    <!--商品展示栏-->
    <view>
      <u-gap height="180" bgColor="#398ade"></u-gap>
      <view class="prod-block">
        <view class="bloc-header">
          <text class="bloc-title">每日上新</text>
          <text class="see-more">查看更多</text>
        </view>
        <view class="prod-grid">
          <view class="prod-item" v-for="(item, index) in productList" :key="item.id" @click="handleProdItemClick(item.id)">
            <image class="prod-image" :src="item.image"></image>
            <view class="item-info">
              <view class="info-text">
                <u--text :lines="2" size="14px" color="#333333" :text="item.title"></u--text>
              </view>
              <view class="price-and-cart">
                <custom-text-price color="red" size="12" intSize="18" :price="item.price"></custom-text-price>
                <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view>
      <view class="prod-block half">
        <view class="bloc-header">
          <text class="bloc-title">商品热卖</text>
          <text class="more">更多 &gt;</text>
        </view>
        <view class="prod-grid half">
          <view class="prod-item" v-for="(item, index) in productList" :key="item.id" @click="handleProdItemClick(item.id)">
            <image class="prod-image" :src="item.image"></image>
            <view class="item-info">
              <view class="info-text">
                <u--text :lines="1" size="14px" color="#333333" :text="item.title"></u--text>
                <u--text :lines="1" size="12px" color="#939393" :text="item.desc"></u--text>
              </view>
              <view class="price-and-cart">
                <custom-text-price color="red" size="12" intSize="18" :price="item.price"></custom-text-price>
                <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view>
      <view class="prod-block list">
        <view class="bloc-header">
          <text class="bloc-title">更多宝贝</text>
          <text></text>
        </view>

        <view class="prod-list" v-for="(item, index) in productList" :key="item.id">
          <view class="prod-item" @click="handleProdItemClick(item.id)">
            <image class="prod-image" :src="item.image"></image>
            <view class="item-info">
              <view class="info-text">
                <u--text :lines="1" size="14px" color="#333333" :text="item.title"></u--text>
                <u-gap height="2px"></u-gap>
                <u--text class="info-desc" :lines="2" size="12px" color="#939393" :text="item.desc"></u--text>
              </view>
              <view class="price-and-cart">
                <custom-text-price color="red" size="12" intSize="18" :price="item.price"></custom-text-price>
                <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    <!--加载更多-->
    <u-loadmore fontSize="28rpx" :status="status" :loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText" />
  </view>
</template>

<script>
import { getBannerData, getNoticeData } from '../../api/index'

export default {
  components: {},
  data() {
    return {
      current: 0,
      currentNum: 0,
      bannerList: ['https://cdn.uviewui.com/uview/swiper/swiper3.png', 'https://cdn.uviewui.com/uview/swiper/swiper2.png', 'https://cdn.uviewui.com/uview/swiper/swiper1.png'],
      menuList: [
        { icon: 'gift', title: '热门推荐' },
        { icon: 'star', title: '收藏转发' },
        { icon: 'thumb-up', title: '点赞投币' },
        { icon: 'heart', title: '感谢支持' }
      ],
      noticeList: ['寒雨连江夜入吴', '平明送客楚山孤', '洛阳亲友如相问', '一片冰心在玉壶'],
      productList: [
        {
          id: 1,
          image: 'https://cdn.uviewui.com/uview/album/1.jpg',
          title: '山不在高，有仙则名。水不在深，有龙则灵。斯是陋室，惟吾德馨。',
          desc: '山不在于高，有了神仙就会有名气。水不在于深，有了龙就会有灵气。这是简陋的房子，只是我品德好就感觉不到简陋了。',
          price: '13.00'
        },
        {
          id: 2,
          image: 'https://cdn.uviewui.com/uview/album/2.jpg',
          title: '商品222',
          desc: '',
          price: '23.00'
        },
        {
          id: 3,
          image: 'https://cdn.uviewui.com/uview/album/3.jpg',
          title: '商品333',
          desc: '商品描述信息2',
          price: '33.00'
        },
        {
          id: 4,
          image: 'https://cdn.uviewui.com/uview/album/4.jpg',
          title: '商品444',
          desc: '商品描述信息4',
          price: '43.00'
        },
        {
          id: 5,
          image: 'https://cdn.uviewui.com/uview/album/5.jpg',
          title: '商品555',
          desc: '商品描述信息5',
          price: '53.00'
        }
      ],
      status: 'nomore',
      loadingText: '努力加载中...',
      loadmoreText: '轻轻上拉',
      nomoreText: '实在没有了...'
    }
  },
  onLoad() {
    //this.loadBannerData();
    //this.loadNoticeData();
  },
  methods: {
    loadBannerData() {
      getBannerData().then(res => {
        this.bannerList = res.data
      })
    },
    loadNoticeData() {
      getNoticeData().then(res => {
        this.noticeList = res.data
      })
    },
    handleSearchClick(e) {
      uni.$u.route('/pages/search/search')
    },
    handleSwiperClick(index) {
      console.log('点击了图片索引值：', index)
    },
    handleProdItemClick(productId) {
      uni.$u.route('/pages/product/product', {
        productId: productId
      })
    }
  },
  computed: {
    swiperList() {
      return this.bannerList.map(item => {
        if (item) {
          return item
        }
      })
    },
    noticeTextList() {
      return this.noticeList.map(item => {
        if (item.title) {
          return item.title
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.search-wrap {
  background: $custom-bg-color;
  padding: 20rpx;
}

.indicator {
  @include flex(row);
  justify-content: center;

  &__dot {
    height: 15rpx;
    width: 15rpx;
    border-radius: 100rpx;
    background-color: rgba(255, 255, 255, 0.35);
    margin: 0 10rpx;
    transition: background-color 0.3s;

    &--active {
      background-color: $custom-bg-color;
    }
  }
}

.grid-title {
  line-height: 50rpx;
  font-size: 26rpx;
}

.prod-block {
  margin-top: -160px;
  .bloc-header {
    @include flex-space-between;
    padding: 10rpx 20rpx;

    .bloc-title {
      color: $custom-bg-color;
      font-size: 34rpx;
    }
    .see-more {
      color: $custom-bg-color;
      background: $u-primary;
      padding: 0 30rpx;
      height: 50rpx;
      line-height: 50rpx;
      border-radius: 50rpx;
      font-size: 24rpx;
    }
  }

  &.half,
  &.list {
    margin-top: 0;
    .bloc-header {
      margin-top: 50rpx;
      margin-bottom: 20rpx;
      .bloc-title {
        color: #333333;
      }
      .more {
        font-size: 24rpx;
      }
    }
  }

  .prod-grid {
    width: 730rpx;
    margin: 0 auto;
    @include flex;
    flex-wrap: wrap;
    justify-content: left;

    &.half {
      .prod-item {
        width: 346rpx;
        margin: 10rpx;
        .prod-image {
          width: 346rpx;
          height: 346rpx;
        }
      }
    }

    .prod-item {
      width: 224rpx;
      margin: 10rpx;
      background: #ffffff;
      border-radius: 10rpx;
      box-shadow: -1rpx 1rpx 2rpx #afd3f5, 1rpx 1rpx 0rpx #afd3f5;
      .prod-image {
        width: 224rpx;
        height: 224rpx;
        border-radius: 10rpx 10rpx 0 0;
      }
      .item-info {
        padding: 15rpx;
        .info-text {
          height: 70rpx;
          padding-bottom: 10rpx;
        }
        .price-and-cart {
          @include flex-space-between;
        }
      }
    }
  }
}

.prod-list {
  .prod-item {
    background: #ffffff;
    @include flex-space-between;
    border-bottom: $custom-border-style;
    padding: 20rpx;
    .prod-image {
      width: 200rpx;
      height: 200rpx;
      border-radius: 10rpx;
    }

    .item-info {
      flex: 1;
      padding: 20rpx 20rpx 0;
      .info-text {
        height: 100rpx;
        padding-bottom: 10rpx;
      }
      .price-and-cart {
        @include flex-space-between;
      }
    }
  }
}
</style>
