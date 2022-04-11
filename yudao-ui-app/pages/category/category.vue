<template>
	<view class="container">
    <view class="search-wrap">
      <u-search placeholder="搜索" disabled height="32" :show-action="false" @click="handleSearchClick"></u-search>
    </view>
    <view class="category-box">
      <view class="box-left">
        <u-list @scrolltolower="scrolltolower">
          <u-list-item class="category-item" v-for="(item, index) in categoryList" :key="index">
            <view class="item-title" :class="index === currentIndex ? 'active' : ''" @click="handleCategoryClick(index)">
              <text>{{item.name}}</text>
            </view>
          </u-list-item>
        </u-list>
      </view>
      <view class="box-right">
        <view class="category-image">
          <u--image :showLoading="true" :src="categoryList[currentIndex].image" width="530rpx" height="160rpx" @click="click"></u--image>
        </view>
        <view>
          <u-list class="prod-list" @scrolltolower="scrolltolower">
            <u-list-item v-for="(item, index) in productList" :key="index">
              <view class="prod-item" @click="handleProdItemClick(item.id)">
                <u--image class="prod-image" width="140rpx" height="140rpx" :src="item.image"></u--image>
                <view class="item-info">
                  <view class="info-text">
                    <u--text :lines="1" size="14px" color="#333333" :text="item.title"></u--text>
                    <u-gap height="2px"></u-gap>
                    <u--text :lines="1" size="12px" color="#939393" :text="item.desc"></u--text>
                  </view>
                  <view class="price-and-cart">
                    <u--text-price color="red" size="12" intSize="18" :text="item.price"></u--text-price>
                    <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
                  </view>
                </view>
              </view>
            </u-list-item>
          </u-list>
        </view>
      </view>
    </view>
	</view>
</template>

<script>
	import UText from "../../uni_modules/uview-ui/components/u-text/u-text";
  export default {
    components: {UText},
    data() {
			return {
        currentIndex: 0,
        categoryList: [
          {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper1.png',
            name: '关注'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper2.png',
            name: '推荐'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper3.png',
            name: '电影'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper1.png',
            name: '科技'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper2.png',
            name: '音乐'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper3.png',
            name: '美食'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper1.png',
            name: '文化'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper2.png',
            name: '财经'
          }, {
            id: 1,
            image: 'https://cdn.uviewui.com/uview/swiper/swiper3.png',
            name: '手工'
          }],
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
        ]
			}
		},
		onLoad() {
		},
		methods: {
      handleSearchClick(e) {
        console.log('监听点击准备跳转页面')
      },
      handleCategoryClick(index){
        if (this.currentIndex !== index) {
          this.currentIndex = index;
          // TODO 查询下分类商品
        }
      },
      handleProdItemClick(productId){
        uni.$u.route('/pages/product/product', {
          productId: productId
        });
      }
		}
	}
</script>

<style lang="scss" scoped>

.search-wrap {
  background: $custom-bg-color;
  padding: 20rpx;
}

.category-box{
  display: flex;
  .box-left{
    width: 180rpx;
    padding-top: 20rpx;
    border-right: $custom-border-style;
    .category-item{
      border-bottom: $custom-border-style;
      padding: 20rpx 0;
      .item-title{
        padding-left: 30rpx;
        font-size: 30rpx;
        &.active{
          border-left: 6rpx solid $u-primary;
          font-weight: 700;
        }
      }
    }
  }
  .box-right{
    width: 550rpx;
    padding-right: 20rpx;
    .category-image{
      padding: 20rpx;
    }

    .prod-list {
      height: auto !important;

      .prod-item {
        padding: 10rpx 20rpx;
        background: #fff;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        border-bottom: $custom-border-style;

        .prod-image {
          border-radius: 10rpx;
          /deep/ * {
            border-radius: 10rpx;
          }
        }

        .item-info {
          width: 350rpx;
          padding: 5rpx;
          .info-text {
            height: 70rpx;
            padding-bottom: 10rpx;
          }
          .price-and-cart {
            display: flex;
            justify-content: space-between;
          }
        }
      }
    }
  }
}
</style>
