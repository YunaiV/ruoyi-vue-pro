import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IoTOtaTaskRecordApi {
  /** IoT OTA 升级任务记录 VO */
  export interface TaskRecord {
    id?: number;
    taskId: number;
    taskName?: string;
    deviceId: number;
    deviceName?: string;
    firmwareId?: number;
    firmwareName?: string;
    firmwareVersion?: string;
    status?: number;
    progress?: number;
    errorMessage?: string;
    startTime?: Date;
    endTime?: Date;
    createTime?: Date;
  }
}

// TODO @AI：这里应该拿到 IoTOtaTaskRecordApi 里
/** IoT OTA 升级任务记录 */
export interface OtaTaskRecord {
  id?: number;
  taskId?: number;
  taskName?: string;
  deviceId?: number;
  deviceName?: string;
  firmwareId?: number;
  firmwareName?: string;
  firmwareVersion?: string;
  status?: number;
  progress?: number;
  errorMessage?: string;
  startTime?: Date;
  endTime?: Date;
  createTime?: Date;
}

/** 查询 OTA 升级任务记录分页 */
export function getOtaTaskRecordPage(params: PageParam) {
  return requestClient.get<PageResult<IoTOtaTaskRecordApi.TaskRecord>>(
    '/iot/ota/task/record/page',
    { params },
  );
}

/** 查询 OTA 升级任务记录详情 */
export function getOtaTaskRecord(id: number) {
  return requestClient.get<IoTOtaTaskRecordApi.TaskRecord>(
    `/iot/ota/task/record/get?id=${id}`,
  );
}

/** 根据任务 ID 查询记录列表 */
export function getOtaTaskRecordListByTaskId(taskId: number) {
  return requestClient.get<IoTOtaTaskRecordApi.TaskRecord[]>(
    '/iot/ota/task/record/list-by-task-id',
    { params: { taskId } },
  );
}

/** 根据设备 ID 查询记录列表 */
export function getOtaTaskRecordListByDeviceId(deviceId: number) {
  return requestClient.get<IoTOtaTaskRecordApi.TaskRecord[]>(
    '/iot/ota/task/record/list-by-device-id',
    { params: { deviceId } },
  );
}

/** 根据固件 ID 查询记录列表 */
export function getOtaTaskRecordListByFirmwareId(firmwareId: number) {
  return requestClient.get<IoTOtaTaskRecordApi.TaskRecord[]>(
    '/iot/ota/task/record/list-by-firmware-id',
    { params: { firmwareId } },
  );
}

/** 重试升级任务记录 */
export function retryOtaTaskRecord(id: number) {
  return requestClient.put(`/iot/ota/task/record/retry?id=${id}`);
}

/** 取消升级任务记录 */
export function cancelOtaTaskRecord(id: number) {
  return requestClient.put(`/iot/ota/task/record/cancel?id=${id}`);
}

/** 获取升级任务记录状态统计 */
export function getOtaTaskRecordStatusStatistics(
  firmwareId?: number,
  taskId?: number,
) {
  return requestClient.get<Record<string, number>>(
    '/iot/ota/task/record/get-status-statistics',
    { params: { firmwareId, taskId } },
  );
}
