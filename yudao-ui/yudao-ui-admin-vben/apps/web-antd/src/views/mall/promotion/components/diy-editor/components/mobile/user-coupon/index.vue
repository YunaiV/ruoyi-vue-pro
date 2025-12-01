<script setup lang="ts">
import type { UserCouponProperty } from './config';

import { Image } from 'ant-design-vue';

/** 用户卡券 */
defineOptions({ name: 'UserCoupon' });
// 定义属性
defineProps<{ property: UserCouponProperty }>();
</script>
<template>
  <Image
    src="https://shopro.sheepjs.com/admin/static/images/shop/decorate/couponCardStyle.png"
    :preview="false"
  />
</template>
