import { MOBILE_REGEX } from './regex';

/**
 * 验证是否为手机号码（中国）
 *
 * @param value 值
 * @returns 是否为手机号码（中国）
 */
function isMobile(value?: null | string): boolean {
  if (!value) {
    return false;
  }
  return MOBILE_REGEX.test(value);
}

export { isMobile };
