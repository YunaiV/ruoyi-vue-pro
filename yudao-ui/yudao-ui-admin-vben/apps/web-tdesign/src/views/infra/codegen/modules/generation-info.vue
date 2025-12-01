<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';

import { computed, ref, watch } from 'vue';

import { InfraCodegenTemplateTypeEnum } from '@vben/constants';
import { isEmpty } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import { getCodegenTableList } from '#/api/infra/codegen';

import {
  useGenerationInfoBaseFormSchema,
  useGenerationInfoSubTableFormSchema,
  useGenerationInfoTreeFormSchema,
} from '../data';

const props = defineProps<{
  columns?: InfraCodegenApi.CodegenColumn[];
  table?: InfraCodegenApi.CodegenTable;
}>();

const tables = ref<InfraCodegenApi.CodegenTable[]>([]);

/** 计算当前模板类型 */
const currentTemplateType = ref<number>();
const isTreeTable = computed(
  () => currentTemplateType.value === InfraCodegenTemplateTypeEnum.TREE,
);
const isSubTable = computed(
  () => currentTemplateType.value === InfraCodegenTemplateTypeEnum.SUB,
);

/** 基础表单实例 */
const [BaseForm, baseFormApi] = useVbenForm({
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 gap-4', // 配置表单布局为两列
  layout: 'horizontal',
  showDefaultActions: false,
  schema: useGenerationInfoBaseFormSchema(),
  handleValuesChange: (values) => {
    // 监听模板类型变化
    if (
      values.templateType !== undefined &&
      values.templateType !== currentTemplateType.value
    ) {
      currentTemplateType.value = values.templateType;
    }
  },
});

/** 树表信息表单实例 */
const [TreeForm, treeFormApi] = useVbenForm({
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 gap-4', // 配置表单布局为两列
  layout: 'horizontal',
  showDefaultActions: false,
  schema: [],
});

/** 主子表信息表单实例 */
const [SubForm, subFormApi] = useVbenForm({
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 gap-4', // 配置表单布局为两列
  layout: 'horizontal',
  showDefaultActions: false,
  schema: [],
});

/** 更新树表信息表单 schema */
function updateTreeSchema(): void {
  treeFormApi.setState({
    schema: useGenerationInfoTreeFormSchema(props.columns),
  });
  // 树表信息回显
  treeFormApi.setValues(props.table as any);
}

/** 更新主子表信息表单 schema */
function updateSubSchema(): void {
  subFormApi.setState({
    schema: useGenerationInfoSubTableFormSchema(props.columns, tables.value),
  });
  // 主子表信息回显
  subFormApi.setValues(props.table as any);
}

/** 获取合并的表单值 */
async function getAllFormValues(): Promise<Record<string, any>> {
  // 基础表单值
  const baseValues = await baseFormApi.getValues();
  // 根据模板类型获取对应的额外表单值
  let extraValues = {};
  if (isTreeTable.value) {
    extraValues = await treeFormApi.getValues();
  } else if (isSubTable.value) {
    extraValues = await subFormApi.getValues();
  }
  // 合并表单值
  return { ...baseValues, ...extraValues };
}

/** 验证所有表单 */
async function validateAllForms() {
  // 验证基础表单
  const { valid: baseFormValid } = await baseFormApi.validate();
  // 根据模板类型验证对应的额外表单
  let extraValid = true;
  if (isTreeTable.value) {
    const { valid: treeFormValid } = await treeFormApi.validate();
    extraValid = treeFormValid;
  } else if (isSubTable.value) {
    const { valid: subFormValid } = await subFormApi.validate();
    extraValid = subFormValid;
  }
  return baseFormValid && extraValid;
}

/** 设置表单值 */
function setAllFormValues(values: Record<string, any>): void {
  if (!values) {
    return;
  }

  // 记录模板类型
  currentTemplateType.value = values.templateType;

  // 设置基础表单值
  baseFormApi.setValues(values);
  // 根据模板类型设置对应的额外表单值
  if (isTreeTable.value) {
    treeFormApi.setValues(values);
  } else if (isSubTable.value) {
    subFormApi.setValues(values);
  }
}

/** 监听表格数据变化 */
watch(
  () => props.table,
  async (val) => {
    if (!val || isEmpty(val)) {
      return;
    }

    const table = val as InfraCodegenApi.CodegenTable;
    // 初始化树表的 schema
    updateTreeSchema();
    // 设置表单值
    setAllFormValues(table);
    // 获取表数据，用于主子表选择
    const dataSourceConfigId = table.dataSourceConfigId;
    if (dataSourceConfigId === undefined) {
      return;
    }
    tables.value = await getCodegenTableList(dataSourceConfigId);
    // 初始化子表 schema
    updateSubSchema();
  },
  { immediate: true },
);

/** 暴露出表单校验方法和表单值获取方法 */
defineExpose({
  validate: validateAllForms,
  getValues: getAllFormValues,
});
</script>

<template>
  <div>
    <!-- 基础表单 -->
    <BaseForm />
    <!-- 树表信息表单 -->
    <TreeForm v-if="isTreeTable" />
    <!-- 主子表信息表单 -->
    <SubForm v-if="isSubTable" />
  </div>
</template>
