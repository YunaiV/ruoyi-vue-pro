<script lang="ts" setup>
import type { FormInstance, UploadProps } from 'ant-design-vue';

import type { UploadData } from './upload';

import { inject, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Divider,
  Form,
  Input,
  message,
  Modal,
  Upload,
} from 'ant-design-vue';

import { beforeVideoUpload, HEADERS, UPLOAD_URL, UploadType } from './upload';

withDefaults(
  defineProps<{
    open?: boolean;
  }>(),
  {
    open: false,
  },
);

const emit = defineEmits<{
  'update:open': [v: boolean];
  uploaded: [v: void];
}>();

const accountId = inject<number>('accountId');

const uploadRules = {
  introduction: [
    { message: '请输入描述', required: true, trigger: 'blur' } as const,
  ],
  title: [{ message: '请输入标题', required: true, trigger: 'blur' } as const],
};

function handleCancel() {
  emit('update:open', false);
}

const fileList = ref<any[]>([]);

const uploadData: UploadData = reactive({
  accountId: accountId!,
  introduction: '',
  title: '',
  type: UploadType.Video,
});

const uploadFormRef = ref<FormInstance | null>(null);
const uploadVideoRef = ref<any>(null);

async function submitVideo() {
  await uploadFormRef.value?.validate();
  uploadVideoRef.value?.submit();
}

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

    emit('update:open', false);
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
  <Modal
    :open="open"
    title="新建视频"
    width="600px"
    @cancel="handleCancel"
    @ok="submitVideo"
  >
    <Upload
      ref="uploadVideoRef"
      :action="UPLOAD_URL"
      :auto-upload="false"
      :before-upload="beforeVideoUpload"
      :custom-request="customRequest"
      :file-list="fileList"
      :headers="HEADERS"
      :limit="1"
      :multiple="true"
      class="mb-4"
    >
      <Button type="primary">
        <IconifyIcon icon="lucide:video" class="mr-1" />
        选择视频
      </Button>
    </Upload>
    <div class="mb-4 ml-1 text-sm text-gray-500">
      格式支持 MP4，文件大小不超过 10MB
    </div>

    <Divider />

    <Form
      ref="uploadFormRef"
      :model="uploadData"
      :rules="uploadRules"
      layout="vertical"
    >
      <Form.Item label="标题" name="title">
        <Input
          v-model:value="uploadData.title"
          placeholder="标题将展示在相关播放页面，建议填写清晰、准确、生动的标题"
        />
      </Form.Item>
      <Form.Item label="描述" name="introduction">
        <Input.TextArea
          v-model:value="uploadData.introduction"
          :rows="3"
          placeholder="介绍语将展示在相关播放页面，建议填写简洁明确、有信息量的内容"
        />
      </Form.Item>
    </Form>
  </Modal>
</template>
