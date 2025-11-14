<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message, Tabs } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDemo03Student,
  getDemo03Student,
  updateDemo03Student,
} from '#/api/infra/demo/demo03/normal';
import { $t } from '#/locales';

import { useFormSchema } from '../data';
import Demo03CourseForm from './demo03-course-form.vue';
import Demo03GradeForm from './demo03-grade-form.vue';

const emit = defineEmits(['success']);
const formData = ref<Demo03StudentApi.Demo03Student>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['学生'])
    : $t('ui.actionTitle.create', ['学生']);
});

/** 子表的表单 */
const subTabsName = ref('demo03Course');
const demo03CourseFormRef = ref<InstanceType<typeof Demo03CourseForm>>();
const demo03GradeFormRef = ref<InstanceType<typeof Demo03GradeForm>>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    // 校验子表单
    const demo03GradeValid = await demo03GradeFormRef.value?.validate();
    if (!demo03GradeValid) {
      subTabsName.value = 'demo03Grade';
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as Demo03StudentApi.Demo03Student;
    // 拼接子表的数据
    data.demo03courses = demo03CourseFormRef.value?.getData();
    data.demo03grade = await demo03GradeFormRef.value?.getValues();
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
      formData.value = undefined;
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
    // 设置到 values
    formData.value = data;
    await formApi.setValues(formData.value);
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
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
