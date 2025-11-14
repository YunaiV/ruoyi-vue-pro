import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDeliveryExpressTemplateApi {
  /** 运费模板计费 */
  export interface TemplateCharge {
    /** 区域编号列表 */
    areaIds: number[];
    /** 首件数量 */
    startCount: number;
    /** 首件价格，单位：分 */
    startPrice: number;
    /** 续件数量 */
    extraCount: number;
    /** 续件价格，单位：分 */
    extraPrice: number;
  }

  /** 运费模板包邮 */
  export interface TemplateFree {
    /** 区域编号列表 */
    areaIds: number[];
    /** 包邮件数 */
    freeCount: number;
    /** 包邮金额，单位：分 */
    freePrice: number;
  }

  /** 快递运费模板 */
  export interface ExpressTemplate {
    /** 编号 */
    id: number;
    /** 模板名称 */
    name: string;
    /** 计费方式 */
    chargeMode: number;
    /** 排序 */
    sort: number;
    /** 计费区域列表 */
    charges: TemplateCharge[];
    /** 包邮区域列表 */
    frees: TemplateFree[];
  }

  /** 运费模板精简信息 */
  export interface SimpleTemplate {
    /** 编号 */
    id: number;
    /** 模板名称 */
    name: string;
  }
}

/** 查询快递运费模板列表 */
export function getDeliveryExpressTemplatePage(params: PageParam) {
  return requestClient.get<
    PageResult<MallDeliveryExpressTemplateApi.ExpressTemplate>
  >('/trade/delivery/express-template/page', { params });
}

/** 查询快递运费模板详情 */
export function getDeliveryExpressTemplate(id: number) {
  return requestClient.get<MallDeliveryExpressTemplateApi.ExpressTemplate>(
    `/trade/delivery/express-template/get?id=${id}`,
  );
}

/** 查询快递运费模板详情 */
export function getSimpleTemplateList() {
  return requestClient.get<MallDeliveryExpressTemplateApi.SimpleTemplate[]>(
    '/trade/delivery/express-template/list-all-simple',
  );
}

/** 新增快递运费模板 */
export function createDeliveryExpressTemplate(
  data: MallDeliveryExpressTemplateApi.ExpressTemplate,
) {
  return requestClient.post('/trade/delivery/express-template/create', data);
}

/** 修改快递运费模板 */
export function updateDeliveryExpressTemplate(
  data: MallDeliveryExpressTemplateApi.ExpressTemplate,
) {
  return requestClient.put('/trade/delivery/express-template/update', data);
}

/** 删除快递运费模板 */
export function deleteDeliveryExpressTemplate(id: number) {
  return requestClient.delete(
    `/trade/delivery/express-template/delete?id=${id}`,
  );
}
