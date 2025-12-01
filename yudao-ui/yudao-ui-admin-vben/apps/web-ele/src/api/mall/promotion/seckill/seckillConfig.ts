import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallSeckillConfigApi {
  /** 秒杀时段 */
  export interface SeckillConfig {
    id: number; // 编号
    name: string; // 秒杀时段名称
    startTime: string; // 开始时间点
    endTime: string; // 结束时间点
    sliderPicUrls: string[]; // 秒杀轮播图
    status: number; // 活动状态
  }
}

/** 查询秒杀时段分页 */
export function getSeckillConfigPage(params: PageParam) {
  return requestClient.get<PageResult<MallSeckillConfigApi.SeckillConfig>>(
    '/promotion/seckill-config/page',
    { params },
  );
}

/** 查询秒杀时段列表 */
export function getSimpleSeckillConfigList() {
  return requestClient.get<MallSeckillConfigApi.SeckillConfig[]>(
    '/promotion/seckill-config/list',
  );
}

/** 查询秒杀时段详情 */
export function getSeckillConfig(id: number) {
  return requestClient.get<MallSeckillConfigApi.SeckillConfig>(
    `/promotion/seckill-config/get?id=${id}`,
  );
}

/** 新增秒杀时段 */
export function createSeckillConfig(data: MallSeckillConfigApi.SeckillConfig) {
  return requestClient.post('/promotion/seckill-config/create', data);
}

/** 修改秒杀时段 */
export function updateSeckillConfig(data: MallSeckillConfigApi.SeckillConfig) {
  return requestClient.put('/promotion/seckill-config/update', data);
}

/** 删除秒杀时段 */
export function deleteSeckillConfig(id: number) {
  return requestClient.delete(`/promotion/seckill-config/delete?id=${id}`);
}

/** 修改时段配置状态 */
export function updateSeckillConfigStatus(id: number, status: number) {
  return requestClient.put('/promotion/seckill-config/update-status', {
    id,
    status,
  });
}
