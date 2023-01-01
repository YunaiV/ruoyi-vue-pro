<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Server">
import { ref } from 'vue'
import { IFrame } from '@/components/IFrame'

const BASE_URL = import.meta.env.VITE_BASE_URL
const src = ref(BASE_URL + '/admin/applications')
</script>
