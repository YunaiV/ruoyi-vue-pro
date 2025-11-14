import request from '@/sheep/request';

const RewardActivityApi = {
  // 获得满减送活动
  getRewardActivity: (id) => {
    return request({
      url: '/promotion/reward-activity/get',
      method: 'GET',
      params: { id },
    });
  }
};

export default RewardActivityApi;