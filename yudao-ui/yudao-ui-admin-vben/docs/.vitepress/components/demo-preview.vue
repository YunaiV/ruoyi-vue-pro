<script setup lang="ts">
import { computed } from 'vue';

import PreviewGroup from './preview-group.vue';

interface Props {
  files?: string;
}

const props = withDefaults(defineProps<Props>(), { files: '() => []' });

const parsedFiles = computed(() => {
  try {
    return JSON.parse(decodeURIComponent(props.files ?? ''));
  } catch {
    return [];
  }
});
</script>

<template>
  <div class="border-border shadow-float relative rounded-xl border">
    <div
      class="not-prose relative w-full overflow-x-auto rounded-t-lg px-4 py-6"
    >
      <div class="flex w-full max-w-[700px] px-2">
        <ClientOnly>
          <slot v-if="parsedFiles.length > 0"></slot>
          <div v-else class="text-destructive text-sm">
            <span class="bg-destructive text-foreground rounded-sm px-1 py-1">
              ERROR:
            </span>
            The preview directory does not exist. Please check the 'dir'
            parameter.
          </div>
        </ClientOnly>
      </div>
    </div>
    <PreviewGroup v-if="parsedFiles.length > 0" :files="parsedFiles">
      <template v-for="file in parsedFiles" #[file]>
        <slot :name="file"></slot>
      </template>
    </PreviewGroup>
  </div>
</template>
