<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Swagger">
const BASE_URL = import.meta.env.VITE_BASE_URL
// const src = ref(BASE_URL + '/doc.html')
const src = ref(BASE_URL + '/swagger-ui')
</script>
