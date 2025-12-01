import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDeliveryExpressTemplateApi {
  /** 快递运费模板 */
  export interface DeliveryExpressTemplate {
    id: number; // 编号
    name: string; // 模板名称
    chargeMode: number; // 计费方式
    sort: number; // 排序
    charges: DeliveryExpressTemplateCharge[]; // 计费区域列表
    frees: DeliveryExpressTemplateFree[]; // 包邮区域列表
  }

  /** 运费模板计费 */
  export interface DeliveryExpressTemplateCharge {
    areaIds: number[]; // 区域编号列表
    startCount: number; // 首件数量
    startPrice: number; // 首件价格，单位：分
    extraCount: number; // 续件数量
    extraPrice: number; // 续件价格，单位：分
  }

  /** 运费模板包邮 */
  export interface DeliveryExpressTemplateFree {
    areaIds: number[]; // 区域编号列表
    freeCount: number; // 包邮件数
    freePrice: number; // 包邮金额，单位：分
  }
}

/** 查询快递运费模板列表 */
export function getDeliveryExpressTemplatePage(params: PageParam) {
  return requestClient.get<
    PageResult<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>
  >('/trade/delivery/express-template/page', { params });
}

/** 查询快递运费模板详情 */
export function getDeliveryExpressTemplate(id: number) {
  return requestClient.get<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>(
    `/trade/delivery/express-template/get?id=${id}`,
  );
}

/** 查询快递运费模板详情 */
export function getSimpleTemplateList() {
  return requestClient.get<
    MallDeliveryExpressTemplateApi.DeliveryExpressTemplate[]
  >('/trade/delivery/express-template/list-all-simple');
}

/** 新增快递运费模板 */
export function createDeliveryExpressTemplate(
  data: MallDeliveryExpressTemplateApi.DeliveryExpressTemplate,
) {
  return requestClient.post('/trade/delivery/express-template/create', data);
}

/** 修改快递运费模板 */
export function updateDeliveryExpressTemplate(
  data: MallDeliveryExpressTemplateApi.DeliveryExpressTemplate,
) {
  return requestClient.put('/trade/delivery/express-template/update', data);
}

/** 删除快递运费模板 */
export function deleteDeliveryExpressTemplate(id: number) {
  return requestClient.delete(
    `/trade/delivery/express-template/delete?id=${id}`,
  );
}
