<script lang="ts" setup>
import type { Reply } from './types';

import type { UploadRawFile } from '#/views/mp/hooks/useUpload';

import { computed, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Button, Col, message, Modal, Row, Upload } from 'ant-design-vue';

import { WxMaterialSelect } from '#/views/mp/components';
import { UploadType, useBeforeUpload } from '#/views/mp/hooks/useUpload';

defineOptions({ name: 'TabImage' });

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
  type: 'image',
});

/** 图片上传前校验 */
function beforeImageUpload(rawFile: UploadRawFile) {
  return useBeforeUpload(UploadType.Image, 2)(rawFile);
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

/** 删除图片 */
function onDelete() {
  reply.value.mediaId = null;
  reply.value.url = null;
  reply.value.name = null;
}

/** 选择素材 */
function selectMaterial(item: any) {
  showDialog.value = false;
  reply.value.mediaId = item.mediaId;
  reply.value.url = item.url;
  reply.value.name = item.name;
}
</script>

<template>
  <div>
    <!-- 情况一：已经选择好素材、或者上传好图片 -->
    <div
      v-if="reply.url"
      class="mx-auto mb-[10px] border border-[#eaeaea] p-[10px]"
    >
      <img class="w-full" :src="reply.url" alt="图片素材" />
      <p
        v-if="reply.name"
        class="overflow-hidden text-ellipsis whitespace-nowrap text-center text-xs"
      >
        {{ reply.name }}
      </p>
      <Row class="pt-[10px] text-center" justify="center">
        <Button danger shape="circle" @click="onDelete">
          <template #icon>
            <IconifyIcon icon="lucide:trash-2" />
          </template>
        </Button>
      </Row>
    </div>

    <!-- 情况二：未做完上述操作 -->
    <Row v-else class="text-center" align="middle">
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
          title="选择图片"
          :width="1200"
          :footer="null"
          destroy-on-close
        >
          <WxMaterialSelect
            type="image"
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
          :before-upload="beforeImageUpload"
          :show-upload-list="false"
        >
          <Button type="primary">
            上传图片
            <template #icon>
              <IconifyIcon icon="lucide:upload" />
            </template>
          </Button>
        </Upload>
        <div class="mt-2 text-center text-xs leading-[18px] text-[#666]">
          支持 bmp/png/jpeg/jpg/gif 格式，大小不超过 2M
        </div>
      </Col>
    </Row>
  </div>
</template>
