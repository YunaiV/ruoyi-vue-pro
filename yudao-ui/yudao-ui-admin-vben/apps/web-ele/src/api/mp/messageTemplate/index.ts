import { requestClient } from '#/api/request';

export namespace MpMessageTemplateApi {
  /** 消息模板信息 */
  export interface MessageTemplate {
    id: number;
    accountId: number;
    appId: string;
    templateId: string;
    title: string;
    content: string;
    example: string;
    primaryIndustry: string;
    deputyIndustry: string;
    createTime?: Date;
  }

  /** 发送消息模板请求 */
  export interface MessageTemplateSendVO {
    id: number;
    userId: number;
    data?: Record<string, string>;
    url?: string;
    miniProgramAppId?: string;
    miniProgramPagePath?: string;
    miniprogram?: string;
  }
}

/** 查询消息模板列表 */
export function getMessageTemplateList(params: { accountId: number }) {
  return requestClient.get<MpMessageTemplateApi.MessageTemplate[]>(
    '/mp/message-template/list',
    { params },
  );
}

/** 删除消息模板 */
export function deleteMessageTemplate(id: number) {
  return requestClient.delete('/mp/message-template/delete', {
    params: { id },
  });
}

/** 同步公众号模板 */
export function syncMessageTemplate(accountId: number) {
  return requestClient.post('/mp/message-template/sync', null, {
    params: { accountId },
  });
}

/** 发送消息模板 */
export function sendMessageTemplate(
  data: MpMessageTemplateApi.MessageTemplateSendVO,
) {
  return requestClient.post('/mp/message-template/send', data);
}
