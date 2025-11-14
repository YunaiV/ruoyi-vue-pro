import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    /** 后端路由模式 */
    accessMode: 'backend',
    name: import.meta.env.VITE_APP_TITLE,
    enableRefreshToken: true,
  },
  footer: {
    /** 默认关闭 footer 页脚，因为有一定遮挡 */
    enable: false,
    fixed: false,
  },
  copyright: {
    companyName: import.meta.env.VITE_APP_TITLE,
    companySiteLink: 'https://gitee.com/yudaocode/yudao-ui-admin-vben',
  },
});
