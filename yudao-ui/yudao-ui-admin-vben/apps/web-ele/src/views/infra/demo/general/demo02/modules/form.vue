<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import type { Demo02CategoryApi } from '#/api/infra/demo/demo02';

import { computed, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { handleTree } from '@vben/utils';

import {
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElTreeSelect,
} from 'element-plus';

import {
  createDemo02Category,
  getDemo02Category,
  getDemo02CategoryList,
  updateDemo02Category,
} from '#/api/infra/demo/demo02';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const formRef = ref();
const formData = ref<Partial<Demo02CategoryApi.Demo02Category>>({
  id: undefined,
  name: undefined,
  parentId: undefined,
});
const rules = reactive<FormRules>({
  name: [{ required: true, message: '名字不能为空', trigger: 'blur' }],
  parentId: [{ required: true, message: '父级编号不能为空', trigger: 'blur' }],
});
const demo02CategoryTree = ref<any[]>([]); // 树形结构
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['示例分类'])
    : $t('ui.actionTitle.create', ['示例分类']);
});

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    parentId: undefined,
  };
  formRef.value?.resetFields();
};

/** 获得示例分类树 */
const getDemo02CategoryTree = async () => {
  demo02CategoryTree.value = [];
  const data = await getDemo02CategoryList({});
  data.unshift({
    id: 0,
    name: '顶级示例分类',
  });
  demo02CategoryTree.value = handleTree(data);
};

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formRef.value?.validate();
    modalApi.lock();
    // 提交表单
    const data = formData.value as Demo02CategoryApi.Demo02Category;
    try {
      await (formData.value?.id
        ? updateDemo02Category(data)
        : createDemo02Category(data));
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
    let data = modalApi.getData<Demo02CategoryApi.Demo02Category>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getDemo02Category(data.id);
      } finally {
        modalApi.unlock();
      }
    }
    formData.value = data;
    // 加载树数据
    await getDemo02CategoryTree();
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
      <ElFormItem label="父级编号" prop="parentId">
        <ElTreeSelect
          v-model="formData.parentId"
          :data="demo02CategoryTree"
          :props="{
            label: 'name',
            value: 'id',
            children: 'children',
          }"
          check-strictly
          default-expand-all
          placeholder="请选择父级编号"
        />
      </ElFormItem>
    </ElForm>
  </Modal>
</template>
