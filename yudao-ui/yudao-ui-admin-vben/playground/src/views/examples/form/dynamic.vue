<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Button, Card, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const [Form, formApi] = useVbenForm({
  // 提交函数
  handleSubmit: onSubmit,
  schema: [
    {
      component: 'Input',
      defaultValue: 'hidden value',
      dependencies: {
        show: false,
        // 随意一个字段改变时，都会触发
        triggerFields: ['field1Switch'],
      },
      fieldName: 'hiddenField',
      label: '隐藏字段',
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'field1Switch',
      help: '通过Dom控制销毁',
      label: '显示字段1',
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'field2Switch',
      help: '通过css控制隐藏',
      label: '显示字段2',
    },
    {
      component: 'Switch',
      fieldName: 'field3Switch',
      label: '禁用字段3',
    },
    {
      component: 'Switch',
      fieldName: 'field4Switch',
      label: '字段4必填',
    },
    {
      component: 'Input',
      dependencies: {
        if(values) {
          return !!values.field1Switch;
        },
        // 只有指定的字段改变时，才会触发
        triggerFields: ['field1Switch'],
      },
      // 字段名
      fieldName: 'field1',
      // 界面显示的label
      label: '字段1',
    },
    {
      component: 'Input',
      dependencies: {
        show(values) {
          return !!values.field2Switch;
        },
        triggerFields: ['field2Switch'],
      },
      fieldName: 'field2',
      label: '字段2',
    },
    {
      component: 'Input',
      dependencies: {
        disabled(values) {
          return !!values.field3Switch;
        },
        triggerFields: ['field3Switch'],
      },
      fieldName: 'field3',
      label: '字段3',
    },
    {
      component: 'Input',
      dependencies: {
        required(values) {
          return !!values.field4Switch;
        },
        triggerFields: ['field4Switch'],
      },
      fieldName: 'field4',
      label: '字段4',
    },
    {
      component: 'Input',
      dependencies: {
        rules(values) {
          if (values.field1 === '123') {
            return 'required';
          }
          return null;
        },
        triggerFields: ['field1'],
      },
      fieldName: 'field5',
      help: '当字段1的值为`123`时，必填',
      label: '动态rules',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        class: 'w-full',
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
      dependencies: {
        componentProps(values) {
          if (values.field2 === '123') {
            return {
              options: [
                {
                  label: '选项1',
                  value: '1',
                },
                {
                  label: '选项2',
                  value: '2',
                },
                {
                  label: '选项3',
                  value: '3',
                },
              ],
            };
          }
          return {};
        },
        triggerFields: ['field2'],
      },
      fieldName: 'field6',
      help: '当字段2的值为`123`时，更改下拉选项',
      label: '动态配置',
    },
    {
      component: 'Input',
      fieldName: 'field7',
      label: '字段7',
    },
  ],
  // 大屏一行显示3个，中屏一行显示2个，小屏一行显示1个
  wrapperClass: 'grid-cols-1 md:grid-cols-3 lg:grid-cols-4',
});

const [SyncForm] = useVbenForm({
  handleSubmit: onSubmit,
  schema: [
    {
      component: 'Input',
      // 字段名
      fieldName: 'field1',
      // 界面显示的label
      label: '字段1',
    },
    {
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      dependencies: {
        trigger(values, form) {
          form.setFieldValue('field2', values.field1);
        },
        // 只有指定的字段改变时，才会触发
        triggerFields: ['field1'],
      },
      // 字段名
      fieldName: 'field2',
      // 界面显示的label
      label: '字段2',
    },
  ],
  // 大屏一行显示3个，中屏一行显示2个，小屏一行显示1个
  wrapperClass: 'grid-cols-1 md:grid-cols-3 lg:grid-cols-4',
});

function onSubmit(values: Record<string, any>) {
  message.success({
    content: `form values: ${JSON.stringify(values)}`,
  });
}

function handleDelete() {
  formApi.setState((prev) => {
    return {
      schema: prev.schema?.filter((item) => item.fieldName !== 'field7'),
    };
  });
}

function handleAdd() {
  formApi.setState((prev) => {
    return {
      schema: [
        ...(prev?.schema ?? []),
        {
          component: 'Input',
          fieldName: `field${Date.now()}`,
          label: '字段+',
        },
      ],
    };
  });
}

function handleUpdate() {
  formApi.setState((prev) => {
    return {
      schema: prev.schema?.map((item) => {
        if (item.fieldName === 'field3') {
          return {
            ...item,
            label: '字段3-修改',
          };
        }
        return item;
      }),
    };
  });
}
</script>

<template>
  <Page
    description="表单组件动态联动示例，包含了常用的场景。增删改，本质上是修改schema，你也可以通过 `setState` 动态修改schema。"
    title="表单组件"
  >
    <Card title="表单动态联动示例">
      <template #extra>
        <Button class="mr-2" @click="handleUpdate">修改字段3</Button>
        <Button class="mr-2" @click="handleDelete">删除字段7</Button>
        <Button @click="handleAdd">添加字段</Button>
      </template>
      <Form />
    </Card>

    <Card class="mt-5" title="字段同步，字段1数据与字段2数据同步">
      <SyncForm />
    </Card>
  </Page>
</template>
