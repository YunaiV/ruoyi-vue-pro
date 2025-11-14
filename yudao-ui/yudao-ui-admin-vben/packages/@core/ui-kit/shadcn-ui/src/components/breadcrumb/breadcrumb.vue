<script lang="ts" setup>
import type { BreadcrumbProps } from './types';

import { ChevronDown } from '@vben-core/icons';

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../../ui';
import { VbenIcon } from '../icon';

interface Props extends BreadcrumbProps {}

defineOptions({ name: 'Breadcrumb' });
withDefaults(defineProps<Props>(), {
  showIcon: false,
});

const emit = defineEmits<{ select: [string] }>();

function handleClick(path?: string) {
  if (!path) {
    return;
  }
  emit('select', path);
}
</script>
<template>
  <Breadcrumb>
    <BreadcrumbList>
      <TransitionGroup name="breadcrumb-transition">
        <template
          v-for="(item, index) in breadcrumbs"
          :key="`${item.path}-${item.title}-${index}`"
        >
          <BreadcrumbItem>
            <div v-if="item.items?.length ?? 0 > 0">
              <DropdownMenu>
                <DropdownMenuTrigger class="flex items-center gap-1">
                  <VbenIcon v-if="showIcon" :icon="item.icon" class="size-5" />
                  {{ item.title }}
                  <ChevronDown class="size-4" />
                </DropdownMenuTrigger>
                <DropdownMenuContent align="start">
                  <template
                    v-for="menuItem in item.items"
                    :key="`sub-${menuItem.path}`"
                  >
                    <DropdownMenuItem @click.stop="handleClick(menuItem.path)">
                      {{ menuItem.title }}
                    </DropdownMenuItem>
                  </template>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            <BreadcrumbLink
              v-else-if="index !== breadcrumbs.length - 1"
              href="javascript:void 0"
              @click.stop="handleClick(item.path)"
            >
              <div class="flex-center">
                <VbenIcon
                  v-if="showIcon"
                  :class="{ 'size-5': item.isHome }"
                  :icon="item.icon"
                  class="mr-1 size-4"
                />
                {{ item.title }}
              </div>
            </BreadcrumbLink>
            <BreadcrumbPage v-else>
              <div class="flex-center">
                <VbenIcon
                  v-if="showIcon"
                  :class="{ 'size-5': item.isHome }"
                  :icon="item.icon"
                  class="mr-1 size-4"
                />
                {{ item.title }}
              </div>
            </BreadcrumbPage>
            <BreadcrumbSeparator
              v-if="index < breadcrumbs.length - 1 && !item.isHome"
            />
          </BreadcrumbItem>
        </template>
      </TransitionGroup>
    </BreadcrumbList>
  </Breadcrumb>
</template>
