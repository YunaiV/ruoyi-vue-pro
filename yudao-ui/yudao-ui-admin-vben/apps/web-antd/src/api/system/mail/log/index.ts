import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemMailLogApi {
  /** 邮件日志 */
  export interface MailLog {
    id: number;
    userId: number;
    userType: number;
    toMails: string[];
    ccMails?: string[];
    bccMails?: string[];
    accountId: number;
    fromMail: string;
    templateId: number;
    templateCode: string;
    templateNickname: string;
    templateTitle: string;
    templateContent: string;
    templateParams: string;
    sendStatus: number;
    sendTime: string;
    sendMessageId: string;
    sendException: string;
    createTime: string;
  }
}

/** 查询邮件日志列表 */
export function getMailLogPage(params: PageParam) {
  return requestClient.get<PageResult<SystemMailLogApi.MailLog>>(
    '/system/mail-log/page',
    { params },
  );
}
