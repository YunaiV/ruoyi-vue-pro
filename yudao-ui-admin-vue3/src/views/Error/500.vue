<template>
  <Error type="500" @error-click="push('/')" />
</template>
<script setup lang="ts">
const { push } = useRouter()
</script>
