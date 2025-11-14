<script lang="ts" setup>
import type { UploadFileInfo } from 'naive-ui';

import { useVbenModal } from '@vben/common-ui';

import { NUpload, NUploadDragger } from 'naive-ui';

import { useVbenForm } from '#/adapter/form';
import { message } from '#/adapter/naive';
import { useUpload } from '#/components/upload/use-upload';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
    hideLabel: true,
  },
  layout: 'horizontal',
  schema: useFormSchema().map((item) => ({ ...item, label: '' })), // 去除label
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = await formApi.getValues();
    try {
      await useUpload().httpRequest(data.file);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
});

/** 上传前 */
function beforeUpload(data: {
  file: UploadFileInfo;
  fileList: UploadFileInfo[];
}) {
  formApi.setFieldValue('file', data.file.file);
  return false;
}
</script>

<template>
  <Modal title="上传图片">
    <Form class="mx-4">
      <template #file>
        <div class="w-full">
          <!-- 上传区域 -->
          <NUpload
            name="file"
            :max="1"
            :multiple="false"
            accept=".jpg,.png,.gif,.webp"
            @before-upload="beforeUpload"
          >
            <NUploadDragger>
              <p class="ant-upload-drag-icon">
                <span class="icon-[ant-design--inbox-outlined] text-2xl"></span>
              </p>
              <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
              <p class="ant-upload-hint">
                支持 .jpg、.png、.gif、.webp 格式图片文件
              </p>
            </NUploadDragger>
          </NUpload>
        </div>
      </template>
    </Form>
  </Modal>
</template>
