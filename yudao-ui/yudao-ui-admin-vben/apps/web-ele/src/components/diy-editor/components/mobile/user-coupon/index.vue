<script setup lang="ts">
import type { UserCouponProperty } from './config';

import { ElImage } from 'element-plus';

/** 用户卡券 */
defineOptions({ name: 'UserCoupon' });
// 定义属性
defineProps<{ property: UserCouponProperty }>();
</script>
<template>
  <ElImage
    src="https://shopro.sheepjs.com/admin/static/images/shop/decorate/couponCardStyle.png"
  />
</template>

<style scoped lang="scss"></style>
