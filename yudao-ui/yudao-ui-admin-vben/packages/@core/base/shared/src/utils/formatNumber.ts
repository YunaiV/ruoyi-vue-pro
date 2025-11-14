import { isEmpty, isString, isUndefined } from './inference';

/**
 * 将一个整数转换为分数保留传入的小数
 * @param num
 * @param digit
 */
export function formatToFractionDigit(
  num: number | string | undefined,
  digit: number = 2,
): string {
  if (isUndefined(num)) return '0.00';
  const parsedNumber = isString(num) ? Number.parseFloat(num) : num;
  return (parsedNumber / 100).toFixed(digit);
}

/**
 * 将一个整数转换为分数保留两位小数
 * @param num
 */
export function formatToFraction(num: number | string | undefined): string {
  return formatToFractionDigit(num, 2);
}

/**
 * 将一个数转换为 1.00 这样
 * 数据呈现的时候使用
 *
 * @param num 整数
 */
export function floatToFixed2(num: number | string | undefined): string {
  let str = '0.00';
  if (isUndefined(num)) return str;
  const f = formatToFraction(num);
  const decimalPart = f.toString().split('.')[1];
  const len = decimalPart ? decimalPart.length : 0;
  switch (len) {
    case 0: {
      str = `${f.toString()}.00`;
      break;
    }
    case 1: {
      str = `${f.toString()}0`;
      break;
    }
    case 2: {
      str = f.toString();
      break;
    }
  }
  return str;
}

/**
 * 将一个分数转换为整数
 * @param num
 */
export function convertToInteger(num: number | string | undefined): number {
  if (isUndefined(num)) return 0;
  const parsedNumber = isString(num) ? Number.parseFloat(num) : num;
  return Math.round(parsedNumber * 100);
}

/**
 * 元转分
 */
export function yuanToFen(amount: number | string): number {
  return convertToInteger(amount);
}

/**
 * 分转元
 */
export function fenToYuan(price: number | string): string {
  return formatToFraction(price);
}

// 格式化金额【分转元】
export const fenToYuanFormat = (_: any, __: any, cellValue: any, ___: any) => {
  return `￥${floatToFixed2(cellValue)}`;
};

/**
 * 计算环比
 *
 * @param value 当前数值
 * @param reference 对比数值
 */
export function calculateRelativeRate(
  value?: number,
  reference?: number,
): number {
  // 防止除0
  if (!reference || reference === 0) return 0;

  return Number.parseFloat(
    ((100 * ((value || 0) - reference)) / reference).toFixed(0),
  );
}

// ========== ERP 专属方法 ==========

const ERP_COUNT_DIGIT = 3;
const ERP_PRICE_DIGIT = 2;

/**
 * 【ERP】格式化 Input 数字
 *
 * 例如说：库存数量
 *
 * @param num 数量
 * @package
 * @return 格式化后的数量
 */
export function erpNumberFormatter(
  num: number | string | undefined,
  digit: number,
) {
  if (num === null || num === undefined) {
    return '';
  }
  if (typeof num === 'string') {
    num = Number.parseFloat(num);
  }
  // 如果非 number，则直接返回空串
  if (Number.isNaN(num)) {
    return '';
  }
  return num.toFixed(digit);
}

/**
 * 【ERP】格式化数量，保留三位小数
 *
 * 例如说：库存数量
 *
 * @param num 数量
 * @return 格式化后的数量
 */
export function erpCountInputFormatter(num: number | string | undefined) {
  return erpNumberFormatter(num, ERP_COUNT_DIGIT);
}

/**
 * 【ERP】格式化数量，保留三位小数
 *
 * @param cellValue 数量
 * @return 格式化后的数量
 */
export function erpCountTableColumnFormatter(cellValue: any) {
  return erpNumberFormatter(cellValue, ERP_COUNT_DIGIT);
}

/**
 * 【ERP】格式化金额，保留二位小数
 *
 * 例如说：库存数量
 *
 * @param num 数量
 * @return 格式化后的数量
 */
export function erpPriceInputFormatter(num: number | string | undefined) {
  return erpNumberFormatter(num, ERP_PRICE_DIGIT);
}

/**
 * 【ERP】格式化金额，保留二位小数
 *
 * @param cellValue 数量
 * @return 格式化后的数量
 */
export function erpPriceTableColumnFormatter(cellValue: any) {
  return erpNumberFormatter(cellValue, ERP_PRICE_DIGIT);
}

/**
 * 【ERP】价格计算，四舍五入保留两位小数
 *
 * @param price 价格
 * @param count 数量
 * @return 总价格。如果有任一为空，则返回 undefined
 */
export function erpPriceMultiply(price: number, count: number) {
  if (isEmpty(price) || isEmpty(count)) return undefined;
  return Number.parseFloat((price * count).toFixed(ERP_PRICE_DIGIT));
}

/**
 * 【ERP】百分比计算，四舍五入保留两位小数
 *
 * 如果 total 为 0，则返回 0
 *
 * @param value 当前值
 * @param total 总值
 */
export function erpCalculatePercentage(value: number, total: number) {
  if (total === 0) return 0;
  return ((value / total) * 100).toFixed(2);
}
