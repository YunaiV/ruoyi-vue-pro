import type { Component } from 'vue';

interface VbenDropdownMenuItem {
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
   * @zh_CN 标题
   */
  label: string;
  /**
   * @zh_CN 是否是分割线
   */
  separator?: boolean;
  /**
   * @zh_CN 唯一标识
   */
  value: string;
}

interface DropdownMenuProps {
  menus: VbenDropdownMenuItem[];
}

export type { DropdownMenuProps, VbenDropdownMenuItem };
