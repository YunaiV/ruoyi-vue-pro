import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDeliveryPickUpStoreApi {
  /** 自提门店 */
  export interface PickUpStore {
    /** 编号 */
    id: number;
    /** 门店名称 */
    name: string;
    /** 门店简介 */
    introduction: string;
    /** 联系电话 */
    phone: string;
    /** 区域编号 */
    areaId: number;
    /** 详细地址 */
    detailAddress: string;
    /** 门店 logo */
    logo: string;
    /** 营业开始时间 */
    openingTime: string;
    /** 营业结束时间 */
    closingTime: string;
    /** 纬度 */
    latitude: number;
    /** 经度 */
    longitude: number;
    /** 状态 */
    status: number;
    /** 营业时间 用于fieldMappingTime */
    rangeTime: any[];
    /** 绑定用户编号组数 */
    verifyUserIds?: number[];
    verifyUsers?: any[];
  }

  /** 绑定自提店员请求 */
  export interface DeliveryPickUpBindReqVO {
    id?: number;
    /** 用户编号列表 */
    verifyUserIds: number[];
  }
}

/** 查询自提门店列表 */
export function getDeliveryPickUpStorePage(params: PageParam) {
  return requestClient.get<PageResult<MallDeliveryPickUpStoreApi.PickUpStore>>(
    '/trade/delivery/pick-up-store/page',
    { params },
  );
}

/** 查询自提门店详情 */
export function getDeliveryPickUpStore(id: number) {
  return requestClient.get<MallDeliveryPickUpStoreApi.PickUpStore>(
    `/trade/delivery/pick-up-store/get?id=${id}`,
  );
}

/** 查询自提门店精简列表 */
export function getSimpleDeliveryPickUpStoreList() {
  return requestClient.get<MallDeliveryPickUpStoreApi.PickUpStore[]>(
    '/trade/delivery/pick-up-store/simple-list',
  );
}

/** 新增自提门店 */
export function createDeliveryPickUpStore(
  data: MallDeliveryPickUpStoreApi.PickUpStore,
) {
  return requestClient.post('/trade/delivery/pick-up-store/create', data);
}

/** 修改自提门店 */
export function updateDeliveryPickUpStore(
  data: MallDeliveryPickUpStoreApi.PickUpStore,
) {
  return requestClient.put('/trade/delivery/pick-up-store/update', data);
}

/** 删除自提门店 */
export function deleteDeliveryPickUpStore(id: number) {
  return requestClient.delete(`/trade/delivery/pick-up-store/delete?id=${id}`);
}

/** 绑定自提店员 */
export function bindDeliveryPickUpStore(
  data: MallDeliveryPickUpStoreApi.DeliveryPickUpBindReqVO,
) {
  return requestClient.post('/trade/delivery/pick-up-store/bind', data);
}
