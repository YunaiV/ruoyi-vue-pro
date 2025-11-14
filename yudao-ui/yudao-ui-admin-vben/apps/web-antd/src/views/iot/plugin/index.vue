<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

defineOptions({ name: 'IotPlugin' });
</script>

<template>
  <Page description="物聯網插件管理" title="插件管理">
    <div class="p-4">
      <Button
        danger
        type="link"
        target="_blank"
        href="https://github.com/yudaocode/yudao-ui-admin-vue3"
      >
        該功能支持 Vue3 + element-plus 版本！
      </Button>
      <div class="mt-4">
        <h3>功能說明：</h3>
        <p>IoT 插件管理功能</p>
        <h3 class="mt-4">待實現：</h3>
        <ul>
          <li>⚠️ API 接口定義</li>
          <li>⚠️ 頁面實現</li>
        </ul>
      </div>
    </div>
  </Page>
</template>
