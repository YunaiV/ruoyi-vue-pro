<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, message, Step, Steps, Switch } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const currentTab = ref(0);
function onFirstSubmit(values: Record<string, any>) {
  message.success({
    content: `form1 values: ${JSON.stringify(values)}`,
  });
  currentTab.value = 1;
}
function onSecondReset() {
  currentTab.value = 0;
}
function onSecondSubmit(values: Record<string, any>) {
  message.success({
    content: `form2 values: ${JSON.stringify(values)}`,
  });
}

const [FirstForm, firstFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  handleSubmit: onFirstSubmit,
  layout: 'horizontal',
  resetButtonOptions: {
    show: false,
  },
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'formFirst',
      label: '表单1字段',
      rules: 'required',
    },
  ],
  submitButtonOptions: {
    content: '下一步',
  },
  wrapperClass: 'grid-cols-1 md:grid-cols-1 lg:grid-cols-1',
});
const [SecondForm, secondFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  handleReset: onSecondReset,
  handleSubmit: onSecondSubmit,
  layout: 'horizontal',
  resetButtonOptions: {
    content: '上一步',
  },
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '请输入',
      },
      fieldName: 'formSecond',
      label: '表单2字段',
      rules: 'required',
    },
  ],
  wrapperClass: 'grid-cols-1 md:grid-cols-1 lg:grid-cols-1',
});
const needMerge = ref(true);
async function handleMergeSubmit() {
  const values = await firstFormApi
    .merge(secondFormApi)
    .submitAllForm(needMerge.value);
  message.success({
    content: `merged form values: ${JSON.stringify(values)}`,
  });
}
</script>

<template>
  <Page
    description="表单组件合并示例：在某些场景下，例如分步表单，需要合并多个表单并统一提交。默认情况下，使用 Object.assign 规则合并表单。如果需要特殊处理数据，可以传入 false。"
    title="表单组件"
  >
    <Card title="基础示例">
      <template #extra>
        <Switch
          v-model:checked="needMerge"
          checked-children="开启字段合并"
          class="mr-4"
          un-checked-children="关闭字段合并"
        />
        <Button type="primary" @click="handleMergeSubmit">合并提交</Button>
      </template>
      <div class="mx-auto max-w-lg">
        <Steps :current="currentTab" class="steps">
          <Step title="表单1" />
          <Step title="表单2" />
        </Steps>
        <div class="p-20">
          <FirstForm v-show="currentTab === 0" />
          <SecondForm v-show="currentTab === 1" />
        </div>
      </div>
    </Card>
  </Page>
</template>
