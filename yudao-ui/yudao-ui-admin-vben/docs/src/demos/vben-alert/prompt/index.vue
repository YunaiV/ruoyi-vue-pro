<script lang="ts" setup>
import { h } from 'vue';

import { alert, prompt, useAlertContext, VbenButton } from '@vben/common-ui';

import { Input, RadioGroup, Select } from 'ant-design-vue';
import { BadgeJapaneseYen } from 'lucide-vue-next';

function showPrompt() {
  prompt({
    content: '请输入一些东西',
  })
    .then((val) => {
      alert(`已收到你的输入：${val}`);
    })
    .catch(() => {
      alert('Canceled');
    });
}

function showSlotsPrompt() {
  prompt({
    component: () => {
      // 获取弹窗上下文。注意：只能在setup或者函数式组件中调用
      const { doConfirm } = useAlertContext();
      return h(
        Input,
        {
          onKeydown(e: KeyboardEvent) {
            if (e.key === 'Enter') {
              e.preventDefault();
              // 调用弹窗提供的确认方法
              doConfirm();
            }
          },
          placeholder: '请输入',
          prefix: '充值金额：',
          type: 'number',
        },
        {
          addonAfter: () => h(BadgeJapaneseYen),
        },
      );
    },
    content:
      '此弹窗演示了如何使用自定义插槽，并且可以使用useAlertContext获取到弹窗的上下文。\n在输入框中按下回车键会触发确认操作。',
    icon: 'question',
    modelPropName: 'value',
  }).then((val) => {
    if (val) alert(`你输入的是${val}`);
  });
}

function showSelectPrompt() {
  prompt({
    component: Select,
    componentProps: {
      options: [
        { label: 'Option A', value: 'Option A' },
        { label: 'Option B', value: 'Option B' },
        { label: 'Option C', value: 'Option C' },
      ],
      placeholder: '请选择',
      // 弹窗会设置body的pointer-events为none，这回影响下拉框的点击事件
      popupClassName: 'pointer-events-auto',
    },
    content: '此弹窗演示了如何使用component传递自定义组件',
    icon: 'question',
    modelPropName: 'value',
  }).then((val) => {
    if (val) {
      alert(`你选择了${val}`);
    }
  });
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function showAsyncPrompt() {
  prompt({
    async beforeClose(scope) {
      if (scope.isConfirm) {
        if (scope.value) {
          // 模拟异步操作，如果不成功，可以返回false
          await sleep(2000);
        } else {
          alert('请选择一个选项');
          return false;
        }
      }
    },
    component: RadioGroup,
    componentProps: {
      class: 'flex flex-col',
      options: [
        { label: 'Option 1', value: 'option1' },
        { label: 'Option 2', value: 'option2' },
        { label: 'Option 3', value: 'option3' },
      ],
    },
    content: '选择一个选项后再点击[确认]',
    icon: 'question',
    modelPropName: 'value',
  }).then((val) => {
    alert(`${val} 已设置。`);
  });
}
</script>
<template>
  <div class="flex gap-4">
    <VbenButton @click="showPrompt">Prompt</VbenButton>
    <VbenButton @click="showSlotsPrompt"> Prompt With slots </VbenButton>
    <VbenButton @click="showSelectPrompt">Prompt With Select</VbenButton>
    <VbenButton @click="showAsyncPrompt">Prompt With Async</VbenButton>
  </div>
</template>
