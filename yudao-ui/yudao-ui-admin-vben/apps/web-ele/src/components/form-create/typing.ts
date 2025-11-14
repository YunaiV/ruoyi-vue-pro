import type { Rule } from '@form-create/element-ui'; // 左侧拖拽按钮

/** 数据字典 Select 选择器组件 Props 类型 */
export interface DictSelectProps {
  dictType: string; // 字典类型
  valueType?: 'bool' | 'int' | 'str'; // 字典值类型 TODO @芋艿：'boolean' | 'number' | 'string'；需要和 vue3 一起统一！
  selectType?: 'checkbox' | 'radio' | 'select'; // 选择器类型，下拉框 select、多选框 checkbox、单选框 radio
  formCreateInject?: any;
}

/** 左侧拖拽按钮 */
export interface MenuItem {
  label: string;
  name: string;
  icon: string;
}

/** 左侧拖拽按钮分类 */
export interface Menu {
  title: string;
  name: string;
  list: MenuItem[];
}

export type MenuList = Array<Menu>;

// TODO @dhb52：MenuList、Menu、MenuItem、DragRule 这几个，是不是没用到呀？
// 拖拽组件的规则
export interface DragRule {
  icon: string;
  name: string;
  label: string;
  children?: string;
  inside?: true;
  drag?: string | true;
  dragBtn?: false;
  mask?: false;

  rule(): Rule;

  props(v: any, v1: any): Rule[];
}

/** 通用 API 下拉组件 Props 类型 */
export interface ApiSelectProps {
  name: string; // 组件名称
  labelField?: string; // 选项标签
  valueField?: string; // 选项的值
  url?: string; // url 接口
  isDict?: boolean; // 是否字典选择器
}

/** 选择组件规则配置类型 */
export interface SelectRuleOption {
  label: string; // label 名称
  name: string; // 组件名称
  icon: string; // 组件图标
  props?: any[]; // 组件规则
  event?: any[]; // 事件配置
}
