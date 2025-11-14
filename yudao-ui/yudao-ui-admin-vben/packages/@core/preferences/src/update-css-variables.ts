import type { Preferences } from './types';

import { generatorColorVariables } from '@vben-core/shared/color';
import { updateCSSVariables as executeUpdateCSSVariables } from '@vben-core/shared/utils';

import { BUILT_IN_THEME_PRESETS } from './constants';

/**
 * 更新主题的 CSS 变量以及其他 CSS 变量
 * @param preferences - 当前偏好设置对象，它的主题值将被用来设置文档的主题。
 */
function updateCSSVariables(preferences: Preferences) {
  // 当修改到颜色变量时，更新 css 变量
  const root = document.documentElement;
  if (!root) {
    return;
  }

  const theme = preferences?.theme ?? {};

  const { builtinType, mode, radius } = theme;

  // html 设置 dark 类
  if (Reflect.has(theme, 'mode')) {
    const dark = isDarkTheme(mode);
    root.classList.toggle('dark', dark);
  }

  // html 设置 data-theme=[builtinType]
  if (Reflect.has(theme, 'builtinType')) {
    const rootTheme = root.dataset.theme;
    if (rootTheme !== builtinType) {
      root.dataset.theme = builtinType;
    }
  }

  // 获取当前的内置主题
  const currentBuiltType = [...BUILT_IN_THEME_PRESETS].find(
    (item) => item.type === builtinType,
  );

  let builtinTypeColorPrimary: string | undefined = '';

  if (currentBuiltType) {
    const isDark = isDarkTheme(preferences.theme.mode);
    // 设置不同主题的主要颜色
    const color = isDark
      ? currentBuiltType.darkPrimaryColor || currentBuiltType.primaryColor
      : currentBuiltType.primaryColor;
    builtinTypeColorPrimary = color || currentBuiltType.color;
  }

  // 如果内置主题颜色和自定义颜色都不存在，则不更新主题颜色
  if (
    builtinTypeColorPrimary ||
    Reflect.has(theme, 'colorPrimary') ||
    Reflect.has(theme, 'colorDestructive') ||
    Reflect.has(theme, 'colorSuccess') ||
    Reflect.has(theme, 'colorWarning')
  ) {
    // preferences.theme.colorPrimary = builtinTypeColorPrimary || colorPrimary;
    updateMainColorVariables(preferences);
  }

  // 更新圆角
  if (Reflect.has(theme, 'radius')) {
    document.documentElement.style.setProperty('--radius', `${radius}rem`);
  }
}

/**
 * 更新主要的 CSS 变量
 * @param  preference - 当前偏好设置对象，它的颜色值将被转换成 HSL 格式并设置为 CSS 变量。
 */
function updateMainColorVariables(preference: Preferences) {
  if (!preference.theme) {
    return;
  }
  const { colorDestructive, colorPrimary, colorSuccess, colorWarning } =
    preference.theme;

  const colorVariables = generatorColorVariables([
    { color: colorPrimary, name: 'primary' },
    { alias: 'warning', color: colorWarning, name: 'yellow' },
    { alias: 'success', color: colorSuccess, name: 'green' },
    { alias: 'destructive', color: colorDestructive, name: 'red' },
  ]);

  // 要设置的 CSS 变量映射
  const colorMappings = {
    '--green-500': '--success',
    '--primary-500': '--primary',
    '--red-500': '--destructive',
    '--yellow-500': '--warning',
  };

  // 统一处理颜色变量的更新
  Object.entries(colorMappings).forEach(([sourceVar, targetVar]) => {
    const colorValue = colorVariables[sourceVar];
    if (colorValue) {
      document.documentElement.style.setProperty(targetVar, colorValue);
    }
  });

  executeUpdateCSSVariables(colorVariables);
}

function isDarkTheme(theme: string) {
  let dark = theme === 'dark';
  if (theme === 'auto') {
    dark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  return dark;
}

export { isDarkTheme, updateCSSVariables };
