<template>
  <Error type="403" @error-click="push('/')" />
</template>
<script setup lang="ts">
const { push } = useRouter()
</script>
