<template>
  <ContentWrap>
    <IFrame :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts" name="Jmreport">
import { ref } from 'vue'
import { IFrame } from '@/components/IFrame'
import { getAccessToken } from '@/utils/auth'

const BASE_URL = import.meta.env.VITE_BASE_URL
const src = ref(BASE_URL + '/jmreport/list?token=' + getAccessToken())
</script>
