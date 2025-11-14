// import third from '@/sheep/api/third';
// TODO 芋艿：等后面搞 App 再弄

const login = () => {
  return new Promise(async (resolve, reject) => {
    const loginRes = await uni.login({
      provider: 'apple',
      success: () => {
        uni.getUserInfo({
          provider: 'apple',
          success: async (res) => {
            if (res.errMsg === 'getUserInfo:ok') {
              const payload = res.userInfo;
              const { error } = await third.apple.login({
                payload,
                shareInfo: uni.getStorageSync('shareLog') || {},
              });
              if (error === 0) {
                resolve(true);
              } else {
                resolve(false);
              }
            }
          },
        });
      },
      fail: (err) => {
        resolve(false);
      },
    });
  });
};

export default {
  login,
};
