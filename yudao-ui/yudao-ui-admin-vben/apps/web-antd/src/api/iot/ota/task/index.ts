import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IoTOtaTaskApi {
  /** IoT OTA 升级任务 VO */
  export interface Task {
    id?: number;
    name: string;
    description?: string;
    firmwareId: number;
    firmwareName?: string;
    productId?: number;
    productName?: string;
    deviceScope?: number;
    deviceIds?: number[];
    status?: number;
    successCount?: number;
    failureCount?: number;
    pendingCount?: number;
    createTime?: Date;
    updateTime?: Date;
  }
}

/** IoT OTA 升级任务 */
export interface OtaTask {
  id?: number;
  name?: string;
  description?: string;
  firmwareId?: number;
  firmwareName?: string;
  productId?: number;
  productName?: string;
  deviceScope?: number;
  deviceIds?: number[];
  status?: number;
  successCount?: number;
  failureCount?: number;
  pendingCount?: number;
  createTime?: Date;
  updateTime?: Date;
}

/** 查询 OTA 升级任务分页 */
export function getOtaTaskPage(params: PageParam) {
  return requestClient.get<PageResult<IoTOtaTaskApi.Task>>(
    '/iot/ota/task/page',
    { params },
  );
}

/** 查询 OTA 升级任务详情 */
export function getOtaTask(id: number) {
  return requestClient.get<IoTOtaTaskApi.Task>(`/iot/ota/task/get?id=${id}`);
}

/** 新增 OTA 升级任务 */
export function createOtaTask(data: OtaTask) {
  return requestClient.post('/iot/ota/task/create', data);
}

/** 修改 OTA 升级任务 */
export function updateOtaTask(data: OtaTask) {
  return requestClient.put('/iot/ota/task/update', data);
}

/** 删除 OTA 升级任务 */
export function deleteOtaTask(id: number) {
  return requestClient.delete(`/iot/ota/task/delete?id=${id}`);
}

/** 批量删除 OTA 升级任务 */
export function deleteOtaTaskList(ids: number[]) {
  return requestClient.delete('/iot/ota/task/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 取消 OTA 升级任务 */
export function cancelOtaTask(id: number) {
  return requestClient.put(`/iot/ota/task/cancel?id=${id}`);
}

/** 启动 OTA 升级任务 */
export function startOtaTask(id: number) {
  return requestClient.put(`/iot/ota/task/start?id=${id}`);
}

/** 暂停 OTA 升级任务 */
export function pauseOtaTask(id: number) {
  return requestClient.put(`/iot/ota/task/pause?id=${id}`);
}

/** 恢复 OTA 升级任务 */
export function resumeOtaTask(id: number) {
  return requestClient.put(`/iot/ota/task/resume?id=${id}`);
}
