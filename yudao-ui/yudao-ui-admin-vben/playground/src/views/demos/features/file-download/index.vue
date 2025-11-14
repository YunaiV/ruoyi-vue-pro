<script setup lang="ts">
import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import {
  downloadFileFromBase64,
  downloadFileFromBlobPart,
  downloadFileFromImageUrl,
  downloadFileFromUrl,
} from '@vben/utils';

import { Button, Card } from 'ant-design-vue';

import { downloadFile1, downloadFile2 } from '#/api/examples/download';

import imageBase64 from './base64';

const downloadResult = ref('');

function getBlob() {
  downloadFile1().then((res) => {
    downloadResult.value = `获取Blob成功，长度：${res.size}`;
  });
}

function getResponse() {
  downloadFile2().then((res) => {
    downloadResult.value = `获取Response成功，headers：${JSON.stringify(res.headers)},长度：${res.data.size}`;
  });
}
</script>

<template>
  <Page title="文件下载示例">
    <Card title="根据文件地址下载文件">
      <Button
        type="primary"
        @click="
          downloadFileFromUrl({
            source:
              'https://codeload.github.com/vbenjs/vue-vben-admin-doc/zip/main',
            target: '_self',
          })
        "
      >
        Download File
      </Button>
    </Card>

    <Card class="my-5" title="根据地址下载图片">
      <Button
        type="primary"
        @click="
          downloadFileFromImageUrl({
            source:
              'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
            fileName: 'vben-logo.png',
          })
        "
      >
        Download File
      </Button>
    </Card>

    <Card class="my-5" title="base64流下载">
      <Button
        type="primary"
        @click="
          downloadFileFromBase64({
            source: imageBase64,
            fileName: 'image.png',
          })
        "
      >
        Download Image
      </Button>
    </Card>
    <Card class="my-5" title="文本下载">
      <Button
        type="primary"
        @click="
          downloadFileFromBlobPart({
            source: 'text content',
            fileName: 'test.txt',
          })
        "
      >
        Download TxT
      </Button>
    </Card>

    <Card class="my-5" title="Request download">
      <Button type="primary" @click="getBlob"> 获取Blob </Button>
      <Button type="primary" class="ml-4" @click="getResponse">
        获取Response
      </Button>
      <div class="mt-4">{{ downloadResult }}</div>
    </Card>
  </Page>
</template>
