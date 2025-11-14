import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraApiErrorLogApi {
  /** API 错误日志信息 */
  export interface ApiErrorLog {
    id: number;
    traceId: string;
    userId: number;
    userType: number;
    applicationName: string;
    requestMethod: string;
    requestParams: string;
    requestUrl: string;
    userIp: string;
    userAgent: string;
    exceptionTime: string;
    exceptionName: string;
    exceptionMessage: string;
    exceptionRootCauseMessage: string;
    exceptionStackTrace: string;
    exceptionClassName: string;
    exceptionFileName: string;
    exceptionMethodName: string;
    exceptionLineNumber: number;
    processUserId: number;
    processStatus: number;
    processTime: string;
    resultCode: number;
    createTime: string;
  }
}

/** 查询 API 错误日志列表 */
export function getApiErrorLogPage(params: PageParam) {
  return requestClient.get<PageResult<InfraApiErrorLogApi.ApiErrorLog>>(
    '/infra/api-error-log/page',
    { params },
  );
}

/** 更新 API 错误日志的处理状态 */
export function updateApiErrorLogStatus(id: number, processStatus: number) {
  return requestClient.put(
    `/infra/api-error-log/update-status?id=${id}&processStatus=${processStatus}`,
  );
}

/** 导出 API 错误日志 */
export function exportApiErrorLog(params: any) {
  return requestClient.download('/infra/api-error-log/export-excel', {
    params,
  });
}
