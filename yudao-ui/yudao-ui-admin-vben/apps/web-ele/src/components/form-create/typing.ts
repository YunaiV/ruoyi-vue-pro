/** 数据字典 Select 选择器组件 Props 类型 */
export interface DictSelectProps {
  dictType: string; // 字典类型
  valueType?: 'bool' | 'int' | 'str'; // 字典值类型
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
