<script setup lang="ts">
import type { SetupContext } from 'vue';

import { computed, ref, useSlots } from 'vue';

import { VbenTooltip } from '@vben-core/shadcn-ui';

import { Code } from 'lucide-vue-next';
import {
  TabsContent,
  TabsIndicator,
  TabsList,
  TabsRoot,
  TabsTrigger,
} from 'radix-vue';

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    files?: string[];
  }>(),
  { files: () => [] },
);

const open = ref(false);

const slots: SetupContext['slots'] = useSlots();

const tabs = computed(() => {
  return props.files.map((file) => {
    return {
      component: slots[file],
      label: file,
    };
  });
});

const currentTab = ref('index.vue');

const toggleOpen = () => {
  open.value = !open.value;
};
</script>

<template>
  <TabsRoot
    v-model="currentTab"
    class="bg-background-deep border-border overflow-hidden rounded-b-xl border-t"
    @update:model-value="open = true"
  >
    <div class="border-border bg-background flex border-b-2 pr-2">
      <div class="flex w-full items-center justify-between text-[13px]">
        <TabsList class="relative flex">
          <template v-if="open">
            <TabsIndicator
              class="absolute bottom-0 left-0 h-[2px] w-[--radix-tabs-indicator-size] translate-x-[--radix-tabs-indicator-position] rounded-full transition-[width,transform] duration-300"
            >
              <div class="size-full bg-[var(--vp-c-indigo-1)]"></div>
            </TabsIndicator>
            <TabsTrigger
              v-for="(tab, index) in tabs"
              :key="index"
              :value="tab.label"
              class="border-box text-foreground px-4 py-3 data-[state=active]:text-[var(--vp-c-indigo-1)]"
              tabindex="-1"
            >
              {{ tab.label }}
            </TabsTrigger>
          </template>
        </TabsList>

        <div
          :class="{
            'py-2': !open,
          }"
          class="flex items-center"
        >
          <VbenTooltip side="top">
            <template #trigger>
              <Code
                class="hover:bg-accent size-7 cursor-pointer rounded-full p-1.5"
                @click="toggleOpen"
              />
            </template>
            {{ open ? 'Collapse code' : 'Expand code' }}
          </VbenTooltip>
        </div>
      </div>
    </div>
    <div
      :class="`${open ? 'h-[unset] max-h-[80vh]' : 'h-0'}`"
      class="block overflow-y-scroll bg-[var(--vp-code-block-bg)] transition-all duration-300"
    >
      <TabsContent
        v-for="tab in tabs"
        :key="tab.label"
        :value="tab.label"
        as-child
        class="rounded-xl"
      >
        <div class="text-foreground relative rounded-xl">
          <component :is="tab.component" class="border-0" />
        </div>
      </TabsContent>
    </div>
  </TabsRoot>
</template>
