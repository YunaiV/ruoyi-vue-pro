<template>
  <view class="container">
    <u-navbar :title="title" :autoBack="true" placeholder="true" titleStyle="font-size: 28rpx">
    </u-navbar>
    <view class="context">
      <!-- 分类列表 -->
      <!-- TODO @Luowenfeng：不应该展示商品分类；应该是上面一个筛选；之后是【综合】【销量】【价格】的排序 -->
      <view class="tabs-top">
        <u-tabs :list="categoryList" @click="changeTabs" :current="current" lineHeight="2" lineWidth="85rpx"
          itemStyle="padding-left: 15px; padding-right: 15px; height: 85rpx;"></u-tabs>
      </view>
      <!-- 商品列表 -->
      <scroll-view scroll-y="true" class="product-list" enable-flex="true">
        <view class="flex-box">
          <block v-for="(item, index) in productList[current]" :key="index">
            <view class="product-item">
              <view class="product-image">
                <image :src="item.picUrls[0]" mode='widthFix' />
              </view>
              <view class="product-button">
                <view class="product-text">{{ item.name }}</view>
                <view class="product-price-button">
                  <text class="product-price">￥
                    <text class="price-size">{{ towNumber(item.minPrice) }}</text></text>
                  <text class="product-like-count">销量 {{ item.salesCount }}</text>
                </view>
              </view>
            </view>
          </block>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
  import {
    productSpuPage
  } from '../../api/product';

  export default {
    data() {
      return {
        title: "",
        current: 0,
        categoryList: [],
        productList: {}
      }
    },
    onLoad(option) {
      const item = JSON.parse(decodeURIComponent(option.item))
      this.title = item.name
      this.categoryList = item.children
      this.handleProductSpu(option.index);
    },
    methods: {
      changeTabs(item) {
        if (item.index !== this.current) {
          this.handleProductSpu(item.index)
        }
      },
      handleProductSpu(index) {
        let param = {}
        param.categoryId = this.categoryList[index].id
        console.log(this.categoryList)
        console.log(index)
        productSpuPage(param).then(res => {
          this.productList[index] = res.data.list
          this.current = index
        })
      },
      towNumber(val) {
        return (val / 100).toFixed(2)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .context {
    width: 100vw;
    position: fixed;
    top: 160rpx;
    left: 0;
  }

  .tabs-top {
    position: relative;
    top: 0;
    width: 100%;
    height: 85rpx;
  }

  .product-list {
    position: relative;
    background-color: #f2f2f2;
    height: calc(100vh - 88rpx - 100rpx - var(--status-bar-height));
    width: 100%;

    .flex-box {
      width: 730rpx;
      margin: 0 auto;
      @include flex;
      flex-wrap: wrap;
      justify-content: left;

      .product-item {
        width: 345rpx;
        height: 450rpx;
        background-color: #ffffff;
        margin: 20rpx 10rpx 0;
        border-radius: 20rpx;

        .product-image {
          width: 100%;
          height: 300rpx;
          overflow: hidden;
          border-radius: 20rpx;
          image {
            width: 100%;
          }
        }

        .product-button {
          width: 330rpx;
          margin: 15rpx auto 0;

          .product-text {
            font-size: 25rpx;
            height: 70rpx;
            overflow: hidden;
            -webkit-line-clamp: 2;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-box-orient: vertical;
          }

          .product-price-button {
            font-size: 20rpx;
            margin-top: 20rpx;

            .product-price {
              color: red;

              .price-size {
                font-size: 26rpx;
              }
            }

            .product-like-count {
              font-size: 16rpx;
              margin-left: 10rpx;
            }
          }

        }
      }

    }

    // }

  }
</style>
