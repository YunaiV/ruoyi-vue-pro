import type { Dayjs } from 'dayjs';

import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace Demo01ContactApi {
  /** 示例联系人信息 */
  export interface Demo01Contact {
    id: number; // 编号
    name?: string; // 名字
    sex?: number; // 性别
    birthday?: Dayjs | string; // 出生年
    description?: string; // 简介
    avatar: string; // 头像
  }
}

/** 查询示例联系人分页 */
export function getDemo01ContactPage(params: PageParam) {
  return requestClient.get<PageResult<Demo01ContactApi.Demo01Contact>>(
    '/infra/demo01-contact/page',
    { params },
  );
}

/** 查询示例联系人详情 */
export function getDemo01Contact(id: number) {
  return requestClient.get<Demo01ContactApi.Demo01Contact>(
    `/infra/demo01-contact/get?id=${id}`,
  );
}

/** 新增示例联系人 */
export function createDemo01Contact(data: Demo01ContactApi.Demo01Contact) {
  return requestClient.post('/infra/demo01-contact/create', data);
}

/** 修改示例联系人 */
export function updateDemo01Contact(data: Demo01ContactApi.Demo01Contact) {
  return requestClient.put('/infra/demo01-contact/update', data);
}

/** 删除示例联系人 */
export function deleteDemo01Contact(id: number) {
  return requestClient.delete(`/infra/demo01-contact/delete?id=${id}`);
}

/** 批量删除示例联系人 */
export function deleteDemo01ContactList(ids: number[]) {
  return requestClient.delete(
    `/infra/demo01-contact/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出示例联系人 */
export function exportDemo01Contact(params: any) {
  return requestClient.download('/infra/demo01-contact/export-excel', {
    params,
  });
}
