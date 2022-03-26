<template>
  <i-frame v-model:src="url" />
</template>

<script setup>
import iFrame from '@/components/iFrame'

const url = "http://skywalking.shop.iocoder.cn/trace"; // TODO 芋艿，后续改成配置读取
</script>
