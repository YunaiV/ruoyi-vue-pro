<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Skywalking">
import { ref } from 'vue'
import { IFrame } from '@/components/IFrame'

const src = ref('http://skywalking.shop.iocoder.cn')
</script>
