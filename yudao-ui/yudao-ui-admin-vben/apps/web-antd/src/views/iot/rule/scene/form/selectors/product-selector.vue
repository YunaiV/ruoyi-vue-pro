<!-- 产品选择器组件 -->
<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';

import { Select } from 'ant-design-vue';

import { getSimpleProductList } from '#/api/iot/product/product';
import { DictTag } from '#/components/dict-tag';

/** 产品选择器组件 */
defineOptions({ name: 'ProductSelector' });

defineProps<{
  modelValue?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value?: number): void;
  (e: 'change', value?: number): void;
}>();

const productLoading = ref(false); // 产品加载状态
const productList = ref<any[]>([]); // 产品列表

/**
 * 处理选择变化事件
 * @param value 选中的产品 ID
 */
function handleChange(value?: number) {
  emit('update:modelValue', value);
  emit('change', value);
}

/** 获取产品列表 */
async function getProductList() {
  try {
    productLoading.value = true;
    const res = await getSimpleProductList();
    productList.value = res || [];
  } catch (error) {
    console.error('获取产品列表失败:', error);
    productList.value = [];
  } finally {
    productLoading.value = false;
  }
}

// 组件挂载时获取产品列表
onMounted(() => {
  getProductList();
});
</script>

<template>
  <Select
    :model-value="modelValue"
    @update:model-value="handleChange"
    placeholder="请选择产品"
    filterable
    clearable
    class="w-full"
    :loading="productLoading"
  >
    <Select.Option
      v-for="product in productList"
      :key="product.id"
      :label="product.name"
      :value="product.id"
    >
      <div class="py-4px flex w-full items-center justify-between">
        <div class="flex-1">
          <div class="text-14px font-500 mb-2px text-primary">
            {{ product.name }}
          </div>
          <div class="text-12px text-secondary">
            {{ product.productKey }}
          </div>
        </div>
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="product.status" />
      </div>
    </Select.Option>
  </Select>
</template>
