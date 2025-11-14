import type { NavigationBarProperty } from '#/components/diy-editor/components/mobile/NavigationBar/config';
import type { PageConfigProperty } from '#/components/diy-editor/components/mobile/PageConfig/config';
import type { TabBarProperty } from '#/components/diy-editor/components/mobile/TabBar/config';

// 页面装修组件
export interface DiyComponent<T> {
  // 用于区分同一种组件的不同实例
  uid?: number;
  // 组件唯一标识
  id: string;
  // 组件名称
  name: string;
  // 组件图标
  icon: string;
  /*
   组件位置：
   top: 固定于手机顶部，例如 顶部的导航栏
   bottom: 固定于手机底部，例如 底部的菜单导航栏
   center: 位于手机中心，每个组件占一行，顺序向下排列
   空：同center
   fixed: 由组件自己决定位置，如弹窗位于手机中心、浮动按钮一般位于手机右下角
  */
  position?: '' | 'bottom' | 'center' | 'fixed' | 'top';
  // 组件属性
  property: T;
}

// 页面装修组件库
export interface DiyComponentLibrary {
  // 组件库名称
  name: string;
  // 是否展开
  extended: boolean;
  // 组件列表
  components: string[];
}

// 组件样式
export interface ComponentStyle {
  // 背景类型
  bgType: 'color' | 'img';
  // 背景颜色
  bgColor: string;
  // 背景图片
  bgImg: string;
  // 外边距
  margin: number;
  marginTop: number;
  marginRight: number;
  marginBottom: number;
  marginLeft: number;
  // 内边距
  padding: number;
  paddingTop: number;
  paddingRight: number;
  paddingBottom: number;
  paddingLeft: number;
  // 边框圆角
  borderRadius: number;
  borderTopLeftRadius: number;
  borderTopRightRadius: number;
  borderBottomRightRadius: number;
  borderBottomLeftRadius: number;
}

// 页面配置
export interface PageConfig {
  // 页面属性
  page: PageConfigProperty;
  // 顶部导航栏属性
  navigationBar: NavigationBarProperty;
  // 底部导航菜单属性
  tabBar?: TabBarProperty;
  // 页面组件列表
  components: PageComponent[];
}
// 页面组件，只保留组件ID，组件属性
export type PageComponent = Pick<DiyComponent<any>, 'id' | 'property'>;

// 页面组件库
export const PAGE_LIBS = [
  {
    name: '基础组件',
    extended: true,
    components: [
      'SearchBar',
      'NoticeBar',
      'MenuSwiper',
      'MenuGrid',
      'MenuList',
      'Popover',
      'FloatingActionButton',
    ],
  },
  {
    name: '图文组件',
    extended: true,
    components: [
      'ImageBar',
      'Carousel',
      'TitleBar',
      'VideoPlayer',
      'Divider',
      'MagicCube',
      'HotZone',
    ],
  },
  {
    name: '商品组件',
    extended: true,
    components: ['ProductCard', 'ProductList'],
  },
  {
    name: '用户组件',
    extended: true,
    components: ['UserCard', 'UserOrder', 'UserWallet', 'UserCoupon'],
  },
  {
    name: '营销组件',
    extended: true,
    components: [
      'PromotionCombination',
      'PromotionSeckill',
      'PromotionPoint',
      'CouponCard',
      'PromotionArticle',
    ],
  },
] as DiyComponentLibrary[];
