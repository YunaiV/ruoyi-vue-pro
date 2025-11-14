<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

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
  Tabs,
} from 'ant-design-vue';

import {
  createDemo03Student,
  getDemo03Student,
  updateDemo03Student,
} from '#/api/infra/demo/demo03/normal';
import { Tinymce as RichTextarea } from '#/components/tinymce';
import { $t } from '#/locales';

import Demo03CourseForm from './demo03-course-form.vue';
import Demo03GradeForm from './demo03-grade-form.vue';

const emit = defineEmits(['success']);

const formRef = ref();
const formData = ref<Partial<Demo03StudentApi.Demo03Student>>({
  id: undefined,
  name: undefined,
  sex: undefined,
  birthday: undefined,
  description: undefined,
});
const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  sex: [{ required: true, message: '性别不能为空', trigger: 'blur' }],
  birthday: [{ required: true, message: '出生日期不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '简介不能为空', trigger: 'blur' }],
};
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['学生'])
    : $t('ui.actionTitle.create', ['学生']);
});

/** 子表的表单 */
const subTabsName = ref('demo03Course');
const demo03CourseFormRef = ref<InstanceType<typeof Demo03CourseForm>>();
const demo03GradeFormRef = ref<InstanceType<typeof Demo03GradeForm>>();

/** 重置表单 */
function resetForm() {
  formData.value = {
    id: undefined,
    name: undefined,
    sex: undefined,
    birthday: undefined,
    description: undefined,
  };
  formRef.value?.resetFields();
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formRef.value?.validate();
    // 校验子表单
    try {
      await demo03GradeFormRef.value?.validate();
    } catch {
      subTabsName.value = 'demo03Grade';
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = formData.value as Demo03StudentApi.Demo03Student;
    // 拼接子表的数据
    data.demo03courses = demo03CourseFormRef.value?.getData();
    data.demo03grade =
      demo03GradeFormRef.value?.getValues() as Demo03StudentApi.Demo03Grade;
    try {
      await (formData.value?.id
        ? updateDemo03Student(data)
        : createDemo03Student(data));
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
      <Form.Item label="出生日期" name="birthday">
        <DatePicker
          v-model:value="formData.birthday"
          value-format="x"
          placeholder="选择出生日期"
        />
      </Form.Item>
      <Form.Item label="简介" name="description">
        <RichTextarea v-model="formData.description" height="500px" />
      </Form.Item>
    </Form>
    <!-- 子表的表单 -->
    <Tabs v-model:active-key="subTabsName">
      <Tabs.TabPane key="demo03Course" tab="学生课程" force-render>
        <Demo03CourseForm
          ref="demo03CourseFormRef"
          :student-id="formData?.id"
        />
      </Tabs.TabPane>
      <Tabs.TabPane key="demo03Grade" tab="学生班级" force-render>
        <Demo03GradeForm ref="demo03GradeFormRef" :student-id="formData?.id" />
      </Tabs.TabPane>
    </Tabs>
  </Modal>
</template>
