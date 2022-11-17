<script setup lang="ts">
import { useRouter } from 'vue-router'

const { push } = useRouter()

const errorClick = () => {
  push('/')
}
</script>

<template>
  <Error @error-click="errorClick" />
</template>
