<script lang="ts" setup>
import type { UploadRequestOptions } from 'element-plus';

import { computed, ref } from 'vue';

import { $t } from '@vben/locales';

import { ElButton, ElUpload } from 'element-plus';

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

async function customRequest(options: UploadRequestOptions) {
  // 1. emit 上传中
  const file = options.file as File;
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
    // 调用上传成功回调
    options?.onSuccess(url);
  } catch (error: any) {
    emit('error', name);
    // 调用上传失败回调
    options?.onError(error);
  } finally {
    uploading.value = false;
  }
}
</script>
<template>
  <div :class="[{ fullscreen }]" class="tinymce-image-upload">
    <ElUpload
      :show-file-list="false"
      accept=".jpg,.jpeg,.gif,.png,.webp"
      multiple
      :http-request="customRequest"
    >
      <ElButton type="primary" v-bind="{ ...getButtonProps }">
        {{ $t('ui.upload.imgUpload') }}
      </ElButton>
    </ElUpload>
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
