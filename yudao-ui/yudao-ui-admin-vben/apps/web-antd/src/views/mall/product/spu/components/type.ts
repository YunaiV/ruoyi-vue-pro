/** 商品属性及其值的树形结构（用于前端展示和操作） */
export interface PropertyAndValues {
  id: number;
  name: string;
  values?: PropertyAndValues[];
}

export interface RuleConfig {
  // 需要校验的字段
  // 例：name: 'name' 则表示校验 sku.name 的值
  // 例：name: 'productConfig.stock' 则表示校验 sku.productConfig.name 的值,此处 productConfig 表示我在 Sku 上扩展的属性
  name: string;
  // 校验规格为一个毁掉函数，其中 arg 为需要校验的字段的值。
  // 例：需要校验价格必须大于0.01
  // {
  //  name:'price',
  //  rule:(arg: number) => arg > 0.01
  // }
  rule: (arg: any) => boolean;
  // 校验不通过时的消息提示
  message: string;
}

export interface SpuProperty<T> {
  propertyList: PropertyAndValues[];
  spuDetail: T;
  spuId: number;
}

// Re-export for use in generic constraint

export { type MallSpuApi } from '#/api/mall/product/spu';
