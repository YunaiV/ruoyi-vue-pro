import request from '@/sheep/request';

// 拼团 API
const CombinationApi = {
  // 获得拼团活动分页
  getCombinationActivityPage: (params) => {
    return request({
      url: '/promotion/combination-activity/page',
      method: 'GET',
      params,
    });
  },

  // 获得拼团活动明细
  getCombinationActivity: (id) => {
    return request({
      url: '/promotion/combination-activity/get-detail',
      method: 'GET',
      params: {
        id,
      },
    });
  },

  // 获得拼团活动列表，基于活动编号数组
  getCombinationActivityListByIds: (ids) => {
    return request({
      url: '/promotion/combination-activity/list-by-ids',
      method: 'GET',
      params: {
        ids,
      },
    });
  },

  // 获得最近 n 条拼团记录（团长发起的）
  getHeadCombinationRecordList: (activityId, status, count) => {
    return request({
      url: '/promotion/combination-record/get-head-list',
      method: 'GET',
      params: {
        activityId,
        status,
        count,
      },
    });
  },

  // 获得我的拼团记录分页
  getCombinationRecordPage: (params) => {
    return request({
      url: '/promotion/combination-record/page',
      method: 'GET',
      params,
    });
  },

  // 获得拼团记录明细
  getCombinationRecordDetail: (id) => {
    return request({
      url: '/promotion/combination-record/get-detail',
      method: 'GET',
      params: {
        id,
      },
    });
  },

  // 获得拼团记录的概要信息
  getCombinationRecordSummary: () => {
    return request({
      url: '/promotion/combination-record/get-summary',
      method: 'GET',
    });
  },
};

export default CombinationApi;
