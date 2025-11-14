<script lang="ts" setup>
import { h, ref } from 'vue';

import { alert, confirm, VbenButton } from '@vben/common-ui';

import { Checkbox, message } from 'ant-design-vue';

function showConfirm() {
  confirm('This is an alert message')
    .then(() => {
      alert('Confirmed');
    })
    .catch(() => {
      alert('Canceled');
    });
}

function showIconConfirm() {
  confirm({
    content: 'This is an alert message with icon',
    icon: 'success',
  });
}

function showfooterConfirm() {
  const checked = ref(false);
  confirm({
    cancelText: '不要虾扯蛋',
    confirmText: '是的，我们都是NPC',
    content:
      '刚才发生的事情，为什么我似乎早就经历过一般？\n我甚至能在事情发生过程中潜意识里预知到接下来会发生什么。\n\n听起来挺玄乎的，你有过这种感觉吗？',
    footer: () =>
      h(
        Checkbox,
        {
          checked: checked.value,
          class: 'flex-1',
          'onUpdate:checked': (v) => (checked.value = v),
        },
        '不再提示',
      ),
    icon: 'question',
    title: '未解之谜',
  }).then(() => {
    if (checked.value) {
      message.success('我不会再拿这个问题烦你了');
    } else {
      message.info('下次还要继续问你哟');
    }
  });
}

function showAsyncConfirm() {
  confirm({
    beforeClose({ isConfirm }) {
      if (isConfirm) {
        // 这里可以执行一些异步操作。如果最终返回了false，将阻止关闭弹窗
        return new Promise((resolve) => setTimeout(resolve, 2000));
      }
    },
    content: 'This is an alert message with async confirm',
    icon: 'success',
  }).then(() => {
    alert('Confirmed');
  });
}
</script>
<template>
  <div class="flex gap-4">
    <VbenButton @click="showConfirm">Confirm</VbenButton>
    <VbenButton @click="showIconConfirm">Confirm With Icon</VbenButton>
    <VbenButton @click="showfooterConfirm">Confirm With Footer</VbenButton>
    <VbenButton @click="showAsyncConfirm">Async Confirm</VbenButton>
  </div>
</template>
