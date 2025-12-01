<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { handleTree } from '@vben/utils';

import { TreeSelect } from 'ant-design-vue';

import { getCategoryList } from '#/api/mall/product/category';

/** 商品分类选择组件 */
defineOptions({ name: 'ProductCategorySelect' });

const props = defineProps({
  modelValue: {
    type: [Number, Array<Number>],
    default: undefined,
  }, // 选中的 ID
  multiple: {
    type: Boolean,
    default: false,
  }, // 是否多选
  parentId: {
    type: Number,
    default: undefined,
  }, // 上级品类的编号
});

/** 分类选择 */
const emit = defineEmits(['update:modelValue']);

const categoryList = ref<any[]>([]); // 分类树

/** 选中的分类 ID */
const selectCategoryId = computed({
  get: () => {
    return props.modelValue;
  },
  set: (val: number | number[]) => {
    emit('update:modelValue', val);
  },
});

/** 初始化 */
onMounted(async () => {
  const data = await getCategoryList({
    parentId: props.parentId,
  });
  categoryList.value = handleTree(data, 'id', 'parentId');
});
</script>
<template>
  <TreeSelect
    v-model:value="selectCategoryId"
    :tree-data="categoryList"
    :field-names="{
      children: 'children',
      label: 'name',
      value: 'id',
    }"
    :multiple="multiple"
    :tree-checkable="multiple"
    class="w-full"
    placeholder="请选择商品分类"
    allow-clear
    tree-default-expand-all
  />
</template>
