<script lang="ts" setup>
import type { LoginExpiredModeType } from '@vben/types';

import { Page } from '@vben/common-ui';
import { preferences, updatePreferences } from '@vben/preferences';

import { Button, Card } from 'ant-design-vue';

import { getMockStatusApi } from '#/api';

async function handleClick(type: LoginExpiredModeType) {
  const loginExpiredMode = preferences.app.loginExpiredMode;

  updatePreferences({ app: { loginExpiredMode: type } });
  await getMockStatusApi('401');
  updatePreferences({ app: { loginExpiredMode } });
}
</script>

<template>
  <Page title="登录过期演示">
    <template #description>
      <div class="text-foreground/80 mt-2">
        接口请求遇到401状态码时，需要重新登录。有两种方式：
        <p>1.转到登录页，登录成功后跳转回原页面</p>
        <p>
          2.弹出重新登录弹窗，登录后关闭弹窗，不进行任何页面跳转（刷新后还是会跳转登录页面）
        </p>
      </div>
    </template>

    <Card class="mb-5" title="跳转登录页面方式">
      <Button type="primary" @click="handleClick('page')"> 点击触发 </Button>
    </Card>
    <Card class="mb-5" title="登录弹窗方式">
      <Button type="primary" @click="handleClick('modal')"> 点击触发 </Button>
    </Card>
  </Page>
</template>
