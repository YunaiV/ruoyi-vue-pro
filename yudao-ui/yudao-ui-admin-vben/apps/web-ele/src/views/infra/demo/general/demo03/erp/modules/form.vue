<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { computed, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import {
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElRadio,
  ElRadioGroup,
} from 'element-plus';

import {
  createDemo03Student,
  getDemo03Student,
  updateDemo03Student,
} from '#/api/infra/demo/demo03/erp';
import { Tinymce as RichTextarea } from '#/components/tinymce';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const formRef = ref();
const formData = ref<Partial<Demo03StudentApi.Demo03Student>>({
  id: undefined,
  name: undefined,
  sex: undefined,
  birthday: undefined,
  description: undefined,
});
const rules = reactive<FormRules>({
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  sex: [{ required: true, message: '性别不能为空', trigger: 'blur' }],
  birthday: [{ required: true, message: '出生日期不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '简介不能为空', trigger: 'blur' }],
});
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['学生'])
    : $t('ui.actionTitle.create', ['学生']);
});

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    sex: undefined,
    birthday: undefined,
    description: undefined,
  };
  formRef.value?.resetFields();
};

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formRef.value?.validate();
    modalApi.lock();
    // 提交表单
    const data = formData.value as Demo03StudentApi.Demo03Student;
    try {
      await (formData.value?.id
        ? updateDemo03Student(data)
        : createDemo03Student(data));
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
    let data = modalApi.getData<Demo03StudentApi.Demo03Student>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getDemo03Student(data.id);
      } finally {
        modalApi.unlock();
      }
    }
    formData.value = data;
  },
});
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
      <ElFormItem label="名字" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入名字" />
      </ElFormItem>
      <ElFormItem label="性别" prop="sex">
        <ElRadioGroup v-model="formData.sex">
          <ElRadio
            v-for="dict in getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number')"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="出生日期" prop="birthday">
        <ElDatePicker
          v-model="formData.birthday"
          value-format="x"
          placeholder="选择出生日期"
        />
      </ElFormItem>
      <ElFormItem label="简介" prop="description">
        <RichTextarea v-model="formData.description" height="500px" />
      </ElFormItem>
    </ElForm>
  </Modal>
</template>
