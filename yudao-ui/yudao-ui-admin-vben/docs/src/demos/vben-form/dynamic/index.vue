<script lang="ts" setup>
import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const [Form] = useVbenForm({
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
  ],
  // 大屏一行显示3个，中屏一行显示2个，小屏一行显示1个
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
});

function onSubmit(values: Record<string, any>) {
  message.success({
    content: `form values: ${JSON.stringify(values)}`,
  });
}
</script>

<template>
  <Form />
</template>
