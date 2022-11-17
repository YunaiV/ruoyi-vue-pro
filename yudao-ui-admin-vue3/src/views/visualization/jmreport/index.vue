<script setup lang="ts">
import { IFrame } from '@/components/IFrame'
import { getAccessToken } from '@/utils/auth'
import { ref } from 'vue'
const BASE_URL = import.meta.env.VITE_BASE_URL
const src = ref(BASE_URL + '/jmreport/list?token=' + getAccessToken())
</script>

<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
