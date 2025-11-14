<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';
import type { SystemDictTypeApi } from '#/api/system/dict/type';

import { nextTick, onMounted, ref, watch } from 'vue';

import { ElCheckbox, ElInput, ElOption, ElSelect } from 'element-plus';

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
      <ElInput v-model="row.columnComment" />
    </template>

    <!-- Java 类型 -->
    <template #javaType="{ row, column }">
      <ElSelect v-model="row.javaType" style="width: 100%">
        <ElOption
          v-for="option in column.params.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </ElSelect>
    </template>
    <!-- Java 属性 -->
    <template #javaField="{ row }">
      <ElInput v-model="row.javaField" />
    </template>

    <!-- 插入 -->
    <template #createOperation="{ row }">
      <ElCheckbox v-model="row.createOperation" />
    </template>
    <!-- 编辑 -->
    <template #updateOperation="{ row }">
      <ElCheckbox v-model="row.updateOperation" />
    </template>
    <!-- 列表 -->
    <template #listOperationResult="{ row }">
      <ElCheckbox v-model="row.listOperationResult" />
    </template>
    <!-- 查询 -->
    <template #listOperation="{ row }">
      <ElCheckbox v-model="row.listOperation" />
    </template>

    <!-- 查询方式 -->
    <template #listOperationCondition="{ row, column }">
      <ElSelect v-model="row.listOperationCondition" class="w-full">
        <ElOption
          v-for="option in column.params.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </ElSelect>
    </template>

    <!-- 允许空 -->
    <template #nullable="{ row }">
      <ElCheckbox v-model="row.nullable" />
    </template>

    <!-- 显示类型 -->
    <template #htmlType="{ row, column }">
      <ElSelect v-model="row.htmlType" class="w-full">
        <ElOption
          v-for="option in column.params.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </ElSelect>
    </template>

    <!-- 字典类型 -->
    <template #dictType="{ row }">
      <ElSelect v-model="row.dictType" class="w-full" clearable filterable>
        <ElOption
          v-for="option in dictTypeOptions"
          :key="option.type"
          :label="option.name"
          :value="option.type"
        />
      </ElSelect>
    </template>

    <!-- 示例 -->
    <template #example="{ row }">
      <ElInput v-model="row.example" />
    </template>
  </Grid>
</template>
