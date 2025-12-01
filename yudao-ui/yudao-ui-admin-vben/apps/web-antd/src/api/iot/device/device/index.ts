import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IotDeviceApi {
  // TODO @haohao：需要跟后端对齐，必要的 ReqVO、RespVO
  /** 设备 */
  export interface Device {
    id?: number; // 设备 ID，主键，自增
    deviceName: string; // 设备名称
    productId: number; // 产品编号
    productKey?: string; // 产品标识
    deviceType?: number; // 设备类型
    nickname?: string; // 设备备注名称
    gatewayId?: number; // 网关设备 ID
    state?: number; // 设备状态
    status?: number; // 设备状态（兼容字段）
    onlineTime?: Date; // 最后上线时间
    offlineTime?: Date; // 最后离线时间
    activeTime?: Date; // 设备激活时间
    createTime?: Date; // 创建时间
    ip?: string; // 设备的 IP 地址
    firmwareVersion?: string; // 设备的固件版本
    deviceSecret?: string; // 设备密钥，用于设备认证，需安全存储
    mqttClientId?: string; // MQTT 客户端 ID
    mqttUsername?: string; // MQTT 用户名
    mqttPassword?: string; // MQTT 密码
    authType?: string; // 认证类型
    locationType?: number; // 定位类型
    latitude?: number; // 设备位置的纬度
    longitude?: number; // 设备位置的经度
    areaId?: number; // 地区编码
    address?: string; // 设备详细地址
    serialNumber?: string; // 设备序列号
    config?: string; // 设备配置
    groupIds?: number[]; // 添加分组 ID
    picUrl?: string; // 设备图片
    location?: string; // 位置信息（格式：经度,纬度）
  }

  /** IoT 设备属性详细 VO */
  export interface DevicePropertyDetail {
    identifier: string; // 属性标识符
    value: string; // 最新值
    updateTime: Date; // 更新时间
    name: string; // 属性名称
    dataType: string; // 数据类型
    dataSpecs: any; // 数据定义
    dataSpecsList: any[]; // 数据定义列表
  }

  /** 设备属性 VO */
  export interface DeviceProperty {
    identifier: string; // 属性标识符
    value: string; // 最新值
    updateTime: Date; // 更新时间
  }

  /** 设备认证参数 VO */
  export interface DeviceAuthInfo {
    clientId: string; // 客户端 ID
    username: string; // 用户名
    password: string; // 密码
  }

  /** 设备发送消息 Request VO */
  export interface DeviceMessageSendReq {
    deviceId: number; // 设备编号
    method: string; // 请求方法
    params?: any; // 请求参数
  }

  /** 设备分组更新请求 */
  export interface DeviceGroupUpdateReq {
    ids: number[]; // 设备 ID 列表
    groupIds: number[]; // 分组 ID 列表
  }
}

/** IoT 设备状态枚举 */
// TODO @haohao：packages/constants/src/biz-iot-enum.ts 枚举；
export enum DeviceStateEnum {
  INACTIVE = 0, // 未激活
  OFFLINE = 2, // 离线
  ONLINE = 1, // 在线
}

/** 查询设备分页 */
export function getDevicePage(params: PageParam) {
  return requestClient.get<PageResult<IotDeviceApi.Device>>(
    '/iot/device/page',
    { params },
  );
}

/** 查询设备详情 */
export function getDevice(id: number) {
  return requestClient.get<IotDeviceApi.Device>(`/iot/device/get?id=${id}`);
}

/** 新增设备 */
export function createDevice(data: IotDeviceApi.Device) {
  return requestClient.post('/iot/device/create', data);
}

/** 修改设备 */
export function updateDevice(data: IotDeviceApi.Device) {
  return requestClient.put('/iot/device/update', data);
}

/** 修改设备分组 */
export function updateDeviceGroup(data: IotDeviceApi.DeviceGroupUpdateReq) {
  return requestClient.put('/iot/device/update-group', data);
}

/** 删除单个设备 */
export function deleteDevice(id: number) {
  return requestClient.delete(`/iot/device/delete?id=${id}`);
}

/** 删除多个设备 */
export function deleteDeviceList(ids: number[]) {
  return requestClient.delete('/iot/device/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 导出设备 */
export function exportDeviceExcel(params: any) {
  return requestClient.download('/iot/device/export-excel', { params });
}

/** 获取设备数量 */
export function getDeviceCount(productId: number) {
  return requestClient.get<number>(`/iot/device/count?productId=${productId}`);
}

/** 获取设备的精简信息列表 */
export function getSimpleDeviceList(deviceType?: number, productId?: number) {
  return requestClient.get<IotDeviceApi.Device[]>('/iot/device/simple-list', {
    params: { deviceType, productId },
  });
}

/** 根据产品编号，获取设备的精简信息列表 */
export function getDeviceListByProductId(productId: number) {
  return requestClient.get<IotDeviceApi.Device[]>('/iot/device/simple-list', {
    params: { productId },
  });
}

/** 获取导入模板 */
export function importDeviceTemplate() {
  return requestClient.download('/iot/device/get-import-template');
}

/** 获取设备属性最新数据 */
export function getLatestDeviceProperties(params: any) {
  return requestClient.get<IotDeviceApi.DevicePropertyDetail[]>(
    '/iot/device/property/get-latest',
    { params },
  );
}

/** 获取设备属性历史数据 */
export function getHistoryDevicePropertyList(params: any) {
  return requestClient.get<PageResult<IotDeviceApi.DeviceProperty>>(
    '/iot/device/property/history-list',
    { params },
  );
}

/** 获取设备认证信息 */
export function getDeviceAuthInfo(id: number) {
  return requestClient.get<IotDeviceApi.DeviceAuthInfo>(
    '/iot/device/get-auth-info',
    { params: { id } },
  );
}

/** 查询设备消息分页 */
export function getDeviceMessagePage(params: PageParam) {
  return requestClient.get<PageResult<any>>('/iot/device/message/page', {
    params,
  });
}

/** 查询设备消息配对分页 */
export function getDeviceMessagePairPage(params: PageParam) {
  return requestClient.get<PageResult<any>>('/iot/device/message/pair-page', {
    params,
  });
}

/** 发送设备消息 */
export function sendDeviceMessage(params: IotDeviceApi.DeviceMessageSendReq) {
  return requestClient.post('/iot/device/message/send', params);
}
