<script lang="ts" setup>
import type { CSSProperties } from 'vue';

import type { CropperAvatarProps } from './typing';

import { computed, ref, unref, watch, watchEffect } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { Button, message } from 'ant-design-vue';

import cropperModal from './cropper-modal.vue';

defineOptions({ name: 'CropperAvatar' });

const props = withDefaults(defineProps<CropperAvatarProps>(), {
  width: 200,
  value: '',
  showBtn: true,
  btnProps: () => ({}),
  btnText: '',
  uploadApi: () => Promise.resolve(),
  size: 5,
});

const emit = defineEmits(['update:value', 'change']);

const sourceValue = ref(props.value || '');
const [CropperModal, modalApi] = useVbenModal({
  connectedComponent: cropperModal,
});

const getWidth = computed(() => `${`${props.width}`.replace(/px/, '')}px`);

const getIconWidth = computed(
  () => `${Number.parseInt(`${props.width}`.replace(/px/, '')) / 2}px`,
);

const getStyle = computed((): CSSProperties => ({ width: unref(getWidth) }));

const getImageWrapperStyle = computed(
  (): CSSProperties => ({ height: unref(getWidth), width: unref(getWidth) }),
);

watchEffect(() => {
  sourceValue.value = props.value || '';
});

watch(
  () => sourceValue.value,
  (v: string) => {
    emit('update:value', v);
  },
);

function handleUploadSuccess({ data, source }: any) {
  sourceValue.value = source;
  emit('change', { data, source });
  message.success($t('ui.cropper.uploadSuccess'));
}

const closeModal = () => modalApi.close();
const openModal = () => modalApi.open();

defineExpose({
  closeModal,
  openModal,
});
</script>

<template>
  <!-- 头像容器 -->
  <div class="inline-block text-center" :style="getStyle">
    <!-- 图片包装器 -->
    <div
      class="bg-card group relative cursor-pointer overflow-hidden rounded-full border border-gray-200"
      :style="getImageWrapperStyle"
      @click="openModal"
    >
      <!-- 遮罩层 -->
      <div
        class="duration-400 absolute inset-0 flex cursor-pointer items-center justify-center rounded-full bg-black bg-opacity-40 opacity-0 transition-opacity group-hover:opacity-100"
        :style="getImageWrapperStyle"
      >
        <IconifyIcon
          icon="lucide:cloud-upload"
          class="m-auto text-gray-400"
          :style="{
            ...getImageWrapperStyle,
            width: getIconWidth,
            height: getIconWidth,
            lineHeight: getIconWidth,
          }"
        />
      </div>
      <!-- 头像图片 -->
      <img
        v-if="sourceValue"
        :src="sourceValue"
        alt="avatar"
        class="h-full w-full object-cover"
      />
    </div>
    <!-- 上传按钮 -->
    <Button
      v-if="showBtn"
      class="mx-auto mt-2"
      @click="openModal"
      v-bind="btnProps"
    >
      {{ btnText ? btnText : $t('ui.cropper.selectImage') }}
    </Button>

    <CropperModal
      :size="size"
      :src="sourceValue"
      :upload-api="uploadApi"
      @upload-success="handleUploadSuccess"
    />
  </div>
</template>
