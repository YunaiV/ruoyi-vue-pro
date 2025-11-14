/**
 * 创建验证类名的工具函数
 * @param isValidating 验证状态
 * @param fieldName 字段名
 * @param validationRules 验证规则，可以是字符串或自定义函数
 * @returns 返回 className 函数
 */
function createValidationClassName(
  isValidating: any,
  fieldName: string,
  validationRules: ((row: any) => boolean) | string,
) {
  return ({ row }: { row: any }) => {
    if (!isValidating?.value) return '';

    let isValid = true;
    if (typeof validationRules === 'string') {
      // 处理简单的验证规则
      if (validationRules === 'required') {
        isValid =
          fieldName === 'count'
            ? row[fieldName] && row[fieldName] > 0
            : !!row[fieldName];
      }
    } else if (typeof validationRules === 'function') {
      // 处理自定义验证函数
      isValid = validationRules(row);
    }

    return isValid ? '' : 'required-field-error';
  };
}

/**
 * 创建必填字段验证
 * @param isValidating 验证状态
 * @param fieldName 字段名
 * @returns 返回 className 函数
 */
function createRequiredValidation(isValidating: any, fieldName: string) {
  return createValidationClassName(isValidating, fieldName, 'required');
}

/**
 * 创建自定义验证
 * @param isValidating 验证状态
 * @param validationFn 自定义验证函数
 * @returns 返回 className 函数
 */
function createCustomValidation(
  isValidating: any,
  validationFn: (row: any) => boolean,
) {
  return createValidationClassName(isValidating, '', validationFn);
}

export {
  createCustomValidation,
  createRequiredValidation,
  createValidationClassName,
};
