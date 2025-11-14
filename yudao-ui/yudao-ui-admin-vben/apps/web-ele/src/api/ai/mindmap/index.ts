import { useAppConfig } from '@vben/hooks';
import { fetchEventSource } from '@vben/request';
import { useAccessStore } from '@vben/stores';

import { requestClient } from '#/api/request';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
const accessStore = useAccessStore();
export namespace AiMindmapApi {
  // AI 思维导图
  export interface MindMap {
    id: number; // 编号
    userId: number; // 用户编号
    prompt: string; // 生成内容提示
    generatedContent: string; // 生成的思维导图内容
    platform: string; // 平台
    model: string; // 模型
    errorMessage: string; // 错误信息
  }

  // AI 思维导图生成
  export interface AiMindMapGenerateReq {
    prompt: string;
  }
}

export function generateMindMap({
  data,
  onClose,
  onMessage,
  onError,
  ctrl,
}: {
  ctrl: AbortController;
  data: AiMindmapApi.AiMindMapGenerateReq;
  onClose?: (...args: any[]) => void;
  onError?: (...args: any[]) => void;
  onMessage?: (res: any) => void;
}) {
  const token = accessStore.accessToken;
  return fetchEventSource(`${apiURL}/ai/mind-map/generate-stream`, {
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

// 查询思维导图分页
export function getMindMapPage(params: any) {
  return requestClient.get(`/ai/mind-map/page`, { params });
}

// 删除思维导图
export function deleteMindMap(id: number) {
  return requestClient.delete(`/ai/mind-map/delete?id=${id}`);
}
