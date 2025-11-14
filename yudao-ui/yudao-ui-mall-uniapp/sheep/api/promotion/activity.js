import request from '@/sheep/request';

const ActivityApi = {
  // 获得单个商品，进行中的拼团、秒杀、砍价活动信息
  getActivityListBySpuId: (spuId) => {
    return request({
      url: '/promotion/activity/list-by-spu-id',
      method: 'GET',
      params: {
        spuId,
      },
    });
  },
};

export default ActivityApi;
