import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemMailTemplateApi {
  /** 邮件模版信息 */
  export interface MailTemplate {
    id: number;
    name: string;
    code: string;
    accountId: number;
    nickname: string;
    title: string;
    content: string;
    params: string[];
    status: number;
    remark: string;
    createTime: Date;
  }

  /** 邮件发送信息 */
  export interface MailSendReqVO {
    toMails: string[];
    ccMails?: string[];
    bccMails?: string[];
    templateCode: string;
    templateParams: Record<string, any>;
  }
}

/** 查询邮件模版列表 */
export function getMailTemplatePage(params: PageParam) {
  return requestClient.get<PageResult<SystemMailTemplateApi.MailTemplate>>(
    '/system/mail-template/page',
    { params },
  );
}

/** 查询邮件模版详情 */
export function getMailTemplate(id: number) {
  return requestClient.get<SystemMailTemplateApi.MailTemplate>(
    `/system/mail-template/get?id=${id}`,
  );
}

/** 新增邮件模版 */
export function createMailTemplate(data: SystemMailTemplateApi.MailTemplate) {
  return requestClient.post('/system/mail-template/create', data);
}

/** 修改邮件模版 */
export function updateMailTemplate(data: SystemMailTemplateApi.MailTemplate) {
  return requestClient.put('/system/mail-template/update', data);
}

/** 删除邮件模版 */
export function deleteMailTemplate(id: number) {
  return requestClient.delete(`/system/mail-template/delete?id=${id}`);
}

/** 批量删除邮件模版 */
export function deleteMailTemplateList(ids: number[]) {
  return requestClient.delete(
    `/system/mail-template/delete-list?ids=${ids.join(',')}`,
  );
}

/** 发送邮件 */
export function sendMail(data: SystemMailTemplateApi.MailSendReqVO) {
  return requestClient.post('/system/mail-template/send-mail', data);
}
