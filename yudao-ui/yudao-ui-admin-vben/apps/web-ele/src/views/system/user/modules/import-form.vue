<script lang="ts" setup>
import type { UploadRawFile } from 'element-plus';

import { useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElButton, ElMessage, ElUpload } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { importUser, importUserTemplate } from '#/api/system/user';
import { $t } from '#/locales';

import { useImportFormSchema } from '../data';

const emit = defineEmits(['success']);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useImportFormSchema(),
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
      await importUser(data.file, data.updateSupport);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
});

/** 上传前 */
function beforeUpload(file: UploadRawFile) {
  formApi.setFieldValue('file', file);
  return false;
}

/** 下载模版 */
async function handleDownload() {
  const data = await importUserTemplate();
  downloadFileFromBlobPart({ fileName: '用户导入模板.xls', source: data });
}
</script>

<template>
  <Modal title="导入用户" class="w-1/3">
    <Form class="mx-4">
      <template #file>
        <div class="w-full">
          <ElUpload
            :max-count="1"
            accept=".xls,.xlsx"
            :auto-upload="false"
            :before-upload="beforeUpload"
          >
            <ElButton type="primary"> 选择 Excel 文件 </ElButton>
          </ElUpload>
        </div>
      </template>
    </Form>
    <template #prepend-footer>
      <div class="flex flex-auto items-center">
        <ElButton @click="handleDownload"> 下载导入模板 </ElButton>
      </div>
    </template>
  </Modal>
</template>
