<script lang="ts" setup>
import type { ColPageProps } from './types';

import { computed, ref, useSlots } from 'vue';

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '@vben-core/shadcn-ui';

import Page from '../page/page.vue';

defineOptions({
  name: 'ColPage',
  inheritAttrs: false,
});

const props = withDefaults(defineProps<ColPageProps>(), {
  leftWidth: 30,
  rightWidth: 70,
  resizable: true,
});

const delegatedProps = computed(() => {
  const { leftWidth: _, ...delegated } = props;
  return delegated;
});

const slots = useSlots();

const delegatedSlots = computed(() => {
  const resultSlots: string[] = [];

  for (const key of Object.keys(slots)) {
    if (!['default', 'left'].includes(key)) {
      resultSlots.push(key);
    }
  }
  return resultSlots;
});

const leftPanelRef = ref<InstanceType<typeof ResizablePanel>>();

function expandLeft() {
  leftPanelRef.value?.expand();
}

function collapseLeft() {
  leftPanelRef.value?.collapse();
}

defineExpose({
  expandLeft,
  collapseLeft,
});
</script>
<template>
  <Page v-bind="delegatedProps">
    <!-- 继承默认的slot -->
    <template
      v-for="slotName in delegatedSlots"
      :key="slotName"
      #[slotName]="slotProps"
    >
      <slot :name="slotName" v-bind="slotProps"></slot>
    </template>

    <ResizablePanelGroup class="w-full" direction="horizontal">
      <ResizablePanel
        ref="leftPanelRef"
        :collapsed-size="leftCollapsedWidth"
        :collapsible="leftCollapsible"
        :default-size="leftWidth"
        :max-size="leftMaxWidth"
        :min-size="leftMinWidth"
      >
        <template #default="slotProps">
          <slot
            name="left"
            v-bind="{
              ...slotProps,
              expand: expandLeft,
              collapse: collapseLeft,
            }"
          ></slot>
        </template>
      </ResizablePanel>
      <ResizableHandle
        v-if="resizable"
        :style="{ backgroundColor: splitLine ? undefined : 'transparent' }"
        :with-handle="splitHandle"
      />
      <ResizablePanel
        :collapsed-size="rightCollapsedWidth"
        :collapsible="rightCollapsible"
        :default-size="rightWidth"
        :max-size="rightMaxWidth"
        :min-size="rightMinWidth"
      >
        <template #default>
          <slot></slot>
        </template>
      </ResizablePanel>
    </ResizablePanelGroup>
  </Page>
</template>
