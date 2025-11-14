<!-- 装修基础组件：分割线 -->
<template>
  <su-subline v-bind="data"></su-subline>
</template>

<script setup>
  const props = defineProps({
    data: {
      type: Object,
      default: {},
    },
  });
</script>

<style></style>
