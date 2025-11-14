import request from '@/sheep/request';

const PayOrderApi = {
  // 获得支付订单
  getOrder: (id, sync) => {
    return request({
      url: '/pay/order/get',
      method: 'GET',
      params: { id, sync },
    });
  },
  // 提交支付订单
  submitOrder: (data) => {
    return request({
      url: '/pay/order/submit',
      method: 'POST',
      data,
    });
  },
};

export default PayOrderApi;
