<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Button } from 'ant-design-vue';
</script>

<template>
  <Page>
    <Button
      danger
      type="link"
      target="_blank"
      href="https://github.com/yudaocode/yudao-ui-admin-vue3"
    >
      该功能支持 Vue3 + element-plus 版本！
    </Button>
    <br />
    <Button
      type="link"
      target="_blank"
      href="https://github.com/yudaocode/yudao-ui-admin-vue3/blob/master/src/views/mp/message/index"
    >
      可参考
      https://github.com/yudaocode/yudao-ui-admin-vue3/blob/master/src/views/mp/message/index
      代码，pull request 贡献给我们！
    </Button>
  </Page>
</template>
