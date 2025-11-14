import { requestClient } from '#/api/request';

export namespace MemberConfigApi {
  /** 积分设置信息 */
  export interface Config {
    id?: number;
    pointTradeDeductEnable: number;
    pointTradeDeductUnitPrice: number;
    pointTradeDeductMaxPrice: number;
    pointTradeGivePoint: number;
  }
}

/** 查询积分设置详情 */
export function getConfig() {
  return requestClient.get<MemberConfigApi.Config>('/member/config/get');
}

/** 新增修改积分设置 */
export function saveConfig(data: MemberConfigApi.Config) {
  return requestClient.put('/member/config/save', data);
}
