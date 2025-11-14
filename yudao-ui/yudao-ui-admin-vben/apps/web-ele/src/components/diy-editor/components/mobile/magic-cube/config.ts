import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 广告魔方属性 */
export interface MagicCubeProperty {
  // 上圆角
  borderRadiusTop: number;
  // 下圆角
  borderRadiusBottom: number;
  // 间隔
  space: number;
  // 导航菜单列表
  list: MagicCubeItemProperty[];
  // 组件样式
  style: ComponentStyle;
}

/** 广告魔方项目属性 */
export interface MagicCubeItemProperty {
  // 图标链接
  imgUrl: string;
  // 链接
  url: string;
  // 宽
  width: number;
  // 高
  height: number;
  // 上
  top: number;
  // 左
  left: number;
  // 右
  right: number;
  // 下
  bottom: number;
}

// 定义组件
export const component = {
  id: 'MagicCube',
  name: '广告魔方',
  icon: 'bi:columns',
  property: {
    borderRadiusTop: 0,
    borderRadiusBottom: 0,
    space: 0,
    list: [],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<MagicCubeProperty>;
