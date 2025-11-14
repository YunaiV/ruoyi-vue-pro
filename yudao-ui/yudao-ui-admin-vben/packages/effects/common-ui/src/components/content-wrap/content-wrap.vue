<script setup lang="ts">
import type { StyleValue } from 'vue';

import type { ContentWrapProps } from './types';

import { computed, nextTick, onMounted, ref, useTemplateRef } from 'vue';

import { CSS_VARIABLE_LAYOUT_CONTENT_HEIGHT } from '@vben-core/shared/constants';
import { cn } from '@vben-core/shared/utils';

defineOptions({
  name: 'ContentWrap',
});

const { autoContentHeight = false, heightOffset = 0 } =
  defineProps<ContentWrapProps>();

const headerHeight = ref(0);
const footerHeight = ref(0);
const shouldAutoHeight = ref(false);

const headerRef = useTemplateRef<HTMLDivElement>('headerRef');
const footerRef = useTemplateRef<HTMLDivElement>('footerRef');

const contentStyle = computed<StyleValue>(() => {
  if (autoContentHeight) {
    return {
      height: `calc(var(${CSS_VARIABLE_LAYOUT_CONTENT_HEIGHT}) - ${headerHeight.value}px - ${footerHeight.value}px - ${typeof heightOffset === 'number' ? `${heightOffset}px` : heightOffset})`,
      overflowY: shouldAutoHeight.value ? 'auto' : 'unset',
    };
  }
  return {};
});

async function calcContentHeight() {
  if (!autoContentHeight) {
    return;
  }
  await nextTick();
  headerHeight.value = headerRef.value?.offsetHeight || 0;
  footerHeight.value = footerRef.value?.offsetHeight || 0;
  setTimeout(() => {
    shouldAutoHeight.value = true;
  }, 30);
}

onMounted(() => {
  calcContentHeight();
});
</script>

<template>
  <div
    class="bg-card text-card-foreground border-border relative flex min-h-full flex-col rounded-xl border"
  >
    <div
      v-if="
        description ||
        $slots.description ||
        title ||
        $slots.title ||
        $slots.extra
      "
      ref="headerRef"
      :class="
        cn(
          'border-border relative flex items-end border-b px-6 py-4',
          headerClass,
        )
      "
    >
      <div class="flex-auto">
        <slot name="title">
          <div v-if="title" class="mb-2 flex text-lg font-semibold">
            {{ title }}
          </div>
          <div v-if="$slots.extra" class="flex justify-end">
            <slot name="extra"></slot>
          </div>
        </slot>

        <slot name="description">
          <p v-if="description" class="text-muted-foreground">
            {{ description }}
          </p>
        </slot>
      </div>
    </div>

    <div :class="cn('h-full p-4', contentClass)" :style="contentStyle">
      <slot></slot>
    </div>
    <div
      v-if="$slots.footer"
      ref="footerRef"
      :class="cn('align-center flex px-6 py-4', footerClass)"
    >
      <slot name="footer"></slot>
    </div>
  </div>
</template>
