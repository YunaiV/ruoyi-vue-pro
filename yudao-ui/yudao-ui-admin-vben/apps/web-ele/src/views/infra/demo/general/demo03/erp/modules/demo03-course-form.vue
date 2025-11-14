<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { computed, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus';

import {
  createDemo03Course,
  getDemo03Course,
  updateDemo03Course,
} from '#/api/infra/demo/demo03/erp';
import { $t } from '#/locales';

const emit = defineEmits(['success']);
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['学生课程'])
    : $t('ui.actionTitle.create', ['学生课程']);
});

const formRef = ref();
const formData = ref<Partial<Demo03StudentApi.Demo03Course>>({
  id: undefined,
  studentId: undefined,
  name: undefined,
  score: undefined,
});
const rules = reactive<FormRules>({
  studentId: [{ required: true, message: '学生编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  score: [{ required: true, message: '分数不能为空', trigger: 'blur' }],
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formRef.value?.validate();

    modalApi.lock();
    // 提交表单
    const data = formData.value as Demo03StudentApi.Demo03Course;
    try {
      await (formData.value?.id
        ? updateDemo03Course(data)
        : createDemo03Course(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      resetForm();
      return;
    }

    // 加载数据
    let data = modalApi.getData<Demo03StudentApi.Demo03Course>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getDemo03Course(data.id);
      } finally {
        modalApi.unlock();
      }
    }
    // 设置到 values
    formData.value = data;
  },
});

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    studentId: undefined,
    name: undefined,
    score: undefined,
  };
  formRef.value?.resetFields();
};
</script>

<template>
  <Modal :title="getTitle">
    <ElForm
      ref="formRef"
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
      <ElFormItem label="分数" prop="score">
        <ElInput v-model="formData.score" placeholder="请输入分数" />
      </ElFormItem>
    </ElForm>
  </Modal>
</template>
