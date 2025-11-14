import request from '@/sheep/request';

const SpuHistoryApi = {
  // 删除商品浏览记录
  deleteBrowseHistory: (spuIds) => {
    return request({
      url: '/product/browse-history/delete',
      method: 'DELETE',
      data: { spuIds },
      custom: {
        showSuccess: true,
        successMsg: '删除成功',
      },
    });
  },
  // 清空商品浏览记录
  cleanBrowseHistory: () => {
    return request({
      url: '/product/browse-history/clean',
      method: 'DELETE',
      custom: {
        showSuccess: true,
        successMsg: '清空成功',
      },
    });
  },
  // 获得商品浏览记录分页
  getBrowseHistoryPage: (data) => {
    return request({
      url: '/product/browse-history/page',
      method: 'GET',
      data,
      custom: {
        showLoading: false
      },
    });
  },
};
export default SpuHistoryApi;
