<script setup lang="ts">
import type { SystemUserProfileApi } from '#/api/system/user/profile';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { preferences } from '@vben/preferences';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem, ElTooltip } from 'element-plus';

import { updateUserProfile } from '#/api/system/user/profile';
import { CropperAvatar } from '#/components/cropper';
import { useUpload } from '#/components/upload/use-upload';

const props = defineProps<{
  profile?: SystemUserProfileApi.UserProfileRespVO;
}>();

const emit = defineEmits<{
  (e: 'success'): void;
}>();

const avatar = computed(
  () => props.profile?.avatar || preferences.app.defaultAvatar,
);

// TODO @puhui999：头像上传没跑通
async function handelUpload({
  file,
  filename,
}: {
  file: Blob;
  filename: string;
}) {
  // 1. 上传头像，获取 URL
  const { httpRequest } = useUpload();
  // 将 Blob 转换为 File
  const fileObj = new File([file], filename, { type: file.type });
  const avatar = await httpRequest(fileObj);
  // 2. 更新用户头像
  await updateUserProfile({ avatar });
}
</script>

<template>
  <div v-if="profile">
    <div class="flex flex-col items-center">
      <ElTooltip content="点击上传头像">
        <CropperAvatar
          :show-btn="false"
          :upload-api="handelUpload"
          :value="avatar"
          :width="120"
          @change="emit('success')"
        />
      </ElTooltip>
    </div>
    <div class="mt-8">
      <ElDescriptions :column="2">
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon icon="ant-design:user-outlined" class="mr-1" />
              用户账号
            </div>
          </template>
          {{ profile.username }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon
                icon="ant-design:user-switch-outlined"
                class="mr-1"
              />
              所属角色
            </div>
          </template>
          {{ profile.roles.map((role) => role.name).join(',') }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon icon="ant-design:phone-outlined" class="mr-1" />
              手机号码
            </div>
          </template>
          {{ profile.mobile }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon icon="ant-design:mail-outlined" class="mr-1" />
              用户邮箱
            </div>
          </template>
          {{ profile.email }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon icon="ant-design:team-outlined" class="mr-1" />
              所属部门
            </div>
          </template>
          {{ profile.dept?.name }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon
                icon="ant-design:usergroup-add-outlined"
                class="mr-1"
              />
              所属岗位
            </div>
          </template>
          {{ profile.posts.map((post) => post.name).join(',') }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon
                icon="ant-design:clock-circle-outlined"
                class="mr-1"
              />
              创建时间
            </div>
          </template>
          {{ formatDateTime(profile.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem>
          <template #label>
            <div class="flex items-center">
              <IconifyIcon icon="ant-design:login-outlined" class="mr-1" />
              登录时间
            </div>
          </template>
          {{ formatDateTime(profile.loginDate) }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </div>
  </div>
</template>
