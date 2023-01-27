<template>
  <Error @error-click="errorClick()" />
</template>
<script setup lang="ts">
const { push } = useRouter()

const errorClick = () => {
  push('/')
}
</script>
