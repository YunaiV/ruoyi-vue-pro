<script setup lang="ts">
import { Page, SliderTranslateCaptcha } from '@vben/common-ui';

import { Card, message } from 'ant-design-vue';

function handleSuccess() {
  message.success('success!');
}
</script>

<template>
  <Page
    description="用于前端简单的拼图滑块水平拖动校验场景"
    title="拼图滑块校验"
  >
    <Card class="mb-5" title="基本示例">
      <div class="flex items-center justify-center p-4">
        <SliderTranslateCaptcha
          src="https://unpkg.com/@vbenjs/static-source@0.1.7/source/pro-avatar.webp"
          :canvas-width="420"
          :canvas-height="420"
          @success="handleSuccess"
        />
      </div>
    </Card>
  </Page>
</template>
