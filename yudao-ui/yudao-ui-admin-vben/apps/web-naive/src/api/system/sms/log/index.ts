import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemSmsLogApi {
  /** 短信日志信息 */
  export interface SmsLog {
    id?: number;
    channelId?: number;
    channelCode: string;
    templateId?: number;
    templateCode: string;
    templateType?: number;
    templateContent: string;
    templateParams?: Record<string, any>;
    apiTemplateId: string;
    mobile: string;
    userId?: number;
    userType?: number;
    sendStatus?: number;
    sendTime?: string;
    apiSendCode: string;
    apiSendMsg: string;
    apiRequestId: string;
    apiSerialNo: string;
    receiveStatus?: number;
    receiveTime?: string;
    apiReceiveCode: string;
    apiReceiveMsg: string;
    createTime: string;
  }
}

/** 查询短信日志列表 */
export function getSmsLogPage(params: PageParam) {
  return requestClient.get<PageResult<SystemSmsLogApi.SmsLog>>(
    '/system/sms-log/page',
    { params },
  );
}

/** 导出短信日志 */
export function exportSmsLog(params: any) {
  return requestClient.download('/system/sms-log/export-excel', { params });
}
