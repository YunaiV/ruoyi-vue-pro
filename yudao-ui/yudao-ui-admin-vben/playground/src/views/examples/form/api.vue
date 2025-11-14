<script lang="ts" setup>
import type { RefSelectProps } from 'ant-design-vue/es/select';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, message, Space } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const isReverseActionButtons = ref(false);

const [BaseForm, formApi] = useVbenForm({
  // 翻转操作按钮的位置
  actionButtonsReverse: isReverseActionButtons.value,
  // 所有表单项共用，可单独在表单内覆盖
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  // 使用 tailwindcss grid布局
  // 提交函数
  handleSubmit: onSubmit,
  // 垂直布局，label和input在不同行，值为vertical
  layout: 'horizontal',
  // 水平布局，label和input在同一行
  schema: [
    {
      // 组件需要在 #/adapter.ts内注册，并加上类型
      component: 'Input',
      // 对应组件的参数
      componentProps: {
        placeholder: '请输入用户名',
      },
      // 字段名
      fieldName: 'field1',
      // 界面显示的label
      label: 'field1',
    },
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'field2',
      label: 'field2',
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
      fieldName: 'fieldOptions',
      label: '下拉选',
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

function handleClick(
  action:
    | 'batchAddSchema'
    | 'batchDeleteSchema'
    | 'componentRef'
    | 'disabled'
    | 'hiddenAction'
    | 'hiddenResetButton'
    | 'hiddenSubmitButton'
    | 'labelWidth'
    | 'resetDisabled'
    | 'resetLabelWidth'
    | 'reverseActionButtons'
    | 'showAction'
    | 'showResetButton'
    | 'showSubmitButton'
    | 'updateActionAlign'
    | 'updateResetButton'
    | 'updateSchema'
    | 'updateSubmitButton',
) {
  switch (action) {
    case 'batchAddSchema': {
      formApi.setState((prev) => {
        const currentSchema = prev?.schema ?? [];
        const newSchema = [];
        for (let i = 0; i < 3; i++) {
          newSchema.push({
            component: 'Input',
            componentProps: {
              placeholder: '请输入',
            },
            fieldName: `field${i}${Date.now()}`,
            label: `field+`,
          });
        }
        return {
          schema: [...currentSchema, ...newSchema],
        };
      });
      break;
    }

    case 'batchDeleteSchema': {
      formApi.setState((prev) => {
        const currentSchema = prev?.schema ?? [];
        return {
          schema: currentSchema.slice(0, -3),
        };
      });
      break;
    }
    case 'componentRef': {
      // 获取下拉组件的实例，并调用它的focus方法
      formApi.getFieldComponentRef<RefSelectProps>('fieldOptions')?.focus?.();
      break;
    }
    case 'disabled': {
      formApi.setState({ commonConfig: { disabled: true } });
      break;
    }
    case 'hiddenAction': {
      formApi.setState({ showDefaultActions: false });
      break;
    }
    case 'hiddenResetButton': {
      formApi.setState({ resetButtonOptions: { show: false } });
      break;
    }
    case 'hiddenSubmitButton': {
      formApi.setState({ submitButtonOptions: { show: false } });
      break;
    }
    case 'labelWidth': {
      formApi.setState({
        commonConfig: {
          labelWidth: 150,
        },
      });
      break;
    }
    case 'resetDisabled': {
      formApi.setState({ commonConfig: { disabled: false } });
      break;
    }
    case 'resetLabelWidth': {
      formApi.setState({
        commonConfig: {
          labelWidth: 100,
        },
      });
      break;
    }
    case 'reverseActionButtons': {
      isReverseActionButtons.value = !isReverseActionButtons.value;
      formApi.setState({ actionButtonsReverse: isReverseActionButtons.value });
      break;
    }
    case 'showAction': {
      formApi.setState({ showDefaultActions: true });
      break;
    }
    case 'showResetButton': {
      formApi.setState({ resetButtonOptions: { show: true } });
      break;
    }
    case 'showSubmitButton': {
      formApi.setState({ submitButtonOptions: { show: true } });
      break;
    }

    case 'updateActionAlign': {
      formApi.setState({
        // 可以自行调整class
        actionWrapperClass: 'text-center',
      });
      break;
    }
    case 'updateResetButton': {
      formApi.setState({
        resetButtonOptions: { disabled: true },
      });
      break;
    }
    case 'updateSchema': {
      formApi.updateSchema([
        {
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
              {
                label: '选项3',
                value: '3',
              },
            ],
          },
          fieldName: 'fieldOptions',
        },
      ]);
      message.success('字段 `fieldOptions` 下拉选项更新成功。');
      break;
    }
    case 'updateSubmitButton': {
      formApi.setState({
        submitButtonOptions: { loading: true },
      });
      break;
    }
  }
}
</script>

<template>
  <Page description="表单组件api操作示例。" title="表单组件">
    <Space class="mb-5 flex-wrap">
      <Button @click="handleClick('updateSchema')">updateSchema</Button>
      <Button @click="handleClick('labelWidth')">更改labelWidth</Button>
      <Button @click="handleClick('resetLabelWidth')">还原labelWidth</Button>
      <Button @click="handleClick('disabled')">禁用表单</Button>
      <Button @click="handleClick('resetDisabled')">解除禁用</Button>
      <Button @click="handleClick('reverseActionButtons')">
        翻转操作按钮位置
      </Button>
      <Button @click="handleClick('hiddenAction')">隐藏操作按钮</Button>
      <Button @click="handleClick('showAction')">显示操作按钮</Button>
      <Button @click="handleClick('hiddenResetButton')">隐藏重置按钮</Button>
      <Button @click="handleClick('showResetButton')">显示重置按钮</Button>
      <Button @click="handleClick('hiddenSubmitButton')">隐藏提交按钮</Button>
      <Button @click="handleClick('showSubmitButton')">显示提交按钮</Button>
      <Button @click="handleClick('updateResetButton')">修改重置按钮</Button>
      <Button @click="handleClick('updateSubmitButton')">修改提交按钮</Button>
      <Button @click="handleClick('updateActionAlign')">
        调整操作按钮位置
      </Button>
      <Button @click="handleClick('batchAddSchema')"> 批量添加表单项 </Button>
      <Button @click="handleClick('batchDeleteSchema')">
        批量删除表单项
      </Button>
      <Button @click="handleClick('componentRef')">下拉组件获取焦点</Button>
    </Space>
    <Card title="操作示例">
      <BaseForm />
    </Card>
  </Page>
</template>
