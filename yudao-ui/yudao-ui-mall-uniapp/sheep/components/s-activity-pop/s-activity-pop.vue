<!-- 商品信息：满减送等营销活动的弹窗 -->
<template>
  <su-popup :show="show" type="bottom" round="20" @close="emits('close')" showClose>
    <view class="model-box">
      <view class="title ss-m-t-16 ss-m-l-20 ss-flex">优惠</view>
      <view v-if="state.rewardActivity && state.rewardActivity.id > 0">
        <view class="titleLi">促销</view>
        <scroll-view
          class="model-content"
          scroll-y
          :scroll-with-animation="false"
          :enable-back-to-top="true"
        >
          <view
            class="actBox"
            v-for="(item, index) in getRewardActivityRuleGroupDescriptions(state.rewardActivity)"
            :key="index"
          >
            <view
              class="boxCont ss-flex ss-col-top ss-m-b-40"
              @tap="onGoodsList(state.rewardActivity)"
            >
              <view class="model-content-tag ss-flex ss-row-center">{{ item.name }}</view>
              <view class="model-content-title">
                <view class="contBu">
                  {{ item.values.join(';') }}
                </view>
                <view class="ss-m-b-24 cotBu-txt">
                  {{ sheep.$helper.timeFormat(state.rewardActivity.startTime, 'yyyy.mm.dd') }}
                  -
                  {{ sheep.$helper.timeFormat(state.rewardActivity.endTime, 'yyyy.mm.dd') }}
                </view>
              </view>
              <text class="cicon-forward" />
            </view>
          </view>
        </scroll-view>
      </view>
      <view class="titleLi">可领优惠券</view>
      <scroll-view
        class="model-content"
        scroll-y
        :scroll-with-animation="false"
        :enable-back-to-top="true"
        v-if="state.couponInfo.length"
      >
        <view class="actBox" v-for="item in state.couponInfo" :key="item.id">
          <view class="boxCont ss-flex ss-col-top ss-m-b-40">
            <view class="model-content-tag2">
              <view class="usePrice"> ￥{{ fen2yuan(item.discountPrice) }} </view>
              <view class="impose"> 满￥{{ fen2yuan(item.usePrice) }}可用 </view>
            </view>
            <view class="model-content-title2">
              <view class="contBu">
                {{ item.name }}
              </view>
              <view class="ss-m-b-24 cotBu-txt">
                {{
                  item.validityType == 1
                    ? sheep.$helper.timeFormat(item.validStartTime, 'yyyy-mm-dd') +
                      '-' +
                      sheep.$helper.timeFormat(item.validEndTime, 'yyyy-mm-dd')
                    : '领取后' + item.fixedStartTerm + '-' + item.fixedEndTerm + '天可用'
                }}
              </view>
            </view>
            <view class="coupon" @click.stop="getBuy(item.id)" v-if="item.canTake"> 立即领取 </view>
            <view class="coupon2" v-else> 已领取 </view>
          </view>
        </view>
      </scroll-view>
      <view class="nullBox" v-else> 暂无可领优惠券 </view>
    </view>
  </su-popup>
</template>
<script setup>
  import sheep from '@/sheep';
  import { getRewardActivityRuleGroupDescriptions } from '@/sheep/hooks/useGoods';
  import { computed, reactive, watch, ref } from 'vue';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
    show: {
      type: Boolean,
      default: false,
    },
  });
  const emits = defineEmits(['close']);
  const state = reactive({
    rewardActivity: computed(() => props.modelValue.rewardActivity),
    couponInfo: computed(() => props.modelValue.couponInfo),
  });

  // 领取优惠劵
  const getBuy = (id) => {
    emits('get', id);
  };

  function onGoodsList(e) {
    sheep.$router.go('/pages/activity/index', {
      activityId: e.id,
    });
  }
</script>
<style lang="scss" scoped>
  .model-box {
    height: 60vh;

    .title {
      justify-content: center;
      font-size: 36rpx;
      height: 80rpx;
      font-weight: bold;
      color: #333333;
    }
  }

  .model-content {
    height: fit-content;
    max-height: 380rpx;
    padding: 0 20rpx;
    box-sizing: border-box;
    margin-top: 20rpx;

    .model-content-tag {
      // background: rgba(#ff6911, 0.1);
      font-size: 35rpx;
      font-weight: 500;
      color: #ff6911;
      line-height: 150rpx;
      width: 200rpx;
      height: 150rpx;
      text-align: center;

      // border-radius: 5rpx;
    }

    .model-content-title {
      width: 450rpx;
      height: 150rpx;
      font-size: 26rpx;
      font-weight: 500;
      color: #333333;
      overflow: hidden;
    }

    .cicon-forward {
      font-size: 28rpx;
      color: #999999;
      margin: 0 auto;
    }
  }

  // 新增的
  .titleLi {
    margin: 10rpx 0 10rpx 20rpx;
    font-size: 26rpx;
  }

  .actBox {
    width: 700rpx;
    height: 150rpx;
    background-color: #fff2f2;
    margin: 10rpx auto;
    border-radius: 10rpx;
  }

  .boxCont {
    width: 700rpx;
    height: 150rpx;
    align-items: center;
  }

  .contBu {
    height: 80rpx;
    line-height: 80rpx;
    overflow: hidden;
    font-size: 30rpx;
    white-space: nowrap;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
  }

  .cotBu-txt {
    height: 70rpx;
    line-height: 70rpx;
    font-size: 25rpx;
    color: #999999;
  }

  .model-content-tag2 {
    font-size: 35rpx;
    font-weight: 500;
    color: #ff6911;
    width: 200rpx;
    height: 150rpx;
    text-align: center;
  }

  .usePrice {
    width: 200rpx;
    height: 90rpx;
    line-height: 100rpx;
    // background-color: red;
  }

  .impose {
    width: 200rpx;
    height: 50rpx;
    // line-height: 75rpx;
    font-size: 23rpx;
    // background-color: gold;
  }

  .model-content-title2 {
    width: 330rpx;
    height: 150rpx;
    font-size: 26rpx;
    font-weight: 500;
    color: #333333;
    overflow: hidden;
  }

  .coupon {
    width: 150rpx;
    height: 50rpx;
    line-height: 50rpx;
    background-color: rgb(255, 68, 68);
    color: white;
    border-radius: 30rpx;
    text-align: center;
    font-size: 25rpx;
  }

  .coupon2 {
    width: 150rpx;
    height: 50rpx;
    line-height: 50rpx;
    background-color: rgb(203, 192, 191);
    color: white;
    border-radius: 30rpx;
    text-align: center;
    font-size: 25rpx;
  }
  .nullBox {
    width: 100%;
    height: 300rpx;
    font-size: 25rpx;
    line-height: 300rpx;
    text-align: center;
    color: #999999;
  }
</style>
