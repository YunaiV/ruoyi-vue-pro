import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemOperateLogApi {
  /** 操作日志信息 */
  export interface OperateLog {
    id: number;
    traceId: string;
    userType: number;
    userId: number;
    userName: string;
    type: string;
    subType: string;
    bizId: number;
    action: string;
    extra: string;
    requestMethod: string;
    requestUrl: string;
    userIp: string;
    userAgent: string;
    creator: string;
    creatorName: string;
    createTime: string;
  }
}

/** 查询操作日志列表 */
export function getOperateLogPage(params: PageParam) {
  return requestClient.get<PageResult<SystemOperateLogApi.OperateLog>>(
    '/system/operate-log/page',
    { params },
  );
}

/** 导出操作日志 */
export function exportOperateLog(params: any) {
  return requestClient.download('/system/operate-log/export-excel', { params });
}
