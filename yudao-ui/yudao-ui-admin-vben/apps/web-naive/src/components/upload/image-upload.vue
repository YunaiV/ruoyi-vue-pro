<script lang="ts" setup>
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui';

import type { FileUploadProps } from './typing';

import type { AxiosProgressEvent } from '#/api/infra/file';

import { computed, ref, toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import {
  defaultImageAccepts,
  isFunction,
  isImage,
  isObject,
  isString,
} from '@vben/utils';

import { NImage, NImageGroup, NModal, NUpload, useMessage } from 'naive-ui';

import { useUpload, useUploadType } from './use-upload';

defineOptions({ name: 'ImageUpload', inheritAttrs: false });

const props = withDefaults(defineProps<FileUploadProps>(), {
  value: () => [],
  modelValue: undefined,
  directory: undefined,
  disabled: false,
  listType: 'picture-card',
  helpText: '',
  maxSize: 2,
  maxNumber: 1,
  accept: () => defaultImageAccepts,
  multiple: false,
  api: undefined,
  resultField: '',
  showDescription: true,
});

const emit = defineEmits([
  'change',
  'update:value',
  'update:modelValue',
  'delete',
]);

const { accept, helpText, maxNumber, maxSize } = toRefs(props);
const message = useMessage();
const isInnerOperate = ref<boolean>(false);
const { getStringAccept } = useUploadType({
  acceptRef: accept,
  helpTextRef: helpText,
  maxNumberRef: maxNumber,
  maxSizeRef: maxSize,
});

/** 计算当前绑定的值，优先使用 modelValue */
const currentValue = computed(() => {
  return props.modelValue === undefined ? props.value : props.modelValue;
});

/** 判断是否使用 modelValue */
const isUsingModelValue = computed(() => {
  return props.modelValue !== undefined;
});

const previewOpen = ref<boolean>(false); // 是否展示预览
const previewImage = ref<string>(''); // 预览图片
const previewTitle = ref<string>(''); // 预览标题

const fileList = ref<UploadFileInfo[]>([]);
const isLtMsg = ref<boolean>(true); // 文件大小错误提示
const isActMsg = ref<boolean>(true); // 文件类型错误提示
const isFirstRender = ref<boolean>(true); // 是否第一次渲染
const uploadNumber = ref<number>(0); // 上传文件计数器
const uploadList = ref<any[]>([]); // 临时上传列表

watch(
  currentValue,
  async (v) => {
    if (isInnerOperate.value) {
      isInnerOperate.value = false;
      return;
    }
    let value: string | string[] = [];
    if (v) {
      if (Array.isArray(v)) {
        value = v;
      } else {
        value.push(v);
      }
      fileList.value = value
        .map((item, i) => {
          if (item && isString(item)) {
            return {
              id: `${-i}`,
              name: item.slice(Math.max(0, item.lastIndexOf('/') + 1)),
              status: 'finished',
              url: item,
            } as UploadFileInfo;
          } else if (item && isObject(item)) {
            return item as unknown as UploadFileInfo;
          }
          return null;
        })
        .filter((item) => item !== null) as UploadFileInfo[];
    }
    if (!isFirstRender.value) {
      emit('change', value);
      isFirstRender.value = false;
    }
  },
  {
    immediate: true,
    deep: true,
  },
);

/** 预览图片 */
async function handlePreview(file: UploadFileInfo) {
  previewImage.value = file.url || '';
  previewOpen.value = true;
  previewTitle.value = file.name || '';
}

/** 移除文件 */
function handleRemove(options: {
  file: UploadFileInfo;
  fileList: UploadFileInfo[];
}) {
  const file = options.file;
  const index = fileList.value.findIndex((item) => item.id === file.id);
  if (index !== -1) {
    fileList.value.splice(index, 1);
    const value = getValue();
    isInnerOperate.value = true;
    emit('update:value', value);
    emit('update:modelValue', value);
    emit('change', value);
    emit('delete', file);
  }
}

/** 上传前校验 */
function beforeUpload(options: {
  file: UploadFileInfo;
  fileList: UploadFileInfo[];
}) {
  const file = options.file.file as File;

  // 检查文件数量限制
  if (fileList.value.length >= props.maxNumber) {
    message.error($t('ui.upload.maxNumber', [props.maxNumber]));
    return false;
  }

  const { maxSize, accept } = props;
  const isAct = isImage(file.name, accept);
  if (!isAct) {
    message.error($t('ui.upload.acceptUpload', [accept]));
    isActMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isActMsg.value = true), 1000);
    return false;
  }
  const isLt = file.size / 1024 / 1024 > maxSize;
  if (isLt) {
    message.error($t('ui.upload.maxSizeMultiple', [maxSize]));
    isLtMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isLtMsg.value = true), 1000);
    return false;
  }

  // 只有在验证通过后才增加计数器
  uploadNumber.value++;
  return true;
}

/** 自定义上传 */
async function customRequest(options: UploadCustomRequestOptions) {
  let { api } = props;
  if (!api || !isFunction(api)) {
    api = useUpload(props.directory).httpRequest;
  }
  try {
    // 上传文件
    const progressEvent: AxiosProgressEvent = (e) => {
      const percent = Math.trunc((e.loaded / e.total!) * 100);
      options.onProgress?.({ percent });
    };
    const res = await api?.(options.file.file as File, progressEvent);

    // 处理上传成功后的逻辑
    handleUploadSuccess(res, options.file);

    options.onFinish();
    message.success($t('ui.upload.uploadSuccess'));
  } catch (error: any) {
    console.error(error);
    options.onError();
    handleUploadError(error);
  }
}

/** 处理上传成功 */
function handleUploadSuccess(res: any, file: UploadFileInfo) {
  // 删除临时文件
  const index = fileList.value?.findIndex((item) => item.name === file.name);
  if (index !== -1) {
    fileList.value?.splice(index!, 1);
  }

  // 添加到临时上传列表
  const fileUrl = res?.url || res?.data || res;
  uploadList.value.push({
    id: file.id,
    name: file.name,
    url: fileUrl,
    status: 'finished',
  });

  // 检查是否所有文件都上传完成
  if (uploadList.value.length >= uploadNumber.value) {
    fileList.value?.push(...uploadList.value);
    uploadList.value = [];
    uploadNumber.value = 0;

    // 更新值
    const value = getValue();
    isInnerOperate.value = true;
    emit('update:value', value);
    emit('update:modelValue', value);
    emit('change', value);
  }
}

/** 处理上传错误 */
function handleUploadError(error: any) {
  console.error('上传错误:', error);
  message.error($t('ui.upload.uploadError'));
  // 上传失败时减少计数器
  uploadNumber.value = Math.max(0, uploadNumber.value - 1);
}

/** 获取值 */
function getValue() {
  const list = (fileList.value || [])
    .filter((item) => item?.status === 'finished')
    .map((item: any) => {
      if (item?.response && props?.resultField) {
        return item?.response;
      }
      return item?.url || item?.response?.url || item?.response;
    });

  // 单个文件的情况，根据输入参数类型决定返回格式
  if (props.maxNumber === 1) {
    const singleValue = list.length > 0 ? list[0] : '';
    // 如果原始值是字符串或 modelValue 是字符串，返回字符串
    if (
      isString(props.value) ||
      (isUsingModelValue.value && isString(props.modelValue))
    ) {
      return singleValue;
    }
    return singleValue;
  }

  // 多文件情况，根据输入参数类型决定返回格式
  if (isUsingModelValue.value) {
    return Array.isArray(props.modelValue) ? list : list.join(',');
  }

  return Array.isArray(props.value) ? list : list.join(',');
}

/** 处理文件列表变化 */
function handleChange() {
  // 移除操作已经在 handleRemove 中处理
}
</script>

<template>
  <div>
    <NUpload
      v-bind="$attrs"
      v-model:file-list="fileList"
      :accept="getStringAccept"
      :custom-request="customRequest"
      :disabled="disabled"
      list-type="image-card"
      :max="maxNumber"
      :multiple="multiple"
      :show-preview-button="true"
      :show-remove-button="true"
      @before-upload="beforeUpload"
      @change="handleChange"
      @preview="handlePreview"
      @remove="handleRemove"
    >
      <div
        v-if="fileList && fileList.length < maxNumber"
        class="flex flex-col items-center justify-center p-2"
      >
        <IconifyIcon icon="lucide:cloud-upload" class="text-2xl" />
        <div class="mt-2 text-sm">{{ $t('ui.upload.imgUpload') }}</div>
      </div>
    </NUpload>
    <div
      v-if="showDescription"
      class="mt-2 flex flex-wrap items-center text-sm text-gray-600"
    >
      请上传不超过
      <div class="mx-1 font-bold text-primary">{{ maxSize }}MB</div>
      的
      <div class="mx-1 font-bold text-primary">{{ accept.join('/') }}</div>
      格式文件
    </div>
    <NModal
      v-model:show="previewOpen"
      :title="previewTitle"
      preset="card"
      class="w-[600px]"
    >
      <NImageGroup>
        <NImage :src="previewImage" alt="" class="w-full" />
      </NImageGroup>
    </NModal>
  </div>
</template>

<style scoped>
:deep(.n-upload-trigger) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
