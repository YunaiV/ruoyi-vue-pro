import type { AxiosResponse } from '@vben/request';

import type { AxiosProgressEvent } from '#/api/infra/file';

export enum UploadResultStatus {
  ERROR = 'error',
  PROGRESS = 'progress',
  SUCCESS = 'success',
  WAITING = 'waiting',
}

export type UploadListType = 'picture' | 'picture-card' | 'text';

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
  drag?: boolean; // 是否支持拖拽上传
  helpText?: string;
  listType?: UploadListType;
  // 最大数量的文件，Infinity不限制
  maxNumber?: number;
  modelValue?: string | string[]; // v-model 支持
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
