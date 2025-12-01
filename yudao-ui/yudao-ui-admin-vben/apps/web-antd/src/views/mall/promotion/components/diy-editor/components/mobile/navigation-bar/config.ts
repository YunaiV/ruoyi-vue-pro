import type { DiyComponent } from '../../../util';

/** 顶部导航栏属性 */
export interface NavigationBarProperty {
  bgType: 'color' | 'img'; // 背景类型
  bgColor: string; // 背景颜色
  bgImg: string; // 图片链接
  styleType: 'inner' | 'normal'; // 样式类型：默认 | 沉浸式
  alwaysShow: boolean; // 常驻显示
  mpCells: NavigationBarCellProperty[]; // 小程序单元格列表
  otherCells: NavigationBarCellProperty[]; // 其它平台单元格列表
  _local: {
    previewMp: boolean; // 预览顶部导航（小程序）
    previewOther: boolean; // 预览顶部导航（非小程序）
  }; // 本地变量
}

/** 顶部导航栏 - 单元格 属性 */
export interface NavigationBarCellProperty {
  type: 'image' | 'search' | 'text'; // 类型：文字 | 图片 | 搜索框
  width: number; // 宽度
  height: number; // 高度
  top: number; // 顶部位置
  left: number; // 左侧位置
  text: string; // 文字内容
  textColor: string; // 文字颜色
  imgUrl: string; // 图片地址
  url: string; // 图片链接
  backgroundColor: string; // 搜索框：框体颜色
  placeholder: string; // 搜索框：提示文字
  placeholderPosition: string; // 搜索框：提示文字位置
  showScan: boolean; // 搜索框：是否显示扫一扫
  borderRadius: number; // 搜索框：边框圆角半径
}

/** 定义组件 */
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
