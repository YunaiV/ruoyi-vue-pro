<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import type { Demo01ContactApi } from '#/api/infra/demo/demo01';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import {
  DatePicker,
  Form,
  Input,
  message,
  Radio,
  RadioGroup,
} from 'ant-design-vue';

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
const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  sex: [{ required: true, message: '性别不能为空', trigger: 'blur' }],
  birthday: [{ required: true, message: '出生年不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '简介不能为空', trigger: 'blur' }],
};
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['示例联系人'])
    : $t('ui.actionTitle.create', ['示例联系人']);
});

/** 重置表单 */
function resetForm() {
  formData.value = {
    id: undefined,
    name: undefined,
    sex: undefined,
    birthday: undefined,
    description: undefined,
    avatar: undefined,
  };
  formRef.value?.resetFields();
}

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
    <Form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <Form.Item label="名字" name="name">
        <Input v-model:value="formData.name" placeholder="请输入名字" />
      </Form.Item>
      <Form.Item label="性别" name="sex">
        <RadioGroup v-model:value="formData.sex">
          <Radio
            v-for="dict in getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number')"
            :key="dict.value.toString()"
            :value="dict.value"
          >
            {{ dict.label }}
          </Radio>
        </RadioGroup>
      </Form.Item>
      <Form.Item label="出生年" name="birthday">
        <DatePicker
          v-model:value="formData.birthday"
          value-format="x"
          placeholder="选择出生年"
        />
      </Form.Item>
      <Form.Item label="简介" name="description">
        <RichTextarea v-model="formData.description" height="500px" />
      </Form.Item>
      <Form.Item label="头像" name="avatar">
        <ImageUpload v-model:value="formData.avatar" />
      </Form.Item>
    </Form>
  </Modal>
</template>
