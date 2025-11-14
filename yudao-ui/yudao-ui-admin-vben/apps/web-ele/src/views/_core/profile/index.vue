<script setup lang="ts">
import type { SystemUserProfileApi } from '#/api/system/user/profile';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { ElCard, ElTabPane, ElTabs } from 'element-plus';

import { getUserProfile } from '#/api/system/user/profile';
import { useAuthStore } from '#/store';

import BaseInfo from './modules/base-info.vue';
import ProfileUser from './modules/profile-user.vue';
import ResetPwd from './modules/reset-pwd.vue';
import UserSocial from './modules/user-social.vue';

const authStore = useAuthStore();
const activeName = ref('basicInfo');

/** 加载个人信息 */
const profile = ref<SystemUserProfileApi.UserProfileRespVO>();
async function loadProfile() {
  profile.value = await getUserProfile();
}

/** 刷新个人信息 */
async function refreshProfile() {
  // 加载个人信息
  await loadProfile();

  // 更新 store
  await authStore.fetchUserInfo();
}

/** 初始化 */
onMounted(loadProfile);
</script>

<template>
  <Page auto-content-height>
    <div class="flex">
      <!-- 左侧 个人信息 -->
      <ElCard class="w-2/5" title="个人信息">
        <ProfileUser :profile="profile" @success="refreshProfile" />
      </ElCard>

      <!-- 右侧 标签页 -->
      <ElCard class="ml-3 w-3/5">
        <ElTabs v-model="activeName" class="-mt-4">
          <ElTabPane name="basicInfo" label="基本设置">
            <BaseInfo :profile="profile" @success="refreshProfile" />
          </ElTabPane>
          <ElTabPane name="resetPwd" label="密码设置">
            <ResetPwd />
          </ElTabPane>
          <ElTabPane name="userSocial" label="社交绑定" force-render>
            <UserSocial @update:active-name="activeName = $event" />
          </ElTabPane>
          <!-- TODO @芋艿：在线设备 -->
        </ElTabs>
      </ElCard>
    </div>
  </Page>
</template>
