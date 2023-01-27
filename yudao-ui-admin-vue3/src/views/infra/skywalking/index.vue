<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Skywalking">
const src = ref('http://skywalking.shop.iocoder.cn')
</script>
