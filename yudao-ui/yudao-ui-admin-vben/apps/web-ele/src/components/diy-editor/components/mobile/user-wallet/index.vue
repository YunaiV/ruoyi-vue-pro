<script setup lang="ts">
import type { UserWalletProperty } from './config';

import { ElImage } from 'element-plus';

/** 用户资产 */
defineOptions({ name: 'UserWallet' });
// 定义属性
defineProps<{ property: UserWalletProperty }>();
</script>
<template>
  <ElImage
    src="https://shopro.sheepjs.com/admin/static/images/shop/decorate/walletCardStyle.png"
  />
</template>

<style scoped lang="scss"></style>
