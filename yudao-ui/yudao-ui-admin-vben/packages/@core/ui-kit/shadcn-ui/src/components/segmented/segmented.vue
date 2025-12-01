<script setup lang="ts">
import type { SegmentedItem } from './types';

import { computed } from 'vue';

import { TabsTrigger } from 'reka-ui';

import { Tabs, TabsContent, TabsList } from '../../ui';
import TabsIndicator from './tabs-indicator.vue';

interface Props {
  defaultValue?: string;
  tabs?: SegmentedItem[];
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: '',
  tabs: () => [],
});

const activeTab = defineModel<string>();

const getDefaultValue = computed(() => {
  return props.defaultValue || props.tabs[0]?.value;
});

const tabsStyle = computed(() => {
  return {
    'grid-template-columns': `repeat(${props.tabs.length}, minmax(0, 1fr))`,
  };
});

const tabsIndicatorStyle = computed(() => {
  return {
    width: `${(100 / props.tabs.length).toFixed(0)}%`,
  };
});

function activeClass(tab: string): string[] {
  return tab === activeTab.value ? ['!font-bold', 'text-primary'] : [];
}
</script>

<template>
  <Tabs v-model="activeTab" :default-value="getDefaultValue">
    <TabsList
      :style="tabsStyle"
      class="relative grid w-full bg-accent !outline !outline-2 !outline-heavy"
    >
      <TabsIndicator :style="tabsIndicatorStyle" />
      <template v-for="tab in tabs" :key="tab.value">
        <TabsTrigger
          :value="tab.value"
          :class="activeClass(tab.value)"
          class="z-20 inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium hover:text-primary disabled:pointer-events-none disabled:opacity-50"
        >
          {{ tab.label }}
        </TabsTrigger>
      </template>
    </TabsList>
    <template v-for="tab in tabs" :key="tab.value">
      <TabsContent :value="tab.value">
        <slot :name="tab.value"></slot>
      </TabsContent>
    </template>
  </Tabs>
</template>
