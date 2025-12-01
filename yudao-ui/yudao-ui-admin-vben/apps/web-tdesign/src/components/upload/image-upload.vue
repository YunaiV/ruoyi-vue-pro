<script lang="ts" setup>
import type { UploadFile, UploadProps } from 'tdesign-vue-next';

import type { FileUploadProps } from './typing';

import type { AxiosProgressEvent } from '#/api/infra/file';

import { computed, ref, toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import {
  defaultImageAccepts,
  isFunction,
  isImage,
  isNumber,
  isObject,
  isString,
} from '@vben/utils';

import { Dialog, Upload } from 'tdesign-vue-next';

import { message } from '#/adapter/tdesign';

import { UploadResultStatus } from './typing';
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
const isInnerOperate = ref<boolean>(false);
const { getStringAccept } = useUploadType({
  acceptRef: accept,
  helpTextRef: helpText,
  maxNumberRef: maxNumber,
  maxSizeRef: maxSize,
});

// 计算当前绑定的值，优先使用 modelValue
const currentValue = computed(() => {
  return props.modelValue === undefined ? props.value : props.modelValue;
});

// 判断是否使用 modelValue
const isUsingModelValue = computed(() => {
  return props.modelValue !== undefined;
});
const previewOpen = ref<boolean>(false); // 是否展示预览
const previewImage = ref<string>(''); // 预览图片
const previewTitle = ref<string>(''); // 预览标题

const fileList = ref<UploadProps['files']>([]);
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
      fileList.value = value.map((item, i) => {
        if (item && isString(item)) {
          return {
            uid: `${-i}`,
            name: item.slice(Math.max(0, item.lastIndexOf('/') + 1)),
            status: UploadResultStatus.SUCCESS,
            url: item,
          };
        } else if (item && isObject(item)) {
          return item;
        }
        return null;
      }) as UploadProps['files'];
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

function getBase64<T extends ArrayBuffer | null | string>(file: File) {
  return new Promise<T>((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.addEventListener('load', () => {
      resolve(reader.result as T);
    });
    reader.addEventListener('error', (error) => reject(error));
  });
}

async function handlePreview(file: UploadFile) {
  if (!file.url && !file.preview) {
    // TDesign 使用 raw 而不是 originFileObj
    file.preview = await getBase64<string>(file.raw!);
  }
  previewImage.value = file.url || file.preview || '';
  previewOpen.value = true;
  previewTitle.value =
    file.name ||
    previewImage.value.slice(
      Math.max(0, previewImage.value.lastIndexOf('/') + 1),
    );
}

async function handleRemove(file: UploadFile) {
  if (fileList.value) {
    const index = fileList.value.findIndex((item) => item.uid === file.uid);
    index !== -1 && fileList.value.splice(index, 1);
    const value = getValue();
    isInnerOperate.value = true;
    emit('update:value', value);
    emit('update:modelValue', value);
    emit('change', value);
    emit('delete', file);
  }
}

function handleCancel() {
  previewOpen.value = false;
  previewTitle.value = '';
}

async function beforeUpload(file: UploadFile) {
  // 检查文件数量限制
  if (
    isNumber(fileList.value!.length) &&
    fileList.value!.length >= props.maxNumber
  ) {
    message.error($t('ui.upload.maxNumber', [props.maxNumber]));
    return Upload.LIST_IGNORE;
  }

  const { maxSize, accept } = props;
  const isAct = isImage(file.raw!.name, accept);
  if (!isAct) {
    message.error($t('ui.upload.acceptUpload', [accept]));
    isActMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isActMsg.value = true), 1000);
    return Upload.LIST_IGNORE;
  }
  const isLt = file.size! / 1024 / 1024 > maxSize;
  if (isLt) {
    message.error($t('ui.upload.maxSizeMultiple', [maxSize]));
    isLtMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isLtMsg.value = true), 1000);
    return Upload.LIST_IGNORE;
  }

  // 只有在验证通过后才增加计数器
  uploadNumber.value++;
  return true;
}

async function customRequest(info: UploadFile | UploadFile[]) {
  // 处理单个文件上传
  const uploadFile = Array.isArray(info) ? info[0] : info;

  if (!uploadFile) {
    return {
      status: 'fail' as const,
      error: 'No file provided',
      response: {},
    };
  }

  let { api } = props;
  if (!api || !isFunction(api)) {
    api = useUpload(props.directory).httpRequest;
  }
  try {
    // 上传文件
    const progressEvent: AxiosProgressEvent = (e) => {
      const percent = Math.trunc((e.loaded / e.total!) * 100);
      uploadFile.onProgress!({ percent });
    };
    // TDesign 使用 raw 而不是 file
    const res = await api?.(uploadFile.raw as File, progressEvent);

    // 处理上传成功后的逻辑
    handleUploadSuccess(res, uploadFile.raw as File);

    uploadFile.onSuccess!(res);
    message.success($t('ui.upload.uploadSuccess'));

    // 提取 URL，兼容不同的返回格式
    const fileUrl = (res as any)?.url || (res as any)?.data || res;

    return {
      status: 'success' as const,
      response: {
        url: fileUrl,
      },
    };
  } catch (error: any) {
    console.error(error);
    uploadFile.onError!(error);
    handleUploadError(error);
    return {
      status: 'fail' as const,
      error: error instanceof Error ? error.message : 'Upload failed',
      response: {},
    };
  }
}

// 处理上传成功
function handleUploadSuccess(res: any, file: File) {
  // 删除临时文件
  const index = fileList.value?.findIndex((item) => item.name === file.name);
  if (index !== -1) {
    fileList.value?.splice(index!, 1);
  }

  // 添加到临时上传列表
  const fileUrl = res?.url || res?.data || res;
  uploadList.value.push({
    name: file.name,
    url: fileUrl,
    status: UploadResultStatus.SUCCESS,
    uid: file.name + Date.now(),
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

// 处理上传错误
function handleUploadError(error: any) {
  console.error('上传错误:', error);
  // 上传失败时减少计数器
  uploadNumber.value = Math.max(0, uploadNumber.value - 1);
}

function getValue() {
  const list = (fileList.value || [])
    .filter((item) => item?.status === UploadResultStatus.SUCCESS)
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
</script>

<template>
  <div>
    <Upload
      v-bind="$attrs"
      v-model:file-list="fileList"
      :accept="getStringAccept"
      :before-upload="beforeUpload"
      :request-method="customRequest"
      :disabled="disabled"
      :list-type="listType"
      :max-count="maxNumber"
      :multiple="multiple"
      :progress="{ showInfo: true }"
      @preview="handlePreview"
      @remove="handleRemove"
    >
      <div
        v-if="fileList && fileList.length < maxNumber"
        class="flex flex-col items-center justify-center"
      >
        <IconifyIcon icon="lucide:cloud-upload" />
        <div class="mt-2">{{ $t('ui.upload.imgUpload') }}</div>
      </div>
    </Upload>
    <div
      v-if="showDescription"
      class="mt-2 flex flex-wrap items-center text-sm"
    >
      请上传不超过
      <div class="mx-1 font-bold text-primary">{{ maxSize }}MB</div>
      的
      <div class="mx-1 font-bold text-primary">{{ accept.join('/') }}</div>
      格式文件
    </div>
    <Dialog
      :footer="false"
      :visible="previewOpen"
      :header="previewTitle"
      @close="handleCancel"
    >
      <img :src="previewImage" alt="" class="w-full" />
    </Dialog>
  </div>
</template>

<style scoped>
/* TDesign 上传组件样式 */
:deep(.t-upload__card-item) {
  @apply flex items-center justify-center;
}
</style>
