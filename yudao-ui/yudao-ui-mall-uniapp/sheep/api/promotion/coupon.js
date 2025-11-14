import request from '@/sheep/request';

const CouponApi = {
  // 获得优惠劵模板列表
  getCouponTemplateListByIds: (ids) => {
    return request({
      url: '/promotion/coupon-template/list-by-ids',
      method: 'GET',
      params: { ids },
      custom: {
        showLoading: false, // 不展示 Loading，避免领取优惠劵时，不成功提示
        showError: false,
      },
    });
  },
  // 获得优惠劵模版列表
  getCouponTemplateList: (spuId, productScope, count) => {
    return request({
      url: '/promotion/coupon-template/list',
      method: 'GET',
      params: { spuId, productScope, count },
    });
  },
  // 获得优惠劵模版分页
  getCouponTemplatePage: (params) => {
    return request({
      url: '/promotion/coupon-template/page',
      method: 'GET',
      params,
    });
  },
  // 获得优惠劵模版
  getCouponTemplate: (id) => {
    return request({
      url: '/promotion/coupon-template/get',
      method: 'GET',
      params: { id },
    });
  },
  // 我的优惠劵列表
  getCouponPage: (params) => {
    return request({
      url: '/promotion/coupon/page',
      method: 'GET',
      params,
    });
  },
  // 领取优惠券
  takeCoupon: (templateId) => {
    return request({
      url: '/promotion/coupon/take',
      method: 'POST',
      data: { templateId },
      custom: {
        auth: true,
        showLoading: true,
        loadingMsg: '领取中',
        showSuccess: true,
        successMsg: '领取成功',
      },
    });
  },
  // 获得优惠劵
  getCoupon: (id) => {
    return request({
      url: '/promotion/coupon/get',
      method: 'GET',
      params: { id },
    });
  },
  // 获得未使用的优惠劵数量
  getUnusedCouponCount: () => {
    return request({
      url: '/promotion/coupon/get-unused-count',
      method: 'GET',
      custom: {
        showLoading: false,
        auth: true,
      },
    });
  },
};

export default CouponApi;
