import type { ComponentStyle, DiyComponent } from '../../../util';

/** 商品卡片属性 */
export interface ProductCardProperty {
  layoutType: 'oneColBigImg' | 'oneColSmallImg' | 'twoCol'; // 布局类型：单列大图 | 单列小图 | 双列
  fields: {
    introduction: ProductCardFieldProperty; // 商品简介
    marketPrice: ProductCardFieldProperty; // 商品市场价
    name: ProductCardFieldProperty; // 商品名称
    price: ProductCardFieldProperty; // 商品价格
    salesCount: ProductCardFieldProperty; // 商品销量
    stock: ProductCardFieldProperty; // 商品库存
  }; // 商品字段
  badge: {
    imgUrl: string; // 角标图片
    show: boolean; // 是否显示
  }; // 角标
  btnBuy: {
    bgBeginColor: string; // 文字按钮：背景渐变起始颜色
    bgEndColor: string; // 文字按钮：背景渐变结束颜色
    imgUrl: string; // 图片按钮：图片地址
    text: string; // 文字
    type: 'img' | 'text'; // 类型：文字 | 图片
  }; // 按钮
  borderRadiusTop: number; // 上圆角
  borderRadiusBottom: number; // 下圆角
  space: number; // 间距
  spuIds: number[]; // 商品编号列表
  style: ComponentStyle; // 组件样式
}

/** 商品字段属性 */
export interface ProductCardFieldProperty {
  show: boolean; // 是否显示
  color: string; // 颜色
}

/** 定义组件 */
export const component = {
  id: 'ProductCard',
  name: '商品卡片',
  icon: 'lucide:grid-3x3',
  property: {
    layoutType: 'oneColBigImg',
    fields: {
      name: { show: true, color: '#000' },
      introduction: { show: true, color: '#999' },
      price: { show: true, color: '#ff3000' },
      marketPrice: { show: true, color: '#c4c4c4' },
      salesCount: { show: true, color: '#c4c4c4' },
      stock: { show: false, color: '#c4c4c4' },
    },
    badge: { show: false, imgUrl: '' },
    btnBuy: {
      type: 'text',
      text: '立即购买',
      // todo: @owen 根据主题色配置
      bgBeginColor: '#FF6000',
      bgEndColor: '#FE832A',
      imgUrl: '',
    },
    borderRadiusTop: 6,
    borderRadiusBottom: 6,
    space: 8,
    spuIds: [],
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<ProductCardProperty>;
