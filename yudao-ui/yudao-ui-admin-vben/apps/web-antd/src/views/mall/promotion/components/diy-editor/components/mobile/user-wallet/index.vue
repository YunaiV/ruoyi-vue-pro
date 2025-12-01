<script setup lang="ts">
import type { UserWalletProperty } from './config';

import { Image } from 'ant-design-vue';

/** 用户资产 */
defineOptions({ name: 'UserWallet' });

/** 定义属性 */
defineProps<{ property: UserWalletProperty }>();
</script>
<template>
  <Image
    src="https://shopro.sheepjs.com/admin/static/images/shop/decorate/walletCardStyle.png"
    :preview="false"
  />
</template>
