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

defineOptions({ name: 'TabMusic' });

const props = defineProps<{
  modelValue: Reply;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: Reply): void;
}>();

const UPLOAD_URL = `${import.meta.env.VITE_BASE_URL}/admin-api/mp/material/upload-temporary`;
const HEADERS = { Authorization: `Bearer ${useAccessStore().accessToken}` };
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
  type: 'thumb', // 音乐类型为 thumb
});

/** 图片上传前校验 */
function beforeImageUpload(rawFile: UploadRawFile) {
  return useBeforeUpload(UploadType.Image, 2)(rawFile);
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

  // 上传好的文件，本质是个素材，所以可以进行选中
  selectMaterial(res.data);
}

/** 选择素材 */
function selectMaterial(item: any) {
  showDialog.value = false;
  reply.value.thumbMediaId = item.mediaId;
  reply.value.thumbMediaUrl = item.url;
}
</script>

<template>
  <div>
    <ElRow align="middle" justify="center">
      <ElCol :span="6">
        <ElRow align="middle" justify="center" class="inline-block text-center">
          <ElCol :span="24">
            <ElRow align="middle" justify="center">
              <img
                class="w-[100px]"
                v-if="reply.thumbMediaUrl"
                :src="reply.thumbMediaUrl"
              />
              <IconifyIcon v-else icon="lucide:plus" />
            </ElRow>
            <ElRow align="middle" justify="center" class="mt-[2%]">
              <div>
                <ElUpload
                  :action="UPLOAD_URL"
                  :headers="HEADERS"
                  multiple
                  :limit="1"
                  :file-list="fileList"
                  :data="uploadData"
                  :before-upload="beforeImageUpload"
                  :on-success="onUploadSuccess"
                >
                  <template #trigger>
                    <ElButton type="primary" link>本地上传</ElButton>
                  </template>
                  <ElButton
                    type="primary"
                    link
                    @click="showDialog = true"
                    class="ml-[5px]"
                  >
                    素材库选择
                  </ElButton>
                </ElUpload>
              </div>
            </ElRow>
          </ElCol>
        </ElRow>
        <ElDialog
          title="选择图片"
          v-model="showDialog"
          width="80%"
          append-to-body
          destroy-on-close
        >
          <MaterialSelect
            type="image"
            :account-id="reply.accountId"
            @select-material="selectMaterial"
          />
        </ElDialog>
      </ElCol>
      <ElCol :span="18">
        <ElInput v-model="reply.title" placeholder="请输入标题" />
        <div class="my-5"></div>
        <ElInput v-model="reply.description" placeholder="请输入描述" />
      </ElCol>
    </ElRow>
    <div class="my-5"></div>
    <ElInput v-model="reply.musicUrl" placeholder="请输入音乐链接" />
    <div class="my-5"></div>
    <ElInput v-model="reply.hqMusicUrl" placeholder="请输入高质量音乐链接" />
  </div>
</template>
