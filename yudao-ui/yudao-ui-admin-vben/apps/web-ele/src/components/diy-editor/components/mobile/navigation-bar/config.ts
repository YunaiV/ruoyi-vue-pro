import type { DiyComponent } from '#/components/diy-editor/util';

/** 顶部导航栏属性 */
export interface NavigationBarProperty {
  // 背景类型
  bgType: 'color' | 'img';
  // 背景颜色
  bgColor: string;
  // 图片链接
  bgImg: string;
  // 样式类型：默认 | 沉浸式
  styleType: 'inner' | 'normal';
  // 常驻显示
  alwaysShow: boolean;
  // 小程序单元格列表
  mpCells: NavigationBarCellProperty[];
  // 其它平台单元格列表
  otherCells: NavigationBarCellProperty[];
  // 本地变量
  _local: {
    // 预览顶部导航（小程序）
    previewMp: boolean;
    // 预览顶部导航（非小程序）
    previewOther: boolean;
  };
}

/** 顶部导航栏 - 单元格 属性 */
export interface NavigationBarCellProperty {
  // 类型：文字 | 图片 | 搜索框
  type: 'image' | 'search' | 'text';
  // 宽度
  width: number;
  // 高度
  height: number;
  // 顶部位置
  top: number;
  // 左侧位置
  left: number;
  // 文字内容
  text: string;
  // 文字颜色
  textColor: string;
  // 图片地址
  imgUrl: string;
  // 图片链接
  url: string;
  // 搜索框：提示文字
  placeholder: string;
  // 搜索框：边框圆角半径
  borderRadius: number;
}

// 定义组件
export const component = {
  id: 'NavigationBar',
  name: '顶部导航栏',
  icon: 'tabler:layout-navbar',
  property: {
    bgType: 'color',
    bgColor: '#fff',
    bgImg: '',
    styleType: 'normal',
    alwaysShow: true,
    mpCells: [
      {
        type: 'text',
        textColor: '#111111',
      },
    ],
    otherCells: [
      {
        type: 'text',
        textColor: '#111111',
      },
    ],
    _local: {
      previewMp: true,
      previewOther: false,
    },
  },
} as DiyComponent<NavigationBarProperty>;
