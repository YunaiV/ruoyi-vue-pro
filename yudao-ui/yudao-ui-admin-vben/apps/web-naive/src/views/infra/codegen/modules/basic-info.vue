<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';

import { watch } from 'vue';

import { useVbenForm } from '#/adapter/form';

import { useBasicInfoFormSchema } from '../data';

const props = defineProps<{
  table: InfraCodegenApi.CodegenTable;
}>();

/** 表单实例 */
const [Form, formApi] = useVbenForm({
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 gap-4', // 配置表单布局为两列
  schema: useBasicInfoFormSchema(),
  layout: 'horizontal',
  showDefaultActions: false,
});

/** 动态更新表单值 */
watch(
  () => props.table,
  (val: any) => {
    if (!val) {
      return;
    }
    formApi.setValues(val);
  },
  { immediate: true },
);

/** 暴露出表单校验方法和表单值获取方法 */
defineExpose({
  validate: async () => {
    const { valid } = await formApi.validate();
    return valid;
  },
  getValues: formApi.getValues,
});
</script>
<template>
  <Form />
</template>
