import type { ComponentStyle, DiyComponent } from '../../../util';

/** 营销文章属性 */
export interface PromotionArticleProperty {
  id: number; // 文章编号
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'PromotionArticle',
  name: '营销文章',
  icon: 'ph:article-medium',
  property: {
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<PromotionArticleProperty>;
