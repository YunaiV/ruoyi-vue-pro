<script setup lang="ts">
import type { SystemSocialUserApi } from '#/api/system/social/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { getSocialUser } from '#/api/system/social/user';
import { useDescription } from '#/components/description';
import { $t } from '#/locales';

import { useDetailSchema } from '../data';

const formData = ref<SystemSocialUserApi.SocialUser>();

const [Descriptions] = useDescription({
  bordered: true,
  column: 1,
  schema: useDetailSchema(),
});

const [Modal, modalApi] = useVbenModal({
  title: $t('ui.actionTitle.detail'),
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getSocialUser(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="社交用户详情"
    class="w-1/2"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Descriptions :data="formData" />
  </Modal>
</template>
