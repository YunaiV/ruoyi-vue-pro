import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 营销文章属性 */
export interface PromotionArticleProperty {
  // 文章编号
  id: number;
  // 组件样式
  style: ComponentStyle;
}

// 定义组件
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
