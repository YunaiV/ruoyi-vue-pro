import type { DiyComponent } from '../../../util';

/** 页面设置属性 */
export interface PageConfigProperty {
  description: string; // 页面描述
  backgroundColor: string; // 页面背景颜色
  backgroundImage: string; // 页面背景图片
}

/** 定义页面组件 */
export const component = {
  id: 'PageConfig',
  name: '页面设置',
  icon: 'lucide:file-text',
  property: {
    description: '',
    backgroundColor: '#f5f5f5',
    backgroundImage: '',
  },
} as DiyComponent<PageConfigProperty>;
