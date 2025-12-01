import type { ComponentStyle, DiyComponent } from '../../../util';

/** 广告魔方属性 */
export interface MagicCubeProperty {
  borderRadiusTop: number; // 上圆角
  borderRadiusBottom: number; // 下圆角
  space: number; // 间隔
  list: MagicCubeItemProperty[]; // 导航菜单列表
  style: ComponentStyle; // 组件样式
}

/** 广告魔方项目属性 */
export interface MagicCubeItemProperty {
  imgUrl: string; // 图标链接
  url: string; // 链接
  width: number; // 宽
  height: number; // 高
  top: number; // 上
  left: number; // 左
}

/** 定义组件 */
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
