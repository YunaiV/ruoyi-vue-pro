<!-- 商品分类列表 -->
<template>
  <s-layout :bgStyle="{ color: '#fff' }" tabbar="/pages/index/category" title="分类">
    <view class="s-category">
      <view class="three-level-wrap ss-flex ss-col-top">
        <!-- 商品分类（左） -->
        <view class="side-menu-wrap" :style="[{ top: Number(statusBarHeight + 88) + 'rpx' }]">
          <scroll-view scroll-y :style="[{ height: pageHeight + 'px' }]">
            <view
              class="menu-item ss-flex"
              v-for="(item, index) in state.categoryList"
              :key="item.id"
              :class="[{ 'menu-item-active': index === state.activeMenu }]"
              @tap="onMenu(index)"
            >
              <view class="menu-title ss-line-1">
                {{ item.name }}
              </view>
            </view>
          </scroll-view>
        </view>
        <!-- 商品分类（右） -->
        <view class="goods-list-box" v-if="state.categoryList?.length">
          <scroll-view scroll-y :style="[{ height: pageHeight + 'px' }]">
            <image
              v-if="state.categoryList[state.activeMenu].picUrl"
              class="banner-img"
              :src="sheep.$url.cdn(state.categoryList[state.activeMenu].picUrl)"
              mode="widthFix"
            />
            <first-one v-if="state.style === 'first_one'" :pagination="state.pagination" />
            <first-two v-if="state.style === 'first_two'" :pagination="state.pagination" />
            <second-one
              v-if="state.style === 'second_one'"
              :data="state.categoryList"
              :activeMenu="state.activeMenu"
            />
            <uni-load-more
              v-if="
                (state.style === 'first_one' || state.style === 'first_two') &&
                state.pagination.total > 0
              "
              :status="state.loadStatus"
              :content-text="{
                contentdown: '点击查看更多',
              }"
              @tap="loadMore"
            />
          </scroll-view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import secondOne from './components/second-one.vue';
  import firstOne from './components/first-one.vue';
  import firstTwo from './components/first-two.vue';
  import sheep from '@/sheep';
  import CategoryApi from '@/sheep/api/product/category';
  import SpuApi from '@/sheep/api/product/spu';
  import { onLoad } from '@dcloudio/uni-app';
  import { computed, reactive } from 'vue';
  import _ from 'lodash-es';
  import { handleTree } from '@/sheep/helper/utils';

  const state = reactive({
    style: 'second_one', // first_one（一级 - 样式一）, first_two（二级 - 样式二）, second_one（二级）
    categoryList: [], // 商品分类树
    activeMenu: 0, // 选中的一级菜单，在 categoryList 的下标

    pagination: {
      // 商品分页
      list: [], // 商品列表
      total: [], // 商品总数
      pageNo: 1,
      pageSize: 6,
    },
    loadStatus: '',
  });

  const { safeArea } = sheep.$platform.device;
  const pageHeight = computed(() => safeArea.height - 44 - 50);
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;

  // 加载商品分类
  async function getList() {
    const { code, data } = await CategoryApi.getCategoryList();
    if (code !== 0) {
      return;
    }
    state.categoryList = handleTree(data);
  }

  // 选中菜单
  const onMenu = (val) => {
    state.activeMenu = val;
    if (state.style === 'first_one' || state.style === 'first_two') {
      state.pagination.pageNo = 1;
      state.pagination.list = [];
      state.pagination.total = 0;
      getGoodsList();
    }
  };

  // 加载商品列表
  async function getGoodsList() {
    // 加载列表
    state.loadStatus = 'loading';
    const res = await SpuApi.getSpuPage({
      categoryId: state.categoryList[state.activeMenu].id,
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
    });
    if (res.code !== 0) {
      return;
    }
    // 合并列表
    state.pagination.list = _.concat(state.pagination.list, res.data.list);
    state.pagination.total = res.data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  // 加载更多商品
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getGoodsList();
  }

  onLoad(async (params) => {
    await getList();

    // 首页点击分类的处理：查找满足条件的分类
    const foundCategory = state.categoryList.find((category) => category.id === Number(params.id));
    // 如果找到则调用 onMenu 自动勾选相应分类，否则调用 onMenu(0) 勾选第一个分类
    onMenu(foundCategory ? state.categoryList.indexOf(foundCategory) : 0);
  });

  function handleScrollToLower() {
    loadMore();
  }
</script>

<style lang="scss" scoped>
  .s-category {
    :deep() {
      .side-menu-wrap {
        width: 200rpx;
        height: 100%;
        padding-left: 12rpx;
        background-color: #f6f6f6;
        position: fixed;
        left: 0;

        .menu-item {
          width: 100%;
          height: 88rpx;
          position: relative;
          transition: all linear 0.2s;

          .menu-title {
            line-height: 32rpx;
            font-size: 30rpx;
            font-weight: 400;
            color: #333;
            margin-left: 28rpx;
            position: relative;
            z-index: 0;

            &::before {
              content: '';
              width: 64rpx;
              height: 12rpx;
              background: linear-gradient(
                90deg,
                var(--ui-BG-Main-gradient),
                var(--ui-BG-Main-light)
              ) !important;
              position: absolute;
              left: -64rpx;
              bottom: 0;
              z-index: -1;
              transition: all linear 0.2s;
            }
          }

          &.menu-item-active {
            background-color: #fff;
            border-radius: 20rpx 0 0 20rpx;

            &::before {
              content: '';
              position: absolute;
              right: 0;
              bottom: -20rpx;
              width: 20rpx;
              height: 20rpx;
              background: radial-gradient(circle at 0 100%, transparent 20rpx, #fff 0);
            }

            &::after {
              content: '';
              position: absolute;
              top: -20rpx;
              right: 0;
              width: 20rpx;
              height: 20rpx;
              background: radial-gradient(circle at 0% 0%, transparent 20rpx, #fff 0);
            }

            .menu-title {
              font-weight: 600;

              &::before {
                left: 0;
              }
            }
          }
        }
      }

      .goods-list-box {
        background-color: #fff;
        width: calc(100vw - 200rpx);
        padding: 10px;
        margin-left: 200rpx;
      }

      .banner-img {
        width: calc(100vw - 130px);
        border-radius: 5px;
        margin-bottom: 20rpx;
      }
    }
  }
</style>
