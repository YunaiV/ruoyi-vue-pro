import { requestClient } from '#/api/request';

export namespace CrmPermissionApi {
  /** 数据权限信息 */
  export interface Permission {
    id?: number; // 数据权限编号
    ids?: number[];
    userId?: number; // 用户编号
    bizType: number; // Crm 类型
    bizId: number; // Crm 类型数据编号
    level: number; // 权限级别
    toBizTypes?: number[]; // 同时添加至
    deptName?: string; // 部门名称
    nickname?: string; // 用户昵称
    postNames?: string[]; // 岗位名称数组
    createTime?: Date;
  }

  /** 数据权限转移请求 */
  export interface TransferReq {
    id: number; // 模块编号
    newOwnerUserId: number; // 新负责人的用户编号
    oldOwnerPermissionLevel?: number; // 老负责人加入团队后的权限级别
    toBizTypes?: number[]; // 转移客户时，需要额外有【联系人】【商机】【合同】的 checkbox 选择
  }

  export interface PermissionListReq {
    bizId: number; // 模块数据编号
    bizType: number; // 模块类型
  }
}

/**
 * CRM 业务类型枚举
 */
export enum BizTypeEnum {
  CRM_BUSINESS = 4, // 商机
  CRM_CLUE = 1, // 线索
  CRM_CONTACT = 3, // 联系人
  CRM_CONTRACT = 5, // 合同
  CRM_CUSTOMER = 2, // 客户
  CRM_PRODUCT = 6, // 产品
  CRM_RECEIVABLE = 7, // 回款
  CRM_RECEIVABLE_PLAN = 8, // 回款计划
}

/**
 * CRM 数据权限级别枚举
 */
export enum PermissionLevelEnum {
  OWNER = 1, // 负责人
  READ = 2, // 只读
  WRITE = 3, // 读写
}

/** 获得数据权限列表（查询团队成员列表） */
export function getPermissionList(params: CrmPermissionApi.PermissionListReq) {
  return requestClient.get<CrmPermissionApi.Permission[]>(
    '/crm/permission/list',
    { params },
  );
}

/** 创建数据权限（新增团队成员） */
export function createPermission(data: CrmPermissionApi.Permission) {
  return requestClient.post('/crm/permission/create', data);
}

/** 编辑数据权限（修改团队成员权限级别） */
export function updatePermission(data: CrmPermissionApi.Permission) {
  return requestClient.put('/crm/permission/update', data);
}

/** 删除数据权限（删除团队成员） */
export function deletePermissionBatch(ids: number[]) {
  return requestClient.delete(`/crm/permission/delete?ids=${ids.join(',')}`);
}

/** 删除自己的数据权限（退出团队） */
export function deleteSelfPermission(id: number) {
  return requestClient.delete(`/crm/permission/delete-self?id=${id}`);
}
