import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraJobLogApi {
  /** 任务日志信息 */
  export interface JobLog {
    id?: number;
    jobId: number;
    handlerName: string;
    handlerParam: string;
    cronExpression: string;
    executeIndex: string;
    beginTime: Date;
    endTime: Date;
    duration: string;
    status: number;
    createTime?: string;
    result: string;
  }
}

/** 查询任务日志列表 */
export function getJobLogPage(params: PageParam) {
  return requestClient.get<PageResult<InfraJobLogApi.JobLog>>(
    '/infra/job-log/page',
    { params },
  );
}

/** 查询任务日志详情 */
export function getJobLog(id: number) {
  return requestClient.get<InfraJobLogApi.JobLog>(
    `/infra/job-log/get?id=${id}`,
  );
}

/** 导出定时任务日志 */
export function exportJobLog(params: any) {
  return requestClient.download('/infra/job-log/export-excel', { params });
}
