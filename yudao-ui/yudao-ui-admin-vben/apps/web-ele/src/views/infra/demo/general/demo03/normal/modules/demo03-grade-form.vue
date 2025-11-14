<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { nextTick, reactive, ref, watch } from 'vue';

import { ElForm, ElFormItem, ElInput } from 'element-plus';

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
const rules = reactive<FormRules>({
  studentId: [{ required: true, message: '学生编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  teacher: [{ required: true, message: '班主任不能为空', trigger: 'blur' }],
});
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
  <ElForm
    ref="formRef"
    class="mx-4"
    :model="formData"
    :rules="rules"
    label-width="120px"
    label-position="right"
  >
    <ElFormItem label="学生编号" prop="studentId">
      <ElInput v-model="formData.studentId" placeholder="请输入学生编号" />
    </ElFormItem>
    <ElFormItem label="名字" prop="name">
      <ElInput v-model="formData.name" placeholder="请输入名字" />
    </ElFormItem>
    <ElFormItem label="班主任" prop="teacher">
      <ElInput v-model="formData.teacher" placeholder="请输入班主任" />
    </ElFormItem>
  </ElForm>
</template>
