<script lang="ts" setup>
import { h } from 'vue';

import { alert, VbenButton } from '@vben/common-ui';

import { Result } from 'ant-design-vue';

function showAlert() {
  alert('This is an alert message');
}

function showIconAlert() {
  alert({
    content: 'This is an alert message with icon',
    icon: 'success',
  });
}

function showCustomAlert() {
  alert({
    buttonAlign: 'center',
    content: h(Result, {
      status: 'success',
      subTitle: '已成功创建订单。订单ID：2017182818828182881',
      title: '操作成功',
    }),
  });
}
</script>
<template>
  <div class="flex gap-4">
    <VbenButton @click="showAlert">Alert</VbenButton>
    <VbenButton @click="showIconAlert">Alert With Icon</VbenButton>
    <VbenButton @click="showCustomAlert">Alert With Custom Content</VbenButton>
  </div>
</template>
