<!-- 页面 -->
<template>
  <s-layout title="我的团队" :class="state.scrollTop ? 'team-wrap' : ''" navbar="inner">
    <view
      class="header-box"
      :style="[
        {
          marginTop: '-' + Number(statusBarHeight + 88) + 'rpx',
          paddingTop: Number(statusBarHeight + 108) + 'rpx',
        },
      ]"
    >
      <!-- 推广数据总览 -->
      <view class="team-data-box ss-flex ss-col-center ss-row-between" style="width: 100%">
        <view class="data-card" style="width: 100%">
          <view class="total-item" style="width: 100%">
            <view class="item-title" style="text-align: center">推广人数</view>
            <view class="total-num" style="text-align: center">
              {{
                state.summary.firstBrokerageUserCount + state.summary.secondBrokerageUserCount || 0
              }}
            </view>
          </view>
        </view>
      </view>
    </view>
    <view class="promoter-list">
      <!--<view
        class="promoterHeader bg-color"
        style="backgroundcolor: #e93323 !important; height: 218rpx; color: #fff"
      >
        <view class="headerCon acea-row row-between" style="padding: 28px 29px 0 29px">
          <view>
            <view class="name" style="color: #fff">推广人数</view>
            <view>
              <text class="num" style="color: #fff">
                {{
                  state.summary.firstBrokerageUserCount + state.summary.secondBrokerageUserCount ||
                  0
                }}
              </text>
              人
            </view>
          </view>
          <view class="iconfont icon-tuandui" />
        </view>
      </view>-->
      <view style="padding: 0 20rpx">
        <view class="nav acea-row row-around l1" style="margin-top: 20rpx">
          <view :class="state.level == 1 ? 'item on' : 'item'" @click="setType(1)">
            一级({{ state.summary.firstBrokerageUserCount || 0 }})
          </view>
          <view :class="state.level == 2 ? 'item on' : 'item'" @click="setType(2)">
            二级({{ state.summary.secondBrokerageUserCount || 0 }})
          </view>
        </view>
        <view
          class="search acea-row row-between-wrapper"
          style="display: flex; height: 100rpx; align-items: center"
        >
          <view class="input">
            <input
              placeholder="点击搜索会员名称"
              v-model="state.nickname"
              confirm-type="search"
              name="search"
              @confirm="submitForm"
            />
          </view>
          <image
            :src="sheep.$url.static('/static/img/shop/search.png')"
            mode=""
            style="width: 60rpx; height: 64rpx"
            @click="submitForm"
          />
        </view>
        <view class="list">
          <view class="sortNav acea-row row-middle" style="display: flex; align-items: center">
            <view
              class="sortItem"
              @click="setSort('userCount', 'asc')"
              v-if="sort === 'userCountDESC'"
            >
              团队排序
              <image :src="sheep.$url.static('/static/img/shop/sort1.png')" />
            </view>
            <view
              class="sortItem"
              @click="setSort('userCount', 'desc')"
              v-else-if="sort === 'userCountASC'"
            >
              团队排序
              <image :src="sheep.$url.static('/static/img/shop/sort3.png')" />
            </view>
            <view class="sortItem" @click="setSort('userCount', 'desc')" v-else>
              团队排序
              <image :src="sheep.$url.static('/static/img/shop/sort2.png')" />
            </view>
            <view class="sortItem" @click="setSort('price', 'asc')" v-if="sort === 'priceDESC'">
              金额排序
              <image :src="sheep.$url.static('/static/img/shop/sort1.png')" />
            </view>
            <view
              class="sortItem"
              @click="setSort('price', 'desc')"
              v-else-if="sort === 'priceASC'"
            >
              金额排序
              <image :src="sheep.$url.static('/static/img/shop/sort3.png')" />
            </view>
            <view class="sortItem" @click="setSort('price', 'desc')" v-else>
              金额排序
              <image :src="sheep.$url.static('/static/img/shop/sort2.png')" />
            </view>
            <view
              class="sortItem"
              @click="setSort('orderCount', 'asc')"
              v-if="sort === 'orderCountDESC'"
            >
              订单排序
              <image :src="sheep.$url.static('/static/img/shop/sort1.png')" />
            </view>
            <view
              class="sortItem"
              @click="setSort('orderCount', 'desc')"
              v-else-if="sort === 'orderCountASC'"
            >
              订单排序
              <image :src="sheep.$url.static('/static/img/shop/sort3.png')" />
            </view>
            <view class="sortItem" @click="setSort('orderCount', 'desc')" v-else>
              订单排序
              <image :src="sheep.$url.static('/static/img/shop/sort2.png')" />
            </view>
          </view>
          <block v-for="(item, index) in state.pagination.list" :key="index">
            <view class="item acea-row row-between-wrapper" style="display: flex">
              <view
                class="picTxt acea-row row-between-wrapper"
                style="display: flex; align-items: center"
              >
                <view class="pictrue">
                  <image :src="item.avatar" />
                </view>
                <view class="text">
                  <view class="name line1">{{ item.nickname }}</view>
                  <view>
                    加入时间:
                    {{ sheep.$helper.timeFormat(item.brokerageTime, 'yyyy-mm-dd hh:MM:ss') }}
                  </view>
                </view>
              </view>
              <view
                class="right"
                style="
                  justify-content: center;
                  flex-direction: column;
                  display: flex;
                  margin-left: auto;
                "
              >
                <view>
                  <text class="num font-color">{{ item.brokerageUserCount || 0 }} </text>人
                </view>
                <view>
                  <text class="num">{{ item.orderCount || 0 }}</text
                  >单</view
                >
                <view>
                  <text class="num">{{ fen2yuan(item.brokeragePrice) || 0 }}</text
                  >元
                </view>
              </view>
            </view>
          </block>
          <block v-if="state.pagination.list.length === 0">
            <view style="text-align: center; margin-top: 30rpx">暂无推广人数</view>
          </block>
        </view>
      </view>
    </view>
    <!-- <home></home> -->

    <!-- 		<view class="header-box" :style="[
        {
          marginTop: '-' + Number(statusBarHeight + 88) + 'rpx',
          paddingTop: Number(statusBarHeight + 108) + 'rpx',
        },
      ]">
			<view v-if="userInfo.parent_user" class="referrer-box ss-flex ss-col-center">
				推荐人：
				<image class="referrer-avatar ss-m-r-10" :src="sheep.$url.cdn(userInfo.parent_user.avatar)"
					mode="aspectFill">
				</image>
				{{ userInfo.parent_user.nickname }}
			</view>
			<view class="team-data-box ss-flex ss-col-center ss-row-between">
				<view class="data-card">
					<view class="total-item">
						<view class="item-title">团队总人数（人）</view>
						<view class="total-num">
							{{ (state.summary.firstBrokerageUserCount+ state.summary.secondBrokerageUserCount)|| 0 }}
						</view>
					</view>
					<view class="category-item ss-flex">
						<view class="ss-flex-1">
							<view class="item-title">一级成员</view>
							<view class="category-num">{{ state.summary.firstBrokerageUserCount || 0 }}</view>
						</view>
						<view class="ss-flex-1">
							<view class="item-title">二级成员</view>
							<view class="category-num">{{ state.summary.secondBrokerageUserCount || 0 }}</view>
						</view>
					</view>
				</view>
				<view class="data-card">
					<view class="total-item">
						<view class="item-title">团队分销商人数（人）</view>
						<view class="total-num">{{ agentInfo?.child_agent_count_all || 0 }}</view>
					</view>
					<view class="category-item ss-flex">
						<view class="ss-flex-1">
							<view class="item-title">一级分销商</view>
							<view class="category-num">{{ agentInfo?.child_agent_count_1 || 0 }}</view>
						</view>
						<view class="ss-flex-1">
							<view class="item-title">二级分销商</view>
							<view class="category-num">{{ agentInfo?.child_agent_count_2 || 0 }}</view>
						</view>
					</view>
				</view>
			</view>
		</view>
		<view class="list-box">
			<uni-list :border="false">
				<uni-list-chat v-for="item in state.pagination.data" :key="item.id" :avatar-circle="true"
					:title="item.nickname" :avatar="sheep.$url.cdn(item.avatar)"
					:note="filterUserNum(item.agent?.child_user_count_1)">
					<view class="chat-custom-right">
						<view v-if="item.avatar" class="tag-box ss-flex ss-col-center">
							<image class="tag-img" :src="sheep.$url.cdn(item.avatar)" mode="aspectFill">
							</image>
							<text class="tag-title">{{ item.nickname }}</text>
						</view>
						<text
							class="time-text">{{ sheep.$helper.timeFormat(item.brokerageTime, 'yyyy-mm-dd hh:MM:ss') }}</text>
					</view>
				</uni-list-chat>
			</uni-list>
		</view>
		<s-empty v-if="state.pagination.total === 0" icon="/static/data-empty.png" text="暂无团队信息">
		</s-empty> -->
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { computed, reactive, ref } from 'vue';
  import _ from 'lodash-es';
  import { onPageScroll } from '@dcloudio/uni-app';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '../../sheep/hooks/useGoods';

  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;
  // const agentInfo = computed(() => sheep.$store('user').agentInfo);
  const userInfo = computed(() => sheep.$store('user').userInfo);
  const headerBg = sheep.$url.css('/static/img/shop/user/withdraw_bg.png');

  onPageScroll((e) => {
    state.scrollTop = e.scrollTop <= 100;
  });

  let sort = ref();
  const state = reactive({
    summary: {},
    pagination: {
      pageNo: 1,
      pageSize: 8,
      list: [],
      total: 0,
    },
    loadStatus: '',
    // ↓ 新 ui 逻辑
    level: 1,
    nickname: ref(''),
    sortKey: '',
    isAsc: '',
  });

  function filterUserNum(num) {
    if (_.isNil(num)) {
      return '';
    }
    return `下级团队${num}人`;
  }

  function submitForm() {
    state.pagination.list = [];
    getTeamList();
  }

  async function getTeamList() {
    state.loadStatus = 'loading';
    let { code, data } = await BrokerageApi.getBrokerageUserChildSummaryPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      level: state.level,
      'sortingField.order': state.isAsc,
      'sortingField.field': state.sortKey,
      nickname: state.nickname,
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  function setType(e) {
    state.pagination.list = [];
    state.level = e + '';
    getTeamList();
  }

  function setSort(sortKey, isAsc) {
    state.pagination.list = [];
    sort = sortKey + isAsc.toUpperCase();
    state.isAsc = isAsc;
    state.sortKey = sortKey;
    getTeamList();
  }

  onLoad(async () => {
    await getTeamList();
    // 概要数据
    let { data } = await BrokerageApi.getBrokerageUserSummary();
    state.summary = data;
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getTeamList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  .l1 {
    background-color: #fff;
    height: 86rpx;
    line-height: 86rpx;
    font-size: 28rpx;
    color: #282828;
    border-bottom: 1rpx solid #eee;
    border-top-left-radius: 14rpx;
    border-top-right-radius: 14rpx;
    display: flex;
    justify-content: space-around;
  }

  .header-box {
    box-sizing: border-box;
    padding: 0 20rpx 20rpx 20rpx;
    width: 750rpx;
    z-index: 3;
    position: relative;
    background: v-bind(headerBg) no-repeat,
      linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    background-size: 750rpx 100%;

    // 团队信息总览
    .team-data-box {
      .data-card {
        width: 305rpx;
        background: #ffffff;
        border-radius: 20rpx;
        padding: 20rpx;

        .item-title {
          font-size: 22rpx;
          font-weight: 500;
          color: #999999;
          line-height: 30rpx;
          margin-bottom: 10rpx;
        }

        .total-item {
          margin-bottom: 30rpx;
        }

        .total-num {
          font-size: 38rpx;
          font-weight: 500;
          color: #333333;
          font-family: OPPOSANS;
        }

        .category-num {
          font-size: 26rpx;
          font-weight: 500;
          color: #333333;
          font-family: OPPOSANS;
        }
      }
    }
  }

  .list-box {
    z-index: 3;
    position: relative;
  }

  .chat-custom-right {
    .time-text {
      font-size: 22rpx;
      font-weight: 400;
      color: #999999;
    }

    .tag-box {
      background: rgba(0, 0, 0, 0.2);
      border-radius: 21rpx;
      line-height: 30rpx;
      padding: 5rpx 10rpx;
      width: 140rpx;

      .tag-img {
        width: 34rpx;
        height: 34rpx;
        margin-right: 6rpx;
        border-radius: 50%;
      }

      .tag-title {
        font-size: 18rpx;
        font-weight: 500;
        color: rgba(255, 255, 255, 1);
        line-height: 20rpx;
      }
    }
  }

  // 推荐人
  .referrer-box {
    font-size: 28rpx;
    font-weight: 500;
    color: #ffffff;
    padding: 20rpx;

    .referrer-avatar {
      width: 34rpx;
      height: 34rpx;
      border-radius: 50%;
    }
  }

  .promoter-list .nav {
    background-color: #fff;
    height: 86rpx;
    line-height: 86rpx;
    font-size: 28rpx;
    color: #282828;
    border-bottom: 1rpx solid #eee;
    border-top-left-radius: 14rpx;
    border-top-right-radius: 14rpx;
    margin-top: -30rpx;
  }

  .promoter-list .nav .item.on {
    border-bottom: 5rpx solid;
    // $theme-color
    color: var(--ui-BG-Main);
    // $theme-color
  }

  .promoter-list .search {
    width: 100%;
    background-color: #fff;
    height: 100rpx;
    padding: 0 24rpx;
    box-sizing: border-box;
    border-bottom-left-radius: 14rpx;
    border-bottom-right-radius: 14rpx;
  }

  .promoter-list .search .input {
    width: 592rpx;
    height: 60rpx;
    border-radius: 50rpx;
    background-color: #f5f5f5;
    text-align: center;
    position: relative;
  }

  .promoter-list .search .input input {
    height: 100%;
    font-size: 26rpx;
    width: 610rpx;
    text-align: center;
  }

  .promoter-list .search .input .placeholder {
    color: #bbb;
  }

  .promoter-list .search .input .iconfont {
    position: absolute;
    right: 28rpx;
    color: #999;
    font-size: 28rpx;
    top: 50%;
    transform: translateY(-50%);
  }

  .promoter-list .search .iconfont {
    font-size: 32rpx;
    color: #515151;
    height: 60rpx;
    line-height: 60rpx;
  }

  .promoter-list .list {
    margin-top: 20rpx;
  }

  .promoter-list .list .sortNav {
    background-color: #fff;
    height: 76rpx;
    border-bottom: 1rpx solid #eee;
    color: #333;
    font-size: 28rpx;
    border-top-left-radius: 14rpx;
    border-top-right-radius: 14rpx;
  }

  .promoter-list .list .sortNav .sortItem {
    text-align: center;
    flex: 1;
  }

  .promoter-list .list .sortNav .sortItem image {
    width: 24rpx;
    height: 24rpx;
    margin-left: 6rpx;
    vertical-align: -3rpx;
  }

  .promoter-list .list .item {
    background-color: #fff;
    border-bottom: 1rpx solid #eee;
    height: 152rpx;
    padding: 0 24rpx;
    font-size: 24rpx;
    color: #666;
  }

  .promoter-list .list .item .picTxt .pictrue {
    width: 106rpx;
    height: 106rpx;
    border-radius: 50%;
  }

  .promoter-list .list .item .picTxt .pictrue image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 3rpx solid #fff;
    box-shadow: 0 0 10rpx #aaa;
    box-sizing: border-box;
  }

  .promoter-list .list .item .picTxt .text {
    // width: 304rpx;
    font-size: 24rpx;
    color: #666;
    margin-left: 14rpx;
  }

  .promoter-list .list .item .picTxt .text .name {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 13rpx;
  }

  .promoter-list .list .item .right {
    text-align: right;
    font-size: 22rpx;
    color: #333;
  }

  .promoter-list .list .item .right .num {
    margin-right: 7rpx;
  }
</style>
