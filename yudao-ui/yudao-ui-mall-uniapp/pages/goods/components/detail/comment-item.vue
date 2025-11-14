<!-- 商品评论项 -->
<template>
  <view>
    <!-- 用户评论 -->
    <view class="user ss-flex ss-m-b-14">
      <view class="ss-m-r-20 ss-flex">
        <image class="avatar" :src="item.userAvatar"></image>
      </view>
      <view class="nickname ss-m-r-20">{{ item.userNickname }}</view>
      <view class="">
        <uni-rate :readonly="true" v-model="item.scores" size="18" />
      </view>
    </view>
    <view class="content"> {{ item.content }} </view>
    <view class="ss-m-t-24" v-if="item.picUrls?.length">
      <scroll-view class="scroll-box" scroll-x scroll-anchoring>
        <view class="ss-flex">
          <view v-for="(picUrl, index) in item.picUrls" :key="picUrl" class="ss-m-r-10">
            <su-image
              class="content-img"
              isPreview
              :previewList="item.picUrls"
              :current="index"
              :src="picUrl"
              :height="120"
              :width="120"
              mode="aspectFill"
            />
          </view>
        </view>
      </scroll-view>
    </view>
    <!-- 商家回复 -->
    <view class="ss-m-t-20 reply-box" v-if="item.replyTime">
      <view class="reply-title">商家回复：</view>
      <view class="reply-content">{{ item.replyContent }}</view>
    </view>
  </view>
</template>

<script setup>
  const props = defineProps({
    item: {
      type: Object,
      default() {},
    },
  });
</script>

<style lang="scss" scoped>
  .avatar {
    width: 52rpx;
    height: 52rpx;
    border-radius: 50%;
  }

  .nickname {
    font-size: 26rpx;
    font-weight: 500;
    color: #999999;
  }

  .content {
    width: 636rpx;
    font-size: 26rpx;
    font-weight: 400;
    color: #333333;
  }

  .reply-box {
    position: relative;
    background: #f8f8f8;
    border-radius: 8rpx;
    padding: 16rpx;
  }

  .reply-title {
    position: absolute;
    left: 16rpx;
    top: 16rpx;
    font-weight: 400;
    font-size: 26rpx;
    line-height: 40rpx;
    color: #333333;
  }

  .reply-content {
    text-indent: 128rpx;
    font-weight: 400;
    font-size: 26rpx;
    line-height: 40rpx;
    color: #333333;
  }
</style>
