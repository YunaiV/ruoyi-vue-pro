import type { ComponentStyle, DiyComponent } from '../../../util';

/** 标题栏属性 */
export interface TitleBarProperty {
  bgImgUrl: string; // 背景图
  marginLeft: number; // 偏移
  textAlign: 'center' | 'left'; // 显示位置
  title: string; // 主标题
  description: string; // 副标题
  titleSize: number; // 标题大小
  descriptionSize: number; // 描述大小
  titleWeight: number; // 标题粗细
  descriptionWeight: number; // 描述粗细
  titleColor: string; // 标题颜色
  descriptionColor: string; // 描述颜色
  height: number; // 高度
  more: {
    show: false; // 是否显示查看更多
    text: string; // 自定义文字
    type: 'all' | 'icon' | 'text'; // 样式选择
    url: string; // 链接
  }; // 查看更多
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'TitleBar',
  name: '标题栏',
  icon: 'material-symbols:line-start',
  property: {
    title: '主标题',
    description: '副标题',
    titleSize: 16,
    descriptionSize: 12,
    titleWeight: 400,
    textAlign: 'left',
    descriptionWeight: 200,
    titleColor: 'rgba(50, 50, 51, 10)',
    descriptionColor: 'rgba(150, 151, 153, 10)',
    marginLeft: 0,
    height: 40,
    more: {
      // 查看更多
      show: false,
      type: 'icon',
      text: '查看更多',
      url: '',
    },
    style: {
      bgType: 'color',
      bgColor: '#fff',
    } as ComponentStyle,
  },
} as DiyComponent<TitleBarProperty>;
