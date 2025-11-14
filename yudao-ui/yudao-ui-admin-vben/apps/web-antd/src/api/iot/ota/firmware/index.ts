import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IoTOtaFirmwareApi {
  /** IoT OTA 固件 VO */
  export interface Firmware {
    id?: number;
    name: string;
    version: string;
    productId: number;
    productName?: string;
    description?: string;
    fileUrl?: string;
    fileMd5?: string;
    fileSize?: number;
    status?: number;
    createTime?: Date;
    updateTime?: Date;
  }
}

/** IoT OTA 固件 */
export interface IoTOtaFirmware {
  id?: number;
  name?: string;
  version?: string;
  productId?: number;
  productName?: string;
  description?: string;
  fileUrl?: string;
  fileMd5?: string;
  fileSize?: number;
  status?: number;
  createTime?: Date;
  updateTime?: Date;
}

/** 查询 OTA 固件分页 */
export function getOtaFirmwarePage(params: PageParam) {
  return requestClient.get<PageResult<IoTOtaFirmwareApi.Firmware>>(
    '/iot/ota/firmware/page',
    { params },
  );
}

/** 查询 OTA 固件详情 */
export function getOtaFirmware(id: number) {
  return requestClient.get<IoTOtaFirmwareApi.Firmware>(
    `/iot/ota/firmware/get?id=${id}`,
  );
}

/** 新增 OTA 固件 */
export function createOtaFirmware(data: IoTOtaFirmware) {
  return requestClient.post('/iot/ota/firmware/create', data);
}

/** 修改 OTA 固件 */
export function updateOtaFirmware(data: IoTOtaFirmware) {
  return requestClient.put('/iot/ota/firmware/update', data);
}

/** 删除 OTA 固件 */
export function deleteOtaFirmware(id: number) {
  return requestClient.delete(`/iot/ota/firmware/delete?id=${id}`);
}

/** 批量删除 OTA 固件 */
export function deleteOtaFirmwareList(ids: number[]) {
  return requestClient.delete('/iot/ota/firmware/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 更新 OTA 固件状态 */
export function updateOtaFirmwareStatus(id: number, status: number) {
  return requestClient.put(`/iot/ota/firmware/update-status`, {
    id,
    status,
  });
}

/** 根据产品 ID 查询固件列表 */
export function getOtaFirmwareListByProductId(productId: number) {
  return requestClient.get<IoTOtaFirmwareApi.Firmware[]>(
    '/iot/ota/firmware/list-by-product-id',
    { params: { productId } },
  );
}
