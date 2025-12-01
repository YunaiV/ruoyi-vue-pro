import type { DiyComponent } from '../../../util';

/** 分割线属性 */
export interface DividerProperty {
  height: number; // 高度
  lineWidth: number; // 线宽
  paddingType: 'horizontal' | 'none'; // 边距类型
  lineColor: string; // 颜色
  borderType: 'dashed' | 'dotted' | 'none' | 'solid'; // 类型
}

/** 定义组件 */
export const component = {
  id: 'Divider',
  name: '分割线',
  icon: 'tdesign:component-divider-vertical',
  property: {
    height: 30,
    lineWidth: 1,
    paddingType: 'none',
    lineColor: '#dcdfe6',
    borderType: 'solid',
  },
} as DiyComponent<DividerProperty>;
