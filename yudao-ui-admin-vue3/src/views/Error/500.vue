<script setup lang="ts">
import { Error } from '@/components/Error'
import { useRouter } from 'vue-router'

const { push } = useRouter()

const errorClick = () => {
  push('/')
}
</script>

<template>
  <Error type="500" @error-click="errorClick" />
</template>
