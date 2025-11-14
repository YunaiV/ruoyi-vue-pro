type LayoutType =
  | 'full-content'
  | 'header-mixed-nav'
  | 'header-nav'
  | 'header-sidebar-nav'
  | 'mixed-nav'
  | 'sidebar-mixed-nav'
  | 'sidebar-nav';

type ThemeModeType = 'auto' | 'dark' | 'light';

/**
 * 偏好设置按钮位置
 * fixed 固定在右侧
 * header 顶栏
 * auto 自动
 */
type PreferencesButtonPositionType = 'auto' | 'fixed' | 'header';

type BuiltinThemeType =
  | 'custom'
  | 'deep-blue'
  | 'deep-green'
  | 'default'
  | 'gray'
  | 'green'
  | 'neutral'
  | 'orange'
  | 'pink'
  | 'red'
  | 'rose'
  | 'sky-blue'
  | 'slate'
  | 'stone'
  | 'violet'
  | 'yellow'
  | 'zinc'
  | (Record<never, never> & string);

type ContentCompactType = 'compact' | 'wide';

type LayoutHeaderModeType = 'auto' | 'auto-scroll' | 'fixed' | 'static';
type LayoutHeaderMenuAlignType = 'center' | 'end' | 'start';

/**
 * 登录过期模式
 * modal 弹窗模式
 * page 页面模式
 */
type LoginExpiredModeType = 'modal' | 'page';

/**
 * 面包屑样式
 * background 背景
 * normal 默认
 */
type BreadcrumbStyleType = 'background' | 'normal';

/**
 * 权限模式
 * backend 后端权限模式
 * frontend 前端权限模式
 * mixed 混合权限模式
 */
type AccessModeType = 'backend' | 'frontend' | 'mixed';

/**
 * 导航风格
 * plain 朴素
 * rounded 圆润
 */
type NavigationStyleType = 'plain' | 'rounded';

/**
 * 标签栏风格
 * brisk 轻快
 * card 卡片
 * chrome 谷歌
 * plain 朴素
 */
type TabsStyleType = 'brisk' | 'card' | 'chrome' | 'plain';

/**
 * 页面切换动画
 */
type PageTransitionType = 'fade' | 'fade-down' | 'fade-slide' | 'fade-up';

/**
 * 页面切换动画
 * panel-center 居中布局
 * panel-left 居左布局
 * panel-right 居右布局
 */
type AuthPageLayoutType = 'panel-center' | 'panel-left' | 'panel-right';

export type {
  AccessModeType,
  AuthPageLayoutType,
  BreadcrumbStyleType,
  BuiltinThemeType,
  ContentCompactType,
  LayoutHeaderMenuAlignType,
  LayoutHeaderModeType,
  LayoutType,
  LoginExpiredModeType,
  NavigationStyleType,
  PageTransitionType,
  PreferencesButtonPositionType,
  TabsStyleType,
  ThemeModeType,
};
