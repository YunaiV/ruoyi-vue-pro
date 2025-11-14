import type { AxiosRequestConfig, PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

/** Axios 上传进度事件 */
export type AxiosProgressEvent = AxiosRequestConfig['onUploadProgress'];

export namespace InfraFileApi {
  /** 文件信息 */
  export interface File {
    id?: number;
    configId?: number;
    path: string;
    name?: string;
    url?: string;
    size?: number;
    type?: string;
    createTime?: Date;
  }

  /** 文件预签名地址 */
  export interface FilePresignedUrlRespVO {
    configId: number; // 文件配置编号
    uploadUrl: string; // 文件上传 URL
    url: string; // 文件 URL
    path: string; // 文件路径
  }

  /** 上传文件 */
  export interface FileUploadReqVO {
    file: globalThis.File;
    directory?: string;
  }
}

/** 查询文件列表 */
export function getFilePage(params: PageParam) {
  return requestClient.get<PageResult<InfraFileApi.File>>('/infra/file/page', {
    params,
  });
}

/** 删除文件 */
export function deleteFile(id: number) {
  return requestClient.delete(`/infra/file/delete?id=${id}`);
}

/** 批量删除文件 */
export function deleteFileList(ids: number[]) {
  return requestClient.delete(`/infra/file/delete-list?ids=${ids.join(',')}`);
}

/** 获取文件预签名地址 */
export function getFilePresignedUrl(name: string, directory?: string) {
  return requestClient.get<InfraFileApi.FilePresignedUrlRespVO>(
    '/infra/file/presigned-url',
    {
      params: { name, directory },
    },
  );
}

/** 创建文件 */
export function createFile(data: InfraFileApi.File) {
  return requestClient.post('/infra/file/create', data);
}

/** 上传文件 */
export function uploadFile(
  data: InfraFileApi.FileUploadReqVO,
  onUploadProgress?: AxiosProgressEvent,
) {
  // 特殊：由于 upload 内部封装，即使 directory 为 undefined，也会传递给后端
  if (!data.directory) {
    delete data.directory;
  }
  return requestClient.upload('/infra/file/upload', data, { onUploadProgress });
}
