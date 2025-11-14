<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, Input, message } from 'ant-design-vue';

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
const rules: Record<string, Rule[]> = {
  studentId: [{ required: true, message: '学生编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  score: [{ required: true, message: '分数不能为空', trigger: 'blur' }],
};

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
      message.success($t('ui.actionMessage.operationSuccess'));
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
function resetForm() {
  formData.value = {
    id: undefined,
    studentId: undefined,
    name: undefined,
    score: undefined,
  };
  formRef.value?.resetFields();
}
</script>

<template>
  <Modal :title="getTitle">
    <Form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <Form.Item label="学生编号" name="studentId">
        <Input
          v-model:value="formData.studentId"
          placeholder="请输入学生编号"
        />
      </Form.Item>
      <Form.Item label="名字" name="name">
        <Input v-model:value="formData.name" placeholder="请输入名字" />
      </Form.Item>
      <Form.Item label="分数" name="score">
        <Input v-model:value="formData.score" placeholder="请输入分数" />
      </Form.Item>
    </Form>
  </Modal>
</template>
