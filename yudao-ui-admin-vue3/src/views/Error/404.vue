<template>
  <Error @error-click="push('/')" />
</template>
<script setup lang="ts">
const { push } = useRouter()
</script>
