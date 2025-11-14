/**
 * 更新 CSS 变量的函数
 * @param variables 要更新的 CSS 变量与其新值的映射
 */
function updateCSSVariables(
  variables: { [key: string]: string },
  id = '__vben-styles__',
): void {
  // 获取或创建内联样式表元素
  const styleElement =
    document.querySelector(`#${id}`) || document.createElement('style');

  styleElement.id = id;

  // 构建要更新的 CSS 变量的样式文本
  let cssText = ':root {';
  for (const key in variables) {
    if (Object.prototype.hasOwnProperty.call(variables, key)) {
      cssText += `${key}: ${variables[key]};`;
    }
  }
  cssText += '}';

  // 将样式文本赋值给内联样式表
  styleElement.textContent = cssText;

  // 将内联样式表添加到文档头部
  if (!document.querySelector(`#${id}`)) {
    setTimeout(() => {
      document.head.append(styleElement);
    });
  }
}

export { updateCSSVariables };
