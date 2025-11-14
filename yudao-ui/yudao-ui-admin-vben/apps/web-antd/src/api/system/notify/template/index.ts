import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemNotifyTemplateApi {
  /** 站内信模板信息 */
  export interface NotifyTemplate {
    id?: number;
    name: string;
    nickname: string;
    code: string;
    content: string;
    type?: number;
    params: string[];
    status: number;
    remark: string;
  }

  /** 发送站内信请求 */
  export interface NotifySendReqVO {
    userId: number;
    userType: number;
    templateCode: string;
    templateParams: Record<string, any>;
  }
}

/** 查询站内信模板列表 */
export function getNotifyTemplatePage(params: PageParam) {
  return requestClient.get<PageResult<SystemNotifyTemplateApi.NotifyTemplate>>(
    '/system/notify-template/page',
    { params },
  );
}

/** 查询站内信模板详情 */
export function getNotifyTemplate(id: number) {
  return requestClient.get<SystemNotifyTemplateApi.NotifyTemplate>(
    `/system/notify-template/get?id=${id}`,
  );
}

/** 新增站内信模板 */
export function createNotifyTemplate(
  data: SystemNotifyTemplateApi.NotifyTemplate,
) {
  return requestClient.post('/system/notify-template/create', data);
}

/** 修改站内信模板 */
export function updateNotifyTemplate(
  data: SystemNotifyTemplateApi.NotifyTemplate,
) {
  return requestClient.put('/system/notify-template/update', data);
}

/** 删除站内信模板 */
export function deleteNotifyTemplate(id: number) {
  return requestClient.delete(`/system/notify-template/delete?id=${id}`);
}

/** 批量删除站内信模板 */
export function deleteNotifyTemplateList(ids: number[]) {
  return requestClient.delete(
    `/system/notify-template/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出站内信模板 */
export function exportNotifyTemplate(params: any) {
  return requestClient.download('/system/notify-template/export-excel', {
    params,
  });
}

/** 发送站内信 */
export function sendNotify(data: SystemNotifyTemplateApi.NotifySendReqVO) {
  return requestClient.post('/system/notify-template/send-notify', data);
}
