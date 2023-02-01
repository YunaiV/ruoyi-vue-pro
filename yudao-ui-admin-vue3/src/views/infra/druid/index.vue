<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Druid">
const BASE_URL = import.meta.env.VITE_BASE_URL
const src = ref(BASE_URL + '/druid/index.html')
</script>
