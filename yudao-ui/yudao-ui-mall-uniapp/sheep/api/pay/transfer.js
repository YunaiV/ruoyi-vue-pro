import request from '@/sheep/request';

const PayTransferApi = {
  // 同步转账单
  syncTransfer: (id) => {
    return request({
      url: '/pay/transfer/sync',
      method: 'GET',
      params: { id },
    });
  },
};

export default PayTransferApi;
