import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiImageApi {
  export interface ImageMidjourneyButtons {
    customId: string; // MJ::JOB::upsample::1::85a4b4c1-8835-46c5-a15c-aea34fad1862 动作标识
    emoji: string; // 图标 emoji
    label: string; // Make Variations 文本
    style: number; // 样式: 2（Primary）、3（Green）
  }
  // AI 绘图
  export interface Image {
    id: number; // 编号
    platform: string; // 平台
    model: string; // 模型
    prompt: string; // 提示词
    width: number; // 图片宽度
    height: number; // 图片高度
    status: number; // 状态
    publicStatus: boolean; // 公开状态
    picUrl: string; // 任务地址
    errorMessage: string; // 错误信息
    options: any; // 配置 Map<string, string>
    taskId: number; // 任务编号
    buttons: ImageMidjourneyButtons[]; // mj 操作按钮
    createTime: Date; // 创建时间
    finishTime: Date; // 完成时间
  }

  export interface ImageDrawReq {
    prompt: string; // 提示词
    modelId: number; // 模型
    style: string; // 图像生成的风格
    width: string; // 图片宽度
    height: string; // 图片高度
    options: object; // 绘制参数，Map<String, String>
  }

  export interface ImageMidjourneyImagineReq {
    prompt: string; // 提示词
    modelId: number; // 模型
    base64Array?: string[]; // size不能为空
    width: string; // 图片宽度
    height: string; // 图片高度
    version: string; // 版本
  }

  export interface ImageMidjourneyAction {
    id: number; // 图片编号
    customId: string; // MJ::JOB::upsample::1::85a4b4c1-8835-46c5-a15c-aea34fad1862 动作标识
  }
}

// 获取【我的】绘图分页
export function getImagePageMy(params: PageParam) {
  return requestClient.get<PageResult<AiImageApi.Image>>('/ai/image/my-page', {
    params,
  });
}

// 获取【我的】绘图记录
export function getImageMy(id: number) {
  return requestClient.get<AiImageApi.Image>(`/ai/image/get-my?id=${id}`);
}

// 获取【我的】绘图记录列表
export function getImageListMyByIds(ids: number[]) {
  return requestClient.get<AiImageApi.Image[]>(`/ai/image/my-list-by-ids`, {
    params: { ids: ids.join(',') },
  });
}

// 生成图片
export function drawImage(data: AiImageApi.ImageDrawReq) {
  return requestClient.post(`/ai/image/draw`, data);
}

// 删除【我的】绘画记录
export function deleteImageMy(id: number) {
  return requestClient.delete(`/ai/image/delete-my?id=${id}`);
}

// ================ midjourney 专属 ================
// 【Midjourney】生成图片
export function midjourneyImagine(data: AiImageApi.ImageMidjourneyImagineReq) {
  return requestClient.post(`/ai/image/midjourney/imagine`, data);
}

// 【Midjourney】Action 操作（二次生成图片）
export function midjourneyAction(data: AiImageApi.ImageMidjourneyAction) {
  return requestClient.post(`/ai/image/midjourney/action`, data);
}

// ================ 绘图管理 ================
// 查询绘画分页
export function getImagePage(params: any) {
  return requestClient.get<AiImageApi.Image[]>(`/ai/image/page`, { params });
}

// 更新绘画发布状态
export function updateImage(data: any) {
  return requestClient.put(`/ai/image/update`, data);
}

// 删除绘画
export function deleteImage(id: number) {
  return requestClient.delete(`/ai/image/delete?id=${id}`);
}
