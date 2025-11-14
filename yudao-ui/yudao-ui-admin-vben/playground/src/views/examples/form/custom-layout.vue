<script lang="ts" setup>
import { h } from 'vue';

import { Page } from '@vben/common-ui';

import { Card } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

import DocButton from '../doc-button.vue';

const [CustomLayoutForm] = useVbenForm({
  // 所有表单项共用，可单独在表单内覆盖
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  schema: [
    {
      component: 'Select',
      fieldName: 'field1',
      label: '字符串',
    },
    {
      component: 'TreeSelect',
      fieldName: 'field2',
      label: '字符串',
    },
    {
      component: 'Mentions',
      fieldName: 'field3',
      label: '字符串',
    },
    {
      component: 'Input',
      fieldName: 'field4',
      label: '字符串',
    },
    {
      component: 'InputNumber',
      fieldName: 'field5',
      // 从第三列开始 相当于中间空了一列
      formItemClass: 'col-start-3',
      label: '前面空了一列',
    },
    {
      component: 'Divider',
      fieldName: '_divider',
      formItemClass: 'col-span-3',
      hideLabel: true,
      renderComponentContent: () => {
        return {
          default: () => h('div', '分割线'),
        };
      },
    },
    {
      component: 'Textarea',
      fieldName: 'field6',
      // 占满三列空间 基线对齐
      formItemClass: 'col-span-3 items-baseline',
      label: '占满三列',
    },
    {
      component: 'Input',
      fieldName: 'field7',
      // 占满2列空间 从第二列开始 相当于前面空了一列
      formItemClass: 'col-span-2 col-start-2',
      label: '占满2列',
    },
    {
      component: 'Input',
      fieldName: 'field8',
      // 左右留空
      formItemClass: 'col-start-2',
      label: '左右留空',
    },
    {
      component: 'InputPassword',
      fieldName: 'field9',
      formItemClass: 'col-start-1',
      label: '字符串',
    },
  ],
  // 一共三列
  wrapperClass: 'grid-cols-3',
});
</script>

<template>
  <Page
    content-class="flex flex-col gap-4"
    description="使用tailwind自定义表单项的布局"
    title="表单自定义布局"
  >
    <template #description>
      <div class="text-muted-foreground">
        <p>使用tailwind自定义表单项的布局，使用Divider分割表单。</p>
      </div>
    </template>
    <template #extra>
      <DocButton class="mb-2" path="/components/common-ui/vben-form" />
    </template>
    <Card title="使用tailwind自定义布局">
      <CustomLayoutForm />
    </Card>
  </Page>
</template>
