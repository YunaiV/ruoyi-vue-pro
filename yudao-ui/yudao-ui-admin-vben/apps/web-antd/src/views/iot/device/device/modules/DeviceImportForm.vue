<script setup lang="ts">
import { computed } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { importDeviceTemplate } from '#/api/iot/device/device';

import { useImportFormSchema } from '../data';

defineOptions({ name: 'IoTDeviceImportForm' });

const emit = defineEmits(['success']);
const getTitle = computed(() => '设备导入');

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
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

    const values = await formApi.getValues();
    const file = values.file;

    if (!file || file.length === 0) {
      message.error('请上传文件');
      return;
    }

    modalApi.lock();
    try {
      // 构建表单数据
      const formData = new FormData();
      formData.append('file', file[0].originFileObj);
      formData.append('updateSupport', values.updateSupport ? 'true' : 'false');

      // 使用 fetch 上传文件
      const accessToken = localStorage.getItem('accessToken') || '';
      const response = await fetch(
        `${import.meta.env.VITE_GLOB_API_URL}/iot/device/import?updateSupport=${values.updateSupport}`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          body: formData,
        },
      );

      const result = await response.json();

      if (result.code !== 0) {
        message.error(result.msg || '导入失败');
        return;
      }

      // 拼接提示语
      const data = result.data;
      let text = `上传成功数量：${data.createDeviceNames?.length || 0};`;
      if (data.createDeviceNames) {
        for (const deviceName of data.createDeviceNames) {
          text += `< ${deviceName} >`;
        }
      }
      text += `更新成功数量：${data.updateDeviceNames?.length || 0};`;
      if (data.updateDeviceNames) {
        for (const deviceName of data.updateDeviceNames) {
          text += `< ${deviceName} >`;
        }
      }
      text += `更新失败数量：${Object.keys(data.failureDeviceNames || {}).length};`;
      if (data.failureDeviceNames) {
        for (const deviceName in data.failureDeviceNames) {
          text += `< ${deviceName}: ${data.failureDeviceNames[deviceName]} >`;
        }
      }
      message.info(text);

      // 关闭并提示
      await modalApi.close();
      emit('success');
    } catch (error: any) {
      message.error(error.message || '导入失败');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (isOpen) {
      // 重置表单
      await formApi.resetForm();
      await formApi.setValues({
        updateSupport: false,
      });
    }
  },
});

/** 下载模板 */
const handleDownloadTemplate = async () => {
  try {
    const res = await importDeviceTemplate();
    downloadFileFromBlobPart({ fileName: '设备导入模版.xls', source: res });
  } catch (error: any) {
    message.error(error.message || '下载失败');
  }
};
</script>

<template>
  <Modal :title="getTitle" class="w-1/3">
    <Form class="mx-4" />
    <div class="mx-4 mt-4 text-center">
      <a class="text-primary cursor-pointer" @click="handleDownloadTemplate">
        下载导入模板
      </a>
    </div>
  </Modal>
</template>
