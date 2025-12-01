import type { AiWriteTypeEnum } from '@vben/constants';
import type { PageParam, PageResult } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { fetchEventSource } from '@vben/request';
import { useAccessStore } from '@vben/stores';

import { requestClient } from '#/api/request';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
const accessStore = useAccessStore();

export namespace AiWriteApi {
  export interface Write {
    id?: number;
    type: AiWriteTypeEnum.REPLY | AiWriteTypeEnum.WRITING; // 1:撰写 2:回复
    prompt: string; // 写作内容提示 1。撰写 2回复
    originalContent: string; // 原文
    length: number; // 长度
    format: number; // 格式
    tone: number; // 语气
    language: number; // 语言
    userId?: number; // 用户编号
    platform?: string; // 平台
    model?: string; // 模型
    generatedContent?: string; // 生成的内容
    errorMessage?: string; // 错误信息
    createTime?: Date; // 创建时间
  }

  export interface AiWritePageReqVO extends PageParam {
    userId?: number; // 用户编号
    type?: AiWriteTypeEnum; //  写作类型
    platform?: string; // 平台
    createTime?: [string, string]; // 创建时间
  }
}

/** 写作 Stream */
export function writeStream({
  data,
  onClose,
  onMessage,
  onError,
  ctrl,
}: {
  ctrl: AbortController;
  data: Partial<AiWriteApi.Write>;
  onClose?: (...args: any[]) => void;
  onError?: (...args: any[]) => void;
  onMessage?: (res: any) => void;
}) {
  const token = accessStore.accessToken;
  return fetchEventSource(`${apiURL}/ai/write/generate-stream`, {
    method: 'post',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    openWhenHidden: true,
    body: JSON.stringify(data),
    onmessage: onMessage,
    onerror: onError,
    onclose: onClose,
    signal: ctrl.signal,
  });
}

/** 获取写作列表 */
export function getWritePage(params: AiWriteApi.AiWritePageReqVO) {
  return requestClient.get<PageResult<AiWriteApi.Write>>(`/ai/write/page`, {
    params,
  });
}

/** 删除写作记录 */
export function deleteWrite(id: number) {
  return requestClient.delete(`/ai/write/delete`, { params: { id } });
}
