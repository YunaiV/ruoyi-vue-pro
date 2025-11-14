import type { Component } from 'vue';

interface IContextMenuItem {
  /**
   * @zh_CN 是否禁用
   */
  disabled?: boolean;
  /**
   * @zh_CN 点击事件处理
   * @param data
   */
  handler?: (data: any) => void;
  /**
   * @zh_CN 图标
   */
  icon?: Component;
  /**
   * @zh_CN 是否显示图标
   */
  inset?: boolean;
  /**
   * @zh_CN 唯一标识
   */
  key: string;
  /**
   * @zh_CN 是否是分割线
   */
  separator?: boolean;
  /**
   * @zh_CN 快捷键
   */
  shortcut?: string;
  /**
   * @zh_CN 标题
   */
  text: string;
}
export type { IContextMenuItem };
