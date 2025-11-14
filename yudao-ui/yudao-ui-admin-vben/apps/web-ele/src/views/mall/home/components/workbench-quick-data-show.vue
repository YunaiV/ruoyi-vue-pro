<script setup lang="ts">
import type { WorkbenchQuickDataShowItem } from './data';

import { computed } from 'vue';

import { CountTo } from '@vben/common-ui';

interface Props {
  items?: WorkbenchQuickDataShowItem[];
  modelValue?: WorkbenchQuickDataShowItem[];
  title: string;
}

defineOptions({
  name: 'WorkbenchQuickDataShow',
});

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  modelValue: () => [],
});

const emit = defineEmits(['update:modelValue']);

// 使用计算属性实现双向绑定
const itemsData = computed({
  get: () => (props.modelValue?.length ? props.modelValue : props.items),
  set: (value) => {
    emit('update:modelValue', value);
  },
});
</script>

<template>
  <el-card>
    <template #header>
      <!--      <CardTitle class="text-lg " >{{ title }}</CardTitle>-->
      <div class="text-lg font-semibold">{{ title }}</div>
    </template>
    <template #default>
      <div class="flex flex-wrap p-0">
        <div
          v-for="(item, index) in itemsData"
          :key="item.name"
          :class="{
            'border-r-0': index % 4 === 3,
            'border-b-0': index < 4,
            'pb-4': index > 4,
            'rounded-bl-xl': index === itemsData.length - 4,
            'rounded-br-xl': index === itemsData.length - 1,
          }"
          class="flex-col-center group w-1/4 cursor-pointer py-9"
        >
          <div class="mb-2 flex justify-center">
            <CountTo
              :prefix="item.prefix || ''"
              :end-val="Number(item.value)"
              :decimals="item.decimals || 0"
              class="text-4xl font-normal"
            />
          </div>
          <span class="truncate text-base text-gray-500">{{ item.name }}</span>
        </div>
      </div>
    </template>
    <!--    <CardContent class="flex flex-wrap p-0">-->
    <!--      <template>-->
    <!--        -->
    <!--      </template>-->
    <!--    </CardContent>-->
  </el-card>
</template>
