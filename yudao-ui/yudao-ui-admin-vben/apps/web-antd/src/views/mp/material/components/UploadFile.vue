<script lang="ts" setup>
import type { UploadProps } from 'ant-design-vue';

import type { UploadData } from './upload';

import { inject, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, message, Upload } from 'ant-design-vue';

import {
  beforeImageUpload,
  beforeVoiceUpload,
  HEADERS,
  UPLOAD_URL,
  UploadType,
} from './upload';

const props = defineProps<{ type: UploadType }>();

const emit = defineEmits<{
  uploaded: [v: void];
}>();

const accountId = inject<number>('accountId');

const fileList = ref<any[]>([]);
const uploadData: UploadData = reactive({
  accountId: accountId!,
  introduction: '',
  title: '',
  type: props.type,
});

/** 上传前检查 */
const onBeforeUpload =
  props.type === UploadType.Image ? beforeImageUpload : beforeVoiceUpload;

/** 自定义上传 */
const customRequest: UploadProps['customRequest'] = async function (options) {
  const { file, onError, onSuccess } = options;

  const formData = new FormData();
  formData.append('file', file as File);
  formData.append('type', uploadData.type);
  formData.append('title', uploadData.title);
  formData.append('introduction', uploadData.introduction);
  formData.append('accountId', String(uploadData.accountId));

  try {
    const response = await fetch(UPLOAD_URL, {
      body: formData,
      headers: HEADERS,
      method: 'POST',
    });

    const res = await response.json();

    if (res.code !== 0) {
      message.error(`上传出错：${res.msg}`);
      onError?.(new Error(res.msg));
      return;
    }

    // 清空上传时的各种数据
    fileList.value = [];
    uploadData.title = '';
    uploadData.introduction = '';

    message.success('上传成功');
    onSuccess?.(res);
    emit('uploaded');
  } catch (error: any) {
    message.error(`上传失败: ${error.message}`);
    onError?.(error);
  }
};
</script>

<template>
  <Upload
    :action="UPLOAD_URL"
    :before-upload="onBeforeUpload"
    :custom-request="customRequest"
    :file-list="fileList"
    :headers="HEADERS"
    :multiple="true"
    class="mb-4"
  >
    <Button type="primary">
      <IconifyIcon icon="lucide:upload" class="mr-1" />
      点击上传
    </Button>
    <template #itemRender="{ file, actions }">
      <div class="flex items-center">
        <span>{{ file.name }}</span>
        <Button type="link" size="small" @click="actions.remove"> 删除 </Button>
      </div>
    </template>
  </Upload>
  <div v-if="$slots.default" class="ml-1 text-sm text-gray-500">
    <slot></slot>
  </div>
</template>
