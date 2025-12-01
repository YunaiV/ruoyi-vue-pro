import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ThingModelApi {
  /** IoT 物模型数据 VO */
  export interface ThingModel {
    id?: number;
    productId?: number;
    productKey?: string;
    identifier: string;
    name: string;
    desc?: string;
    type: string;
    property?: ThingModelProperty;
    event?: ThingModelEvent;
    service?: ThingModelService;
  }

  /** IoT 物模型属性 */
  export interface Property {
    identifier: string;
    name: string;
    accessMode: string;
    dataType: string;
    dataSpecs?: any;
    dataSpecsList?: any[];
    desc?: string;
  }

  /** IoT 物模型服务 */
  export interface Service {
    identifier: string;
    name: string;
    callType: string;
    inputData?: any[];
    outputData?: any[];
    desc?: string;
  }

  /** IoT 物模型事件 */
  export interface Event {
    identifier: string;
    name: string;
    type: string;
    outputData?: any[];
    desc?: string;
  }
}

/** IoT 物模型数据 */
export interface ThingModelData {
  id?: number;
  productId?: number;
  productKey?: string;
  identifier?: string;
  name?: string;
  desc?: string;
  type?: string;
  dataType?: string;
  property?: ThingModelProperty;
  event?: ThingModelEvent;
  service?: ThingModelService;
}

/** IoT 物模型属性 */
export interface ThingModelProperty {
  identifier?: string;
  name?: string;
  accessMode?: string;
  dataType?: string;
  dataSpecs?: any;
  dataSpecsList?: any[];
  desc?: string;
}

/** IoT 物模型服务 */
export interface ThingModelService {
  identifier?: string;
  name?: string;
  callType?: string;
  inputData?: any[];
  outputData?: any[];
  desc?: string;
}

/** IoT 物模型事件 */
export interface ThingModelEvent {
  identifier?: string;
  name?: string;
  type?: string;
  outputData?: any[];
  desc?: string;
}

/** IoT 数据定义（数值型） */
export interface DataSpecsNumberData {
  min?: number | string;
  max?: number | string;
  step?: number | string;
  unit?: string;
  unitName?: string;
}

/** IoT 数据定义（枚举/布尔型） */
export interface DataSpecsEnumOrBoolData {
  value: number | string;
  name: string;
}

/** IoT 物模型表单校验规则 */
export interface ThingModelFormRules {
  [key: string]: any;
}

/** 验证布尔型名称 */
export function validateBoolName(_rule: any, value: any, callback: any) {
  if (value) {
    callback();
  } else {
    callback(new Error('枚举描述不能为空'));
  }
}

/** 查询产品物模型分页 */
export function getThingModelPage(params: PageParam) {
  return requestClient.get<PageResult<ThingModelApi.ThingModel>>(
    '/iot/thing-model/page',
    { params },
  );
}

/** 查询产品物模型详情 */
export function getThingModel(id: number) {
  return requestClient.get<ThingModelApi.ThingModel>(
    `/iot/thing-model/get?id=${id}`,
  );
}

/** 根据产品 ID 查询物模型列表 */
export function getThingModelListByProductId(productId: number) {
  return requestClient.get<ThingModelApi.ThingModel[]>(
    '/iot/thing-model/list',
    { params: { productId } },
  );
}

/** 新增物模型 */
export function createThingModel(data: ThingModelData) {
  return requestClient.post('/iot/thing-model/create', data);
}

/** 修改物模型 */
export function updateThingModel(data: ThingModelData) {
  return requestClient.put('/iot/thing-model/update', data);
}

/** 删除物模型 */
export function deleteThingModel(id: number) {
  return requestClient.delete(`/iot/thing-model/delete?id=${id}`);
}

/** 获取物模型 TSL */
export function getThingModelTSL(productId: number) {
  return requestClient.get<ThingModelApi.ThingModel[]>(
    '/iot/thing-model/get-tsl',
    { params: { productId } },
  );
}

/** 导入物模型 TSL
export function importThingModelTSL(productId: number, tslData: any) {
  return requestClient.post('/iot/thing-model/import-tsl', {
    productId,
    tslData,
  });
}
 */

/** 导出物模型 TSL
export function exportThingModelTSL(productId: number) {
  return requestClient.get<any>('/iot/thing-model/export-tsl', {
    params: { productId },
  });
}
 */
