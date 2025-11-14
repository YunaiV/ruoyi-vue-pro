<script lang="ts" setup>
import type { CSSProperties } from 'vue';

import type { CropperAvatarProps } from './typing';

import { computed, ref, unref, watch, watchEffect } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { ElButton, ElMessage } from 'element-plus';

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
const prefixCls = 'cropper-avatar';
const [CropperModal, modalApi] = useVbenModal({
  connectedComponent: cropperModal,
});

const getClass = computed(() => [prefixCls]);

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
  ElMessage.success($t('ui.cropper.uploadSuccess'));
}

const closeModal = () => modalApi.close();
const openModal = () => modalApi.open();

defineExpose({
  closeModal,
  openModal,
});
</script>

<template>
  <div :class="getClass" :style="getStyle">
    <div
      :class="`${prefixCls}-image-wrapper`"
      :style="getImageWrapperStyle"
      @click="openModal"
    >
      <div :class="`${prefixCls}-image-mask`" :style="getImageWrapperStyle">
        <span
          :style="{
            ...getImageWrapperStyle,
            width: `${getIconWidth}`,
            height: `${getIconWidth}`,
            lineHeight: `${getIconWidth}`,
          }"
          class="icon-[ant-design--cloud-upload-outlined] text-[#d6d6d6]"
        ></span>
      </div>
      <img v-if="sourceValue" :src="sourceValue" alt="avatar" />
    </div>
    <ElButton
      v-if="showBtn"
      :class="`${prefixCls}-upload-btn`"
      @click="openModal"
      v-bind="btnProps"
    >
      {{ btnText ? btnText : $t('ui.cropper.selectImage') }}
    </ElButton>

    <CropperModal
      :size="size"
      :src="sourceValue"
      :upload-api="uploadApi"
      @upload-success="handleUploadSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.cropper-avatar {
  display: inline-block;
  text-align: center;

  &-image-wrapper {
    overflow: hidden;
    cursor: pointer;
    background: #fff;
    border: 1px solid #eee;
    border-radius: 50%;

    img {
      width: 100%;
    }
  }

  &-image-mask {
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    width: inherit;
    height: inherit;
    cursor: pointer;
    background: rgb(0 0 0 / 40%);
    border: inherit;
    border-radius: inherit;
    opacity: 0;
    transition: opacity 0.4s;

    ::v-deep(svg) {
      margin: auto;
    }
  }

  &-image-mask:hover {
    opacity: 40;
  }

  &-upload-btn {
    margin: 10px auto;
  }
}
</style>
