import type { Component } from 'vue';
import type { RouteMeta, RouteRecordRaw } from 'vue-router';

/** 路由元信息 */
interface AppRouteRecordRaw extends Omit<RouteRecordRaw, 'meta'> {
  children?: AppRouteRecordRaw[];
  component?: any;
  componentName?: string;
  components?: any;
  fullPath?: string;
  icon?: string;
  id?: any;
  keepAlive?: boolean;
  meta: RouteMeta;
  name: string;
  parentId?: number;
  props?: any;
  sort?: number;
  visible?: boolean;
}

/**
 * 扩展路由原始对象
 */
type ExRouteRecordRaw = RouteRecordRaw & {
  parent?: string;
  parents?: string[];
  path?: any;
};

interface MenuRecordBadgeRaw {
  /**
   * 徽标
   */
  badge?: string;
  /**
   * 徽标类型
   */
  badgeType?: 'dot' | 'normal';
  /**
   * 徽标颜色
   */
  badgeVariants?: 'destructive' | 'primary' | string;
}

/**
 * 菜单原始对象
 */
interface MenuRecordRaw extends MenuRecordBadgeRaw {
  /**
   * 激活时的图标名
   */
  activeIcon?: string;
  /**
   * 子菜单
   */
  children?: MenuRecordRaw[];
  /**
   * 是否禁用菜单
   * @default false
   */
  disabled?: boolean;
  /**
   * 图标名
   */
  icon?: Component | string;
  /**
   * 菜单名
   */
  name: string;
  /**
   * 排序号
   */
  order?: number;
  /**
   * 父级路径
   */
  parent?: string;
  /**
   * 所有父级路径
   */
  parents?: string[];
  /**
   * 菜单路径，唯一，可当作key
   */
  path: string;
  /**
   * 是否显示菜单
   * @default true
   */
  show?: boolean;
}

export type {
  AppRouteRecordRaw,
  ExRouteRecordRaw,
  MenuRecordBadgeRaw,
  MenuRecordRaw,
};
