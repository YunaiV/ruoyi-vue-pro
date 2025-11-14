<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Button, Card, message } from 'ant-design-vue';

import { useVbenForm, z } from '#/adapter/form';

const [Form, formApi] = useVbenForm({
  // 所有表单项共用，可单独在表单内覆盖
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  // 提交函数
  handleSubmit: onSubmit,
  // 垂直布局，label和input在不同行，值为vertical
  // 水平布局，label和input在同一行
  layout: 'horizontal',
  schema: [
    {
      // 组件需要在 #/adapter.ts内注册，并加上类型
      component: 'Input',
      // 对应组件的参数
      componentProps: {
        placeholder: '请输入',
      },
      // 字段名
      fieldName: 'field1',
      // 界面显示的label
      label: '字段1',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      defaultValue: '默认值',
      fieldName: 'field2',
      label: '默认值(必填)',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'field3',
      label: '默认值(非必填)',
      rules: z.string().default('默认值').optional(),
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'field31',
      label: '自定义信息',
      rules: z.string().min(1, { message: '最少输入1个字符' }),
    },
    {
      component: 'Input',
      // 对应组件的参数
      componentProps: {
        placeholder: '请输入',
      },
      // 字段名
      fieldName: 'field4',
      // 界面显示的label
      label: '邮箱',
      rules: z.string().email('请输入正确的邮箱'),
    },
    {
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'number',
      label: '数字',
      rules: 'required',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        filterOption: true,
        options: [
          {
            label: '选项1',
            value: '1',
          },
          {
            label: '选项2',
            value: '2',
          },
        ],
        placeholder: '请选择',
        showSearch: true,
      },
      defaultValue: undefined,
      fieldName: 'options',
      label: '下拉选',
      rules: 'selectRequired',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          {
            label: '选项1',
            value: '1',
          },
          {
            label: '选项2',
            value: '2',
          },
        ],
      },
      fieldName: 'radioGroup',
      label: '单选组',
      rules: 'selectRequired',
    },
    {
      component: 'CheckboxGroup',
      componentProps: {
        name: 'cname',
        options: [
          {
            label: '选项1',
            value: '1',
          },
          {
            label: '选项2',
            value: '2',
          },
        ],
      },
      fieldName: 'checkboxGroup',
      label: '多选组',
      rules: 'selectRequired',
    },
    {
      component: 'Checkbox',
      fieldName: 'checkbox',
      label: '',
      renderComponentContent: () => {
        return {
          default: () => ['我已阅读并同意'],
        };
      },
      rules: z.boolean().refine((value) => value, {
        message: '请勾选',
      }),
    },
    {
      component: 'DatePicker',
      defaultValue: undefined,
      fieldName: 'datePicker',
      label: '日期选择框',
      rules: 'selectRequired',
    },
    {
      component: 'RangePicker',
      defaultValue: undefined,
      fieldName: 'rangePicker',
      label: '区间选择框',
      rules: 'selectRequired',
    },
    {
      component: 'InputPassword',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'password',
      label: '密码',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'input-blur',
      formFieldProps: {
        validateOnChange: false,
        validateOnModelUpdate: false,
      },
      help: 'blur时才会触发校验',
      label: 'blur触发',
      rules: 'required',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'input-async',
      label: '异步校验',
      rules: z
        .string()
        .min(3, '用户名至少需要3个字符')
        .refine(
          async (username) => {
            // 假设这是一个异步函数，模拟检查用户名是否已存在
            const checkUsernameExists = async (
              username: string,
            ): Promise<boolean> => {
              await new Promise((resolve) => setTimeout(resolve, 1000));
              return username === 'existingUser';
            };
            const exists = await checkUsernameExists(username);
            return !exists;
          },
          {
            message: '用户名已存在',
          },
        ),
    },
  ],
  // 大屏一行显示3个，中屏一行显示2个，小屏一行显示1个
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
});

function onSubmit(values: Record<string, any>) {
  message.success({
    content: `form values: ${JSON.stringify(values)}`,
  });
}
</script>

<template>
  <Page description="表单校验示例" title="表单组件">
    <Card title="基础组件校验示例">
      <template #extra>
        <Button @click="() => formApi.validate()">校验表单</Button>
        <Button class="mx-2" @click="() => formApi.resetValidate()">
          清空校验信息
        </Button>
      </template>
      <Form />
    </Card>
  </Page>
</template>
