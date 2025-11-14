<script lang="ts" setup>
const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <nav :class="props.class" aria-label="breadcrumb" role="navigation">
    <slot></slot>
  </nav>
</template>
