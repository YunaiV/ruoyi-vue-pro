// 坐标点
export interface Point {
  x: number;
  y: number;
}

// 矩形
export interface Rect {
  // 左上角 X 轴坐标
  left: number;
  // 左上角 Y 轴坐标
  top: number;
  // 右下角 X 轴坐标
  right: number;
  // 右下角 Y 轴坐标
  bottom: number;
  // 矩形宽度
  width: number;
  // 矩形高度
  height: number;
}

/**
 * 判断两个矩形是否重叠
 * @param a 矩形 A
 * @param b 矩形 B
 */
export const isOverlap = (a: Rect, b: Rect): boolean => {
  return (
    a.left < b.left + b.width &&
    a.left + a.width > b.left &&
    a.top < b.top + b.height &&
    a.height + a.top > b.top
  );
};
/**
 * 检查坐标点是否在矩形内
 * @param hotArea 矩形
 * @param point 坐标
 */
export const isContains = (hotArea: Rect, point: Point): boolean => {
  return (
    point.x >= hotArea.left &&
    point.x < hotArea.right &&
    point.y >= hotArea.top &&
    point.y < hotArea.bottom
  );
};

/**
 * 在两个坐标点中间，创建一个矩形
 *
 * 存在以下情况：
 * 1. 两个坐标点是同一个位置，只占一个位置的正方形，宽高都为 1
 * 2. X 轴坐标相同，只占一行的矩形，高度为 1
 * 3. Y 轴坐标相同，只占一列的矩形，宽度为 1
 * 4. 多行多列的矩形
 *
 * @param a 坐标点一
 * @param b 坐标点二
 */
export const createRect = (a: Point, b: Point): Rect => {
  // 计算矩形的范围
  const [left, left2] = [a.x, b.x].sort();
  const [top, top2] = [a.y, b.y].sort();
  const right = left2 + 1;
  const bottom = top2 + 1;
  const height = bottom - top;
  const width = right - left;

  return { left, right, top, bottom, height, width };
};
