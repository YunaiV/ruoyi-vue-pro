<script setup lang="ts">
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { useClipboard } from '@vueuse/core';
import { Button, Card, Input } from 'ant-design-vue';

const source = ref('Hello');
const { copy, text } = useClipboard({ legacy: true, source });
</script>

<template>
  <Page title="剪切板示例">
    <Card title="基本使用">
      <p class="mb-3">
        Current copied: <code>{{ text || 'none' }}</code>
      </p>
      <div class="flex">
        <Input v-model:value="source" class="mr-3 flex w-[200px]" />
        <Button type="primary" @click="copy(source)"> Copy </Button>
      </div>
    </Card>
  </Page>
</template>
