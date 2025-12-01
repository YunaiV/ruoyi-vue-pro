<script lang="ts" setup>
import type { Reply } from './types';

import type { UploadRawFile } from '#/views/mp/hooks/useUpload';

import { computed, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import {
  Button,
  Col,
  Input,
  message,
  Modal,
  Row,
  Upload,
} from 'ant-design-vue';

import { WxMaterialSelect, WxVideoPlayer } from '#/views/mp/components';
import { UploadType, useBeforeUpload } from '#/views/mp/hooks/useUpload';

defineOptions({ name: 'TabVideo' });

const props = defineProps<{
  modelValue: Reply;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: Reply): void;
}>();

const accessStore = useAccessStore();
const UPLOAD_URL = `${import.meta.env.VITE_BASE_URL}/admin-api/mp/material/upload-temporary`;
const HEADERS = { Authorization: `Bearer ${accessStore.accessToken}` };

const reply = computed<Reply>({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

const showDialog = ref(false);
const fileList = ref([]);
const uploadData = reactive({
  accountId: reply.value.accountId,
  introduction: '',
  title: '',
  type: 'video',
});

/** 视频上传前校验 */
function beforeVideoUpload(rawFile: UploadRawFile) {
  return useBeforeUpload(UploadType.Video, 10)(rawFile);
}

/** 自定义上传请求 */
async function customRequest(options: any) {
  const { file, onSuccess, onError } = options;

  const formData = new FormData();
  formData.append('file', file);
  formData.append('accountId', String(uploadData.accountId));
  formData.append('type', uploadData.type);
  formData.append('title', uploadData.title);
  formData.append('introduction', uploadData.introduction);

  try {
    const response = await fetch(UPLOAD_URL, {
      method: 'POST',
      headers: HEADERS,
      body: formData,
    });

    const result = await response.json();

    if (result.code !== 0) {
      message.error(result.msg || '上传出错');
      onError(new Error(result.msg || '上传失败'));
      return;
    }
    // 清空上传时的各种数据
    fileList.value = [];
    uploadData.title = '';
    uploadData.introduction = '';

    // 选择素材
    selectMaterial(result.data);
    message.success('上传成功');
    onSuccess(result, file);
  } catch (error) {
    message.error('上传失败，请重试');
    onError(error);
  }
}

/** 选择素材后设置 */
function selectMaterial(item: any) {
  showDialog.value = false;

  reply.value.mediaId = item.mediaId;
  reply.value.url = item.url;
  reply.value.name = item.name;

  // title、introduction：从 item 到 tempObjItem，因为素材里有 title、introduction
  if (item.title) {
    reply.value.title = item.title || '';
  }
  if (item.introduction) {
    reply.value.description = item.introduction || '';
  }
}
</script>

<template>
  <div>
    <Row :gutter="[0, 16]">
      <Col :span="24">
        <Input
          :value="reply.title || undefined"
          placeholder="请输入标题"
          @update:value="(val) => (reply.title = val || null)"
        />
      </Col>
      <Col :span="24">
        <Input
          :value="reply.description || undefined"
          placeholder="请输入描述"
          @update:value="(val) => (reply.description = val || null)"
        />
      </Col>
      <Col :span="24">
        <Row class="w-full pt-[10px] text-center" justify="center">
          <WxVideoPlayer v-if="reply.url" :url="reply.url" />
        </Row>
      </Col>
      <Col :span="24">
        <Row class="text-center" align="middle">
          <!-- 选择素材 -->
          <Col :span="12">
            <Button type="primary" @click="showDialog = true">
              素材库选择
              <template #icon>
                <IconifyIcon icon="lucide:circle-check" />
              </template>
            </Button>
            <Modal
              v-model:open="showDialog"
              title="选择视频"
              :width="1200"
              :footer="null"
              destroy-on-close
            >
              <WxMaterialSelect
                type="video"
                :account-id="reply.accountId"
                @select-material="selectMaterial"
              />
            </Modal>
          </Col>

          <!-- 文件上传 -->
          <Col :span="12">
            <Upload
              :custom-request="customRequest"
              :multiple="true"
              :max-count="1"
              :file-list="fileList"
              :before-upload="beforeVideoUpload"
              :show-upload-list="false"
            >
              <Button type="primary">
                新建视频
                <template #icon>
                  <IconifyIcon icon="lucide:upload" />
                </template>
              </Button>
            </Upload>
          </Col>
        </Row>
      </Col>
    </Row>
  </div>
</template>
