<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { handleTree } from '@vben/utils';

import * as ProductCategoryApi from '#/api/mall/product/category';

/** 商品分类选择组件 */
defineOptions({ name: 'ProductCategorySelect' });

const props = defineProps({
  // 选中的ID
  modelValue: {
    type: [Number, Array<Number>],
    default: undefined,
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false,
  },
  // 上级品类的编号
  parentId: {
    type: Number,
    default: undefined,
  },
});

/** 分类选择 */
const emit = defineEmits(['update:modelValue']);

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
const categoryList = ref<any[]>([]); // 分类树
onMounted(async () => {
  // 获得分类树
  const data = await ProductCategoryApi.getCategoryList({
    parentId: props.parentId,
  });
  categoryList.value = handleTree(data, 'id', 'parentId');
});
</script>
<template>
  <el-tree-select
    v-model="selectCategoryId"
    :data="categoryList"
    :props="{
      children: 'children',
      label: 'name',
      value: 'id',
      isLeaf: 'leaf',
      emitPath: false,
    }"
    :multiple="multiple"
    :show-checkbox="multiple"
    class="w-1/1"
    node-key="id"
    placeholder="请选择商品分类"
  />
</template>
