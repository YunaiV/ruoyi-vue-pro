<script setup lang="ts">
import type { WorkbenchProjectItem } from '../typing';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  VbenIcon,
} from '@vben-core/shadcn-ui';

interface Props {
  items?: WorkbenchProjectItem[];
  title: string;
}

defineOptions({
  name: 'WorkbenchProject',
});

withDefaults(defineProps<Props>(), {
  items: () => [],
});

defineEmits(['click']);
</script>

<template>
  <Card>
    <CardHeader class="py-4">
      <CardTitle class="text-lg">{{ title }}</CardTitle>
    </CardHeader>
    <CardContent class="flex flex-wrap p-0">
      <template v-for="(item, index) in items" :key="item.title">
        <div
          :class="{
            'border-r-0': index % 3 === 2,
            'border-b-0': index < 3,
            'pb-4': index > 2,
            'rounded-bl-xl': index === items.length - 3,
            'rounded-br-xl': index === items.length - 1,
          }"
          class="border-border group w-full cursor-pointer border-r border-t p-4 transition-all hover:shadow-xl md:w-1/2 lg:w-1/3"
        >
          <div class="flex items-center">
            <VbenIcon
              :color="item.color"
              :icon="item.icon"
              class="size-8 transition-all duration-300 group-hover:scale-110"
              @click="$emit('click', item)"
            />
            <span class="ml-4 text-lg font-medium">{{ item.title }}</span>
          </div>
          <div class="text-foreground/80 mt-4 flex h-10">
            {{ item.content }}
          </div>
          <div class="text-foreground/80 flex justify-between">
            <span>{{ item.group }}</span>
            <span>{{ item.date }}</span>
          </div>
        </div>
      </template>
    </CardContent>
  </Card>
</template>
