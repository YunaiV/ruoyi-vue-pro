<script lang="ts" setup>
defineOptions({ name: 'AiMusicTitleIndex' });

defineProps({
  title: {
    type: String,
    default: '',
  },
  desc: {
    type: String,
    default: '',
  },
});
</script>

<template>
  <div class="mb-3">
    <div class="flex items-center justify-between text-gray-600">
      <span>{{ title }}</span>
      <slot name="extra"></slot>
    </div>
    <div class="my-2 text-xs text-gray-400">
      {{ desc }}
    </div>
    <slot></slot>
  </div>
</template>
