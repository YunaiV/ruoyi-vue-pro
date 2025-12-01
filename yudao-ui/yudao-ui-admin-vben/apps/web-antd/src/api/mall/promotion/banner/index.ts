import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBannerApi {
  /** Banner 信息 */
  export interface Banner {
    id: number; // Banner 编号
    title: string; // Banner 标题
    picUrl: string; // Banner 图片
    status: number; // 状态
    url: string; // 链接地址
    position: number; // Banner 位置
    sort: number; // 排序
    memo: string; // 备注
  }
}

/** 查询 Banner 管理列表 */
export function getBannerPage(params: PageParam) {
  return requestClient.get<PageResult<MallBannerApi.Banner>>(
    '/promotion/banner/page',
    { params },
  );
}

/** 查询 Banner 管理详情 */
export function getBanner(id: number) {
  return requestClient.get<MallBannerApi.Banner>(
    `/promotion/banner/get?id=${id}`,
  );
}

/** 新增 Banner 管理 */
export function createBanner(data: MallBannerApi.Banner) {
  return requestClient.post('/promotion/banner/create', data);
}

/** 修改 Banner 管理 */
export function updateBanner(data: MallBannerApi.Banner) {
  return requestClient.put('/promotion/banner/update', data);
}

/** 删除 Banner 管理 */
export function deleteBanner(id: number) {
  return requestClient.delete(`/promotion/banner/delete?id=${id}`);
}
