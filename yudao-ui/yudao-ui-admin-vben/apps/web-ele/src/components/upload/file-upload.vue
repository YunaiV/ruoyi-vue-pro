<script lang="ts" setup>
// TODO @puhui999：这个看看怎么和对应的 antd 【代码风格】，保持一致一些；
import type {
  UploadFile,
  UploadInstance,
  UploadProps,
  UploadRawFile,
  UploadRequestOptions,
  UploadUserFile,
} from 'element-plus';

import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { isString } from '@vben/utils';

import { ElButton, ElLink, ElMessage, ElUpload } from 'element-plus';

import { useUpload } from './use-upload';

defineOptions({ name: 'FileUpload', inheritAttrs: false });

const props = withDefaults(
  defineProps<{
    autoUpload?: boolean;
    directory?: string;
    disabled?: boolean;
    drag?: boolean;
    fileSize?: number;
    fileType?: string[];
    isShowTip?: boolean;
    limit?: number;
    modelValue: string | string[];
  }>(),
  {
    fileType: () => ['doc', 'xls', 'ppt', 'txt', 'pdf'], // 文件类型, 例如['png', 'jpg', 'jpeg']
    fileSize: 5, // 大小限制(MB)
    limit: 5, // 数量限制
    autoUpload: true, // 自动上传
    drag: false, // 拖拽上传
    isShowTip: true, // 是否显示提示
    disabled: false, // 是否禁用上传组件 ==> 非必传（默认为 false）
    directory: undefined, // 上传目录 ==> 非必传（默认为 undefined）
  },
);

const emit = defineEmits(['update:modelValue']);

// ========== 上传相关 ==========
const uploadRef = ref<UploadInstance>();
const uploadList = ref<UploadUserFile[]>([]);
const fileList = ref<UploadUserFile[]>([]);
const uploadNumber = ref<number>(0);

const { uploadUrl, httpRequest }: any = useUpload(props.directory);

/** httpRequest 适配 ele */
const httpRequest0 = (options: UploadRequestOptions) => {
  return httpRequest(options.file);
};

// 文件上传之前判断
const beforeUpload: UploadProps['beforeUpload'] = (file: UploadRawFile) => {
  if (fileList.value.length >= props.limit) {
    ElMessage.error(`上传文件数量不能超过${props.limit}个!`);
    return false;
  }
  let fileExtension = '';
  // eslint-disable-next-line unicorn/prefer-includes
  if (file.name.lastIndexOf('.') > -1) {
    fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1);
  }
  const isImg = props.fileType.some((type: string) => {
    // eslint-disable-next-line unicorn/prefer-includes
    if (file.type.indexOf(type) > -1) return true;
    // eslint-disable-next-line unicorn/prefer-includes
    return !!(fileExtension && fileExtension.indexOf(type) > -1);
  });
  const isLimit = file.size < props.fileSize * 1024 * 1024;
  if (!isImg) {
    ElMessage.error(`文件格式不正确, 请上传${props.fileType.join('/')}格式!`);
    return false;
  }
  if (!isLimit) {
    ElMessage.error(`上传文件大小不能超过${props.fileSize}MB!`);
    return false;
  }
  ElMessage.success('正在上传文件，请稍候...');
  // 只有在验证通过后才增加计数器
  uploadNumber.value++;
  return true;
};

// 文件上传成功
const handleFileSuccess: UploadProps['onSuccess'] = (url: any): void => {
  ElMessage.success('上传成功');
  // 删除自身
  const index = fileList.value.findIndex((item: any) => item.response === url);
  fileList.value.splice(index, 1);
  uploadList.value.push({ name: url, url });
  if (uploadList.value.length === uploadNumber.value) {
    fileList.value.push(...uploadList.value);
    uploadList.value = [];
    uploadNumber.value = 0;
    emitUpdateModelValue();
  }
};
// 文件数超出提示
const handleExceed: UploadProps['onExceed'] = (): void => {
  ElMessage.error(`上传文件数量不能超过${props.limit}个!`);
};
// 上传错误提示
const excelUploadError: UploadProps['onError'] = (): void => {
  ElMessage.error('导入数据失败，请您重新上传！');
  // 上传失败时减少计数器，避免后续上传被阻塞
  uploadNumber.value = Math.max(0, uploadNumber.value - 1);
};
// 删除上传文件
const handleRemove = (file: UploadFile) => {
  const index = fileList.value.map((f) => f.name).indexOf(file.name);
  if (index !== -1) {
    fileList.value.splice(index, 1);
    emitUpdateModelValue();
  }
};
const handlePreview: UploadProps['onPreview'] = (_) => {
  // console.log(uploadFile);
};

// 监听模型绑定值变动
watch(
  () => props.modelValue,
  (val: string | string[]) => {
    if (!val) {
      fileList.value = []; // fix：处理掉缓存，表单重置后上传组件的内容并没有重置
      return;
    }

    fileList.value = []; // 保障数据为空
    // 情况1：字符串
    if (isString(val)) {
      fileList.value.push(
        ...val.split(',').map((url) => ({
          // eslint-disable-next-line unicorn/prefer-string-slice
          name: url.substring(url.lastIndexOf('/') + 1),
          url,
        })),
      );
      return;
    }
    // 情况2：数组
    fileList.value.push(
      ...(val as string[]).map((url) => ({
        // eslint-disable-next-line unicorn/prefer-string-slice
        name: url.substring(url.lastIndexOf('/') + 1),
        url,
      })),
    );
  },
  { immediate: true, deep: true },
);
// 发送文件链接列表更新
const emitUpdateModelValue = () => {
  // 情况1：数组结果
  let result: string | string[] = fileList.value.map((file) => file.url!);
  // 情况2：逗号分隔的字符串
  if (props.limit === 1 || isString(props.modelValue)) {
    result = result.join(',');
  }
  emit('update:modelValue', result);
};
</script>
<template>
  <div v-if="!disabled" class="upload-file">
    <ElUpload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="uploadUrl"
      :auto-upload="autoUpload"
      :before-upload="beforeUpload"
      :disabled="disabled"
      :drag="drag"
      :http-request="httpRequest0"
      :limit="props.limit"
      :multiple="props.limit > 1"
      :on-error="excelUploadError"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-success="handleFileSuccess"
      :show-file-list="true"
      class="upload-file-uploader"
      name="file"
    >
      <ElButton type="primary">
        <IconifyIcon icon="ep:upload-filled" />
        选取文件
      </ElButton>
      <template v-if="isShowTip" #tip>
        <div style="font-size: 8px">
          大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
        </div>
        <div style="font-size: 8px">
          格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b> 的文件
        </div>
      </template>
      <template #file="row">
        <div class="flex items-center">
          <span>{{ row.file.name }}</span>
          <div class="ml-10px">
            <ElLink
              :href="row.file.url"
              :underline="false"
              download
              target="_blank"
              type="primary"
            >
              下载
            </ElLink>
          </div>
          <div class="ml-10px">
            <ElButton link type="danger" @click="handleRemove(row.file)">
              删除
            </ElButton>
          </div>
        </div>
      </template>
    </ElUpload>
  </div>

  <!-- 上传操作禁用时 -->
  <div v-if="disabled" class="upload-file">
    <div
      v-for="(file, index) in fileList"
      :key="index"
      class="file-list-item flex items-center"
    >
      <span>{{ file.name }}</span>
      <div class="ml-10px">
        <ElLink
          :href="file.url"
          :underline="false"
          download
          target="_blank"
          type="primary"
        >
          下载
        </ElLink>
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.upload-file-uploader {
  margin-bottom: 5px;
}

:deep(.upload-file-list .el-upload-list__item) {
  position: relative;
  margin-bottom: 10px;
  line-height: 2;
  border: 1px solid #e4e7ed;
}

:deep(.el-upload-list__item-file-name) {
  max-width: 250px;
}

:deep(.upload-file-list .ele-upload-list__item-content) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: inherit;
}

:deep(.ele-upload-list__item-content-action .el-link) {
  margin-right: 10px;
}

.file-list-item {
  border: 1px dashed var(--el-border-color-darker);
  border-radius: 8px;
}
</style>
