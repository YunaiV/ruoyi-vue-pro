<!-- 秒杀活动列表 -->
<template>
  <s-layout :bgStyle="{ color: 'rgb(245,28,19)' }" navbar="inner">
    <!--顶部背景图-->
    <view
      class="page-bg"
      :style="[{ marginTop: '-' + Number(statusBarHeight + 88) + 'rpx' }]"
    ></view>
    <!-- 时间段轮播图 -->
    <view class="header" v-if="activeTimeConfig?.sliderPicUrls?.length > 0">
      <swiper
        indicator-dots="true"
        autoplay="true"
        :circular="true"
        interval="3000"
        duration="1500"
        indicator-color="rgba(255,255,255,0.6)"
        indicator-active-color="#fff"
      >
        <block v-for="(picUrl, index) in activeTimeConfig.sliderPicUrls" :key="index">
          <swiper-item class="borRadius14">
            <image :src="picUrl" class="slide-image borRadius14" lazy-load />
          </swiper-item>
        </block>
      </swiper>
    </view>
    <!-- 时间段列表 -->
    <view class="flex align-center justify-between ss-p-25">
      <!-- 左侧图标 -->
      <view class="time-icon">
        <image
          class="ss-w-100 ss-h-100"
          :src="sheep.$url.static('/static/img/shop/priceTag.png')"
        />
      </view>
      <scroll-view
        class="time-list"
        :scroll-into-view="activeTimeElId"
        scroll-x
        scroll-with-animation
      >
        <view
          v-for="(config, index) in timeConfigList"
          :key="index"
          :class="['item', { active: activeTimeIndex === index }]"
          :id="`timeItem${index}`"
          @tap="handleChangeTimeConfig(index, config.id)"
        >
          <!-- 活动起始时间 -->
          <view class="time">{{ config.startTime }}</view>
          <!-- 活动状态 -->
          <view class="status">{{ config?.status }}</view>
        </view>
      </scroll-view>
    </view>

    <!-- 内容区 -->
    <view class="list-content">
      <!-- 活动倒计时 -->
      <view class="content-header ss-flex-col ss-col-center ss-row-center">
        <view class="content-header-box ss-flex ss-row-center">
          <view
            class="countdown-box ss-flex"
            v-if="activeTimeConfig?.status === TimeStatusEnum.STARTED"
          >
            <view class="countdown-title ss-m-r-12">距结束</view>
            <view class="ss-flex countdown-time">
              <view class="ss-flex countdown-h">{{ countDown.h }}</view>
              <view class="ss-m-x-4">:</view>
              <view class="countdown-num ss-flex ss-row-center">{{ countDown.m }}</view>
              <view class="ss-m-x-4">:</view>
              <view class="countdown-num ss-flex ss-row-center">{{ countDown.s }}</view>
            </view>
          </view>
          <view v-else> {{ activeTimeConfig?.status }} </view>
        </view>
      </view>

      <!-- 活动列表 -->
      <scroll-view
        class="scroll-box"
        :style="{ height: pageHeight + 'rpx' }"
        scroll-y="true"
        :scroll-with-animation="false"
        :enable-back-to-top="true"
      >
        <view class="goods-box ss-m-b-20" v-for="activity in activityList" :key="activity.id">
          <s-goods-column
            size="lg"
            :data="{ ...activity, price: activity.seckillPrice }"
            :goodsFields="goodsFields"
            :seckillTag="true"
          >
            <!-- 抢购进度 -->
            <template #activity>
              <view class="limit">
                限量
                <text class="ss-m-l-5">{{ activity.stock }} {{ activity.unitName }}</text>
              </view>
              <su-progress :percentage="activity.percent" strokeWidth="10" textInside isAnimate />
            </template>
            <!-- 抢购按钮 -->
            <template #cart>
              <button
                :class="[
                  'ss-reset-button cart-btn',
                  { disabled: activeTimeConfig?.status === TimeStatusEnum.END },
                ]"
                v-if="activeTimeConfig?.status === TimeStatusEnum.WAIT_START"
              >
                <span>未开始</span>
              </button>
              <button
                :class="[
                  'ss-reset-button cart-btn',
                  { disabled: activeTimeConfig?.status === TimeStatusEnum.END },
                ]"
                @click="sheep.$router.go('/pages/goods/seckill', { id: activity.id })"
                v-else-if="activeTimeConfig?.status === TimeStatusEnum.STARTED"
              >
                <span>马上抢</span>
              </button>
              <button
                :class="[
                  'ss-reset-button cart-btn',
                  { disabled: activeTimeConfig?.status === TimeStatusEnum.END },
                ]"
                v-else
              >
                <span>已结束</span>
              </button>
            </template>
          </s-goods-column>
        </view>
        <uni-load-more
          v-if="activityTotal > 0"
          :status="loadStatus"
          :content-text="{
            contentdown: '上拉加载更多',
          }"
          @tap="loadMore"
        />
      </scroll-view>
    </view>
  </s-layout>
</template>
<script setup>
  import { computed, nextTick, reactive, ref } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { useDurationTime } from '@/sheep/hooks/useGoods';
  import SeckillApi from '@/sheep/api/promotion/seckill';
  import dayjs from 'dayjs';
  import { TimeStatusEnum } from '@/sheep/helper/const';

  // 计算页面高度
  const { safeAreaInsets, safeArea } = sheep.$platform.device;
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;
  const pageHeight =
    (safeArea.height + safeAreaInsets.bottom) * 2 + statusBarHeight - sheep.$platform.navbar - 350;
  const headerBg = sheep.$url.css('/static/img/shop/goods/seckill-header.png');

  // 商品控件显示的字段（不显示库存、销量。改为显示自定义的进度条）
  const goodsFields = {
    name: {
      show: true,
    },
    introduction: {
      show: true,
    },
    price: {
      show: true,
    },
    marketPrice: {
      show: true,
    },
  };

  //#region 时间段
  // 时间段列表
  const timeConfigList = ref([]);
  // 查询时间段
  const getSeckillConfigList = async () => {
    const { data } = await SeckillApi.getSeckillConfigList();
    const now = dayjs();
    const today = now.format('YYYY-MM-DD');
    const select = ref([]);
    // 判断时间段的状态
    data.forEach((config, index) => {
      const startTime = dayjs(`${today} ${config.startTime}`);
      const endTime = dayjs(`${today} ${config.endTime}`);
      select.value[index] = config.id;
      if (now.isBefore(startTime)) {
        config.status = TimeStatusEnum.WAIT_START;
      } else if (now.isAfter(endTime)) {
        config.status = TimeStatusEnum.END;
      } else {
        config.status = TimeStatusEnum.STARTED;
        activeTimeIndex.value = index;
      }
    });
    timeConfigList.value = data;
    // 默认选中进行中的活动
    handleChangeTimeConfig(activeTimeIndex.value, select.value[activeTimeIndex.value]);
    // 滚动到进行中的时间段
    scrollToTimeConfig(activeTimeIndex.value);
  };

  // 滚动到指定时间段
  const activeTimeElId = ref(''); // 当前选中的时间段的元素ID
  const scrollToTimeConfig = (index) => {
    nextTick(() => (activeTimeElId.value = `timeItem${index}`));
  };

  // 切换时间段
  const activeTimeIndex = ref(0); // 当前选中的时间段的索引
  const activeTimeConfig = computed(() => timeConfigList.value[activeTimeIndex.value]); // 当前选中的时间段
  const handleChangeTimeConfig = (index, id) => {
    activeTimeIndex.value = index;

    // 查询活动列表
    activityPageParams.pageNo = 1;
    activityPageParams.configId = id;
    activityList.value = [];
    getActivityList();
  };

  // 倒计时
  const countDown = computed(() => {
    const endTime = activeTimeConfig.value?.endTime;
    if (endTime) {
      return useDurationTime(`${dayjs().format('YYYY-MM-DD')} ${endTime}`);
    }
  });

  //#endregion

  //#region 分页查询活动列表

  // 查询活动列表
  const activityPageParams = reactive({
    configId: 0, // 时间段 ID
    pageNo: 1, // 页码
    pageSize: 5, // 每页数量
  });
  const activityTotal = ref(0); // 活动总数
  const activityList = ref([]); // 活动列表
  const loadStatus = ref(''); // 页面加载状态
  async function getActivityList() {
    loadStatus.value = 'loading';
    const { data } = await SeckillApi.getSeckillActivityPage(activityPageParams);
    data.list.forEach((activity) => {
      // 计算抢购进度
      activity.percent = parseInt(
        (100 * (activity.totalStock - activity.stock)) / activity.totalStock,
      );
    });
    activityList.value = activityList.value.concat(...data.list);
    activityTotal.value = data.total;

    loadStatus.value = activityList.value.length < activityTotal.value ? 'more' : 'noMore';
  }

  // 加载更多
  function loadMore() {
    if (loadStatus.value !== 'noMore') {
      activityPageParams.pageNo += 1;
      getActivityList();
    }
  }
  // 上拉加载更多
  onReachBottom(() => loadMore());

  //#endregion

  // 页面初始化
  onLoad(async () => {
    await getSeckillConfigList();
  });
</script>
<style lang="scss" scoped>
  // 顶部背景图
  .page-bg {
    width: 100%;
    height: 458rpx;
    background: v-bind(headerBg) no-repeat;
    background-size: 100% 100%;
  }

  // 时间段轮播图
  .header {
    width: 710rpx;
    height: 330rpx;
    margin: -276rpx auto 0 auto;
    border-radius: 14rpx;
    overflow: hidden;

    swiper {
      height: 330rpx !important;
      border-radius: 14rpx;
      overflow: hidden;
    }

    image {
      width: 100%;
      height: 100%;
      border-radius: 14rpx;
      overflow: hidden;

      img {
        border-radius: 14rpx;
      }
    }
  }

  // 时间段列表：左侧图标
  .time-icon {
    width: 75rpx;
    height: 70rpx;
  }

  // 时间段列表
  .time-list {
    width: 596rpx;
    white-space: nowrap;

    // 时间段
    .item {
      display: inline-block;
      font-size: 20rpx;
      color: #666;
      text-align: center;
      box-sizing: border-box;
      margin-right: 30rpx;
      width: 130rpx;

      // 开始时间
      .time {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
      }

      // 选中的时间段
      &.active {
        .time {
          color: var(--ui-BG-Main);
        }

        // 状态
        .status {
          height: 30rpx;
          line-height: 30rpx;
          border-radius: 15rpx;
          width: 128rpx;
          background: linear-gradient(90deg, var(--ui-BG-Main) 0%, var(--ui-BG-Main-gradient) 100%);
          color: #fff;
        }
      }
    }
  }

  // 内容区
  .list-content {
    position: relative;
    z-index: 3;
    margin: 0 20rpx 0 20rpx;
    background: #fff;
    border-radius: 20rpx 20rpx 0 0;

    .content-header {
      width: 100%;
      border-radius: 20rpx 20rpx 0 0;
      height: 150rpx;
      background: linear-gradient(180deg, #fff4f7, #ffe6ec);

      .content-header-box {
        width: 678rpx;
        height: 64rpx;
        background: rgba($color: #fff, $alpha: 0.66);
        border-radius: 32px;

        // 场次倒计时内容
        .countdown-title {
          font-size: 28rpx;
          font-weight: 500;
          color: #333333;
          line-height: 28rpx;
        }

        // 场次倒计时
        .countdown-time {
          font-size: 28rpx;
          color: rgba(#ed3c30, 0.23);

          // 场次倒计时：小时部分
          .countdown-h {
            font-size: 24rpx;
            font-family: OPPOSANS;
            font-weight: 500;
            color: #ffffff;
            padding: 0 4rpx;
            height: 40rpx;
            background: rgba(#ed3c30, 0.23);
            border-radius: 6rpx;
          }

          // 场次倒计时：分钟、秒
          .countdown-num {
            font-size: 24rpx;
            font-family: OPPOSANS;
            font-weight: 500;
            color: #ffffff;
            width: 40rpx;
            height: 40rpx;
            background: rgba(#ed3c30, 0.23);
            border-radius: 6rpx;
          }
        }
      }
    }

    // 活动列表
    .scroll-box {
      height: 900rpx;

      // 活动
      .goods-box {
        position: relative;

        // 抢购按钮
        .cart-btn {
          position: absolute;
          bottom: 10rpx;
          right: 20rpx;
          z-index: 11;
          height: 44rpx;
          line-height: 50rpx;
          padding: 0 20rpx;
          border-radius: 25rpx;
          font-size: 24rpx;
          color: #fff;
          background: linear-gradient(90deg, #ff6600 0%, #fe832a 100%);

          &.disabled {
            background: $gray-b;
            color: #fff;
          }
        }

        // 秒杀限量商品数
        .limit {
          font-size: 22rpx;
          color: $dark-9;
          margin-bottom: 5rpx;
        }
      }
    }
  }
</style>
