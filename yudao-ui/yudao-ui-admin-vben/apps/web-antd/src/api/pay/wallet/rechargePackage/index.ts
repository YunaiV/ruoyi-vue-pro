import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WalletRechargePackageApi {
  /** 充值套餐信息 */
  export interface WalletRechargePackage {
    id?: number;
    name: string;
    payPrice: number;
    bonusPrice: number;
    status: number;
  }
}

/** 查询充值套餐列表 */
export function getWalletRechargePackagePage(params: PageParam) {
  return requestClient.get<
    PageResult<WalletRechargePackageApi.WalletRechargePackage>
  >('/pay/wallet-recharge-package/page', {
    params,
  });
}

/** 查询充值套餐详情 */
export function getWalletRechargePackage(id: number) {
  return requestClient.get<WalletRechargePackageApi.WalletRechargePackage>(
    `/pay/wallet-recharge-package/get?id=${id}`,
  );
}

/** 新增充值套餐 */
export function createWalletRechargePackage(
  data: WalletRechargePackageApi.WalletRechargePackage,
) {
  return requestClient.post('/pay/wallet-recharge-package/create', data);
}

/** 修改充值套餐 */
export function updateWalletRechargePackage(
  data: WalletRechargePackageApi.WalletRechargePackage,
) {
  return requestClient.put('/pay/wallet-recharge-package/update', data);
}

/** 删除充值套餐 */
export function deleteWalletRechargePackage(id: number) {
  return requestClient.delete(`/pay/wallet-recharge-package/delete?id=${id}`);
}
