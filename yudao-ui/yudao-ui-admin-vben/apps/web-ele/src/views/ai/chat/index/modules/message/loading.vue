<script setup lang="ts">
import { ElSkeleton } from 'element-plus';
</script>

<template>
  <div class="p-8">
    <ElSkeleton :rows="5" animated />
  </div>
</template>
