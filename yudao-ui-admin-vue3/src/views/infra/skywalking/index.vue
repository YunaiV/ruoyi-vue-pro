<script setup lang="ts">
import { IFrame } from '@/components/IFrame'
import { ref } from 'vue'
const src = ref('http://skywalking.shop.iocoder.cn')
</script>

<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
