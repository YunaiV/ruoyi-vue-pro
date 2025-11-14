<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';
import type { SystemDictTypeApi } from '#/api/system/dict/type';

import { nextTick, onMounted, ref, watch } from 'vue';

import { Checkbox, Input, Select } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSimpleDictTypeList } from '#/api/system/dict/type';

import { useCodegenColumnTableColumns } from '../data';

const props = defineProps<{
  columns?: InfraCodegenApi.CodegenColumn[];
}>();

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useCodegenColumnTableColumns(),
    border: true,
    showOverflow: true,
    autoResize: true,
    keepSource: true,
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 监听外部传入的列数据 */
watch(
  () => props.columns,
  async (columns) => {
    if (!columns) {
      return;
    }
    await nextTick();
    gridApi.grid?.loadData(columns);
  },
  {
    immediate: true,
  },
);

/** 提供获取表格数据的方法供父组件调用 */
defineExpose({
  getData: (): InfraCodegenApi.CodegenColumn[] => gridApi.grid.getData(),
});

/** 初始化 */
const dictTypeOptions = ref<SystemDictTypeApi.DictType[]>([]); // 字典类型选项
onMounted(async () => {
  dictTypeOptions.value = await getSimpleDictTypeList();
});
</script>

<template>
  <Grid>
    <!-- 字段描述 -->
    <template #columnComment="{ row }">
      <Input v-model:value="row.columnComment" />
    </template>

    <!-- Java 类型 -->
    <template #javaType="{ row, column }">
      <Select v-model:value="row.javaType" style="width: 100%">
        <Select.Option
          v-for="option in column.params.options"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </Select.Option>
      </Select>
    </template>
    <!-- Java 属性 -->
    <template #javaField="{ row }">
      <Input v-model:value="row.javaField" />
    </template>

    <!-- 插入 -->
    <template #createOperation="{ row }">
      <Checkbox v-model:checked="row.createOperation" />
    </template>
    <!-- 编辑 -->
    <template #updateOperation="{ row }">
      <Checkbox v-model:checked="row.updateOperation" />
    </template>
    <!-- 列表 -->
    <template #listOperationResult="{ row }">
      <Checkbox v-model:checked="row.listOperationResult" />
    </template>
    <!-- 查询 -->
    <template #listOperation="{ row }">
      <Checkbox v-model:checked="row.listOperation" />
    </template>

    <!-- 查询方式 -->
    <template #listOperationCondition="{ row, column }">
      <Select v-model:value="row.listOperationCondition" class="w-full">
        <Select.Option
          v-for="option in column.params.options"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </Select.Option>
      </Select>
    </template>

    <!-- 允许空 -->
    <template #nullable="{ row }">
      <Checkbox v-model:checked="row.nullable" />
    </template>

    <!-- 显示类型 -->
    <template #htmlType="{ row, column }">
      <Select v-model:value="row.htmlType" class="w-full">
        <Select.Option
          v-for="option in column.params.options"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </Select.Option>
      </Select>
    </template>

    <!-- 字典类型 -->
    <template #dictType="{ row }">
      <Select
        v-model:value="row.dictType"
        class="w-full"
        allow-clear
        show-search
      >
        <Select.Option
          v-for="option in dictTypeOptions"
          :key="option.type"
          :value="option.type"
        >
          {{ option.name }}
        </Select.Option>
      </Select>
    </template>

    <!-- 示例 -->
    <template #example="{ row }">
      <Input v-model:value="row.example" />
    </template>
  </Grid>
</template>
