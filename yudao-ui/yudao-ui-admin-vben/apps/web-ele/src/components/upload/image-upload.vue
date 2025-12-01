<script lang="ts" setup>
import type {
  UploadFile,
  UploadProgressEvent,
  UploadRequestOptions,
} from 'element-plus';

import type { AxiosResponse } from '@vben/request';

import type { UploadListType } from './typing';

import type { AxiosProgressEvent } from '#/api/infra/file';

import { ref, toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import {
  defaultImageAccepts,
  isFunction,
  isImage,
  isObject,
  isString,
} from '@vben/utils';

import { ElMessage, ElUpload } from 'element-plus';

import { UploadResultStatus } from './typing';
import { useUpload, useUploadType } from './use-upload';

defineOptions({ name: 'ImageUpload', inheritAttrs: false });

// TODO @xingyu：这个要不要抽时间看看，upload 组件，和 antd 要不要进一步对齐下；（主要是代码风格。微信沟通~~~）
const props = withDefaults(
  defineProps<{
    // 根据后缀，或者其他
    accept?: string[];
    api?: (
      file: File,
      onUploadProgress?: AxiosProgressEvent,
    ) => Promise<AxiosResponse<any>>;
    // 组件边框圆角
    borderradius?: string;
    // 上传的目录
    directory?: string;
    disabled?: boolean;
    // 上传框高度
    height?: number | string;
    helpText?: string;
    listType?: UploadListType;
    // 最大数量的文件，Infinity不限制
    maxNumber?: number;
    // 文件最大多少MB
    maxSize?: number;
    modelValue?: string | string[];
    // 是否支持多选
    multiple?: boolean;
    // support xxx.xxx.xx
    resultField?: string;
    // 是否显示下面的描述
    showDescription?: boolean;
    // 上传框宽度
    width?: number | string;
  }>(),
  {
    modelValue: () => [],
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
    width: '',
    height: '',
    borderradius: '8px',
  },
);

const emit = defineEmits(['change', 'update:modelValue', 'delete']);
const { accept, helpText, maxNumber, maxSize, width, height, borderradius } =
  toRefs(props);
const isInnerOperate = ref<boolean>(false);
const { getStringAccept } = useUploadType({
  acceptRef: accept,
  helpTextRef: helpText,
  maxNumberRef: maxNumber,
  maxSizeRef: maxSize,
});

const fileList = ref<UploadFile[]>([]);
const isLtMsg = ref<boolean>(true); // 文件大小错误提示
const isActMsg = ref<boolean>(true); // 文件类型错误提示
const isFirstRender = ref<boolean>(true); // 是否第一次渲染

watch(
  () => props.modelValue,
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
              uid: -i,
              name: item.slice(Math.max(0, item.lastIndexOf('/') + 1)),
              status: UploadResultStatus.SUCCESS,
              url: item,
            } as UploadFile;
          } else if (item && isObject(item)) {
            const file = item as Record<string, any>;
            return {
              uid: file.uid || -i,
              name: file.name || '',
              status: UploadResultStatus.SUCCESS,
              url: file.url,
            } as UploadFile;
          }
          return null;
        })
        .filter(Boolean) as UploadFile[];
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

const handlePreview = async (file: UploadFile) => {
  if (!file.url) {
    const preview = await getBase64<string>(file.raw!);
    window.open(preview || '');
    return;
  }
  window.open(file.url);
};

const handleRemove = async (file: UploadFile) => {
  if (fileList.value) {
    const index = fileList.value.findIndex((item) => item.uid === file.uid);
    index !== -1 && fileList.value.splice(index, 1);
    const value = getValue();
    isInnerOperate.value = true;
    emit('update:modelValue', value);
    emit('change', value);
    emit('delete', file);
  }
};

const beforeUpload = async (file: File) => {
  const { maxSize, accept } = props;
  const isAct = isImage(file.name, accept);
  if (!isAct) {
    ElMessage.error($t('ui.upload.acceptUpload', [accept]));
    isActMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isActMsg.value = true), 1000);
    return false;
  }
  const isLt = file.size / 1024 / 1024 > maxSize;
  if (isLt) {
    ElMessage.error($t('ui.upload.maxSizeMultiple', [maxSize]));
    isLtMsg.value = false;
    // 防止弹出多个错误提示
    setTimeout(() => (isLtMsg.value = true), 1000);
    return false;
  }
  return true;
};

async function customRequest(options: UploadRequestOptions) {
  let { api } = props;
  if (!api || !isFunction(api)) {
    api = useUpload(props.directory).httpRequest;
  }
  try {
    // 上传文件
    const progressEvent: AxiosProgressEvent = (e) => {
      const percent = Math.trunc((e.loaded / e.total!) * 100);
      options.onProgress!({
        percent,
        total: e.total || 0,
        loaded: e.loaded || 0,
        lengthComputable: true,
      } as unknown as UploadProgressEvent);
    };
    const res = await api?.(options.file, progressEvent);

    // TODO @xingyu：看看有没更好的实现代码。
    // 更新 fileList 中对应文件的 URL 为服务器返回的真实 URL
    const uploadedFile = fileList.value.find(
      (file) => file.uid === (options.file as any).uid,
    );
    if (uploadedFile) {
      const responseData = res?.data || res;
      uploadedFile.url =
        props.resultField && responseData[props.resultField]
          ? responseData[props.resultField]
          : responseData.url || responseData;
    }

    options.onSuccess!(res);
    ElMessage.success($t('ui.upload.uploadSuccess'));

    // 更新文件
    const value = getValue();
    isInnerOperate.value = true;
    emit('update:modelValue', value);
    emit('change', value);
  } catch (error: any) {
    console.error(error);
    options.onError!(error);
  }
}

function getValue() {
  const list = (fileList.value || [])
    .filter((item) => item?.status === UploadResultStatus.SUCCESS)
    .map((item: any) => {
      if (item?.response && props?.resultField) {
        return item?.response;
      }
      return item?.response?.url || item?.response;
    });
  // add by 芋艿：【特殊】单个文件的情况，获取首个元素，保证返回的是 String 类型
  if (props.maxNumber === 1) {
    return list.length > 0 ? list[0] : '';
  }
  return list;
}
</script>

<template>
  <div
    class="upload-box"
    :style="{
      width: width || '150px',
      height: height || '150px',
      borderRadius: borderradius,
    }"
  >
    <template
      v-if="
        fileList.length > 0 &&
        fileList[0] &&
        fileList[0].status === UploadResultStatus.SUCCESS
      "
    >
      <div class="upload-image-wrapper">
        <img :src="fileList[0].url" class="upload-image" />
        <div class="upload-handle">
          <div class="handle-icon" @click="handlePreview(fileList[0]!)">
            <IconifyIcon icon="lucide:circle-plus" />
            <span>详情</span>
          </div>
          <div
            v-if="!disabled"
            class="handle-icon"
            @click="handleRemove(fileList[0]!)"
          >
            <IconifyIcon icon="lucide:trash" />
            <span>删除</span>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <ElUpload
        v-bind="$attrs"
        v-model:file-list="fileList"
        :accept="getStringAccept"
        :before-upload="beforeUpload"
        :http-request="customRequest"
        :disabled="disabled"
        :list-type="listType"
        :limit="maxNumber"
        :multiple="multiple"
        :on-preview="handlePreview"
        :on-remove="handleRemove"
        class="upload"
        :style="{
          width: width || '150px',
          height: height || '150px',
          borderRadius: borderradius,
        }"
      >
        <div class="upload-content flex flex-col items-center justify-center">
          <IconifyIcon icon="lucide:plus" />
        </div>
      </ElUpload>
    </template>
    <!-- TODO @xingyu：相比 antd 来说，EL 有点丑；貌似是这里展示的位置不太对； -->
    <div v-if="showDescription" class="mt-2 text-xs text-gray-500">
      {{ getStringAccept }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.upload-box {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fafafa;
  border: 1px dashed var(--el-border-color-darker);
  transition: border-color 0.2s;

  .upload {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100% !important;
    height: 100% !important;
    background: transparent;
    border: none !important;
  }

  .upload-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    cursor: pointer;
  }

  .upload-image-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background: #fff;
    border-radius: inherit;
  }

  .upload-image {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: inherit;
  }

  .upload-handle {
    position: absolute;
    top: 0;
    right: 0;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    cursor: pointer;
    background: rgb(0 0 0 / 50%);
    opacity: 0;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }

    .handle-icon {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin: 0 8px;
      font-size: 18px;
      color: #fff;

      span {
        margin-top: 2px;
        font-size: 12px;
      }
    }
  }

  .upload-image-wrapper:hover .upload-handle {
    opacity: 1;
  }
}
</style>
