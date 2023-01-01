<template>
  <view class="container">
    <!--搜索栏-->
    <u-sticky style="top: 0" offset-top="0">
      <view class="search-wrap">
        <u-search placeholder="搜索" disabled height="32" :show-action="false" @click="handleSearchClick"></u-search>
      </view>
    </u-sticky>

    <!--轮播图-->
    <yd-banner :banner-list="bannerList"></yd-banner>

    <u-gap height="20px"></u-gap>

    <!--宫格菜单按钮-->
    <u-grid :border="false" col="4">
      <u-grid-item v-for="(item, index) in menuList" :key="index">
        <u-icon :name="item.icon" :size="40"></u-icon>
        <text class="grid-title">{{ item.title }}</text>
      </u-grid-item>
    </u-grid>

    <u-gap height="15px"></u-gap>

    <!--消息滚动栏-->
    <u-notice-bar style="padding: 13px 12px" :text="noticeList" mode="link" direction="column" @click="click"></u-notice-bar>

    <!--商品展示栏-->
    <yd-product-box :product-list="productList" :title="'每日上新'" show-type="normal"></yd-product-box>
    <yd-product-box :product-list="productList" :title="'热卖商品'" show-type="half"></yd-product-box>
    <yd-product-more :product-list="productList" :more-status="moreStatus"></yd-product-more>

    <u-gap height="5px"></u-gap>
  </view>
</template>

<script>
import { getBannerData, getNoticeData } from '../../api/index'

export default {
  components: {},
  data() {
    return {
      bannerList: [
        {
          id: 1,
          title: '山不在高，有仙则名',
          url: 'https://cdn.uviewui.com/uview/swiper/swiper1.png'
        },
        {
          id: 2,
          title: '水不在深，有龙则灵',
          url: 'https://cdn.uviewui.com/uview/swiper/swiper2.png'
        },
        {
          id: 3,
          title: '斯是陋室，惟吾德馨',
          url: 'https://cdn.uviewui.com/uview/swiper/swiper3.png'
        }
      ],
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
      moreStatus: 'nomore'
    }
  },
  onLoad() {
    this.loadBannerData()
    this.loadNoticeData()
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
    }
  },
  computed: {
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

.grid-title {
  line-height: 50rpx;
  font-size: 26rpx;
}
</style>
