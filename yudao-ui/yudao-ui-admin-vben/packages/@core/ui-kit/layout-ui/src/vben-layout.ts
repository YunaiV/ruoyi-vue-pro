import type {
  ContentCompactType,
  LayoutHeaderModeType,
  LayoutType,
  ThemeModeType,
} from '@vben-core/typings';

interface VbenLayoutProps {
  /**
   * 内容区域定宽
   * @default 'wide'
   */
  contentCompact?: ContentCompactType;
  /**
   * 定宽布局宽度
   * @default 1200
   */
  contentCompactWidth?: number;
  /**
   * padding
   * @default 16
   */
  contentPadding?: number;
  /**
   * paddingBottom
   * @default 16
   */
  contentPaddingBottom?: number;
  /**
   * paddingLeft
   * @default 16
   */
  contentPaddingLeft?: number;
  /**
   * paddingRight
   * @default 16
   */
  contentPaddingRight?: number;
  /**
   * paddingTop
   * @default 16
   */
  contentPaddingTop?: number;
  /**
   * footer 是否可见
   * @default false
   */
  footerEnable?: boolean;
  /**
   * footer 是否固定
   * @default true
   */
  footerFixed?: boolean;
  /**
   * footer 高度
   * @default 32
   */
  footerHeight?: number;

  /**
   * header高度
   * @default 48
   */
  headerHeight?: number;
  /**
   * 顶栏是否隐藏
   * @default false
   */
  headerHidden?: boolean;
  /**
   * header 显示模式
   * @default 'fixed'
   */
  headerMode?: LayoutHeaderModeType;
  /**
   * header 顶栏主题
   */
  headerTheme?: ThemeModeType;
  /**
   * 是否显示header切换侧边栏按钮
   * @default
   */
  headerToggleSidebarButton?: boolean;
  /**
   * header是否显示
   * @default true
   */
  headerVisible?: boolean;
  /**
   * 是否移动端显示
   * @default false
   */
  isMobile?: boolean;
  /**
   * 布局方式
   * sidebar-nav 侧边菜单布局
   * header-nav 顶部菜单布局
   * mixed-nav 侧边&顶部菜单布局
   * sidebar-mixed-nav 侧边混合菜单布局
   * full-content 全屏内容布局
   * @default sidebar-nav
   */
  layout?: LayoutType;
  /**
   * 侧边菜单折叠状态
   * @default false
   */
  sidebarCollapse?: boolean;
  /**
   * 侧边菜单折叠按钮
   * @default true
   */
  sidebarCollapsedButton?: boolean;
  /**
   * 侧边菜单是否折叠时，是否显示title
   * @default true
   */
  sidebarCollapseShowTitle?: boolean;
  /**
   * 侧边栏是否可见
   * @default true
   */
  sidebarEnable?: boolean;
  /**
   * 侧边菜单折叠额外宽度
   * @default 48
   */
  sidebarExtraCollapsedWidth?: number;
  /**
   * 侧边菜单折叠按钮是否固定
   * @default true
   */
  sidebarFixedButton?: boolean;
  /**
   * 侧边栏是否隐藏
   * @default false
   */
  sidebarHidden?: boolean;
  /**
   * 混合侧边栏宽度
   * @default 80
   */
  sidebarMixedWidth?: number;
  /**
   * 侧边栏
   * @default dark
   */
  sidebarTheme?: ThemeModeType;
  /**
   * 侧边栏宽度
   * @default 210
   */
  sidebarWidth?: number;
  /**
   *  侧边菜单折叠宽度
   * @default 48
   */
  sideCollapseWidth?: number;
  /**
   * tab是否可见
   * @default true
   */
  tabbarEnable?: boolean;
  /**
   * tab高度
   * @default 30
   */
  tabbarHeight?: number;
  /**
   * zIndex
   * @default 100
   */
  zIndex?: number;
}
export type { VbenLayoutProps };
