<script lang="ts" setup>
import type { FormRules } from 'element-plus';

import { ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElForm, ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus';

const modelData = defineModel<any>(); // 创建本地数据副本
const formRef = ref(); // 表单引用
const rules: FormRules = {
  code: [{ required: true, message: '流程标识不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
};

/** 表单校验 */
async function validate() {
  await formRef.value?.validate();
}

defineExpose({ validate });
</script>

<template>
  <ElForm
    ref="formRef"
    :model="modelData"
    :rules="rules"
    label-width="120px"
    label-position="right"
    class="mt-5"
  >
    <ElFormItem label="流程标识" prop="code" class="mb-5">
      <ElInput
        class="w-full"
        v-model="modelData.code"
        clearable
        placeholder="请输入流程标识"
      />
    </ElFormItem>
    <ElFormItem label="流程名称" prop="name" class="mb-5">
      <ElInput
        v-model="modelData.name"
        clearable
        placeholder="请输入流程名称"
      />
    </ElFormItem>
    <ElFormItem label="状态" prop="status" class="mb-5">
      <ElSelect
        class="w-full"
        v-model="modelData.status"
        clearable
        placeholder="请选择状态"
      >
        <ElOption
          v-for="dict in getDictOptions(DICT_TYPE.COMMON_STATUS, 'number')"
          :key="dict.value"
          :value="dict.value"
          :label="dict.label"
        />
      </ElSelect>
    </ElFormItem>
    <ElFormItem label="流程描述" prop="description" class="mb-5">
      <ElInput v-model="modelData.description" type="textarea" clearable />
    </ElFormItem>
  </ElForm>
</template>
