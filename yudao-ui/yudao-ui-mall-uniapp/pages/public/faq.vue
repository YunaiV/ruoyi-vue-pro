<!-- FAQ 常见问题 -->
<template>
  <s-layout :bgStyle="{ color: '#FFF' }" class="set-wrap" title="常见问题">
    <uni-collapse>
      <uni-collapse-item v-for="(item, index) in state.list" :key="item">
        <template v-slot:title>
          <view class="ss-flex ss-col-center header">
            <view class="ss-m-l-20 ss-m-r-20 icon">
              <view class="rectangle">
                <view class="num ss-flex ss-row-center ss-col-center">
                  {{ index + 1 < 10 ? '0' + (index + 1) : index + 1 }}
                </view>
              </view>
              <view class="triangle"> </view>
            </view>
            <view class="title ss-m-t-36 ss-m-b-36">
              {{ item.title }}
            </view>
          </view>
        </template>
        <view class="content ss-p-l-78 ss-p-r-40 ss-p-b-50 ss-p-t-20">
          <text class="text">{{ item.content }}</text>
        </view>
      </uni-collapse-item>
    </uni-collapse>
    <s-empty
      v-if="state.list.length === 0 && !state.loading"
      text="暂无常见问题"
      icon="/static/collect-empty.png"
    />
  </s-layout>
</template>

<script setup>
  import { onLoad } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import sheep from '@/sheep';

  const state = reactive({
    list: [],
    loading: true,
  });

  async function getFaqList() {
    const { error, data } = await sheep.$api.data.faq();
    if (error === 0) {
      state.list = data;
      state.loading = false;
    }
  }
  onLoad(() => {
    // TODO 芋艿：【文章】目前简单做，使用营销文章，作为 faq
    if (true) {
      sheep.$router.go('/pages/public/richtext', {
        title: '常见问题',
      })
      return;
    }
    getFaqList();
  });
</script>

<style lang="scss" scoped>
  .header {
    .title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
      line-height: 30rpx;
      max-width: 688rpx;
    }

    .icon {
      position: relative;
      width: 40rpx;
      height: 40rpx;

      .rectangle {
        position: absolute;
        left: 0;
        top: 0;
        width: 40rpx;
        height: 36rpx;
        background: var(--ui-BG-Main);
        border-radius: 4px;

        .num {
          width: 100%;
          height: 100%;
          font-size: 24rpx;
          font-weight: 500;
          color: var(--ui-BG);
          line-height: 32rpx;
        }
      }

      .triangle {
        width: 0;
        height: 0;
        border-left: 4rpx solid transparent;
        border-right: 4rpx solid transparent;
        border-top: 8rpx solid var(--ui-BG-Main);
        position: absolute;
        left: 16rpx;
        bottom: -4rpx;
      }
    }
  }

  .content {
    border-bottom: 1rpx solid #dfdfdf;

    .text {
      font-size: 26rpx;
      color: #666666;
    }
  }
</style>
