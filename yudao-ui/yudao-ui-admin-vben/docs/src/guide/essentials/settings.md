# 配置

## 环境变量配置

项目的环境变量配置位于应用目录下的 `.env`、`.env.development`、`.env.production`。

规则与 [Vite Env Variables and Modes](https://vitejs.dev/guide/env-and-mode.html) 一致。格式如下：

```bash
.env                # 在所有的环境中被载入
.env.local          # 在所有的环境中被载入，但会被 git 忽略
.env.[mode]         # 只在指定的模式中被载入
.env.[mode].local   # 只在指定的模式中被载入，但会被 git 忽略
```

::: tip

- 只有以 `VITE_` 开头的变量会被嵌入到客户端侧的包中，你可以在项目代码中这样访问它们：

  ```ts
  console.log(import.meta.env.VITE_PROT);
  ```

- 以 `VITE_GLOB_*` 开头的的变量，在打包的时候，会被加入 `_app.config.js`配置文件当中.

:::

## 环境配置说明

::: code-group

```bash [.env]
# 应用标题
VITE_APP_TITLE=Vben Admin

# 应用命名空间，用于缓存、store等功能的前缀，确保隔离
VITE_APP_NAMESPACE=vben-web-antd
```

```bash [.env.development]
# 端口号
VITE_PORT=5555

# 资源公共路径,需要以 / 开头和结尾
VITE_BASE=/

# 接口地址
VITE_GLOB_API_URL=/api

# 是否开启 Nitro Mock服务，true 为开启，false 为关闭
VITE_NITRO_MOCK=true

# 是否打开 devtools，true 为打开，false 为关闭
VITE_DEVTOOLS=true

# 是否注入全局loading
VITE_INJECT_APP_LOADING=true

```

```bash [.env.production]
# 资源公共路径,需要以 / 开头和结尾
VITE_BASE=/

# 接口地址
VITE_GLOB_API_URL=https://mock-napi.vben.pro/api

# 是否开启压缩，可以设置为 none, brotli, gzip
VITE_COMPRESS=gzip

# 是否开启 PWA
VITE_PWA=false

# vue-router 的模式
VITE_ROUTER_HISTORY=hash

# 是否注入全局loading
VITE_INJECT_APP_LOADING=true

# 打包后是否生成dist.zip
VITE_ARCHIVER=true

```

:::

## 生产环境动态配置

当在大仓根目录下，执行 `pnpm build`构建项目之后，会自动在对应的应用下生成 `dist/_app.config.js`文件并插入 `index.html`。

`_app.config.js` 是一个动态配置文件，可以在项目构建之后，根据不同的环境动态修改配置。内容如下：

```ts
window._VBEN_ADMIN_PRO_APP_CONF_ = {
  VITE_GLOB_API_URL: 'https://mock-napi.vben.pro/api',
};
Object.freeze(window._VBEN_ADMIN_PRO_APP_CONF_);
Object.defineProperty(window, '_VBEN_ADMIN_PRO_APP_CONF_', {
  configurable: false,
  writable: false,
});
```

### 作用

`_app.config.js` 用于项目在打包后，需要动态修改配置的需求，如接口地址。不用重新进行打包，可在打包后修改 /`dist/_app.config.js` 内的变量，刷新即可更新代码内的局部变量。这里使用`js`文件，是为了确保配置文件加载顺序保持在前面。

### 使用

想要获取 `_app.config.js` 内的变量，需要使用`@vben/hooks`提供的 `useAppConfig`方法。

```ts
const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
```

### 新增

新增一个可动态修改的配置项，只需要按照如下步骤即可：

- 首先在 `.env` 或者对应的开发环境配置文件内，新增需要可动态配置的变量，需要以 `VITE_GLOB_*` 开头的变量，如：

  ```bash
  VITE_GLOB_OTHER_API_URL=https://mock-napi.vben.pro/other-api
  ```

- 在 `packages/types/global.d.ts`,新增对应的类型定义，如：

  ```ts
  export interface VbenAdminProAppConfigRaw {
    VITE_GLOB_API_URL: string;
    VITE_GLOB_OTHER_API_URL: string; // [!code ++]
  }

  export interface ApplicationConfig {
    apiURL: string;
    otherApiURL: string; // [!code ++]
  }
  ```

- 在 `packages/effects/hooks/src/use-app-config.ts` 中，新增对应的配置项，如：

  ```ts
  export function useAppConfig(
    env: Record<string, any>,
    isProduction: boolean,
  ): ApplicationConfig {
    // 生产环境下，直接使用 window._VBEN_ADMIN_PRO_APP_CONF_ 全局变量
    const config = isProduction
      ? window._VBEN_ADMIN_PRO_APP_CONF_
      : (env as VbenAdminProAppConfigRaw);

    const { VITE_GLOB_API_URL, VITE_GLOB_OTHER_API_URL } = config; // [!code ++]

    return {
      apiURL: VITE_GLOB_API_URL,
      otherApiURL: VITE_GLOB_OTHER_API_URL, // [!code ++]
    };
  }
  ```

到这里，就可以在项目内使用 `useAppConfig`方法获取到新增的配置项了。

```ts
const { otherApiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
```

::: warning 注意

`useAppConfig`方法只能在应用内使用，不要耦合到包内部去使用。这里传入 `import.meta.env`和`import.meta.env.PROD`是为了避免这种情况，一个纯粹的包，应避免使用特定构建工具的变量。

:::

## 偏好设置

项目提供了非常丰富的偏好设置，用于动态配置项目的各种功能：

![](/guide/preferences.png)

如果你找不到文档说明，可以尝试自己配置好以后，点击`复制偏好设置`，覆盖项目默认即可。配置文件位于应用目录下的`preferences.ts`，在这里，你可以覆盖框架默认的配置，实现自定义配置。

```ts
import { useAppConfig } from '@vben/hooks';
import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
});
```

### 框架默认配置

::: details 查看框架默认配置

```ts
const defaultPreferences: Preferences = {
  app: {
    accessMode: 'frontend',
    authPageLayout: 'panel-right',
    checkUpdatesInterval: 1,
    colorGrayMode: false,
    colorWeakMode: false,
    compact: false,
    contentCompact: 'wide',
    contentCompactWidth: 1200,
    contentPadding: 0,
    contentPaddingBottom: 0,
    contentPaddingLeft: 0,
    contentPaddingRight: 0,
    contentPaddingTop: 0,
    defaultAvatar:
      'https://unpkg.com/@vbenjs/static-source@0.1.7/source/avatar-v1.webp',
    defaultHomePath: '/analytics',
    dynamicTitle: true,
    enableCheckUpdates: true,
    enablePreferences: true,
    enableRefreshToken: false,
    isMobile: false,
    layout: 'sidebar-nav',
    locale: 'zh-CN',
    loginExpiredMode: 'page',
    name: 'Vben Admin',
    preferencesButtonPosition: 'auto',
    watermark: false,
    zIndex: 200,
  },
  breadcrumb: {
    enable: true,
    hideOnlyOne: false,
    showHome: false,
    showIcon: true,
    styleType: 'normal',
  },
  copyright: {
    companyName: 'Vben',
    companySiteLink: 'https://www.vben.pro',
    date: '2024',
    enable: true,
    icp: '',
    icpLink: '',
    settingShow: true,
  },
  footer: {
    enable: false,
    fixed: false,
    height: 32,
  },
  header: {
    enable: true,
    height: 50,
    hidden: false,
    menuAlign: 'start',
    mode: 'fixed',
  },
  logo: {
    enable: true,
    fit: 'contain',
    source: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
  },
  navigation: {
    accordion: true,
    split: true,
    styleType: 'rounded',
  },
  shortcutKeys: {
    enable: true,
    globalLockScreen: true,
    globalLogout: true,
    globalPreferences: true,
    globalSearch: true,
  },
  sidebar: {
    autoActivateChild: false,
    collapsed: false,
    collapsedButton: true,
    collapsedShowTitle: false,
    collapseWidth: 60,
    enable: true,
    expandOnHover: true,
    extraCollapse: false,
    extraCollapsedWidth: 60,
    fixedButton: true,
    hidden: false,
    mixedWidth: 80,
    width: 224,
  },
  tabbar: {
    draggable: true,
    enable: true,
    height: 38,
    keepAlive: true,
    maxCount: 0,
    middleClickToClose: false,
    persist: true,
    showIcon: true,
    showMaximize: true,
    showMore: true,
    styleType: 'chrome',
    wheelable: true,
  },
  theme: {
    builtinType: 'default',
    colorDestructive: 'hsl(348 100% 61%)',
    colorPrimary: 'hsl(212 100% 45%)',
    colorSuccess: 'hsl(144 57% 58%)',
    colorWarning: 'hsl(42 84% 61%)',
    mode: 'dark',
    radius: '0.5',
    semiDarkHeader: false,
    semiDarkSidebar: false,
  },
  transition: {
    enable: true,
    loading: true,
    name: 'fade-slide',
    progress: true,
  },
  widget: {
    fullscreen: true,
    globalSearch: true,
    languageToggle: true,
    lockScreen: true,
    notification: true,
    refresh: true,
    sidebarToggle: true,
    themeToggle: true,
  },
};
```

:::

::: details 查看框架默认配置类型

```ts
interface AppPreferences {
  /** 权限模式 */
  accessMode: AccessModeType;
  /** 登录注册页面布局 */
  authPageLayout: AuthPageLayoutType;
  /** 检查更新轮询时间 */
  checkUpdatesInterval: number;
  /** 是否开启灰色模式 */
  colorGrayMode: boolean;
  /** 是否开启色弱模式 */
  colorWeakMode: boolean;
  /** 是否开启紧凑模式 */
  compact: boolean;
  /** 是否开启内容紧凑模式 */
  contentCompact: ContentCompactType;
  /** 内容紧凑宽度 */
  contentCompactWidth: number;
  /** 内容内边距 */
  contentPadding: number;
  /** 内容底部内边距 */
  contentPaddingBottom: number;
  /** 内容左侧内边距 */
  contentPaddingLeft: number;
  /** 内容右侧内边距 */
  contentPaddingRight: number;
  /** 内容顶部内边距 */
  contentPaddingTop: number;
  // /** 应用默认头像 */
  defaultAvatar: string;
  /** 默认首页地址 */
  defaultHomePath: string;
  // /** 开启动态标题 */
  dynamicTitle: boolean;
  /** 是否开启检查更新 */
  enableCheckUpdates: boolean;
  /** 是否显示偏好设置 */
  enablePreferences: boolean;
  /**
   * @zh_CN 是否开启refreshToken
   */
  enableRefreshToken: boolean;
  /** 是否移动端 */
  isMobile: boolean;
  /** 布局方式 */
  layout: LayoutType;
  /** 支持的语言 */
  locale: SupportedLanguagesType;
  /** 登录过期模式 */
  loginExpiredMode: LoginExpiredModeType;
  /** 应用名 */
  name: string;
  /** 偏好设置按钮位置 */
  preferencesButtonPosition: PreferencesButtonPositionType;
  /**
   * @zh_CN 是否开启水印
   */
  watermark: boolean;
  /** z-index */
  zIndex: number;
}

interface BreadcrumbPreferences {
  /** 面包屑是否启用 */
  enable: boolean;
  /** 面包屑是否只有一个时隐藏 */
  hideOnlyOne: boolean;
  /** 面包屑首页图标是否可见 */
  showHome: boolean;
  /** 面包屑图标是否可见 */
  showIcon: boolean;
  /** 面包屑风格 */
  styleType: BreadcrumbStyleType;
}

interface CopyrightPreferences {
  /** 版权公司名 */
  companyName: string;
  /** 版权公司名链接 */
  companySiteLink: string;
  /** 版权日期 */
  date: string;
  /** 版权是否可见 */
  enable: boolean;
  /** 备案号 */
  icp: string;
  /** 备案号链接 */
  icpLink: string;
  /** 设置面板是否显示*/
  settingShow?: boolean;
}

interface FooterPreferences {
  /** 底栏是否可见 */
  enable: boolean;
  /** 底栏是否固定 */
  fixed: boolean;
  /** 底栏高度 */
  height: number;
}

interface HeaderPreferences {
  /** 顶栏是否启用 */
  enable: boolean;
  /** 顶栏高度 */
  height: number;
  /** 顶栏是否隐藏,css-隐藏 */
  hidden: boolean;
  /** 顶栏菜单位置 */
  menuAlign: LayoutHeaderMenuAlignType;
  /** header显示模式 */
  mode: LayoutHeaderModeType;
}

interface LogoPreferences {
  /** logo是否可见 */
  enable: boolean;
  /** logo图片适应方式 */
  fit: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down';
  /** logo地址 */
  source: string;
}

interface NavigationPreferences {
  /** 导航菜单手风琴模式 */
  accordion: boolean;
  /** 导航菜单是否切割，只在 layout=mixed-nav 生效 */
  split: boolean;
  /** 导航菜单风格 */
  styleType: NavigationStyleType;
}

interface SidebarPreferences {
  /** 点击目录时自动激活子菜单   */
  autoActivateChild: boolean;
  /** 侧边栏是否折叠 */
  collapsed: boolean;
  /** 侧边栏折叠按钮是否可见 */
  collapsedButton: boolean;
  /** 侧边栏折叠时，是否显示title */
  collapsedShowTitle: boolean;
  /** 侧边栏折叠宽度 */
  collapseWidth: number;
  /** 侧边栏是否可见 */
  enable: boolean;
  /** 菜单自动展开状态 */
  expandOnHover: boolean;
  /** 侧边栏扩展区域是否折叠 */
  extraCollapse: boolean;
  /** 侧边栏扩展区域折叠宽度 */
  extraCollapsedWidth: number;
  /** 侧边栏固定按钮是否可见 */
  fixedButton: boolean;
  /** 侧边栏是否隐藏 - css */
  hidden: boolean;
  /** 混合侧边栏宽度 */
  mixedWidth: number;
  /** 侧边栏宽度 */
  width: number;
}

interface ShortcutKeyPreferences {
  /** 是否启用快捷键-全局 */
  enable: boolean;
  /** 是否启用全局锁屏快捷键 */
  globalLockScreen: boolean;
  /** 是否启用全局注销快捷键 */
  globalLogout: boolean;
  /** 是否启用全局偏好设置快捷键 */
  globalPreferences: boolean;
  /** 是否启用全局搜索快捷键 */
  globalSearch: boolean;
}

interface TabbarPreferences {
  /** 是否开启多标签页拖拽 */
  draggable: boolean;
  /** 是否开启多标签页 */
  enable: boolean;
  /** 标签页高度 */
  height: number;
  /** 开启标签页缓存功能 */
  keepAlive: boolean;
  /** 限制最大数量 */
  maxCount: number;
  /** 是否点击中键时关闭标签 */
  middleClickToClose: boolean;
  /** 是否持久化标签 */
  persist: boolean;
  /** 是否开启多标签页图标 */
  showIcon: boolean;
  /** 显示最大化按钮 */
  showMaximize: boolean;
  /** 显示更多按钮 */
  showMore: boolean;
  /** 标签页风格 */
  styleType: TabsStyleType;
  /** 是否开启鼠标滚轮响应 */
  wheelable: boolean;
}

interface ThemePreferences {
  /** 内置主题名 */
  builtinType: BuiltinThemeType;
  /** 错误色 */
  colorDestructive: string;
  /** 主题色 */
  colorPrimary: string;
  /** 成功色 */
  colorSuccess: string;
  /** 警告色 */
  colorWarning: string;
  /** 当前主题 */
  mode: ThemeModeType;
  /** 圆角 */
  radius: string;
  /** 是否开启半深色header（只在theme='light'时生效） */
  semiDarkHeader: boolean;
  /** 是否开启半深色菜单（只在theme='light'时生效） */
  semiDarkSidebar: boolean;
}

interface TransitionPreferences {
  /** 页面切换动画是否启用 */
  enable: boolean;
  // /** 是否开启页面加载loading */
  loading: boolean;
  /** 页面切换动画 */
  name: PageTransitionType | string;
  /** 是否开启页面加载进度动画 */
  progress: boolean;
}

interface WidgetPreferences {
  /** 是否启用全屏部件 */
  fullscreen: boolean;
  /** 是否启用全局搜索部件 */
  globalSearch: boolean;
  /** 是否启用语言切换部件 */
  languageToggle: boolean;
  /** 是否开启锁屏功能 */
  lockScreen: boolean;
  /** 是否显示通知部件 */
  notification: boolean;
  /** 显示刷新按钮 */
  refresh: boolean;
  /** 是否显示侧边栏显示/隐藏部件 */
  sidebarToggle: boolean;
  /** 是否显示主题切换部件 */
  themeToggle: boolean;
}

interface Preferences {
  /** 全局配置 */
  app: AppPreferences;
  /** 顶栏配置 */
  breadcrumb: BreadcrumbPreferences;
  /** 版权配置 */
  copyright: CopyrightPreferences;
  /** 底栏配置 */
  footer: FooterPreferences;
  /** 面包屑配置 */
  header: HeaderPreferences;
  /** logo配置 */
  logo: LogoPreferences;
  /** 导航配置 */
  navigation: NavigationPreferences;
  /** 快捷键配置 */
  shortcutKeys: ShortcutKeyPreferences;
  /** 侧边栏配置 */
  sidebar: SidebarPreferences;
  /** 标签页配置 */
  tabbar: TabbarPreferences;
  /** 主题配置 */
  theme: ThemePreferences;
  /** 动画配置 */
  transition: TransitionPreferences;
  /** 功能配置 */
  widget: WidgetPreferences;
}
```

:::

::: warning 注意

- `overridesPreferences`方法只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置。
- 任何配置项都可以覆盖，只需要在`overridesPreferences`方法内覆盖即可，不要修改默认配置文件。
- 更改配置后请清空缓存，否则可能不生效。

:::
