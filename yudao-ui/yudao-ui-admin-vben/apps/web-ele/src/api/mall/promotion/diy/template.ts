import type { PageParam, PageResult } from '@vben/request';

import type { MallDiyPageApi } from './page';

import { requestClient } from '#/api/request';

export namespace MallDiyTemplateApi {
  /** 装修模板 */
  export interface DiyTemplate {
    id?: number; // 模板编号
    name: string; // 模板名称
    used: boolean; // 是否使用
    usedTime?: Date; // 使用时间
    remark: string; // 备注
    previewPicUrls: string[]; // 预览图片地址数组
    property: string; // 模板属性
  }

  /** 装修模板属性（包含页面列表） */
  export interface DiyTemplateProperty extends DiyTemplate {
    pages: MallDiyPageApi.DiyPage[]; // 页面列表
  }
}

/** 查询装修模板列表 */
export function getDiyTemplatePage(params: PageParam) {
  return requestClient.get<PageResult<MallDiyTemplateApi.DiyTemplate>>(
    '/promotion/diy-template/page',
    { params },
  );
}

/** 查询装修模板详情 */
export function getDiyTemplate(id: number) {
  return requestClient.get<MallDiyTemplateApi.DiyTemplate>(
    `/promotion/diy-template/get?id=${id}`,
  );
}

/** 新增装修模板 */
export function createDiyTemplate(data: MallDiyTemplateApi.DiyTemplate) {
  return requestClient.post('/promotion/diy-template/create', data);
}

/** 修改装修模板 */
export function updateDiyTemplate(data: MallDiyTemplateApi.DiyTemplate) {
  return requestClient.put('/promotion/diy-template/update', data);
}

/** 删除装修模板 */
export function deleteDiyTemplate(id: number) {
  return requestClient.delete(`/promotion/diy-template/delete?id=${id}`);
}

/** 使用装修模板 */
export function useDiyTemplate(id: number) {
  return requestClient.put(`/promotion/diy-template/use?id=${id}`);
}

/** 获得装修模板属性 */
export function getDiyTemplateProperty(id: number) {
  return requestClient.get<MallDiyTemplateApi.DiyTemplateProperty>(
    `/promotion/diy-template/get-property?id=${id}`,
  );
}

/** 更新装修模板属性 */
export function updateDiyTemplateProperty(
  data: MallDiyTemplateApi.DiyTemplate,
) {
  return requestClient.put('/promotion/diy-template/update-property', data);
}
