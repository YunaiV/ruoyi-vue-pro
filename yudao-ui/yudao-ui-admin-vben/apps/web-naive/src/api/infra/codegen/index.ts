import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraCodegenApi {
  /** 代码生成表定义 */
  export interface CodegenTable {
    id: number;
    tableId: number;
    isParentMenuIdValid: boolean;
    dataSourceConfigId: number;
    scene: number;
    tableName: string;
    tableComment: string;
    remark: string;
    moduleName: string;
    businessName: string;
    className: string;
    classComment: string;
    author: string;
    createTime: Date;
    updateTime: Date;
    templateType: number;
    parentMenuId: number;
  }

  /** 代码生成字段定义 */
  export interface CodegenColumn {
    id: number;
    tableId: number;
    columnName: string;
    dataType: string;
    columnComment: string;
    nullable: number;
    primaryKey: number;
    ordinalPosition: number;
    javaType: string;
    javaField: string;
    dictType: string;
    example: string;
    createOperation: number;
    updateOperation: number;
    listOperation: number;
    listOperationCondition: string;
    listOperationResult: number;
    htmlType: string;
  }

  /** 数据库表定义 */
  export interface DatabaseTable {
    name: string;
    comment: string;
  }

  /** 代码生成详情 */
  export interface CodegenDetail {
    table: CodegenTable;
    columns: CodegenColumn[];
  }

  /** 代码预览 */
  export interface CodegenPreview {
    filePath: string;
    code: string;
  }

  /** 更新代码生成请求 */
  export interface CodegenUpdateReqVO {
    table: any | CodegenTable;
    columns: CodegenColumn[];
  }

  /** 创建代码生成请求 */
  export interface CodegenCreateListReqVO {
    dataSourceConfigId?: number;
    tableNames: string[];
  }
}

/** 查询列表代码生成表定义 */
export function getCodegenTableList(dataSourceConfigId: number) {
  return requestClient.get<InfraCodegenApi.CodegenTable[]>(
    '/infra/codegen/table/list?',
    {
      params: { dataSourceConfigId },
    },
  );
}

/** 查询列表代码生成表定义 */
export function getCodegenTablePage(params: PageParam) {
  return requestClient.get<PageResult<InfraCodegenApi.CodegenTable>>(
    '/infra/codegen/table/page',
    { params },
  );
}

/** 查询详情代码生成表定义 */
export function getCodegenTable(tableId: number) {
  return requestClient.get<InfraCodegenApi.CodegenDetail>(
    '/infra/codegen/detail',
    {
      params: { tableId },
    },
  );
}

/** 修改代码生成表定义 */
export function updateCodegenTable(data: InfraCodegenApi.CodegenUpdateReqVO) {
  return requestClient.put('/infra/codegen/update', data);
}

/** 基于数据库的表结构，同步数据库的表和字段定义 */
export function syncCodegenFromDB(tableId: number) {
  return requestClient.put(
    '/infra/codegen/sync-from-db',
    {},
    {
      params: { tableId },
    },
  );
}

/** 预览生成代码 */
export function previewCodegen(tableId: number) {
  return requestClient.get<InfraCodegenApi.CodegenPreview[]>(
    '/infra/codegen/preview',
    {
      params: { tableId },
    },
  );
}

/** 下载生成代码 */
export function downloadCodegen(tableId: number) {
  return requestClient.download('/infra/codegen/download', {
    params: { tableId },
  });
}

/** 获得表定义 */
export function getSchemaTableList(params: any) {
  return requestClient.get<InfraCodegenApi.DatabaseTable[]>(
    '/infra/codegen/db/table/list',
    { params },
  );
}

/** 基于数据库的表结构，创建代码生成器的表定义 */
export function createCodegenList(
  data: InfraCodegenApi.CodegenCreateListReqVO,
) {
  return requestClient.post('/infra/codegen/create-list', data);
}

/** 删除代码生成表定义 */
export function deleteCodegenTable(tableId: number) {
  return requestClient.delete('/infra/codegen/delete', {
    params: { tableId },
  });
}

/** 批量删除代码生成表定义 */
export function deleteCodegenTableList(tableIds: number[]) {
  return requestClient.delete(
    `/infra/codegen/delete-list?tableIds=${tableIds.join(',')}`,
  );
}
