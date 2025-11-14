<script setup lang="ts">
import type { WorkbenchTodoItem } from '../typing';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  VbenCheckbox,
} from '@vben-core/shadcn-ui';

interface Props {
  items?: WorkbenchTodoItem[];
  title: string;
}

defineOptions({
  name: 'WorkbenchTodo',
});

withDefaults(defineProps<Props>(), {
  items: () => [],
});
</script>

<template>
  <Card>
    <CardHeader class="py-4">
      <CardTitle class="text-lg">{{ title }}</CardTitle>
    </CardHeader>
    <CardContent class="flex flex-wrap p-5 pt-0">
      <ul class="divide-border w-full divide-y" role="list">
        <li
          v-for="item in items"
          :key="item.title"
          :class="{
            'select-none line-through opacity-60': item.completed,
          }"
          class="flex cursor-pointer justify-between gap-x-6 py-5"
        >
          <div class="flex min-w-0 items-center gap-x-4">
            <VbenCheckbox v-model:checked="item.completed" name="completed" />
            <div class="min-w-0 flex-auto">
              <p class="text-foreground text-sm font-semibold leading-6">
                {{ item.title }}
              </p>
              <!-- eslint-disable vue/no-v-html -->
              <p
                class="text-foreground/80 *:text-primary mt-1 truncate text-xs leading-5"
                v-html="item.content"
              ></p>
            </div>
          </div>
          <div class="hidden h-full shrink-0 sm:flex sm:flex-col sm:items-end">
            <span class="text-foreground/80 mt-6 text-xs leading-6">
              {{ item.date }}
            </span>
          </div>
        </li>
      </ul>
    </CardContent>
  </Card>
</template>
