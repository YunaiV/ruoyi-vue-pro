import type { CSSProperties } from 'vue';

import type { ClassType } from '@vben/types';

export interface CaptchaData {
  /**
   * x
   */
  x: number;
  /**
   * y
   */
  y: number;
  /**
   * 时间戳
   */
  t: number;
}
export interface CaptchaPoint extends CaptchaData {
  /**
   * 数据索引
   */
  i: number;
}
export interface PointSelectionCaptchaCardProps {
  /**
   * 验证码图片
   */
  captchaImage: string;
  /**
   * 验证码图片高度
   * @default '220px'
   */
  height?: number | string;
  /**
   * 水平内边距
   * @default '12px'
   */
  paddingX?: number | string;
  /**
   * 垂直内边距
   * @default '16px'
   */
  paddingY?: number | string;
  /**
   * 标题
   * @default '请按图依次点击'
   */
  title?: string;
  /**
   * 验证码图片宽度
   * @default '300px'
   */
  width?: number | string;
}

export interface PointSelectionCaptchaProps
  extends PointSelectionCaptchaCardProps {
  /**
   * 是否展示确定按钮
   * @default false
   */
  showConfirm?: boolean;
  /**
   * 提示图片
   * @default ''
   */
  hintImage?: string;
  /**
   * 提示文本
   * @default ''
   */
  hintText?: string;
}

export interface SliderCaptchaProps {
  class?: ClassType;
  /**
   * @description 滑块的样式
   * @default {}
   */
  actionStyle?: CSSProperties;

  /**
   * @description 滑块条的样式
   * @default {}
   */
  barStyle?: CSSProperties;

  /**
   * @description 内容的样式
   * @default {}
   */
  contentStyle?: CSSProperties;

  /**
   * @description 组件的样式
   * @default {}
   */
  wrapperStyle?: CSSProperties;

  /**
   * @description 是否作为插槽使用，用于联动组件，可参考旋转校验组件
   * @default false
   */
  isSlot?: boolean;

  /**
   * @description 验证成功的提示
   * @default '验证通过'
   */
  successText?: string;

  /**
   * @description 提示文字
   * @default '请按住滑块拖动'
   */
  text?: string;
}

export interface SliderRotateCaptchaProps {
  /**
   * @description 旋转的角度
   * @default 20
   */
  diffDegree?: number;

  /**
   * @description 图片的宽度
   * @default 260
   */
  imageSize?: number;

  /**
   * @description 图片的样式
   * @default {}
   */
  imageWrapperStyle?: CSSProperties;

  /**
   * @description 最大旋转角度
   * @default 270
   */
  maxDegree?: number;

  /**
   * @description 最小旋转角度
   * @default 90
   */
  minDegree?: number;

  /**
   * @description 图片的地址
   */
  src?: string;
  /**
   * @description 默认提示文本
   */
  defaultTip?: string;
}

export interface SliderTranslateCaptchaProps {
  /**
   * @description 拼图的宽度
   * @default 420
   */
  canvasWidth?: number;
  /**
   * @description 拼图的高度
   * @default 280
   */
  canvasHeight?: number;
  /**
   * @description 切块上正方形的长度
   * @default 42
   */
  squareLength?: number;
  /**
   * @description 切块上圆形的半径
   * @default 10
   */
  circleRadius?: number;
  /**
   * @description 图片的地址
   */
  src?: string;
  /**
   * @description 允许的最大差距
   * @default 3
   */
  diffDistance?: number;
  /**
   * @description 默认提示文本
   */
  defaultTip?: string;
}

export interface CaptchaVerifyPassingData {
  isPassing: boolean;
  time: number | string;
}

export interface SliderCaptchaActionType {
  resume: () => void;
}

export interface SliderRotateVerifyPassingData {
  event: MouseEvent | TouchEvent;
  moveDistance: number;
  moveX: number;
}
