import type { UploadProps } from 'ant-design-vue';

import type { UploadRawFile } from '#/views/mp/hooks/useUpload';

import { useAccessStore } from '@vben/stores';

import { UploadType, useBeforeUpload } from '#/views/mp/hooks/useUpload';

const accessStore = useAccessStore();
const HEADERS = { Authorization: `Bearer ${accessStore.accessToken}` }; // 请求头
const UPLOAD_URL = `${import.meta.env.VITE_BASE_URL}/admin-api/mp/material/upload-permanent`; // 上传地址

interface UploadData {
  accountId: number;
  introduction: string;
  title: string;
  type: UploadType;
}

const beforeImageUpload: UploadProps['beforeUpload'] = function (
  rawFile: UploadRawFile,
) {
  return useBeforeUpload(UploadType.Image, 2)(rawFile);
};

const beforeVoiceUpload: UploadProps['beforeUpload'] = function (
  rawFile: UploadRawFile,
) {
  return useBeforeUpload(UploadType.Voice, 2)(rawFile);
};

const beforeVideoUpload: UploadProps['beforeUpload'] = function (
  rawFile: UploadRawFile,
) {
  return useBeforeUpload(UploadType.Video, 10)(rawFile);
};

export {
  beforeImageUpload,
  beforeVideoUpload,
  beforeVoiceUpload,
  HEADERS,
  UPLOAD_URL,
  type UploadData,
  UploadType,
};
