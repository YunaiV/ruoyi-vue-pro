<script setup lang="ts">
import type { AvatarImageProps } from 'radix-vue';

import { AvatarImage } from 'radix-vue';

const props = defineProps<AvatarImageProps>();
</script>

<template>
  <AvatarImage v-bind="props" class="h-full w-full object-cover" />
</template>
