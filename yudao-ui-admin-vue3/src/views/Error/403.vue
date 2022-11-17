<script setup lang="ts">
import { useRouter } from 'vue-router'

const { push } = useRouter()

const errorClick = () => {
  push('/')
}
</script>

<template>
  <Error type="403" @error-click="errorClick" />
</template>
