import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IotDeviceGroupApi {
  /** 设备分组 */
  export interface DeviceGroup {
    id?: number; // 分组 ID
    name: string; // 分组名字
    status?: number; // 分组状态
    description?: string; // 分组描述
    deviceCount?: number; // 设备数量
  }
}

/** 查询设备分组分页 */
export function getDeviceGroupPage(params: PageParam) {
  return requestClient.get<PageResult<IotDeviceGroupApi.DeviceGroup>>(
    '/iot/device-group/page',
    { params },
  );
}

/** 查询设备分组详情 */
export function getDeviceGroup(id: number) {
  return requestClient.get<IotDeviceGroupApi.DeviceGroup>(
    `/iot/device-group/get?id=${id}`,
  );
}

/** 新增设备分组 */
export function createDeviceGroup(data: IotDeviceGroupApi.DeviceGroup) {
  return requestClient.post('/iot/device-group/create', data);
}

/** 修改设备分组 */
export function updateDeviceGroup(data: IotDeviceGroupApi.DeviceGroup) {
  return requestClient.put('/iot/device-group/update', data);
}

/** 删除设备分组 */
export function deleteDeviceGroup(id: number) {
  return requestClient.delete(`/iot/device-group/delete?id=${id}`);
}

/** 获取设备分组的精简信息列表 */
export function getSimpleDeviceGroupList() {
  return requestClient.get<IotDeviceGroupApi.DeviceGroup[]>(
    '/iot/device-group/simple-list',
  );
}
