<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { useWatermark } from '@vben/hooks';

import { Button, Card } from 'ant-design-vue';

const { destroyWatermark, updateWatermark, watermark } = useWatermark();

async function recreateWaterMark() {
  destroyWatermark();
  await createWaterMark();
}

async function createWaterMark() {
  await updateWatermark({
    advancedStyle: {
      colorStops: [
        {
          color: 'red',
          offset: 0,
        },
        {
          color: 'blue',
          offset: 1,
        },
      ],
      type: 'linear',
    },
    content: `hello my watermark\n${new Date().toLocaleString()}`,
    globalAlpha: 0.5,
    gridLayoutOptions: {
      cols: 2,
      gap: [20, 20],
      matrix: [
        [1, 0],
        [0, 1],
      ],
      rows: 2,
    },
    height: 200,
    layout: 'grid',
    rotate: 22,
    width: 200,
  });
}
</script>

<template>
  <Page title="水印">
    <template #description>
      <div class="text-foreground/80 mt-2">
        水印使用了
        <a
          class="text-primary"
          href="https://zhensherlock.github.io/watermark-js-plus/"
          target="_blank"
        >
          watermark-js-plus
        </a>
        开源插件，详细配置可见插件配置。
      </div>
    </template>

    <Card title="使用">
      <Button
        :disabled="!!watermark"
        class="mr-2"
        type="primary"
        @click="recreateWaterMark"
      >
        创建水印
      </Button>
      <Button
        :disabled="!watermark"
        class="mr-2"
        type="primary"
        @click="createWaterMark"
      >
        更新水印
      </Button>
      <Button :disabled="!watermark" danger @click="destroyWatermark">
        移除水印
      </Button>
    </Card>
  </Page>
</template>
