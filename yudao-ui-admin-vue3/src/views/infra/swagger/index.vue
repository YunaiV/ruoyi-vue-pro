<script setup lang="ts">
import { IFrame } from '@/components/IFrame'
import { ref } from 'vue'
const BASE_URL = import.meta.env.VITE_BASE_URL
const src = ref(BASE_URL + '/doc.html')
</script>

<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
