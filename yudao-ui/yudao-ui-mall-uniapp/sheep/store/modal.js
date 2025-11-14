import { defineStore } from 'pinia';

const modal = defineStore({
  id: 'modal',
  state: () => ({
    auth: '', // 授权弹框 accountLogin|smsLogin|resetPassword|changeMobile|changePassword|changeUsername
    share: false, // 分享弹框
    menu: false, // 快捷菜单弹框
    advHistory: [], // 广告弹框记录
    lastTimer: {
      // 短信验证码计时器，为了防止刷新请求做了持久化
      smsLogin: 0,
      changeMobile: 0,
      resetPassword: 0,
      changePassword: 0,
    }
  }),
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'modal-store',
        paths: ['lastTimer', 'advHistory'],
      },
    ],
  },
});

export default modal;
