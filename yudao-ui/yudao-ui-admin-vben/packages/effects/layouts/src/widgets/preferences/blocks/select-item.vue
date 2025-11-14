<script setup lang="ts">
import type { SelectOption } from '@vben/types';

import { useSlots } from 'vue';

import { CircleHelp } from '@vben/icons';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  VbenTooltip,
} from '@vben-core/shadcn-ui';

defineOptions({
  name: 'PreferenceSelectItem',
});

withDefaults(
  defineProps<{
    disabled?: boolean;
    items?: SelectOption[];
    placeholder?: string;
  }>(),
  {
    disabled: false,
    placeholder: '',
    items: () => [],
  },
);

const selectValue = defineModel<string>();

const slots = useSlots();
</script>

<template>
  <div
    :class="{
      'hover:bg-accent': !slots.tip,
      'pointer-events-none opacity-50': disabled,
    }"
    class="my-1 flex w-full items-center justify-between rounded-md px-2 py-1"
  >
    <span class="flex items-center text-sm">
      <slot></slot>

      <VbenTooltip v-if="slots.tip" side="bottom">
        <template #trigger>
          <CircleHelp class="ml-1 size-3 cursor-help" />
        </template>
        <slot name="tip"></slot>
      </VbenTooltip>
    </span>
    <Select v-model="selectValue">
      <SelectTrigger class="h-8 w-[165px]">
        <SelectValue :placeholder="placeholder" />
      </SelectTrigger>
      <SelectContent>
        <template v-for="item in items" :key="item.value">
          <SelectItem :value="item.value"> {{ item.label }} </SelectItem>
        </template>
      </SelectContent>
    </Select>
  </div>
</template>
