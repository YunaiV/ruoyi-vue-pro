<script setup lang="ts">
import type { UserCardProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElAvatar } from 'element-plus';

/** 用户卡片 */
defineOptions({ name: 'UserCard' });
// 定义属性
defineProps<{ property: UserCardProperty }>();
</script>
<template>
  <div class="flex flex-col">
    <div class="flex items-center justify-between px-[18px] py-[24px]">
      <div class="flex flex-1 items-center gap-[16px]">
        <ElAvatar :size="60">
          <IconifyIcon icon="ep:avatar" :size="60" />
        </ElAvatar>
        <span class="text-[18px] font-bold">芋道源码</span>
      </div>
      <IconifyIcon icon="tdesign:qrcode" :size="20" />
    </div>
    <div
      class="flex items-center justify-between bg-white px-[20px] py-[8px] text-[12px]"
    >
      <span class="text-[#ff690d]">点击绑定手机号</span>
      <span class="rounded-[26px] bg-[#ff6100] px-[8px] py-[5px] text-white">
        去绑定
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
