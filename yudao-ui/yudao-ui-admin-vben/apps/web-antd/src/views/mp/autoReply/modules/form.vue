<script lang="ts" setup>
import type { Reply } from '#/views/mp/components/wx-reply/types';

import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { AutoReplyMsgType, ReplyType } from '@vben/constants';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createAutoReply, updateAutoReply } from '#/api/mp/autoReply';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

const formData = ref<{
  accountId?: number;
  msgType: AutoReplyMsgType;
  row?: any;
}>();
const getTitle = computed(() => {
  return formData.value?.row?.id
    ? $t('ui.actionTitle.edit', ['自动回复'])
    : $t('ui.actionTitle.create', ['自动回复']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(AutoReplyMsgType.Keyword),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }

    // 处理回复消息
    const submitForm: any = await formApi.getValues();
    // 确保 type 字段使用当前选中的 tab 值
    submitForm.type = formData.value?.msgType;
    // 确保 accountId 字段存在
    submitForm.accountId = formData.value?.accountId;
    // 编辑模式下，确保 id 字段存在（从 row 中获取，因为表单 schema 中没有 id 字段）
    if (formData.value?.row?.id && !submitForm.id) {
      submitForm.id = formData.value.row.id;
    }
    const reply = submitForm.reply as Reply;
    if (reply) {
      submitForm.responseMessageType = reply.type;
      submitForm.responseContent = reply.content;
      submitForm.responseMediaId = reply.mediaId;
      submitForm.responseMediaUrl = reply.url;
      submitForm.responseTitle = reply.title;
      submitForm.responseDescription = reply.description;
      submitForm.responseThumbMediaId = reply.thumbMediaId;
      submitForm.responseThumbMediaUrl = reply.thumbMediaUrl;
      submitForm.responseArticles = reply.articles;
      submitForm.responseMusicUrl = reply.musicUrl;
      submitForm.responseHqMusicUrl = reply.hqMusicUrl;
    }
    delete submitForm.reply;

    modalApi.lock();
    try {
      if (submitForm.id === undefined) {
        await createAutoReply(submitForm);
        message.success('新增成功');
      } else {
        await updateAutoReply(submitForm);
        message.success('修改成功');
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
      return;
    }
    // 加载数据
    const data = modalApi.getData<{
      accountId?: number;
      msgType: AutoReplyMsgType;
      row?: any;
    }>();
    if (!data) {
      return;
    }
    // 先更新 schema，确保表单字段正确
    formApi.setState({ schema: useFormSchema(data.msgType) });
    // 等待 schema 更新完成
    await nextTick();

    formData.value = data;
    if (data.row?.id) {
      // 编辑：加载数据
      const rowData = data.row;
      const formValues: any = {
        ...rowData,
        reply: {
          type: rowData.responseMessageType,
          accountId: data.accountId || -1,
          content: rowData.responseContent,
          mediaId: rowData.responseMediaId,
          url: rowData.responseMediaUrl,
          title: rowData.responseTitle,
          description: rowData.responseDescription,
          thumbMediaId: rowData.responseThumbMediaId,
          thumbMediaUrl: rowData.responseThumbMediaUrl,
          articles: rowData.responseArticles,
          musicUrl: rowData.responseMusicUrl,
          hqMusicUrl: rowData.responseHqMusicUrl,
        },
      };
      await formApi.setValues(formValues);
    } else {
      // 新建：初始化表单
      const initialValues: any = {
        id: undefined,
        accountId: data.accountId || -1,
        type: data.msgType,
        requestKeyword: undefined,
        requestMatch: data.msgType === AutoReplyMsgType.Keyword ? 1 : undefined,
        requestMessageType: undefined,
        reply: {
          type: ReplyType.Text,
          accountId: data.accountId || -1,
        },
      };
      await formApi.setValues(initialValues);
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-4/5">
    <Form class="mx-4" />
  </Modal>
</template>
