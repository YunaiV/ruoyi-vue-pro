import type { PageResult } from '@vben/request';

import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { requestClient } from '#/api/request';

export namespace CrmOperateLogApi {
  /** 操作日志查询参数 */
  export interface OperateLogQuery {
    bizType: number;
    bizId: number;
  }

  /** 操作日志信息 */
  export interface OperateLog {
    id: number;
    bizType: number;
    bizId: number;
    type: number;
    content: string;
    creator: string;
    creatorName?: string;
    createTime: Date;
  }
}

/** 获得操作日志 */
export function getOperateLogPage(params: CrmOperateLogApi.OperateLogQuery) {
  return requestClient.get<PageResult<SystemOperateLogApi.OperateLog>>(
    '/crm/operate-log/page',
    { params },
  );
}
