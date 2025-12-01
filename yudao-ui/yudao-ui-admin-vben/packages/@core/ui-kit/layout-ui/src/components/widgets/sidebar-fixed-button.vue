<script setup lang="ts">
import { Pin, PinOff } from '@vben-core/icons';

const expandOnHover = defineModel<boolean>('expandOnHover');

function toggleFixed() {
  expandOnHover.value = !expandOnHover.value;
}
</script>

<template>
  <div
    class="flex-center absolute bottom-2 right-3 z-10 cursor-pointer rounded-sm bg-accent p-[5px] text-foreground/60 transition-all duration-300 hover:bg-accent-hover hover:text-foreground"
    @click="toggleFixed"
  >
    <PinOff v-if="!expandOnHover" class="size-3.5" />
    <Pin v-else class="size-3.5" />
  </div>
</template>
