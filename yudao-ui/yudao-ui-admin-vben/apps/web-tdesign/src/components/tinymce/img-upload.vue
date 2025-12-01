<script lang="ts" setup>
import type { RequestMethodResponse, UploadFile } from 'tdesign-vue-next';

import { computed, ref } from 'vue';

import { $t } from '@vben/locales';

import { Button, Upload } from 'tdesign-vue-next';

import { useUpload } from '#/components/upload/use-upload';

defineOptions({ name: 'TinymceImageUpload' });

const props = defineProps({
  disabled: {
    default: false,
    type: Boolean,
  },
  fullscreen: {
    // 图片上传，是否放到全屏的位置
    default: false,
    type: Boolean,
  },
});

const emit = defineEmits(['uploading', 'done', 'error']);

const uploading = ref(false);

const getButtonProps = computed(() => {
  const { disabled } = props;
  return {
    disabled,
  };
});

async function customRequest(
  info: UploadFile | UploadFile[],
): Promise<RequestMethodResponse> {
  // 处理单个文件上传
  const uploadFile = Array.isArray(info) ? info[0] : info;

  if (!uploadFile) {
    return {
      status: 'fail',
      error: 'No file provided',
      response: {},
    };
  }

  // 1. emit 上传中
  const file = uploadFile.raw as File;
  const name = file?.name;
  if (!uploading.value) {
    emit('uploading', name);
    uploading.value = true;
  }

  // 2. 执行上传
  const { httpRequest } = useUpload();
  try {
    const url = await httpRequest(file);
    emit('done', name, url);
    uploadFile.onSuccess?.(url);
    return {
      status: 'success',
      response: {
        url,
      },
    };
  } catch (error) {
    emit('error', name);
    uploadFile.onError?.(error);
    return {
      status: 'fail',
      error: error instanceof Error ? error.message : 'Upload failed',
      response: {},
    };
  } finally {
    uploading.value = false;
  }
}
</script>
<template>
  <div :class="[{ fullscreen }]" class="tinymce-image-upload">
    <Upload
      :show-upload-list="false"
      accept=".jpg,.jpeg,.gif,.png,.webp"
      multiple
      :request-method="customRequest"
    >
      <Button theme="primary" v-bind="{ ...getButtonProps }">
        {{ $t('ui.upload.imgUpload') }}
      </Button>
    </Upload>
  </div>
</template>

<style lang="scss" scoped>
.tinymce-image-upload {
  position: absolute;
  top: 4px;
  right: 10px;
  z-index: 20;

  &.fullscreen {
    position: fixed;
    z-index: 10000;
  }
}
</style>
