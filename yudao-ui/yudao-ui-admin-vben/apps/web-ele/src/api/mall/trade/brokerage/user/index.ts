import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBrokerageUserApi {
  /** 分销用户 */
  export interface BrokerageUser {
    id: number; // 编号
    bindUserId: number; // 推广员编号
    bindUserTime: Date; // 推广员绑定时间
    brokerageEnabled: boolean; // 是否启用分销
    brokerageTime: Date; // 分销资格时间
    price: number; // 可提现金额，单位：分
    frozenPrice: number; // 冻结金额，单位：分
    nickname: string; // 用户昵称
    avatar: string; // 用户头像
  }

  /** 创建分销用户请求 */
  export interface BrokerageUserCreateReqVO {
    userId: number; // 用户编号
    bindUserId: number; // 推广员编号
  }

  /** 修改推广员请求 */
  export interface BrokerageUserUpdateReqVO {
    id: number; // 用户编号
    bindUserId: number; // 推广员编号
  }

  /** 清除推广员请求 */
  export interface BrokerageUserClearBrokerageUserReqVO {
    id: number; // 用户编号
  }

  /** 修改推广资格请求 */
  export interface BrokerageUserUpdateBrokerageEnabledReqVO {
    id: number; // 用户编号
    enabled: boolean; // 是否启用分销
  }
}

/** 创建分销用户 */
export function createBrokerageUser(
  data: MallBrokerageUserApi.BrokerageUserCreateReqVO,
) {
  return requestClient.post('/trade/brokerage-user/create', data);
}

/** 查询分销用户列表 */
export function getBrokerageUserPage(params: PageParam) {
  return requestClient.get<PageResult<MallBrokerageUserApi.BrokerageUser>>(
    '/trade/brokerage-user/page',
    { params },
  );
}

/** 查询分销用户详情 */
export function getBrokerageUser(id: number) {
  return requestClient.get<MallBrokerageUserApi.BrokerageUser>(
    `/trade/brokerage-user/get?id=${id}`,
  );
}

/** 修改推广员 */
export function updateBindUser(
  data: MallBrokerageUserApi.BrokerageUserUpdateReqVO,
) {
  return requestClient.put('/trade/brokerage-user/update-bind-user', data);
}

/** 清除推广员 */
export function clearBindUser(
  data: MallBrokerageUserApi.BrokerageUserClearBrokerageUserReqVO,
) {
  return requestClient.put('/trade/brokerage-user/clear-bind-user', data);
}

/** 修改推广资格 */
export function updateBrokerageEnabled(
  data: MallBrokerageUserApi.BrokerageUserUpdateBrokerageEnabledReqVO,
) {
  return requestClient.put(
    '/trade/brokerage-user/update-brokerage-enable',
    data,
  );
}
