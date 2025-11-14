<script lang="ts" setup>
import type { CropendResult, CropperModalProps, CropperType } from './typing';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { dataURLtoBlob, isFunction } from '@vben/utils';

import {
  Avatar,
  Button,
  message,
  Space,
  Tooltip,
  Upload,
} from 'ant-design-vue';

import CropperImage from './cropper.vue';

defineOptions({ name: 'CropperModal' });

const props = withDefaults(defineProps<CropperModalProps>(), {
  circled: true,
  size: 0,
  src: '',
  uploadApi: () => Promise.resolve(),
});

const emit = defineEmits(['uploadSuccess', 'uploadError', 'register']);

let filename = '';
const src = ref(props.src || '');
const previewSource = ref('');
const cropper = ref<CropperType>();
let scaleX = 1;
let scaleY = 1;

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleOk,
  onOpenChange(isOpen) {
    if (isOpen) {
      // 打开时，进行 loading 加载。后续 CropperImage 组件加载完毕，会自动关闭 loading（通过 handleReady）
      modalLoading(true);
      const img = new Image();
      img.src = src.value;
      img.addEventListener('load', () => {
        modalLoading(false);
      });
      img.addEventListener('error', () => {
        modalLoading(false);
      });
    } else {
      // 关闭时，清空右侧预览
      previewSource.value = '';
      modalLoading(false);
    }
  },
});

function modalLoading(loading: boolean) {
  modalApi.setState({ confirmLoading: loading, loading });
}

// Block upload
function handleBeforeUpload(file: File) {
  if (props.size > 0 && file.size > 1024 * 1024 * props.size) {
    emit('uploadError', { msg: $t('ui.cropper.imageTooBig') });
    return false;
  }
  const reader = new FileReader();
  reader.readAsDataURL(file);
  src.value = '';
  previewSource.value = '';
  reader.addEventListener('load', (e) => {
    src.value = (e.target?.result as string) ?? '';
    filename = file.name;
  });
  return false;
}

function handleCropend({ imgBase64 }: CropendResult) {
  previewSource.value = imgBase64;
}

function handleReady(cropperInstance: CropperType) {
  cropper.value = cropperInstance;
  // 画布加载完毕 关闭 loading
  modalLoading(false);
}

function handlerToolbar(event: string, arg?: number) {
  if (event === 'scaleX') {
    scaleX = arg = scaleX === -1 ? 1 : -1;
  }
  if (event === 'scaleY') {
    scaleY = arg = scaleY === -1 ? 1 : -1;
  }
  (cropper?.value as any)?.[event]?.(arg);
}

async function handleOk() {
  const uploadApi = props.uploadApi;
  if (uploadApi && isFunction(uploadApi)) {
    if (!previewSource.value) {
      message.warn('未选择图片');
      return;
    }
    const blob = dataURLtoBlob(previewSource.value);
    try {
      modalLoading(true);
      const url = await uploadApi({ file: blob, filename, name: 'file' });
      emit('uploadSuccess', { data: url, source: previewSource.value });
      await modalApi.close();
    } finally {
      modalLoading(false);
    }
  }
}
</script>

<template>
  <Modal
    v-bind="$attrs"
    :confirm-text="$t('ui.cropper.okText')"
    :fullscreen-button="false"
    :title="$t('ui.cropper.modalTitle')"
    class="w-2/3"
  >
    <div class="flex h-96">
      <!-- 左侧区域 -->
      <div class="h-full w-3/5">
        <!-- 裁剪器容器 -->
        <div
          class="relative h-[300px] bg-gradient-to-b from-neutral-50 to-neutral-200"
        >
          <CropperImage
            v-if="src"
            :circled="circled"
            :src="src"
            height="300px"
            @cropend="handleCropend"
            @ready="handleReady"
          />
        </div>

        <!-- 工具栏 -->
        <div class="mt-4 flex items-center justify-between">
          <Upload
            :before-upload="handleBeforeUpload"
            :file-list="[]"
            accept="image/*"
          >
            <Tooltip :title="$t('ui.cropper.selectImage')" placement="bottom">
              <Button size="small" type="primary">
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="lucide:upload" />
                  </div>
                </template>
              </Button>
            </Tooltip>
          </Upload>
          <Space>
            <Tooltip :title="$t('ui.cropper.btn_reset')" placement="bottom">
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('reset')"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="lucide:rotate-ccw" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip
              :title="$t('ui.cropper.btn_rotate_left')"
              placement="bottom"
            >
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('rotate', -45)"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="ant-design:rotate-left-outlined" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip
              :title="$t('ui.cropper.btn_rotate_right')"
              placement="bottom"
            >
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('rotate', 45)"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="ant-design:rotate-right-outlined" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip :title="$t('ui.cropper.btn_scale_x')" placement="bottom">
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('scaleX')"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="vaadin:arrows-long-h" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip :title="$t('ui.cropper.btn_scale_y')" placement="bottom">
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('scaleY')"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="vaadin:arrows-long-v" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip :title="$t('ui.cropper.btn_zoom_in')" placement="bottom">
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('zoom', 0.1)"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="lucide:zoom-in" />
                  </div>
                </template>
              </Button>
            </Tooltip>
            <Tooltip :title="$t('ui.cropper.btn_zoom_out')" placement="bottom">
              <Button
                :disabled="!src"
                size="small"
                type="primary"
                @click="handlerToolbar('zoom', -0.1)"
              >
                <template #icon>
                  <div class="flex items-center justify-center">
                    <IconifyIcon icon="lucide:zoom-out" />
                  </div>
                </template>
              </Button>
            </Tooltip>
          </Space>
        </div>
      </div>

      <!-- 右侧区域 -->
      <div class="h-full w-2/5">
        <!-- 预览区域 -->
        <div
          class="mx-auto h-56 w-56 overflow-hidden rounded-full border border-gray-200"
        >
          <img
            v-if="previewSource"
            :alt="$t('ui.cropper.preview')"
            :src="previewSource"
            class="h-full w-full object-cover"
          />
        </div>

        <!-- 头像组合预览 -->
        <template v-if="previewSource">
          <div
            class="mt-2 flex items-center justify-around border-t border-gray-200 pt-2"
          >
            <Avatar :src="previewSource" size="large" />
            <Avatar :size="48" :src="previewSource" />
            <Avatar :size="64" :src="previewSource" />
            <Avatar :size="80" :src="previewSource" />
          </div>
        </template>
      </div>
    </div>
  </Modal>
</template>
