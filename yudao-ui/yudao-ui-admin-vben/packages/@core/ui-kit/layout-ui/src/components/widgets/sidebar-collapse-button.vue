<script setup lang="ts">
import { ChevronsLeft, ChevronsRight } from '@vben-core/icons';

const collapsed = defineModel<boolean>('collapsed');

function handleCollapsed() {
  collapsed.value = !collapsed.value;
}
</script>

<template>
  <div
    class="flex-center absolute bottom-2 left-3 z-10 cursor-pointer rounded-sm bg-accent p-1 text-foreground/60 hover:bg-accent-hover hover:text-foreground"
    @click.stop="handleCollapsed"
  >
    <ChevronsRight v-if="collapsed" class="size-4" />
    <ChevronsLeft v-else class="size-4" />
  </div>
</template>
