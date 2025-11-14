<!-- 分类展示：first-one 风格  -->
<template>
  <view class="ss-flex-col">
    <view class="goods-box" v-for="item in pagination.list" :key="item.id">
      <s-goods-column
        size="sl"
        :data="item"
        @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
      />
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';

  const props = defineProps({
    pagination: Object,
  });
</script>

<style lang="scss" scoped>
  .goods-box {
    width: 100%;
  }
</style>
