export interface JsonViewerProps {
  /** 要展示的结构数据 */
  value: any;
  /** 展开深度 */
  expandDepth?: number;
  /** 是否可复制 */
  copyable?: boolean;
  /** 是否排序 */
  sort?: boolean;
  /** 显示边框 */
  boxed?: boolean;
  /** 主题 */
  theme?: string;
  /** 是否展开 */
  expanded?: boolean;
  /** 时间格式化函数 */
  timeformat?: (time: Date | number | string) => string;
  /** 预览模式 */
  previewMode?: boolean;
  /** 显示数组索引 */
  showArrayIndex?: boolean;
  /** 显示双引号 */
  showDoubleQuotes?: boolean;
}

export interface JsonViewerAction {
  action: string;
  text: string;
  trigger: HTMLElement;
}

export interface JsonViewerValue {
  value: any;
  path: string;
  depth: number;
  el: HTMLElement;
}

export interface JsonViewerToggle {
  /** 鼠标事件 */
  event: MouseEvent;
  /** 当前展开状态 */
  open: boolean;
}
