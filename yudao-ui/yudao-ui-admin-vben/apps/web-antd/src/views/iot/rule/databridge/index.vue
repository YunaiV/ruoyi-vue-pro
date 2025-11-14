<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

defineOptions({ name: 'IotRuleDataBridge' });
</script>

<template>
  <Page description="物聯網規則引擎 - 數據橋接" title="數據橋接">
    <div class="p-4">
      <Button
        danger
        type="link"
        target="_blank"
        href="https://github.com/yudaocode/yudao-ui-admin-vue3/tree/master/src/views/iot/rule"
      >
        該功能支持 Vue3 + element-plus 版本！
      </Button>
      <br />
      <Button
        type="link"
        target="_blank"
        href="https://github.com/yudaocode/yudao-ui-admin-vue3/blob/master/src/views/iot/rule/data/rule/index.vue"
      >
        可參考源代碼進行遷移！
      </Button>
      <div class="mt-4">
        <h3>功能說明：</h3>
        <p>規則引擎包括：</p>
        <ul>
          <li>數據規則配置</li>
          <li>數據轉發配置</li>
          <li>場景聯動配置</li>
        </ul>
        <h3 class="mt-4">待實現：</h3>
        <ul>
          <li>⚠️ API 接口定義</li>
          <li>⚠️ 頁面實現</li>
        </ul>
      </div>
    </div>
  </Page>
</template>
