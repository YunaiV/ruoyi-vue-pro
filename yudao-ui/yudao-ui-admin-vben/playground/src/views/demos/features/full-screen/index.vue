<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { useFullscreen } from '@vueuse/core';
import { Button, Card } from 'ant-design-vue';

const domRef = ref<HTMLElement>();

const { enter, exit, isFullscreen, toggle } = useFullscreen();

const { isFullscreen: isDomFullscreen, toggle: toggleDom } =
  useFullscreen(domRef);
</script>

<template>
  <Page title="全屏示例">
    <Card title="Window Full Screen">
      <div class="flex flex-wrap items-center gap-4">
        <Button :disabled="isFullscreen" type="primary" @click="enter">
          Enter Window Full Screen
        </Button>
        <Button @click="toggle"> Toggle Window Full Screen </Button>

        <Button :disabled="!isFullscreen" danger @click="exit">
          Exit Window Full Screen
        </Button>

        <span class="text-nowrap"> Current State: {{ isFullscreen }} </span>
      </div>
    </Card>

    <Card class="mt-5" title="Dom Full Screen">
      <Button type="primary" @click="toggleDom"> Enter Dom Full Screen </Button>
    </Card>

    <div
      ref="domRef"
      class="mx-auto mt-10 flex h-64 w-1/2 items-center justify-center rounded-md bg-yellow-400"
    >
      <Button class="mr-2" type="primary" @click="toggleDom">
        {{ isDomFullscreen ? 'Exit Dom Full Screen' : 'Enter Dom Full Screen' }}
      </Button>
    </div>
  </Page>
</template>
