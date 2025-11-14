<script lang="ts" setup>
import { nextTick, watch } from 'vue';

import { useVbenForm } from '#/adapter/form';
import { getDemo03GradeByStudentId } from '#/api/infra/demo/demo03/inner';

import { useDemo03GradeFormSchema } from '../data';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useDemo03GradeFormSchema(),
  showDefaultActions: false,
});

/** 暴露出表单校验方法和表单值获取方法 */
defineExpose({
  validate: async () => {
    const { valid } = await formApi.validate();
    return valid;
  },
  getValues: formApi.getValues,
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await formApi.setValues(await getDemo03GradeByStudentId(props.studentId!));
  },
  { immediate: true },
);
</script>

<template>
  <Form class="mx-4" />
</template>
