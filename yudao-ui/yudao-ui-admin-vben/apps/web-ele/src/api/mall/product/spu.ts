import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallSpuApi {
  /** 商品属性 */
  export interface Property {
    /** 属性编号 */
    propertyId?: number;
    /** 属性名称 */
    propertyName?: string;
    /** 属性值编号 */
    valueId?: number;
    /** 属性值名称 */
    valueName?: string;
  }

  /** 商品 SKU */
  export interface Sku {
    /** 商品 SKU 编号 */
    id?: number;
    /** 商品 SKU 名称 */
    name?: string;
    /** SPU 编号 */
    spuId?: number;
    /** 属性数组 */
    properties?: Property[];
    /** 商品价格 */
    price?: number | string;
    /** 市场价 */
    marketPrice?: number | string;
    /** 成本价 */
    costPrice?: number | string;
    /** 商品条码 */
    barCode?: string;
    /** 图片地址 */
    picUrl?: string;
    /** 库存 */
    stock?: number;
    /** 商品重量，单位：kg 千克 */
    weight?: number;
    /** 商品体积，单位：m^3 平米 */
    volume?: number;
    /** 一级分销的佣金 */
    firstBrokeragePrice?: number | string;
    /** 二级分销的佣金 */
    secondBrokeragePrice?: number | string;
    /** 商品销量 */
    salesCount?: number;
  }

  /** 优惠券模板 */
  export interface GiveCouponTemplate {
    /** 优惠券编号 */
    id?: number;
    /** 优惠券名称 */
    name?: string;
  }

  /** 商品 SPU */
  export interface Spu {
    /** 商品编号 */
    id?: number;
    /** 商品名称 */
    name?: string;
    /** 商品分类 */
    categoryId?: number;
    /** 关键字 */
    keyword?: string;
    /** 单位 */
    unit?: number | undefined;
    /** 商品封面图 */
    picUrl?: string;
    /** 商品轮播图 */
    sliderPicUrls?: string[];
    /** 商品简介 */
    introduction?: string;
    /** 配送方式 */
    deliveryTypes?: number[];
    /** 运费模版 */
    deliveryTemplateId?: number | undefined;
    /** 商品品牌编号 */
    brandId?: number;
    /** 商品规格 */
    specType?: boolean;
    /** 分销类型 */
    subCommissionType?: boolean;
    /** sku数组 */
    skus?: Sku[];
    /** 商品详情 */
    description?: string;
    /** 商品排序 */
    sort?: number;
    /** 赠送积分 */
    giveIntegral?: number;
    /** 虚拟销量 */
    virtualSalesCount?: number;
    /** 商品价格 */
    price?: number;
    /** 商品拼团价格 */
    combinationPrice?: number;
    /** 商品秒杀价格 */
    seckillPrice?: number;
    /** 商品销量 */
    salesCount?: number;
    /** 市场价 */
    marketPrice?: number;
    /** 成本价 */
    costPrice?: number;
    /** 商品库存 */
    stock?: number;
    /** 商品创建时间 */
    createTime?: Date;
    /** 商品状态 */
    status?: number;
    /** 浏览量 */
    browseCount?: number;
  }

  /** 商品状态更新 */
  export interface StatusUpdate {
    /** 商品编号 */
    id: number;
    /** 商品状态 */
    status: number;
  }
}

/** 获得商品 SPU 列表 */
export function getSpuPage(params: PageParam) {
  return requestClient.get<PageResult<MallSpuApi.Spu>>('/product/spu/page', {
    params,
  });
}

/** 获得商品 SPU 列表 tabsCount */
export function getTabsCount() {
  return requestClient.get<Record<string, number>>('/product/spu/get-count');
}

/** 创建商品 SPU */
export function createSpu(data: MallSpuApi.Spu) {
  return requestClient.post('/product/spu/create', data);
}

/** 更新商品 SPU */
export function updateSpu(data: MallSpuApi.Spu) {
  return requestClient.put('/product/spu/update', data);
}

/** 更新商品 SPU 状态 */
export function updateStatus(data: MallSpuApi.StatusUpdate) {
  return requestClient.put('/product/spu/update-status', data);
}

/** 获得商品 SPU */
export function getSpu(id: number) {
  return requestClient.get<MallSpuApi.Spu>(`/product/spu/get-detail?id=${id}`);
}

/** 获得商品 SPU 详情列表 */
export function getSpuDetailList(ids: number[]) {
  return requestClient.get<MallSpuApi.Spu[]>(`/product/spu/list?spuIds=${ids}`);
}

/** 删除商品 SPU */
export function deleteSpu(id: number) {
  return requestClient.delete(`/product/spu/delete?id=${id}`);
}

/** 导出商品 SPU Excel */
export function exportSpu(params: PageParam) {
  return requestClient.download('/product/spu/export-excel', { params });
}

/** 获得商品 SPU 精简列表 */
export function getSpuSimpleList() {
  return requestClient.get<MallSpuApi.Spu[]>('/product/spu/list-all-simple');
}
