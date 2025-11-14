<!-- 拼团活动参团记录卡片 -->
<template>
  <view v-if="state.list.length > 0" class="groupon-list detail-card ss-p-x-20">
    <view class="join-activity ss-flex ss-row-between ss-m-t-30">
      <view class="">已有{{ state.list.length }}人参与活动</view>
      <text class="cicon-forward"></text>
    </view>
    <view
      v-for="(record, index) in state.list"
      @tap="sheep.$router.go('/pages/activity/groupon/detail', { id: record.id })"
      :key="index"
      class="ss-m-t-40 ss-flex ss-row-between border-bottom ss-p-b-30"
    >
      <view class="ss-flex ss-col-center">
        <image :src="sheep.$url.cdn(record.avatar)" class="user-avatar"></image>
        <view class="user-nickname ss-m-l-20 ss-line-1">{{ record.nickname }}</view>
      </view>
      <view class="ss-flex ss-col-center">
        <view class="ss-flex-col ss-col-bottom ss-m-r-20">
          <view class="title ss-flex ss-m-b-14">
            还差
            <view class="num">{{ record.userSize - record.userCount }}人</view>
            成团
          </view>
          <view class="end-time">{{ endTime(record.expireTime) }}</view>
        </view>
        <view class="">
          <button class="ss-reset-button go-btn" @tap.stop="onJoinGroupon(record)"> 去参团 </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { onMounted, reactive } from 'vue';
  import sheep from '@/sheep';
  import { useDurationTime } from '@/sheep/hooks/useGoods';
  import CombinationApi from '@/sheep/api/promotion/combination';

  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
  });
  const state = reactive({
    list: [],
  });

  // 去参团
  const emits = defineEmits(['join']);
  function onJoinGroupon(record) {
    emits('join', record);
  }

  // 结束时间或状态
  function endTime(time) {
    const durationTime = useDurationTime(time);

    if (durationTime.ms <= 0) {
      return '该团已解散';
    }

    let timeText = '剩余 ';
    timeText += `${durationTime.h}时`;
    timeText += `${durationTime.m}分`;
    timeText += `${durationTime.s}秒`;
    return timeText;
  }

  // 初始化
  onMounted(async () => {
    // 查询参团记录
    // status = 0 表示未成团
    const { data } = await CombinationApi.getHeadCombinationRecordList(props.modelValue.id, 0, 10);
    state.list = data;
  });
</script>

<style lang="scss" scoped>
  .detail-card {
    background-color: $white;
    margin: 14rpx 20rpx;
    border-radius: 10rpx;
    overflow: hidden;
  }
  .groupon-list {
    .join-activity {
      font-size: 28rpx;
      font-weight: 500;
      color: #999999;

      .cicon-forward {
        font-weight: 400;
      }
    }

    .user-avatar {
      width: 60rpx;
      height: 60rpx;
      background: #ececec;
      border-radius: 60rpx;
    }

    .user-nickname {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      width: 160rpx;
    }

    .title {
      font-size: 24rpx;
      font-weight: 500;
      color: #666666;

      .num {
        color: #ff6000;
      }
    }

    .end-time {
      font-size: 24rpx;
      font-weight: 500;
      color: #999999;
    }

    .go-btn {
      width: 140rpx;
      height: 60rpx;
      background: linear-gradient(90deg, #ff6000 0%, #fe832a 100%);
      border-radius: 30rpx;
      color: #fff;
      font-weight: 500;
      font-size: 26rpx;
      line-height: normal;
    }
  }
</style>
