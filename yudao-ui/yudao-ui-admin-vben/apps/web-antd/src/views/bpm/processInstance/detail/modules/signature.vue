<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { base64ToFile } from '@vben/utils';

import { Button, Space, Tooltip } from 'ant-design-vue';
import Vue3Signature from 'vue3-signature';

import { uploadFile } from '#/api/infra/file';

defineOptions({
  name: 'BpmProcessInstanceSignature',
});

const emits = defineEmits(['success']);

const signature = ref<InstanceType<typeof Vue3Signature>>();

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    modalApi.lock();
    try {
      const signFileUrl = await uploadFile({
        file: base64ToFile(signature?.value?.save('image/jpeg') || '', '签名'),
      });
      emits('success', signFileUrl);
      await modalApi.close();
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="流程签名" class="w-3/5">
    <div class="mb-2 flex justify-end">
      <Space>
        <Tooltip title="撤销上一步操作">
          <Button @click="signature?.undo()">
            <template #icon>
              <IconifyIcon icon="lucide:undo" class="mb-1 size-4" />
            </template>
            撤销
          </Button>
        </Tooltip>
        <Tooltip title="清空画布">
          <Button @click="signature?.clear()">
            <template #icon>
              <IconifyIcon icon="lucide:trash" class="mb-1 size-4" />
            </template>
            <span>清除</span>
          </Button>
        </Tooltip>
      </Space>
    </div>

    <Vue3Signature
      class="mx-auto !h-80 border border-solid border-gray-300"
      ref="signature"
    />
  </Modal>
</template>
