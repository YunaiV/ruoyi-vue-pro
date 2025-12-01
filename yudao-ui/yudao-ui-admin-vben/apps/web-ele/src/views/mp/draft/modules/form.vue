<script lang="ts" setup>
import type { MpDraftApi } from '#/api/mp/draft';

import { computed, provide, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { createDraft, updateDraft } from '#/api/mp/draft';

import NewsForm from './news-form.vue';

const emit = defineEmits(['success']);

const formData = ref<{
  accountId: number;
  mediaId?: string;
  newsList?: MpDraftApi.NewsItem[];
}>();
const newsList = ref<MpDraftApi.NewsItem[]>([]);

const getTitle = computed(() => {
  return formData.value?.mediaId ? '修改图文' : '新建图文';
});

provide(
  'accountId',
  computed(() => formData.value?.accountId),
); // 提供 accountId 给子组件

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (!formData.value) {
      return;
    }
    modalApi.lock();
    try {
      if (formData.value.mediaId) {
        await updateDraft(
          formData.value.accountId,
          formData.value.mediaId,
          newsList.value,
        );
        ElMessage.success('更新成功');
      } else {
        await createDraft(formData.value.accountId, newsList.value);
        ElMessage.success('新增成功');
      }
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      newsList.value = [];
      return;
    }
    const data = modalApi.getData<{
      accountId: number;
      isCreating: boolean;
      mediaId?: string;
      newsList?: MpDraftApi.NewsItem[];
    }>();
    if (!data) {
      return;
    }
    formData.value = {
      accountId: data.accountId,
      mediaId: data.mediaId,
      newsList: data.newsList,
    };
    newsList.value = data.newsList || [];
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-4/5" destroy-on-close>
    <NewsForm
      v-if="formData"
      v-model="newsList"
      :is-creating="!formData.mediaId"
    />
  </Modal>
</template>
