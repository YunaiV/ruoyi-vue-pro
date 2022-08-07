<template>
  <view class="container">
    <view class="search-wrap">
      <u-search placeholder="搜索" disabled height="32" :show-action="false" @click="handleSearchClick"></u-search>
    </view>
    <view class="category-box">
      <view class="box-left">
        <view>
          <view class="category-item" v-for="(item, index) in categoryList" :key="item.id">
            <view class="item-title" :class="{ active: currentIndex === index }" @click="handleCategoryClick(index)">
              <text>{{ item.name }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="box-right">
        <image class="category-image" :showLoading="true" :src="categoryList[currentIndex].image" width="530rpx" height="160rpx" @click="click"></image>
        <view class="sub-category-box" v-for="(item, index) in categoryList[currentIndex].children" :key="item.id">
          <view class="sub-category-header">
            <view class="title">{{ item.title }}</view>
            <view class="more">查看更多</view>
          </view>
          <u-grid class="sub-category-grid" col="3">
            <u-grid-item v-for="(subItem, subIndex) in item.category" :key="subItem.id">
              <view class="sub-category-item">
                <u-icon name="photo" :size="80"></u-icon>
                <text class="sub-category-title">{{ subItem.title }}</text>
              </view>
            </u-grid-item>
          </u-grid>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentIndex: 0,
      categoryList: []
    }
  },
  onLoad() {
    for (let i = 0; i < 10; i++) {
      this.categoryList.push({
        id: i,
        image: 'https://cdn.uviewui.com/uview/swiper/swiper1.png',
        name: '商品分类' + i,
        children: [
          {
            id: 0,
            title: '分类' + i + '-1',
            category: [
              {
                id: 0,
                image: '',
                title: '分类' + i + '-1-1'
              },
              {
                id: 2,
                image: '',
                title: '分类' + i + '-1-2'
              },
              {
                id: 3,
                image: '',
                title: '分类' + i + '-1-3'
              }
            ]
          },
          {
            id: 1,
            title: '分类' + i + '-2',
            category: [
              {
                id: 0,
                image: '',
                title: '分类' + i + '-2-1'
              },
              {
                id: 2,
                image: '',
                title: '分类' + i + '-2-2'
              },
              {
                id: 3,
                image: '',
                title: '分类' + i + '-2-3'
              }
            ]
          }
        ]
      })
    }
  },
  methods: {
    handleSearchClick(e) {
      uni.$u.route('/pages/search/search')
    },
    handleCategoryClick(index) {
      if (this.currentIndex !== index) {
        this.currentIndex = index
      }
    }
  }
}
</script>

<style lang="scss" scoped>

.search-wrap {
  background: $custom-bg-color;
  padding: 20rpx;
}

.category-box {
  display: flex;
  .box-left {
    width: 200rpx;
    padding-top: 20rpx;
    border-right: $custom-border-style;
    .category-item {
      border-bottom: $custom-border-style;
      padding: 20rpx 0;
      .item-title {
        padding-left: 30rpx;
        font-size: 28rpx;
        &.active {
          border-left: 6rpx solid $u-primary;
          font-weight: 700;
        }
      }
    }
  }
  .box-right {
    flex: 1;
    .category-image {
      width: 510rpx;
      height: 160rpx;
      padding: 20rpx;
    }

    .sub-category-box {
      .sub-category-header {
        @include flex-space-between;
        padding: 30rpx 20rpx;

        .title {
          font-size: 28rpx;
          font-weight: 700;
        }
        .more {
          font-size: 22rpx;
          color: #939393;
        }
      }

      .sub-category-grid {
        padding: 0 15rpx;

        .sub-category-item {
          @include flex-center(column);
          background: #fff;

          .sub-category-title {
            margin: 15rpx 0;
            font-size: 24rpx;
          }
        }
      }
    }
  }
}
</style>
