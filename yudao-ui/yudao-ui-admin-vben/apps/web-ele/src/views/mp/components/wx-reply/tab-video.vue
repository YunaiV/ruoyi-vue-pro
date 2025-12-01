<script lang="ts" setup>
import type { UploadRawFile } from 'element-plus';

import type { Reply } from './types';

import { computed, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import {
  ElButton,
  ElCol,
  ElDialog,
  ElInput,
  ElMessage,
  ElRow,
  ElUpload,
} from 'element-plus';

import { UploadType, useBeforeUpload } from '#/utils/useUpload';
import MaterialSelect from '#/views/mp/components/wx-material-select/wx-material-select.vue';
import VideoPlayer from '#/views/mp/components/wx-video-play/wx-video-play.vue';

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

/** 上传成功 */
function onUploadSuccess(res: any) {
  if (res.code !== 0) {
    ElMessage.error(`上传出错：${res.msg}`);
    return false;
  }

  // 清空上传时的各种数据
  fileList.value = [];
  uploadData.title = '';
  uploadData.introduction = '';

  selectMaterial(res.data);
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
    <ElRow>
      <ElInput v-model="reply.title" class="mb-[2%]" placeholder="请输入标题" />
      <ElInput
        class="mb-[2%]"
        v-model="reply.description"
        placeholder="请输入描述"
      />
      <ElRow class="w-full pt-[10px] text-center" justify="center">
        <VideoPlayer v-if="reply.url" :url="reply.url" />
      </ElRow>
      <ElCol>
        <ElRow class="text-center" align="middle">
          <!-- 选择素材 -->
          <ElCol :span="12">
            <ElButton type="success" @click="showDialog = true">
              素材库选择 <IconifyIcon icon="lucide:circle-check" />
            </ElButton>
            <ElDialog
              title="选择视频"
              v-model="showDialog"
              width="90%"
              append-to-body
              destroy-on-close
            >
              <MaterialSelect
                type="video"
                :account-id="reply.accountId"
                @select-material="selectMaterial"
              />
            </ElDialog>
          </ElCol>
          <!-- 文件上传 -->
          <ElCol :span="12">
            <ElUpload
              :action="UPLOAD_URL"
              :headers="HEADERS"
              multiple
              :limit="1"
              :file-list="fileList"
              :data="uploadData"
              :before-upload="beforeVideoUpload"
              :on-success="onUploadSuccess"
            >
              <ElButton type="primary">
                新建视频 <IconifyIcon icon="lucide:upload" />
              </ElButton>
            </ElUpload>
          </ElCol>
        </ElRow>
      </ElCol>
    </ElRow>
  </div>
</template>
