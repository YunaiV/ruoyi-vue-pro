<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { nextTick, ref, watch } from 'vue';

import { Form, Input } from 'ant-design-vue';

import { getDemo03GradeByStudentId } from '#/api/infra/demo/demo03/normal';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const formRef = ref();
const formData = ref<Partial<Demo03StudentApi.Demo03Grade>>({
  id: undefined,
  studentId: undefined,
  name: undefined,
  teacher: undefined,
});
const rules: Record<string, Rule[]> = {
  studentId: [{ required: true, message: '学生编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  teacher: [{ required: true, message: '班主任不能为空', trigger: 'blur' }],
};
/** 暴露出表单校验方法和表单值获取方法 */
defineExpose({
  validate: async () => await formRef.value?.validate(),
  getValues: () => formData.value,
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    formData.value = await getDemo03GradeByStudentId(props.studentId!);
  },
  { immediate: true },
);
</script>

<template>
  <Form
    ref="formRef"
    class="mx-4"
    :model="formData"
    :rules="rules"
    :label-col="{ span: 5 }"
    :wrapper-col="{ span: 18 }"
  >
    <Form.Item label="学生编号" name="studentId">
      <Input v-model:value="formData.studentId" placeholder="请输入学生编号" />
    </Form.Item>
    <Form.Item label="名字" name="name">
      <Input v-model:value="formData.name" placeholder="请输入名字" />
    </Form.Item>
    <Form.Item label="班主任" name="teacher">
      <Input v-model:value="formData.teacher" placeholder="请输入班主任" />
    </Form.Item>
  </Form>
</template>
