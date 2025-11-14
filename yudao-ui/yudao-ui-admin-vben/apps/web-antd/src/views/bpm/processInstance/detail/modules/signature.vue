<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { base64ToFile } from '@vben/utils';

import { Button, message, Space, Tooltip } from 'ant-design-vue';
// TODO @ziye：这个可能，适合放到全局？！因为 element-plus 也用这个；
import Vue3Signature from 'vue3-signature';

import { uploadFile } from '#/api/infra/file';

defineOptions({
  name: 'BpmProcessInstanceSignature',
});

const emits = defineEmits(['success']);

const signature = ref<InstanceType<typeof Vue3Signature>>();

const [Modal, modalApi] = useVbenModal({
  title: '流程签名',
  onOpenChange(visible) {
    if (!visible) {
      modalApi.close();
    }
  },
  async onConfirm() {
    message.success({
      content: '签名上传中请稍等。。。',
    });
    const signFileUrl = await uploadFile({
      file: base64ToFile(signature?.value?.save('image/jpeg') || '', '签名'),
    });
    emits('success', signFileUrl);
    // TODO @ziye：下面有个告警哈；ps：所有告警，皆是错误，可以关注 ide 给的提示哈；
    modalApi.close();
  },
});
</script>

<template>
  <Modal class="h-2/5 w-3/5">
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
      class="mx-auto border border-solid border-gray-300"
      ref="signature"
      w="874px"
      h="324px"
    />
  </Modal>
</template>
