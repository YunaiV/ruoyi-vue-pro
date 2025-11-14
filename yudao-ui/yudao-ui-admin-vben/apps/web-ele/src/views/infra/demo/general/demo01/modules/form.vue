<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo01ContactApi } from '#/api/infra/demo/demo01';

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
  createDemo01Contact,
  getDemo01Contact,
  updateDemo01Contact,
} from '#/api/infra/demo/demo01';
import { Tinymce as RichTextarea } from '#/components/tinymce';
import { ImageUpload } from '#/components/upload';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const formRef = ref();
const formData = ref<Partial<Demo01ContactApi.Demo01Contact>>({
  id: undefined,
  name: undefined,
  sex: undefined,
  birthday: undefined,
  description: undefined,
  avatar: undefined,
});
const rules = reactive<FormRules>({
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  sex: [{ required: true, message: '性别不能为空', trigger: 'blur' }],
  birthday: [{ required: true, message: '出生年不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '简介不能为空', trigger: 'blur' }],
});
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['示例联系人'])
    : $t('ui.actionTitle.create', ['示例联系人']);
});

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    sex: undefined,
    birthday: undefined,
    description: undefined,
    avatar: undefined,
  };
  formRef.value?.resetFields();
};

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formRef.value?.validate();
    modalApi.lock();
    // 提交表单
    const data = formData.value as Demo01ContactApi.Demo01Contact;
    try {
      await (formData.value?.id
        ? updateDemo01Contact(data)
        : createDemo01Contact(data));
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
    let data = modalApi.getData<Demo01ContactApi.Demo01Contact>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getDemo01Contact(data.id);
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
            v-for="(dict, index) in getDictOptions(
              DICT_TYPE.SYSTEM_USER_SEX,
              'number',
            )"
            :key="index"
            :label="dict.value"
          >
            {{ dict.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="出生年" prop="birthday">
        <ElDatePicker
          v-model="formData.birthday"
          value-format="x"
          placeholder="选择出生年"
        />
      </ElFormItem>
      <ElFormItem label="简介" prop="description">
        <RichTextarea v-model="formData.description" height="500px" />
      </ElFormItem>
      <ElFormItem label="头像" prop="avatar">
        <ImageUpload v-model="formData.avatar" />
      </ElFormItem>
    </ElForm>
  </Modal>
</template>
