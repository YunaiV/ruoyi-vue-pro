import type { ComponentStyle, DiyComponent } from '../../../util';

/** 热区属性 */
export interface HotZoneProperty {
  imgUrl: string; // 图片地址
  list: HotZoneItemProperty[]; // 导航菜单列表
  style: ComponentStyle; // 组件样式
}

/** 热区项目属性 */
export interface HotZoneItemProperty {
  name: string; // 链接的名称
  url: string; // 链接
  width: number; // 宽
  height: number; // 高
  top: number; // 上
  left: number; // 左
}

/** 定义组件 */
export const component = {
  id: 'HotZone',
  name: '热区',
  icon: 'tabler:hand-click',
  property: {
    imgUrl: '',
    list: [] as HotZoneItemProperty[],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<HotZoneProperty>;
