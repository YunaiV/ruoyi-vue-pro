import type { StyleValue } from 'vue';

import type { HotZoneItemProperty } from '#/components/diy-editor/components/mobile/HotZone/config';

// 热区的最小宽高
export const HOT_ZONE_MIN_SIZE = 100;

// 控制的类型
export enum CONTROL_TYPE_ENUM {
  LEFT,
  TOP,
  WIDTH,
  HEIGHT,
}

// 定义热区的控制点
export interface ControlDot {
  position: string;
  types: CONTROL_TYPE_ENUM[];
  style: StyleValue;
}

// 热区的8个控制点
export const CONTROL_DOT_LIST = [
  {
    position: '左上角',
    types: [
      CONTROL_TYPE_ENUM.LEFT,
      CONTROL_TYPE_ENUM.TOP,
      CONTROL_TYPE_ENUM.WIDTH,
      CONTROL_TYPE_ENUM.HEIGHT,
    ],
    style: { left: '-5px', top: '-5px', cursor: 'nwse-resize' },
  },
  {
    position: '上方中间',
    types: [CONTROL_TYPE_ENUM.TOP, CONTROL_TYPE_ENUM.HEIGHT],
    style: {
      left: '50%',
      top: '-5px',
      cursor: 'n-resize',
      transform: 'translateX(-50%)',
    },
  },
  {
    position: '右上角',
    types: [
      CONTROL_TYPE_ENUM.TOP,
      CONTROL_TYPE_ENUM.WIDTH,
      CONTROL_TYPE_ENUM.HEIGHT,
    ],
    style: { right: '-5px', top: '-5px', cursor: 'nesw-resize' },
  },
  {
    position: '右侧中间',
    types: [CONTROL_TYPE_ENUM.WIDTH],
    style: {
      right: '-5px',
      top: '50%',
      cursor: 'e-resize',
      transform: 'translateX(-50%)',
    },
  },
  {
    position: '右下角',
    types: [CONTROL_TYPE_ENUM.WIDTH, CONTROL_TYPE_ENUM.HEIGHT],
    style: { right: '-5px', bottom: '-5px', cursor: 'nwse-resize' },
  },
  {
    position: '下方中间',
    types: [CONTROL_TYPE_ENUM.HEIGHT],
    style: {
      left: '50%',
      bottom: '-5px',
      cursor: 's-resize',
      transform: 'translateX(-50%)',
    },
  },
  {
    position: '左下角',
    types: [
      CONTROL_TYPE_ENUM.LEFT,
      CONTROL_TYPE_ENUM.WIDTH,
      CONTROL_TYPE_ENUM.HEIGHT,
    ],
    style: { left: '-5px', bottom: '-5px', cursor: 'nesw-resize' },
  },
  {
    position: '左侧中间',
    types: [CONTROL_TYPE_ENUM.LEFT, CONTROL_TYPE_ENUM.WIDTH],
    style: {
      left: '-5px',
      top: '50%',
      cursor: 'w-resize',
      transform: 'translateX(-50%)',
    },
  },
] as ControlDot[];

// region 热区的缩放
// 热区的缩放比例
export const HOT_ZONE_SCALE_RATE = 2;
// 缩小：缩回适合手机屏幕的大小
export const zoomOut = (list?: HotZoneItemProperty[]) => {
  return (
    list?.map((hotZone) => ({
      ...hotZone,
      left: (hotZone.left /= HOT_ZONE_SCALE_RATE),
      top: (hotZone.top /= HOT_ZONE_SCALE_RATE),
      width: (hotZone.width /= HOT_ZONE_SCALE_RATE),
      height: (hotZone.height /= HOT_ZONE_SCALE_RATE),
    })) || []
  );
};
// 放大：作用是为了方便在电脑屏幕上编辑
export const zoomIn = (list?: HotZoneItemProperty[]) => {
  return (
    list?.map((hotZone) => ({
      ...hotZone,
      left: (hotZone.left *= HOT_ZONE_SCALE_RATE),
      top: (hotZone.top *= HOT_ZONE_SCALE_RATE),
      width: (hotZone.width *= HOT_ZONE_SCALE_RATE),
      height: (hotZone.height *= HOT_ZONE_SCALE_RATE),
    })) || []
  );
};
// endregion

/**
 * 封装热区拖拽
 *
 * 注：为什么不使用vueuse的useDraggable。在本场景下，其使用方式比较复杂
 * @param hotZone 热区
 * @param downEvent 鼠标按下事件
 * @param callback 回调函数
 */
export const useDraggable = (
  hotZone: HotZoneItemProperty,
  downEvent: MouseEvent,
  callback: (
    left: number,
    top: number,
    width: number,
    height: number,
    moveWidth: number,
    moveHeight: number,
  ) => void,
) => {
  // 阻止事件冒泡
  downEvent.stopPropagation();

  // 移动前的鼠标坐标
  const { clientX: startX, clientY: startY } = downEvent;
  // 移动前的热区坐标、大小
  const { left, top, width, height } = hotZone;

  // 监听鼠标移动
  const handleMouseMove = (e: MouseEvent) => {
    // 移动宽度
    const moveWidth = e.clientX - startX;
    // 移动高度
    const moveHeight = e.clientY - startY;
    // 移动回调
    callback(left, top, width, height, moveWidth, moveHeight);
  };

  // 松开鼠标后，结束拖拽
  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  };

  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
};
