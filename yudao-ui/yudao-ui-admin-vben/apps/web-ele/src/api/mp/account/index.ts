import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpAccountApi {
  /** 公众号账号信息 */
  export interface Account {
    id: number;
    name: string;
    account?: string;
    appId?: string;
    appSecret?: string;
    token?: string;
    aesKey?: string;
    qrCodeUrl?: string;
    remark?: string;
    createTime?: Date;
  }
}

/** 查询公众号账号列表 */
export function getAccountPage(params: PageParam) {
  return requestClient.get<PageResult<MpAccountApi.Account>>(
    '/mp/account/page',
    {
      params,
    },
  );
}

/** 查询公众号账号详情 */
export function getAccount(id: number) {
  return requestClient.get<MpAccountApi.Account>(`/mp/account/get?id=${id}`);
}

/** 查询公众号账号列表 */
export function getSimpleAccountList() {
  return requestClient.get<MpAccountApi.Account[]>(
    '/mp/account/list-all-simple',
  );
}

/** 新增公众号账号 */
export function createAccount(data: MpAccountApi.Account) {
  return requestClient.post('/mp/account/create', data);
}

/** 修改公众号账号 */
export function updateAccount(data: MpAccountApi.Account) {
  return requestClient.put('/mp/account/update', data);
}

/** 删除公众号账号 */
export function deleteAccount(id: number) {
  return requestClient.delete(`/mp/account/delete?id=${id}`);
}

/** 生成公众号账号二维码 */
export function generateAccountQrCode(id: number) {
  return requestClient.put(`/mp/account/generate-qr-code?id=${id}`);
}

/** 清空公众号账号 API 配额 */
export function clearAccountQuota(id: number) {
  return requestClient.put(`/mp/account/clear-quota?id=${id}`);
}
