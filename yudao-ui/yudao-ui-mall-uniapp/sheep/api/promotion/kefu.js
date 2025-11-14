import request from '@/sheep/request';

const KeFuApi = {
  sendKefuMessage: (data) => {
    return request({
      url: '/promotion/kefu-message/send',
      method: 'POST',
      data,
      custom: {
        auth: true,
        showLoading: true,
        loadingMsg: '发送中',
        showSuccess: true,
        successMsg: '发送成功',
      },
    });
  },
  getKefuMessageList: (params) => {
    return request({
      url: '/promotion/kefu-message/list',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default KeFuApi;
