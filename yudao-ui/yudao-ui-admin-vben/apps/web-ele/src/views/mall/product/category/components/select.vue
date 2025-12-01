<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { handleTree } from '@vben/utils';

import { ElTreeSelect } from 'element-plus';

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
  <ElTreeSelect
    v-model="selectCategoryId"
    :data="categoryList"
    node-key="id"
    :props="{
      children: 'children',
      label: 'name',
    }"
    :multiple="multiple"
    :show-checkbox="multiple"
    check-strictly
    default-expand-all
    class="w-full"
    placeholder="请选择商品分类"
  />
</template>
