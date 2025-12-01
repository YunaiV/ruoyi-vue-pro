<script lang="ts" setup>
import type { UploadFile } from 'ant-design-vue';

import type { MpDraftApi } from '#/api/mp/draft';

import { computed, inject, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Button, Image, message, Modal, Upload } from 'ant-design-vue';

import { UploadType, useBeforeUpload } from '#/utils/useUpload';
import { WxMaterialSelect } from '#/views/mp/components/';

const props = defineProps<{
  isFirst: boolean;
  modelValue: MpDraftApi.NewsItem;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: MpDraftApi.NewsItem): void;
}>();

const UPLOAD_URL = `${import.meta.env.VITE_BASE_URL}/admin-api/mp/material/upload-permanent`; // 上传永久素材的地址
const HEADERS = { Authorization: `Bearer ${useAccessStore().accessToken}` };
const newsItem = computed<MpDraftApi.NewsItem>({
  get() {
    return props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val);
  },
});

const accountId = inject<number>('accountId');
const dialogVisible = ref(false);

const fileList = ref<UploadFile[]>([]);
interface UploadData {
  type: UploadType;
  accountId: number;
}
const uploadData: UploadData = reactive({
  type: UploadType.Image,
  accountId: accountId!,
});

function handleOpenDialog() {
  dialogVisible.value = true;
}

/** 素材选择完成事件 */
function onMaterialSelected(item: any) {
  dialogVisible.value = false;
  newsItem.value.thumbMediaId = item.mediaId;
  newsItem.value.thumbUrl = item.url;
}

/** 上传前校验 */
const onBeforeUpload = (file: UploadFile) =>
  useBeforeUpload(UploadType.Image, 2)(file as any);

/** 上传错误处理 */
function onUploadChange(info: any) {
  if (info.file.status === 'error') {
    onUploadError(info.file.error || new Error('上传失败'));
  }
}

/** 上传成功处理 */
function onUploadSuccess(res: any) {
  if (res.code !== 0) {
    message.error(`上传出错：${res.msg}`);
    return false;
  }

  // 重置上传文件的表单
  fileList.value = [];
  // 设置草稿的封面字段
  newsItem.value.thumbMediaId = res.data.mediaId;
  newsItem.value.thumbUrl = res.data.url;
}

/** 上传失败处理 */
function onUploadError(err: Error) {
  message.error(`上传失败: ${err.message}`);
}
</script>

<template>
  <div>
    <p>封面:</p>
    <div class="flex w-full flex-col items-center justify-center text-center">
      <Image
        v-if="newsItem.thumbUrl"
        class="max-h-[300px] w-[300px]"
        :src="newsItem.thumbUrl"
        :preview="false"
      />
      <IconifyIcon
        v-else
        icon="lucide:plus"
        class="border border-[#d9d9d9] text-center text-[28px] leading-[120px] text-[#8c939d]"
        :class="isFirst ? 'h-[120px] w-[230px]' : 'h-[120px] w-[120px]'"
      />
      <div class="m-[5px]">
        <div class="flex items-center justify-center">
          <Upload
            :action="UPLOAD_URL"
            :headers="HEADERS"
            :file-list="fileList"
            :data="{ ...uploadData }"
            :before-upload="onBeforeUpload"
            @success="onUploadSuccess"
            @change="onUploadChange"
          >
            <template #default>
              <Button size="small" type="primary">本地上传</Button>
            </template>
          </Upload>
          <Button
            size="small"
            type="primary"
            class="ml-[5px]"
            @click="handleOpenDialog"
          >
            素材库选择
          </Button>
        </div>

        <div class="ml-[5px] mt-[5px] text-xs text-[#999]">
          支持 bmp/png/jpeg/jpg/gif 格式，大小不超过 2M
        </div>
      </div>
      <Modal
        v-model:open="dialogVisible"
        title="图片选择"
        width="65%"
        :footer="null"
      >
        <WxMaterialSelect
          type="image"
          :account-id="accountId!"
          @select-material="onMaterialSelected"
        />
      </Modal>
    </div>
  </div>
</template>
