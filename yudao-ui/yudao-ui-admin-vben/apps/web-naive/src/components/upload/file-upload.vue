<script lang="ts" setup>
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui';

import type { FileUploadProps } from './typing';

import type { AxiosProgressEvent } from '#/api/infra/file';

import { computed, ref, toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { checkFileType, isFunction, isObject, isString } from '@vben/utils';

import { NButton, NUpload, useMessage } from 'naive-ui';

import { useUpload, useUploadType } from './use-upload';

defineOptions({ name: 'FileUpload', inheritAttrs: false });

const props = withDefaults(defineProps<FileUploadProps>(), {
  value: () => [],
  modelValue: undefined,
  directory: undefined,
  disabled: false,
  drag: false,
  helpText: '',
  maxSize: 2,
  maxNumber: 1,
  accept: () => [],
  multiple: false,
  api: undefined,
  resultField: '',
  showDescription: false,
});

const emit = defineEmits([
  'change',
  'update:value',
  'update:modelValue',
  'delete',
  'returnText',
  'preview',
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

const fileList = ref<UploadFileInfo[]>([]);
const isLtMsg = ref<boolean>(true); // 文件大小错误提示
const isActMsg = ref<boolean>(true); // 文件类型错误提示
const isFirstRender = ref<boolean>(true); // 是否第一次渲染
const uploadNumber = ref<number>(0); // 上传文件计数器
const uploadList = ref<any[]>([]); // 临时上传列表

watch(
  currentValue,
  (v) => {
    if (isInnerOperate.value) {
      isInnerOperate.value = false;
      return;
    }
    let value: string[] = [];
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

/** 处理文件预览 */
function handlePreview(file: UploadFileInfo) {
  emit('preview', file);
}

/** 处理上传错误 */
function handleUploadError(error: any) {
  console.error('上传错误:', error);
  message.error($t('ui.upload.uploadError'));
  // 上传失败时减少计数器
  uploadNumber.value = Math.max(0, uploadNumber.value - 1);
}

/** 上传前校验 */
async function beforeUpload(options: {
  file: UploadFileInfo;
  fileList: UploadFileInfo[];
}) {
  const file = options.file.file as File;
  const fileContent = await file.text();
  emit('returnText', fileContent);

  // 检查文件数量限制
  if (fileList.value.length >= props.maxNumber) {
    message.error($t('ui.upload.maxNumber', [props.maxNumber]));
    return false;
  }

  const { maxSize, accept } = props;
  const isAct = checkFileType(file, accept);
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
      :directory="drag"
      :max="maxNumber"
      :multiple="multiple"
      :show-download-button="true"
      :show-preview-button="true"
      :show-remove-button="true"
      @before-upload="beforeUpload"
      @change="handleChange"
      @preview="handlePreview"
      @remove="handleRemove"
    >
      <div v-if="drag" class="upload-drag-area">
        <div class="flex flex-col items-center justify-center p-6">
          <IconifyIcon
            icon="lucide:cloud-upload"
            class="mb-4 text-5xl text-gray-400"
          />
          <p class="mb-2 text-base text-gray-600">点击或拖拽文件到此区域上传</p>
          <p class="text-sm text-gray-500">
            支持{{ accept.join('/') }}格式文件，不超过{{ maxSize }}MB
          </p>
        </div>
      </div>
      <NButton v-else-if="fileList && fileList.length < maxNumber" secondary>
        <template #icon>
          <IconifyIcon icon="lucide:cloud-upload" />
        </template>
        {{ $t('ui.upload.upload') }}
      </NButton>
    </NUpload>
    <div
      v-if="showDescription && !drag"
      class="mt-2 flex flex-wrap items-center text-sm text-gray-600"
    >
      请上传不超过
      <div class="mx-1 font-bold text-primary">{{ maxSize }}MB</div>
      的
      <div class="mx-1 font-bold text-primary">{{ accept.join('/') }}</div>
      格式文件
    </div>
  </div>
</template>

<style scoped>
.upload-drag-area {
  width: 100%;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  background-color: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.3s;
}

.upload-drag-area:hover {
  background-color: #f0f9ff;
  border-color: #18a058;
}
</style>
