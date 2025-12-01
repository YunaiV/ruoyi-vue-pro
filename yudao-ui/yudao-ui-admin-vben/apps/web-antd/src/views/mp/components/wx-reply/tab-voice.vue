<script lang="ts" setup>
import type { Reply } from './types';

import type { UploadRawFile } from '#/views/mp/hooks/useUpload';

import { computed, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Button, Col, message, Modal, Row, Upload } from 'ant-design-vue';

import { WxMaterialSelect, WxVoicePlayer } from '#/views/mp/components';
import { UploadType, useBeforeUpload } from '#/views/mp/hooks/useUpload';

defineOptions({ name: 'TabVoice' });

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
  type: 'voice',
});

/** 语音上传前校验 */
function beforeVoiceUpload(rawFile: UploadRawFile) {
  return useBeforeUpload(UploadType.Voice, 10)(rawFile);
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

    // 上传好的文件，本质是个素材，所以可以进行选中
    selectMaterial(result.data);
    message.success('上传成功');
    onSuccess(result, file);
  } catch (error) {
    message.error('上传失败，请重试');
    onError(error);
  }
}

/** 删除语音 */
function onDelete() {
  reply.value.mediaId = null;
  reply.value.url = null;
  reply.value.name = null;
}

/** 选择素材 */
function selectMaterial(item: Reply) {
  showDialog.value = false;
  reply.value.mediaId = item.mediaId;
  reply.value.url = item.url;
  reply.value.name = item.name;
}
</script>

<template>
  <div>
    <div
      v-if="reply.url"
      class="mx-auto mb-[10px] border border-[#eaeaea] p-[10px]"
    >
      <p
        class="overflow-hidden text-ellipsis whitespace-nowrap text-center text-xs"
      >
        {{ reply.name }}
      </p>
      <Row class="w-full pt-[10px] text-center" justify="center">
        <WxVoicePlayer :url="reply.url" />
      </Row>
      <Row class="w-full pt-[10px] text-center" justify="center">
        <Button danger shape="circle" @click="onDelete">
          <template #icon>
            <IconifyIcon icon="lucide:trash-2" />
          </template>
        </Button>
      </Row>
    </div>

    <Row v-else class="text-center">
      <!-- 选择素材 -->
      <Col
        :span="12"
        class="flex h-[160px] w-[49.5%] flex-col items-center justify-center border border-[#eaeaea] py-[50px]"
      >
        <Button type="primary" @click="showDialog = true">
          素材库选择
          <template #icon>
            <IconifyIcon icon="lucide:circle-check" />
          </template>
        </Button>
        <Modal
          v-model:open="showDialog"
          title="选择语音"
          :width="1200"
          :footer="null"
          destroy-on-close
        >
          <WxMaterialSelect
            type="voice"
            :account-id="reply.accountId"
            @select-material="selectMaterial"
          />
        </Modal>
      </Col>

      <!-- 文件上传 -->
      <Col
        :span="12"
        class="float-right flex h-[160px] w-[49.5%] flex-col items-center justify-center border border-[#eaeaea] py-[50px]"
      >
        <Upload
          :custom-request="customRequest"
          :multiple="true"
          :max-count="1"
          :file-list="fileList"
          :before-upload="beforeVoiceUpload"
          :show-upload-list="false"
        >
          <Button type="primary">
            点击上传
            <template #icon>
              <IconifyIcon icon="lucide:upload" />
            </template>
          </Button>
        </Upload>
        <div class="mt-2 text-center text-xs leading-[18px] text-[#666]">
          格式支持 mp3/wma/wav/amr，文件大小不超过 2M，播放长度不超过 60s
        </div>
      </Col>
    </Row>
  </div>
</template>
