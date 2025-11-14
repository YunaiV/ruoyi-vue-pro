<script lang="ts" setup>
import { h, markRaw } from 'vue';

import { Page } from '@vben/common-ui';

import { Card, Input, message } from 'ant-design-vue';

import { useVbenForm, z } from '#/adapter/form';

import TwoFields from './modules/two-fields.vue';

const [Form] = useVbenForm({
  // 所有表单项共用，可单独在表单内覆盖
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
    labelClass: 'w-2/6',
  },
  fieldMappingTime: [['field4', ['phoneType', 'phoneNumber'], null]],
  // 提交函数
  handleSubmit: onSubmit,
  // 垂直布局，label和input在不同行，值为vertical
  // 水平布局，label和input在同一行
  layout: 'horizontal',
  schema: [
    {
      // 组件需要在 #/adapter.ts内注册，并加上类型
      component: 'Input',
      fieldName: 'field',
      label: '自定义后缀',
      suffix: () => h('span', { class: 'text-red-600' }, '元'),
    },
    {
      component: 'Input',
      fieldName: 'field1',
      label: '自定义组件slot',
      renderComponentContent: () => ({
        prefix: () => 'prefix',
        suffix: () => 'suffix',
      }),
    },
    {
      component: h(Input, { placeholder: '请输入Field2' }),
      fieldName: 'field2',
      label: '自定义组件',
      modelPropName: 'value',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'field3',
      label: '自定义组件(slot)',
      rules: 'required',
    },
    {
      component: markRaw(TwoFields),
      defaultValue: [undefined, ''],
      disabledOnChangeListener: false,
      fieldName: 'field4',
      formItemClass: 'col-span-1',
      label: '组合字段',
      rules: z
        .array(z.string().optional())
        .length(2, '请选择类型并输入手机号码')
        .refine((v) => !!v[0], {
          message: '请选择类型',
        })
        .refine((v) => !!v[1] && v[1] !== '', {
          message: '　　　　　　　输入手机号码',
        })
        .refine((v) => v[1]?.match(/^1[3-9]\d{9}$/), {
          // 使用全角空格占位，将错误提示文字挤到手机号码输入框的下面
          message: '　　　　　　　号码格式不正确',
        }),
    },
  ],
  // 中屏一行显示2个，小屏一行显示1个
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
});

function onSubmit(values: Record<string, any>) {
  message.success({
    content: `form values: ${JSON.stringify(values)}`,
  });
}
</script>

<template>
  <Page description="表单组件自定义示例" title="表单组件">
    <Card title="基础示例">
      <Form>
        <template #field3="slotProps">
          <Input placeholder="请输入" v-bind="slotProps" />
        </template>
      </Form>
    </Card>
  </Page>
</template>
