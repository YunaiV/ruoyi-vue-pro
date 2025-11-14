import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraApiAccessLogApi {
  /** API 访问日志信息 */
  export interface ApiAccessLog {
    id: number;
    traceId: string;
    userId: number;
    userType: number;
    applicationName: string;
    requestMethod: string;
    requestParams: string;
    responseBody: string;
    requestUrl: string;
    userIp: string;
    userAgent: string;
    operateModule: string;
    operateName: string;
    operateType: number;
    beginTime: string;
    endTime: string;
    duration: number;
    resultCode: number;
    resultMsg: string;
    createTime: string;
  }
}

/** 查询 API 访问日志列表 */
export function getApiAccessLogPage(params: PageParam) {
  return requestClient.get<PageResult<InfraApiAccessLogApi.ApiAccessLog>>(
    '/infra/api-access-log/page',
    { params },
  );
}

/** 导出 API 访问日志 */
export function exportApiAccessLog(params: any) {
  return requestClient.download('/infra/api-access-log/export-excel', {
    params,
  });
}
