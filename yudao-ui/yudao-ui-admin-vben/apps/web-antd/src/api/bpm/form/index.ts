import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace BpmFormApi {
  /** 流程表单 */
  export interface Form {
    id?: number | undefined;
    name: string;
    conf: string;
    fields: string[];
    status: number;
    remark: string;
    createTime: number;
  }
}

/** 获取表单分页列表 */
export async function getFormPage(params: PageParam) {
  return requestClient.get<PageResult<BpmFormApi.Form>>('/bpm/form/page', {
    params,
  });
}

/** 获取表单详情 */
export async function getFormDetail(id: number) {
  return requestClient.get<BpmFormApi.Form>(`/bpm/form/get?id=${id}`);
}

/** 创建表单 */
export async function createForm(data: BpmFormApi.Form) {
  return requestClient.post('/bpm/form/create', data);
}

/** 更新表单 */
export async function updateForm(data: BpmFormApi.Form) {
  return requestClient.put('/bpm/form/update', data);
}

/** 删除表单 */
export async function deleteForm(id: number) {
  return requestClient.delete(`/bpm/form/delete?id=${id}`);
}

/** 获取表单简单列表 */
export async function getFormSimpleList() {
  return requestClient.get<BpmFormApi.Form[]>('/bpm/form/simple-list');
}
