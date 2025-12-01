<script setup lang="ts">
import { inject } from 'vue';

import { useScrollLock } from '@vben-core/composables';

useScrollLock();
const id = inject('DISMISSABLE_MODAL_ID');
</script>
<template>
  <div :data-dismissable-modal="id" class="z-popup inset-0 bg-overlay"></div>
</template>
