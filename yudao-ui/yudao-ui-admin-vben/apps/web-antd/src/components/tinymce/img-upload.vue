<script lang="ts" setup>
import type { UploadRequestOption } from 'ant-design-vue/lib/vc-upload/interface';

import { computed, ref } from 'vue';

import { $t } from '@vben/locales';

import { Button, Upload } from 'ant-design-vue';

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

async function customRequest(info: UploadRequestOption<any>) {
  // 1. emit 上传中
  const file = info.file as File;
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
  } catch {
    emit('error', name);
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
      :custom-request="customRequest"
    >
      <Button type="primary" v-bind="{ ...getButtonProps }">
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
