import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemLoginLogApi {
  /** 登录日志信息 */
  export interface LoginLog {
    id: number;
    logType: number;
    traceId: number;
    userId: number;
    userType: number;
    username: string;
    result: number;
    status: number;
    userIp: string;
    userAgent: string;
    createTime: string;
  }
}

/** 查询登录日志列表 */
export function getLoginLogPage(params: PageParam) {
  return requestClient.get<PageResult<SystemLoginLogApi.LoginLog>>(
    '/system/login-log/page',
    { params },
  );
}

/** 导出登录日志 */
export function exportLoginLog(params: any) {
  return requestClient.download('/system/login-log/export-excel', { params });
}
