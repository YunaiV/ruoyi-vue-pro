import request from '@/sheep/request';

const PayChannelApi = {
  // 获得指定应用的开启的支付渠道编码列表
  getEnableChannelCodeList: (appId) => {
    return request({
      url: '/pay/channel/get-enable-code-list',
      method: 'GET',
      params: { appId }
    });
  },
};

export default PayChannelApi;
