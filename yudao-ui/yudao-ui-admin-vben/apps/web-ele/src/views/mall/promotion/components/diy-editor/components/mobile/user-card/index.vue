<script setup lang="ts">
import type { UserCardProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElAvatar } from 'element-plus';

/** 用户卡片 */
defineOptions({ name: 'UserCard' });

/** 定义属性 */
defineProps<{ property: UserCardProperty }>();
</script>
<template>
  <div class="flex flex-col">
    <div class="flex items-center justify-between px-4 py-6">
      <div class="flex flex-1 items-center gap-4">
        <ElAvatar :size="60">
          <IconifyIcon icon="ep:avatar" :size="60" />
        </ElAvatar>
        <span class="text-[18px] font-bold">芋道源码</span>
      </div>
      <IconifyIcon icon="tdesign:qrcode" :size="20" />
    </div>
    <div class="flex items-center justify-between bg-white px-5 py-2 text-xs">
      <span class="text-[#ff690d]">点击绑定手机号</span>
      <span class="rounded-[26px] bg-[#ff6100] px-2 py-1.5 text-white">
        去绑定
      </span>
    </div>
  </div>
</template>
