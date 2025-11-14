import { requestClient } from '#/api/request';

export namespace Demo02CategoryApi {
  /** 示例分类信息 */
  export interface Demo02Category {
    id: number; // 编号
    name?: string; // 名字
    parentId?: number; // 父级编号
    children?: Demo02Category[];
  }
}

/** 查询示例分类列表 */
export function getDemo02CategoryList(params: any) {
  return requestClient.get<Demo02CategoryApi.Demo02Category[]>(
    '/infra/demo02-category/list',
    { params },
  );
}

/** 查询示例分类详情 */
export function getDemo02Category(id: number) {
  return requestClient.get<Demo02CategoryApi.Demo02Category>(
    `/infra/demo02-category/get?id=${id}`,
  );
}

/** 新增示例分类 */
export function createDemo02Category(data: Demo02CategoryApi.Demo02Category) {
  return requestClient.post('/infra/demo02-category/create', data);
}

/** 修改示例分类 */
export function updateDemo02Category(data: Demo02CategoryApi.Demo02Category) {
  return requestClient.put('/infra/demo02-category/update', data);
}

/** 删除示例分类 */
export function deleteDemo02Category(id: number) {
  return requestClient.delete(`/infra/demo02-category/delete?id=${id}`);
}

/** 导出示例分类 */
export function exportDemo02Category(params: any) {
  return requestClient.download('/infra/demo02-category/export-excel', {
    params,
  });
}
