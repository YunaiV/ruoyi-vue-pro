import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDiyPageApi {
  /** 装修页面 */
  export interface DiyPage {
    id?: number; // 页面编号
    templateId?: number; // 模板编号
    name: string; // 页面名称
    remark: string; // 备注
    previewPicUrls: string[]; // 预览图片地址数组
    property: string; // 页面属性
  }
}

/** 查询装修页面列表 */
export function getDiyPagePage(params: PageParam) {
  return requestClient.get<PageResult<MallDiyPageApi.DiyPage>>(
    '/promotion/diy-page/page',
    { params },
  );
}

/** 查询装修页面详情 */
export function getDiyPage(id: number) {
  return requestClient.get<MallDiyPageApi.DiyPage>(
    `/promotion/diy-page/get?id=${id}`,
  );
}

/** 新增装修页面 */
export function createDiyPage(data: MallDiyPageApi.DiyPage) {
  return requestClient.post('/promotion/diy-page/create', data);
}

/** 修改装修页面 */
export function updateDiyPage(data: MallDiyPageApi.DiyPage) {
  return requestClient.put('/promotion/diy-page/update', data);
}

/** 删除装修页面 */
export function deleteDiyPage(id: number) {
  return requestClient.delete(`/promotion/diy-page/delete?id=${id}`);
}

/** 获得装修页面属性 */
export function getDiyPageProperty(id: number) {
  return requestClient.get(`/promotion/diy-page/get-property?id=${id}`);
}

/** 更新装修页面属性 */
export function updateDiyPageProperty(data: MallDiyPageApi.DiyPage) {
  return requestClient.put('/promotion/diy-page/update-property', data);
}
