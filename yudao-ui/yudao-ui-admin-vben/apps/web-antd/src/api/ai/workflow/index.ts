import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export function getWorkflowPage(params: PageParam) {
  return requestClient.get<PageResult<any>>('/ai/workflow/page', {
    params,
  });
}

export const getWorkflow = (id: number | string) => {
  return requestClient.get(`/ai/workflow/get?id=${id}`);
};

export const createWorkflow = (data: any) => {
  return requestClient.post('/ai/workflow/create', data);
};

export const updateWorkflow = (data: any) => {
  return requestClient.put('/ai/workflow/update', data);
};

export const deleteWorkflow = (id: number | string) => {
  return requestClient.delete(`/ai/workflow/delete?id=${id}`);
};

export const testWorkflow = (data: any) => {
  return requestClient.post('/ai/workflow/test', data);
};
