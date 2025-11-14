import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraJobApi {
  /** 任务信息 */
  export interface Job {
    id?: number;
    name: string;
    status: number;
    handlerName: string;
    handlerParam: string;
    cronExpression: string;
    retryCount: number;
    retryInterval: number;
    monitorTimeout: number;
    createTime?: Date;
    nextTimes?: Date[];
  }
}

/** 查询任务列表 */
export function getJobPage(params: PageParam) {
  return requestClient.get<PageResult<InfraJobApi.Job>>('/infra/job/page', {
    params,
  });
}

/** 查询任务详情 */
export function getJob(id: number) {
  return requestClient.get<InfraJobApi.Job>(`/infra/job/get?id=${id}`);
}

/** 新增任务 */
export function createJob(data: InfraJobApi.Job) {
  return requestClient.post('/infra/job/create', data);
}

/** 修改定时任务调度 */
export function updateJob(data: InfraJobApi.Job) {
  return requestClient.put('/infra/job/update', data);
}

/** 删除定时任务调度 */
export function deleteJob(id: number) {
  return requestClient.delete(`/infra/job/delete?id=${id}`);
}

/** 批量删除定时任务调度 */
export function deleteJobList(ids: number[]) {
  return requestClient.delete(`/infra/job/delete-list?ids=${ids.join(',')}`);
}

/** 导出定时任务调度 */
export function exportJob(params: any) {
  return requestClient.download('/infra/job/export-excel', { params });
}

/** 任务状态修改 */
export function updateJobStatus(id: number, status: number) {
  return requestClient.put('/infra/job/update-status', undefined, {
    params: {
      id,
      status,
    },
  });
}

/** 定时任务立即执行一次 */
export function runJob(id: number) {
  return requestClient.put(`/infra/job/trigger?id=${id}`);
}

/** 获得定时任务的下 n 次执行时间 */
export function getJobNextTimes(id: number) {
  return requestClient.get(`/infra/job/get_next_times?id=${id}`);
}
