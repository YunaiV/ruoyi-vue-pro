import request from '@/sheep/request';

export default {
  // 苹果相关
  apple: {
    // 第三方登录
    login: (data) =>
      request({
        url: 'third/apple/login',
        method: 'POST',
        data,
        custom: {
          showSuccess: true,
          loadingMsg: '登陆中',
        },
      }),
  },
};
