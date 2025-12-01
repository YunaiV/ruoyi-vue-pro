<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import { ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { Form, Input, Select } from 'ant-design-vue';

const modelData = defineModel<any>(); // 创建本地数据副本
const formRef = ref(); // 表单引用
const rules: Record<string, Rule[]> = {
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
  <Form
    ref="formRef"
    :model="modelData"
    :rules="rules"
    :label-col="{ span: 4 }"
    :wrapper-col="{ span: 20 }"
    class="mt-5"
  >
    <Form.Item label="流程标识" name="code" class="mb-5">
      <Input
        class="w-full"
        v-model:value="modelData.code"
        allow-clear
        placeholder="请输入流程标识"
      />
    </Form.Item>
    <Form.Item label="流程名称" name="name" class="mb-5">
      <Input
        v-model:value="modelData.name"
        allow-clear
        placeholder="请输入流程名称"
      />
    </Form.Item>
    <Form.Item label="状态" name="status" class="mb-5">
      <Select
        class="w-full"
        v-model:value="modelData.status"
        allow-clear
        placeholder="请选择状态"
      >
        <Select.Option
          v-for="dict in getDictOptions(DICT_TYPE.COMMON_STATUS, 'number')"
          :key="dict.value"
          :value="dict.value"
        >
          {{ dict.label }}
        </Select.Option>
      </Select>
    </Form.Item>
    <Form.Item label="流程描述" name="description" class="mb-5">
      <Input.TextArea v-model:value="modelData.description" allow-clear />
    </Form.Item>
  </Form>
</template>
