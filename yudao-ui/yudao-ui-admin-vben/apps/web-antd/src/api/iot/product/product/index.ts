import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IotProductApi {
  /** 产品 */
  export interface Product {
    id?: number; // 产品编号
    name: string; // 产品名称
    productKey?: string; // 产品标识
    protocolId?: number; // 协议编号
    protocolType?: number; // 接入协议类型
    categoryId?: number; // 产品所属品类标识符
    categoryName?: string; // 产品所属品类名称
    icon?: string; // 产品图标
    picUrl?: string; // 产品图片
    description?: string; // 产品描述
    status?: number; // 产品状态
    deviceType?: number; // 设备类型
    locationType?: number; // 定位类型
    netType?: number; // 联网方式
    codecType?: string; // 数据格式（编解码器类型）
    dataFormat?: number; // 数据格式
    validateType?: number; // 认证方式
    deviceCount?: number; // 设备数量
    createTime?: Date; // 创建时间
  }
}

// TODO @haohao：packages/constants/src/biz-iot-enum.ts 枚举；

/** IOT 产品设备类型枚举类 */
export enum DeviceTypeEnum {
  DEVICE = 0, // 直连设备
  GATEWAY = 2, // 网关设备
  GATEWAY_SUB = 1, // 网关子设备
}

/** IOT 产品定位类型枚举类 */
export enum LocationTypeEnum {
  IP = 1, // IP 定位
  MANUAL = 3, // 手动定位
  MODULE = 2, // 设备定位
}

/** IOT 数据格式（编解码器类型）枚举类 */
export enum CodecTypeEnum {
  ALINK = 'Alink', // 阿里云 Alink 协议
}

/** 查询产品分页 */
export function getProductPage(params: PageParam) {
  return requestClient.get<PageResult<IotProductApi.Product>>(
    '/iot/product/page',
    { params },
  );
}

/** 查询产品详情 */
export function getProduct(id: number) {
  return requestClient.get<IotProductApi.Product>(`/iot/product/get?id=${id}`);
}

/** 新增产品 */
export function createProduct(data: IotProductApi.Product) {
  return requestClient.post('/iot/product/create', data);
}

/** 修改产品 */
export function updateProduct(data: IotProductApi.Product) {
  return requestClient.put('/iot/product/update', data);
}

/** 删除产品 */
export function deleteProduct(id: number) {
  return requestClient.delete(`/iot/product/delete?id=${id}`);
}

/** 导出产品 Excel */
export function exportProduct(params: any) {
  return requestClient.download('/iot/product/export-excel', { params });
}

/** 更新产品状态 */
export function updateProductStatus(id: number, status: number) {
  return requestClient.put(
    `/iot/product/update-status?id=${id}&status=${status}`,
  );
}

/** 查询产品（精简）列表 */
export function getSimpleProductList() {
  return requestClient.get<IotProductApi.Product[]>('/iot/product/simple-list');
}

/** 根据 ProductKey 获取产品信息 */
export function getProductByKey(productKey: string) {
  return requestClient.get<IotProductApi.Product>('/iot/product/get-by-key', {
    params: { productKey },
  });
}
