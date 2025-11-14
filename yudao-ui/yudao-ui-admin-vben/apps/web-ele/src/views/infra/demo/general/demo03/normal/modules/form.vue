<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

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
  ElTabPane,
  ElTabs,
} from 'element-plus';

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

/** 子表的表单 */
const subTabsName = ref('demo03Course');
const demo03CourseFormRef = ref<InstanceType<typeof Demo03CourseForm>>();
const demo03GradeFormRef = ref<InstanceType<typeof Demo03GradeForm>>();

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
    <!-- 子表的表单 -->
    <ElTabs v-model="subTabsName">
      <ElTabPane name="demo03Course" label="学生课程">
        <Demo03CourseForm
          ref="demo03CourseFormRef"
          :student-id="formData?.id"
        />
      </ElTabPane>
      <ElTabPane name="demo03Grade" label="学生班级">
        <Demo03GradeForm ref="demo03GradeFormRef" :student-id="formData?.id" />
      </ElTabPane>
    </ElTabs>
  </Modal>
</template>
