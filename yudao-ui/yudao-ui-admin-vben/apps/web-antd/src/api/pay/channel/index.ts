import { requestClient } from '#/api/request';

export namespace PayChannelApi {
  /** 支付渠道信息 */
  export interface Channel {
    id: number;
    code: string;
    config: string;
    status: number;
    remark: string;
    feeRate: number;
    appId: number;
    createTime: Date;
  }
}

/** 查询支付渠道详情 */
export function getChannel(appId: number, code: string) {
  return requestClient.get<PayChannelApi.Channel>('/pay/channel/get', {
    params: { appId, code },
  });
}

/** 新增支付渠道 */
export function createChannel(data: PayChannelApi.Channel) {
  return requestClient.post('/pay/channel/create', data);
}

/** 修改支付渠道 */
export function updateChannel(data: PayChannelApi.Channel) {
  return requestClient.put('/pay/channel/update', data);
}
