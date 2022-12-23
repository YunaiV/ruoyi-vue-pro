<template>
  <view class="container">
    <!-- 搜索框 -->
    <view class="search-wrap">
      <u-search placeholder="搜索" disabled height="32" bgColor="#f2f2f2" margin="0 20rpx" :show-action="false"
        @click="handleSearchClick"></u-search>
    </view>

    <!-- 分类内容 -->
    <view class="category-box">
      <!-- 左侧导航栏 -->
      <scroll-view scroll-y="true" class='box-left'>
        <view class="category-item" v-for="(item, index) in categoryList" :key="item.id">
          <view class="item-title" :class="{ active: currentIndex === index }" @click="handleCategoryClick(index)">
            <text>{{ item.name }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 右侧分类内容 -->
      <scroll-view scroll-y="true" class="box-right">
        <view class="category-image">
          <image :showLoading="true" :src="categoryList[currentIndex].picUrl" mode='widthFix' @click="click"></image>
        </view>

        <view class="sub-category-box" v-for="(item, index) in categoryList[currentIndex].children" :key="item.id">
          <view class="sub-category-header">
            <view class="title">{{ item.name }}</view>
            <view class="more" @click="handleCategory(item, 0)">查看更多</view>
          </view>

          <view class="sub-category-grid">
            <u-grid col="3">
              <u-grid-item v-for="(subItem, subIndex) in item.children" :key="subItem.id">
                <view class="sub-category-item" @click="handleCategory(item, subIndex)">
                  <u-icon name="photo" :size="80" v-if="subItem.picUrl === null"></u-icon>
                  <image :src="item.picUrl" v-if="subItem.picUrl != null" mode='widthFix' />
                  <text class="sub-category-title">{{ subItem.name }}</text>
                </view>
              </u-grid-item>
            </u-grid>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
  import { categoryListData } from '../../api/category';
  import { handleTree, convertTree } from '../../utils/tree.js';

  export default {
    data() {
      return {
        currentIndex: 0,
        categoryList: []
      }
    },
    onLoad() {
      this.handleCategoryList();
    },
    methods: {
      // 点击搜索框
      handleSearchClick(e) {
        uni.$u.route('/pages/search/search')
      },
      // 点击左侧导航栏
      handleCategoryClick(index) {
        if (this.currentIndex !== index) {
          this.currentIndex = index
        }
      },
      // 获取分类列表并构建树形结构
      handleCategoryList() {
        categoryListData().then(res => {
          this.categoryList = handleTree(res.data, "id", "parentId");
        })
      },
      handleCategory(item, index){
        // console.log(item)
        // console.log(index)
        uni.navigateTo({
          url:"./product-list?item="+encodeURIComponent(JSON.stringify(item))+"&index="+index
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .search-wrap {
    background: #ffffff;
    position: fixed;
    top: 0;
    left: 0;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.07);
    padding: 20rpx 0;
    width: 100%;
    z-index: 3;
  }

  .category-box {
    position: fixed;
    display: flex;
    overflow: hidden;
    margin-top: 100rpx;
    height: calc(100% - 100rpx);

    .box-left {
      width: 200rpx;
      padding-top: 5rpx;
      overflow: scroll;
      z-index: 2;
      background-color: #f2f2f2;

      .category-item {
        line-height: 80rpx;
        height: 80rpx;
        text-align: center;
        color: #777;

        .item-title {
          font-size: 28rpx;

          &.active {
            font-size: 28rpx;
            font-weight: bold;
            position: relative;
            background: #fff;
            color: $u-primary;
          }

          &.active::before {
            position: absolute;
            left: 0;
            content: "";
            width: 8rpx;
            height: 32rpx;
            top: 25rpx;
            background: $u-primary;
          }
        }
      }
    }

    .box-right {
      width: 550rpx;
      height: 100%;
      box-sizing: border-box;
      z-index: 1;

      .category-image {
        width: 510rpx;
        box-sizing: border-box;
        overflow: hidden;
        position: relative;
        margin: 30rpx 20rpx 0;

        image {
          width: 100%;
        }
      }

      .sub-category-box {
        .sub-category-header {
          @include flex-space-between;
          padding: 20rpx 20rpx;

          .title {
            font-size: 28rpx;
            font-weight: bolder;
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

            image {
              text-align: center;
              width: 150rpx;
              height: 150rpx;
              line-height: 150rpx;
              font-size: 0;
            }

            .sub-category-title {
              margin: 15rpx 0;
              font-size: 22rpx;
            }
          }
        }
      }
    }
  }
</style>
