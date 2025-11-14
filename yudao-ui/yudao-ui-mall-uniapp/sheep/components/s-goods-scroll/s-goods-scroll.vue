<!-- 商品组 - 横向滚动商品（目前暂时没用到） -->
<template>
  <view class="goods-scroll-box">
    <scroll-view class="scroll-box" scroll-x scroll-anchoring>
      <view class="goods-box ss-flex">
        <view v-for="(item, index) in list" :key="index">
          <s-goods-column
            class="goods-card ss-m-l-20"
            size="sm"
            :data="item"
            :titleWidth="200 - marginLeft - marginRight"
          />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
  /**
   * 商品组 - 横向滚动商品
   */
  const props = defineProps({
    list: {
      type: Array,
      default() {
        return [];
      },
    },
  });
</script>

<style lang="scss" scoped></style>
