import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 标题栏属性 */
export interface TitleBarProperty {
  // 背景图
  bgImgUrl: string;
  // 偏移
  marginLeft: number;
  // 显示位置
  textAlign: 'center' | 'left';
  // 主标题
  title: string;
  // 副标题
  description: string;
  // 标题大小
  titleSize: number;
  // 描述大小
  descriptionSize: number;
  // 标题粗细
  titleWeight: number;
  // 描述粗细
  descriptionWeight: number;
  // 标题颜色
  titleColor: string;
  // 描述颜色
  descriptionColor: string;
  // 高度
  height: number;
  // 查看更多
  more: {
    // 是否显示查看更多
    show: false;
    // 自定义文字
    text: string;
    // 样式选择
    type: 'all' | 'icon' | 'text';
    // 链接
    url: string;
  };
  // 组件样式
  style: ComponentStyle;
}

// 定义组件
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
