import { requestClient } from '#/api/request';

export namespace MemberAddressApi {
  /** 收件地址信息 */
  export interface Address {
    id?: number;
    name: string;
    mobile: string;
    areaId: number;
    detailAddress: string;
    defaultStatus: boolean;
  }
}

/** 查询用户收件地址列表 */
export function getAddressList(params: any) {
  return requestClient.get<MemberAddressApi.Address[]>('/member/address/list', {
    params,
  });
}
