<script setup lang="ts">
import type { UserOrderProperty } from './config';

import { ElImage } from 'element-plus';

/** 用户订单 */
defineOptions({ name: 'UserOrder' });
// 定义属性
defineProps<{ property: UserOrderProperty }>();
</script>
<template>
  <ElImage
    src="https://shopro.sheepjs.com/admin/static/images/shop/decorate/orderCardStyle.png"
  />
</template>

<style scoped lang="scss"></style>
