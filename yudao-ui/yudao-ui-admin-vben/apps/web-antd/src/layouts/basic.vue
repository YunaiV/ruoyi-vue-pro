<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import type { SystemTenantApi } from '#/api/system/tenant';

import { computed, onMounted, ref, watch } from 'vue';

import { useAccess } from '@vben/access';
import { AuthenticationLoginExpiredModal, useVbenModal } from '@vben/common-ui';
import { VBEN_DOC_URL, VBEN_GITHUB_URL } from '@vben/constants';
import { isTenantEnable, useTabs, useWatermark } from '@vben/hooks';
import {
  AntdProfileOutlined,
  BookOpenText,
  CircleHelp,
  SvgGithubIcon,
} from '@vben/icons';
import {
  BasicLayout,
  Help,
  LockScreen,
  Notification,
  TenantDropdown,
  UserDropdown,
} from '@vben/layouts';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { formatDateTime, openWindow } from '@vben/utils';

import { message } from 'ant-design-vue';

import {
  getUnreadNotifyMessageCount,
  getUnreadNotifyMessageList,
  updateAllNotifyMessageRead,
  updateNotifyMessageRead,
} from '#/api/system/notify/message';
import { getSimpleTenantList } from '#/api/system/tenant';
import { $t } from '#/locales';
import { router } from '#/router';
import { useAuthStore } from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';

const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { hasAccessByCodes } = useAccess();
const { destroyWatermark, updateWatermark } = useWatermark();
const { closeOtherTabs, refreshTab } = useTabs();

const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
const showDot = computed(() => unreadCount.value > 0);

const [HelpModal, helpModalApi] = useVbenModal({
  connectedComponent: Help,
});

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    icon: AntdProfileOutlined,
    text: $t('ui.widgets.profile'),
  },
  {
    handler: () => {
      openWindow(VBEN_DOC_URL, {
        target: '_blank',
      });
    },
    icon: BookOpenText,
    text: $t('ui.widgets.document'),
  },
  {
    handler: () => {
      openWindow(VBEN_GITHUB_URL, {
        target: '_blank',
      });
    },
    icon: SvgGithubIcon,
    text: 'GitHub',
  },
  {
    handler: () => {
      helpModalApi.open();
    },
    icon: CircleHelp,
    text: $t('ui.widgets.qa'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

/** 获得未读消息数 */
async function handleNotificationGetUnreadCount() {
  unreadCount.value = await getUnreadNotifyMessageCount();
}

/** 获得消息列表 */
async function handleNotificationGetList() {
  const list = await getUnreadNotifyMessageList();
  notifications.value = list.map((item) => ({
    avatar: preferences.app.defaultAvatar,
    date: formatDateTime(item.createTime) as string,
    isRead: false,
    id: item.id,
    message: item.templateContent,
    title: item.templateNickname,
  }));
}

/** 跳转我的站内信 */
function handleNotificationViewAll() {
  router.push({
    name: 'MyNotifyMessage',
  });
}

/** 标记所有已读 */
async function handleNotificationMakeAll() {
  await updateAllNotifyMessageRead();
  unreadCount.value = 0;
  notifications.value = [];
}

/** 清空通知 */
async function handleNotificationClear() {
  await handleNotificationMakeAll();
}

/** 标记单个已读 */
async function handleNotificationRead(item: NotificationItem) {
  if (!item.id) {
    return;
  }
  await updateNotifyMessageRead([item.id]);
  await handleNotificationGetUnreadCount();
  notifications.value = notifications.value.filter((n) => n.id !== item.id);
}

/** 处理通知打开 */
function handleNotificationOpen(open: boolean) {
  if (!open) {
    return;
  }
  handleNotificationGetList();
  handleNotificationGetUnreadCount();
}

// 租户列表
const tenants = ref<SystemTenantApi.Tenant[]>([]);
const tenantEnable = computed(
  () => hasAccessByCodes(['system:tenant:visit']) && isTenantEnable(),
);

/** 获取租户列表 */
async function handleGetTenantList() {
  if (tenantEnable.value) {
    tenants.value = await getSimpleTenantList();
  }
}

/** 处理租户切换 */
async function handleTenantChange(tenant: SystemTenantApi.Tenant) {
  if (!tenant || !tenant.id) {
    message.error('切换租户失败');
    return;
  }
  // 设置访问租户 ID
  accessStore.setVisitTenantId(tenant.id as number);
  // 关闭其他标签页，只保留当前页
  await closeOtherTabs();
  // 刷新当前页面
  await refreshTab();
  // 提示切换成功
  message.success(`切换当前租户为: ${tenant.name}`);
}

// ========== 初始化 ==========
onMounted(() => {
  // 首次加载未读数量
  handleNotificationGetUnreadCount();
  // 获取租户列表
  handleGetTenantList();
  // 轮询刷新未读数量
  setInterval(
    () => {
      if (userStore.userInfo) {
        handleNotificationGetUnreadCount();
      }
    },
    1000 * 60 * 2,
  );
});

watch(
  () => ({
    enable: preferences.app.watermark,
    content: preferences.app.watermarkContent,
  }),
  async ({ enable, content }) => {
    if (enable) {
      await updateWatermark({
        content:
          content ||
          `${userStore.userInfo?.id} - ${userStore.userInfo?.nickname}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.nickname"
        :description="userStore.userInfo?.email"
        :tag-text="userStore.userInfo?.username"
        @logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNotificationClear"
        @make-all="handleNotificationMakeAll"
        @view-all="handleNotificationViewAll"
        @open="handleNotificationOpen"
        @read="handleNotificationRead"
      />
    </template>
    <template #header-right-1>
      <div v-if="tenantEnable">
        <TenantDropdown
          class="mr-2"
          :tenant-list="tenants"
          :visit-tenant-id="accessStore.visitTenantId"
          @success="handleTenantChange"
        />
      </div>
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
  <HelpModal />
</template>
