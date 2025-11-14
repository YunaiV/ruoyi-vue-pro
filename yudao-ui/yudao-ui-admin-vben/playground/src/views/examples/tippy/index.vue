<script lang="ts" setup>
import type { TippyProps } from '@vben/common-ui';

import { reactive } from 'vue';

import { Page, Tippy } from '@vben/common-ui';

import { Button, Card, Flex } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const tippyProps = reactive<TippyProps>({
  animation: 'shift-away',
  arrow: true,
  content: '这是一个提示',
  delay: [200, 200],
  duration: 200,
  followCursor: false,
  hideOnClick: false,
  inertia: true,
  maxWidth: 'none',
  placement: 'top',
  theme: 'dark',
  trigger: 'mouseenter focusin',
});

function parseBoolean(value: string) {
  switch (value) {
    case 'false': {
      return false;
    }
    case 'true': {
      return true;
    }
    default: {
      return value;
    }
  }
}

const [Form] = useVbenForm({
  handleValuesChange(values) {
    Object.assign(tippyProps, {
      ...values,
      delay: [values.delay1, values.delay2],
      followCursor: parseBoolean(values.followCursor),
      hideOnClick: parseBoolean(values.hideOnClick),
      trigger: values.trigger.join(' '),
    });
  },
  schema: [
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        class: 'w-full',
        options: [
          { label: '自动', value: 'auto' },
          { label: '暗色', value: 'dark' },
          { label: '亮色', value: 'light' },
        ],
        optionType: 'button',
      },
      defaultValue: tippyProps.theme,
      fieldName: 'theme',
      label: '主题',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        options: [
          { label: '向上滑入', value: 'shift-away' },
          { label: '向下滑入', value: 'shift-toward' },
          { label: '缩放', value: 'scale' },
          { label: '透视', value: 'perspective' },
          { label: '淡入', value: 'fade' },
        ],
      },
      defaultValue: tippyProps.animation,
      fieldName: 'animation',
      label: '动画类型',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '是', value: true },
          { label: '否', value: false },
        ],
        optionType: 'button',
      },
      defaultValue: tippyProps.inertia,
      fieldName: 'inertia',
      label: '动画惯性',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        options: [
          { label: '顶部', value: 'top' },
          { label: '顶左', value: 'top-start' },
          { label: '顶右', value: 'top-end' },
          { label: '底部', value: 'bottom' },
          { label: '底左', value: 'bottom-start' },
          { label: '底右', value: 'bottom-end' },
          { label: '左侧', value: 'left' },
          { label: '左上', value: 'left-start' },
          { label: '左下', value: 'left-end' },
          { label: '右侧', value: 'right' },
          { label: '右上', value: 'right-start' },
          { label: '右下', value: 'right-end' },
        ],
      },
      defaultValue: tippyProps.placement,
      fieldName: 'placement',
      label: '位置',
    },
    {
      component: 'InputNumber',
      componentProps: {
        addonAfter: '毫秒',
      },
      defaultValue: tippyProps.duration,
      fieldName: 'duration',
      label: '动画时长',
    },
    {
      component: 'InputNumber',
      componentProps: {
        addonAfter: '毫秒',
      },
      defaultValue: 100,
      fieldName: 'delay1',
      label: '显示延时',
    },
    {
      component: 'InputNumber',
      componentProps: {
        addonAfter: '毫秒',
      },
      defaultValue: 100,
      fieldName: 'delay2',
      label: '隐藏延时',
    },
    {
      component: 'Input',
      defaultValue: tippyProps.content,
      fieldName: 'content',
      label: '内容',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '是', value: true },
          { label: '否', value: false },
        ],
        optionType: 'button',
      },
      defaultValue: tippyProps.arrow,
      fieldName: 'arrow',
      label: '指示箭头',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        options: [
          { label: '不跟随', value: 'false' },
          { label: '完全跟随', value: 'true' },
          { label: '仅横向', value: 'horizontal' },
          { label: '仅纵向', value: 'vertical' },
          { label: '仅初始', value: 'initial' },
        ],
      },
      defaultValue: tippyProps.followCursor?.toString(),
      fieldName: 'followCursor',
      label: '跟随指针',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        mode: 'multiple',
        options: [
          { label: '鼠标移入', value: 'mouseenter' },
          { label: '被点击', value: 'click' },
          { label: '获得焦点', value: 'focusin' },
          { label: '无触发，仅手动', value: 'manual' },
        ],
      },
      defaultValue: tippyProps.trigger?.split(' '),
      fieldName: 'trigger',
      label: '触发方式',
    },
    {
      component: 'Select',
      componentProps: {
        class: 'w-full',
        options: [
          { label: '否', value: 'false' },
          { label: '是', value: 'true' },
          { label: '仅内部', value: 'toggle' },
        ],
      },
      defaultValue: tippyProps.hideOnClick?.toString(),
      dependencies: {
        componentProps(_, formAction) {
          return {
            disabled: !formAction.values.trigger.includes('click'),
          };
        },
        triggerFields: ['trigger'],
      },
      fieldName: 'hideOnClick',
      help: '只有在触发方式为`click`时才有效',
      label: '点击后隐藏',
    },
    {
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: 'none、200px',
      },
      defaultValue: tippyProps.maxWidth,
      fieldName: 'maxWidth',
      label: '最大宽度',
    },
  ],
  showDefaultActions: false,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
});

function goDoc() {
  window.open('https://atomiks.github.io/tippyjs/v6/all-props/');
}
</script>
<template>
  <Page title="Tippy">
    <template #description>
      <div class="flex items-center">
        <p>
          Tippy
          是一个轻量级的提示工具库，它可以用来创建各种交互式提示，如工具提示、引导提示等。
        </p>
        <Button type="link" size="small" @click="goDoc">查看文档</Button>
      </div>
    </template>
    <Card title="指令形式使用">
      <p class="mb-4">
        指令形式使用比较简洁，直接在需要展示tooltip的组件上用v-tippy传递配置，适用于固定内容的工具提示。
      </p>
      <Flex warp="warp" gap="20" align="center">
        <Button v-tippy="'这是一个提示，使用了默认的配置'">默认配置</Button>

        <Button
          v-tippy="{ theme: 'light', content: '这是一个提示，总是light主题' }"
        >
          指定主题
        </Button>
        <Button
          v-tippy="{
            theme: 'light',
            content: '这个提示将在点燃组件100毫秒后激活',
            delay: 100,
          }"
        >
          指定延时
        </Button>
        <Button
          v-tippy="{
            content: '本提示的动画为`scale`',
            animation: 'scale',
          }"
        >
          指定动画
        </Button>
      </Flex>
    </Card>
    <Card title="组件形式使用" class="mt-4">
      <div class="flex w-full justify-center">
        <Tippy v-bind="tippyProps">
          <Button>鼠标移到这个组件上来体验效果</Button>
        </Tippy>
      </div>

      <Form class="mt-4" />
      <template #actions>
        <p
          class="text-secondary-foreground hover:text-secondary-foreground cursor-default"
        >
          更多配置请
          <Button type="link" size="small" @click="goDoc">查看文档</Button>
          ，这里只列出了一些常用的配置
        </p>
      </template>
    </Card>
  </Page>
</template>
