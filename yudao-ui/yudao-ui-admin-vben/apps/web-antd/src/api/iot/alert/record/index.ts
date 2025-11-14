import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AlertRecordApi {
  /** IoT 告警记录 VO */
  export interface AlertRecord {
    id?: number;
    configId?: number;
    configName?: string;
    configLevel?: number;
    deviceId?: number;
    deviceName?: string;
    productId?: number;
    productName?: string;
    deviceMessage?: string;
    processStatus?: boolean;
    processRemark?: string;
    processTime?: Date;
    createTime?: Date;
  }
}

/** IoT 告警记录 */
export interface AlertRecord {
  id?: number;
  configId?: number;
  configName?: string;
  configLevel?: number;
  deviceId?: number;
  deviceName?: string;
  productId?: number;
  productName?: string;
  deviceMessage?: string;
  processStatus?: boolean;
  processRemark?: string;
  processTime?: Date;
  createTime?: Date;
}

/** 查询告警记录分页 */
export function getAlertRecordPage(params: PageParam) {
  return requestClient.get<PageResult<AlertRecordApi.AlertRecord>>(
    '/iot/alert-record/page',
    { params },
  );
}

/** 查询告警记录详情 */
export function getAlertRecord(id: number) {
  return requestClient.get<AlertRecordApi.AlertRecord>(
    `/iot/alert-record/get?id=${id}`,
  );
}

/** 处理告警记录 */
export function processAlertRecord(id: number, remark?: string) {
  return requestClient.put('/iot/alert-record/process', {
    id,
    remark,
  });
}

/** 批量处理告警记录 */
export function batchProcessAlertRecord(ids: number[], remark?: string) {
  return requestClient.put('/iot/alert-record/batch-process', {
    ids,
    remark,
  });
}

/** 删除告警记录 */
export function deleteAlertRecord(id: number) {
  return requestClient.delete(`/iot/alert-record/delete?id=${id}`);
}

/** 批量删除告警记录 */
export function deleteAlertRecordList(ids: number[]) {
  return requestClient.delete('/iot/alert-record/delete-list', {
    params: { ids: ids.join(',') },
  });
}
