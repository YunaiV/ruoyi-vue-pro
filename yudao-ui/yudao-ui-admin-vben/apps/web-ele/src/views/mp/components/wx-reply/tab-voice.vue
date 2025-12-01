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
  ElMessage,
  ElRow,
  ElUpload,
} from 'element-plus';

import { UploadType, useBeforeUpload } from '#/utils/useUpload';
import MaterialSelect from '#/views/mp/components/wx-material-select/wx-material-select.vue';
import VoicePlayer from '#/views/mp/components/wx-voice-play/wx-voice-play.vue';

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

/** 删除语音 */
function onDelete() {
  reply.value.mediaId = null;
  reply.value.url = null;
  reply.value.name = null;
}

/** 选择素材 */
function selectMaterial(item: Reply) {
  showDialog.value = false;

  // reply.value.type = ReplyType.Voice
  reply.value.mediaId = item.mediaId;
  reply.value.url = item.url;
  reply.value.name = item.name;
}
</script>
<template>
  <div>
    <div
      class="mx-auto mb-[10px] border border-[#eaeaea] p-[10px]"
      v-if="reply.url"
    >
      <p
        class="overflow-hidden text-ellipsis whitespace-nowrap text-center text-xs"
      >
        {{ reply.name }}
      </p>
      <ElRow class="w-full pt-[10px] text-center" justify="center">
        <VoicePlayer :url="reply.url" />
      </ElRow>
      <ElRow class="w-full pt-[10px] text-center" justify="center">
        <ElButton type="danger" circle @click="onDelete">
          <IconifyIcon icon="lucide:trash-2" />
        </ElButton>
      </ElRow>
    </div>
    <ElRow v-else class="text-center">
      <!-- 选择素材 -->
      <ElCol
        :span="12"
        class="h-[160px] w-[49.5%] border border-[rgb(234,234,234)] py-[50px]"
      >
        <ElButton type="success" @click="showDialog = true">
          素材库选择<IconifyIcon icon="lucide:circle-check" />
        </ElButton>
        <ElDialog
          title="选择语音"
          v-model="showDialog"
          width="90%"
          append-to-body
          destroy-on-close
        >
          <MaterialSelect
            type="voice"
            :account-id="reply.accountId"
            @select-material="selectMaterial"
          />
        </ElDialog>
      </ElCol>
      <!-- 文件上传 -->
      <ElCol
        :span="12"
        class="float-right h-[160px] w-[49.5%] border border-[rgb(234,234,234)] py-[50px]"
      >
        <ElUpload
          :action="UPLOAD_URL"
          :headers="HEADERS"
          multiple
          :limit="1"
          :file-list="fileList"
          :data="uploadData"
          :before-upload="beforeVoiceUpload"
          :on-success="onUploadSuccess"
        >
          <ElButton type="primary">点击上传</ElButton>
          <template #tip>
            <div class="text-center leading-[18px]">
              格式支持 mp3/wma/wav/amr，文件大小不超过 2M，播放长度不超过 60s
            </div>
          </template>
        </ElUpload>
      </ElCol>
    </ElRow>
  </div>
</template>
