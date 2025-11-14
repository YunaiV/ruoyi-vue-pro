import type { AxiosResponse } from '@vben/request';

import type { AxiosProgressEvent } from '#/api/infra/file';

export type UploadListType = 'picture' | 'picture-card' | 'text';

export type UploadStatus =
  | 'error'
  | 'fail'
  | 'removed'
  | 'success'
  | 'uploading';

export enum UploadResultStatus {
  ERROR = 'error',
  REMOVED = 'removed',
  SUCCESS = 'success',
  UPLOADING = 'uploading',
}

export interface CustomUploadFile {
  uid: number;
  name: string;
  status: UploadStatus;
  url?: string;
  response?: any;
  percentage?: number;
  size?: number;
  raw?: File;
}

export function convertToUploadStatus(
  status: UploadResultStatus,
): UploadStatus {
  switch (status) {
    case UploadResultStatus.ERROR: {
      return 'fail';
    }
    case UploadResultStatus.REMOVED: {
      return 'removed';
    }
    case UploadResultStatus.SUCCESS: {
      return 'success';
    }
    case UploadResultStatus.UPLOADING: {
      return 'uploading';
    }
    default: {
      return 'success';
    }
  }
}

export interface FileUploadProps {
  // 根据后缀，或者其他
  accept?: string[];
  api?: (
    file: File,
    onUploadProgress?: AxiosProgressEvent,
  ) => Promise<AxiosResponse<any>>;
  // 上传的目录
  directory?: string;
  disabled?: boolean;
  helpText?: string;
  listType?: UploadListType;
  // 最大数量的文件，Infinity不限制
  maxNumber?: number;
  // 文件最大多少MB
  maxSize?: number;
  // 是否支持多选
  multiple?: boolean;
  // support xxx.xxx.xx
  resultField?: string;
  // 是否显示下面的描述
  showDescription?: boolean;
  value?: string | string[];
}
