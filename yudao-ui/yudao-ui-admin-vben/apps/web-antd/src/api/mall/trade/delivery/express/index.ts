import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDeliveryExpressApi {
  /** 快递公司 */
  export interface DeliveryExpress {
    id: number; // 编号
    code: string; // 快递公司编码
    name: string; // 快递公司名称
    logo: string; // 快递公司 logo
    sort: number; // 排序
    status: number; // 状态
  }
}

/** 查询快递公司列表 */
export function getDeliveryExpressPage(params: PageParam) {
  return requestClient.get<PageResult<MallDeliveryExpressApi.DeliveryExpress>>(
    '/trade/delivery/express/page',
    { params },
  );
}

/** 查询快递公司详情 */
export function getDeliveryExpress(id: number) {
  return requestClient.get<MallDeliveryExpressApi.DeliveryExpress>(
    `/trade/delivery/express/get?id=${id}`,
  );
}

/** 获得快递公司精简信息列表 */
export function getSimpleDeliveryExpressList() {
  return requestClient.get<MallDeliveryExpressApi.DeliveryExpress[]>(
    '/trade/delivery/express/list-all-simple',
  );
}

/** 新增快递公司 */
export function createDeliveryExpress(
  data: MallDeliveryExpressApi.DeliveryExpress,
) {
  return requestClient.post('/trade/delivery/express/create', data);
}

/** 修改快递公司 */
export function updateDeliveryExpress(
  data: MallDeliveryExpressApi.DeliveryExpress,
) {
  return requestClient.put('/trade/delivery/express/update', data);
}

/** 删除快递公司 */
export function deleteDeliveryExpress(id: number) {
  return requestClient.delete(`/trade/delivery/express/delete?id=${id}`);
}

/** 导出快递公司 Excel */
export function exportDeliveryExpress(params: PageParam) {
  return requestClient.download('/trade/delivery/express/export-excel', {
    params,
  });
}
