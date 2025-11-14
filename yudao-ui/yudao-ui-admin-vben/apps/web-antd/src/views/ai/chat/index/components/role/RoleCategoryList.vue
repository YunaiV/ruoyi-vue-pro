<script setup lang="ts">
import type { PropType } from 'vue';

import { Button } from 'ant-design-vue';
// 定义属性
defineProps({
  categoryList: {
    type: Array as PropType<string[]>,
    required: true,
  },
  active: {
    type: String,
    required: false,
    default: '全部',
  },
});

// 定义回调
const emits = defineEmits(['onCategoryClick']);

/** 处理分类点击事件 */
async function handleCategoryClick(category: string) {
  emits('onCategoryClick', category);
}
</script>

<template>
  <div class="flex flex-wrap items-center">
    <div
      class="mr-2 flex flex-row"
      v-for="category in categoryList"
      :key="category"
    >
      <Button
        size="small"
        shape="round"
        :type="category === active ? 'primary' : 'default'"
        @click="handleCategoryClick(category)"
      >
        {{ category }}
      </Button>
    </div>
  </div>
</template>
