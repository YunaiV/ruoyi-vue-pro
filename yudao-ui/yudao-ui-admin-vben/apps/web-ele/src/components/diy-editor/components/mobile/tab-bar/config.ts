import type { DiyComponent } from '#/components/diy-editor/util';

/** 底部导航菜单属性 */
export interface TabBarProperty {
  // 选项列表
  items: TabBarItemProperty[];
  // 主题
  theme: string;
  // 样式
  style: TabBarStyle;
}

// 选项属性
export interface TabBarItemProperty {
  // 标签文字
  text: string;
  // 链接
  url: string;
  // 默认图标链接
  iconUrl: string;
  // 选中的图标链接
  activeIconUrl: string;
}

// 样式
export interface TabBarStyle {
  // 背景类型
  bgType: 'color' | 'img';
  // 背景颜色
  bgColor: string;
  // 图片链接
  bgImg: string;
  // 默认颜色
  color: string;
  // 选中的颜色
  activeColor: string;
}

// 定义组件
export const component = {
  id: 'TabBar',
  name: '底部导航',
  icon: 'fluent:table-bottom-row-16-filled',
  property: {
    theme: 'red',
    style: {
      bgType: 'color',
      bgColor: '#fff',
      color: '#282828',
      activeColor: '#fc4141',
    },
    items: [
      {
        text: '首页',
        url: '/pages/index/index',
        iconUrl: 'http://mall.yudao.iocoder.cn/static/images/1-001.png',
        activeIconUrl: 'http://mall.yudao.iocoder.cn/static/images/1-002.png',
      },
      {
        text: '分类',
        url: '/pages/index/category?id=3',
        iconUrl: 'http://mall.yudao.iocoder.cn/static/images/2-001.png',
        activeIconUrl: 'http://mall.yudao.iocoder.cn/static/images/2-002.png',
      },
      {
        text: '购物车',
        url: '/pages/index/cart',
        iconUrl: 'http://mall.yudao.iocoder.cn/static/images/3-001.png',
        activeIconUrl: 'http://mall.yudao.iocoder.cn/static/images/3-002.png',
      },
      {
        text: '我的',
        url: '/pages/index/user',
        iconUrl: 'http://mall.yudao.iocoder.cn/static/images/4-001.png',
        activeIconUrl: 'http://mall.yudao.iocoder.cn/static/images/4-002.png',
      },
    ],
  },
} as DiyComponent<TabBarProperty>;

export const THEME_LIST = [
  {
    id: 'red',
    name: '中国红',
    icon: 'icon-park-twotone:theme',
    color: '#d10019',
  },
  {
    id: 'orange',
    name: '桔橙',
    icon: 'icon-park-twotone:theme',
    color: '#f37b1d',
  },
  {
    id: 'gold',
    name: '明黄',
    icon: 'icon-park-twotone:theme',
    color: '#fbbd08',
  },
  {
    id: 'green',
    name: '橄榄绿',
    icon: 'icon-park-twotone:theme',
    color: '#8dc63f',
  },
  {
    id: 'cyan',
    name: '天青',
    icon: 'icon-park-twotone:theme',
    color: '#1cbbb4',
  },
  {
    id: 'blue',
    name: '海蓝',
    icon: 'icon-park-twotone:theme',
    color: '#0081ff',
  },
  {
    id: 'purple',
    name: '姹紫',
    icon: 'icon-park-twotone:theme',
    color: '#6739b6',
  },
  {
    id: 'brightRed',
    name: '嫣红',
    icon: 'icon-park-twotone:theme',
    color: '#e54d42',
  },
  {
    id: 'forestGreen',
    name: '森绿',
    icon: 'icon-park-twotone:theme',
    color: '#39b54a',
  },
  {
    id: 'mauve',
    name: '木槿',
    icon: 'icon-park-twotone:theme',
    color: '#9c26b0',
  },
  {
    id: 'pink',
    name: '桃粉',
    icon: 'icon-park-twotone:theme',
    color: '#e03997',
  },
  {
    id: 'brown',
    name: '棕褐',
    icon: 'icon-park-twotone:theme',
    color: '#a5673f',
  },
  {
    id: 'grey',
    name: '玄灰',
    icon: 'icon-park-twotone:theme',
    color: '#8799a3',
  },
  {
    id: 'gray',
    name: '草灰',
    icon: 'icon-park-twotone:theme',
    color: '#aaaaaa',
  },
  {
    id: 'black',
    name: '墨黑',
    icon: 'icon-park-twotone:theme',
    color: '#333333',
  },
];
