<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, Switch } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

defineOptions({
  name: 'ScrollToErrorTest',
});

const scrollEnabled = ref(true);

const [Form, formApi] = useVbenForm({
  scrollToFirstError: scrollEnabled.value,
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名',
      },
      fieldName: 'username',
      label: '用户名',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入邮箱',
      },
      fieldName: 'email',
      label: '邮箱',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
      },
      fieldName: 'phone',
      label: '手机号',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入地址',
      },
      fieldName: 'address',
      label: '地址',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
      fieldName: 'remark',
      label: '备注',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入公司名称',
      },
      fieldName: 'company',
      label: '公司名称',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入职位',
      },
      fieldName: 'position',
      label: '职位',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        options: [
          { label: '男', value: 'male' },
          { label: '女', value: 'female' },
        ],
        placeholder: '请选择性别',
      },
      fieldName: 'gender',
      label: '性别',
      rules: 'selectRequired',
    },
  ],
  showDefaultActions: false,
});

// 测试 validateAndSubmitForm（验证并提交）
async function testValidateAndSubmit() {
  await formApi.validateAndSubmitForm();
}

// 测试 validate（手动验证整个表单）
async function testValidate() {
  await formApi.validate();
}

// 测试 validateField（验证单个字段）
async function testValidateField() {
  await formApi.validateField('username');
}

// 切换滚动功能
function toggleScrollToError() {
  formApi.setState({ scrollToFirstError: scrollEnabled.value });
}

// 填充部分数据测试
async function fillPartialData() {
  await formApi.resetForm();
  await formApi.setFieldValue('username', '测试用户');
  await formApi.setFieldValue('email', 'test@example.com');
}
</script>

<template>
  <Page
    description="测试表单验证失败时自动滚动到错误字段的功能"
    title="滚动到错误字段测试"
  >
    <Card title="功能测试">
      <template #extra>
        <div class="flex items-center gap-2">
          <Switch
            v-model:checked="scrollEnabled"
            @change="toggleScrollToError"
          />
          <span>启用滚动到错误字段</span>
        </div>
      </template>

      <div class="space-y-4">
        <div class="rounded bg-blue-50 p-4">
          <h3 class="mb-2 font-medium">测试说明：</h3>
          <ul class="list-inside list-disc space-y-1 text-sm">
            <li>所有验证方法在验证失败时都会自动滚动到第一个错误字段</li>
            <li>可以通过右上角的开关控制是否启用自动滚动功能</li>
          </ul>
        </div>

        <div class="rounded border p-4">
          <h4 class="mb-3 font-medium">验证方法测试：</h4>
          <div class="flex flex-wrap gap-2">
            <Button type="primary" @click="testValidateAndSubmit">
              测试 validateAndSubmitForm()
            </Button>
            <Button @click="testValidate"> 测试 validate() </Button>
            <Button @click="testValidateField"> 测试 validateField() </Button>
          </div>
          <div class="mt-2 text-xs text-gray-500">
            <p>• validateAndSubmitForm(): 验证表单并提交</p>
            <p>• validate(): 手动验证整个表单</p>
            <p>• validateField(): 验证单个字段（这里测试用户名字段）</p>
          </div>
        </div>

        <div class="rounded border p-4">
          <h4 class="mb-3 font-medium">数据填充测试：</h4>
          <div class="flex flex-wrap gap-2">
            <Button @click="fillPartialData"> 填充部分数据 </Button>
            <Button @click="() => formApi.resetForm()"> 清空表单 </Button>
          </div>
          <div class="mt-2 text-xs text-gray-500">
            <p>• 填充部分数据后验证，会滚动到第一个错误字段</p>
          </div>
        </div>

        <Form />
      </div>
    </Card>
  </Page>
</template>
