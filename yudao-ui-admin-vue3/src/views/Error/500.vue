<template>
  <Error type="500" @error-click="errorClick()" />
</template>
<script setup lang="ts">
const { push } = useRouter()

const errorClick = () => {
  push('/')
}
</script>
