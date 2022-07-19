<h1>🌈 yudao-ui-admin-vue3</h1>

## 介绍

- 基于 vue3.2+ ，TypeScript ，Element Plus 2.2.0+ ，Vite3 ，Pinia ，Windicss 等开发的后台管理系统

## 注意事项

- 项目路径请不要使用中文命名！！！会造成解析乱码！！！请使用全英文路径！！！
- node >=14.18.0(建议使用16版本) ,pnpm >=7
- 开发建议使用 [谷歌浏览器-开发者版](https://www.google.cn/intl/zh-CN/chrome/dev/) 不支持 IE\QQ 等浏览器

### 前端依赖

| 框架 | 说明 | 版本 |
| --- | --- | --- |
| [Vue](https://staging-cn.vuejs.org/) | vue 框架 | 3.2.37 |
| [Vite](https://cn.vitejs.dev//) | 开发与构建工具 | 3.0.1 |
| [Element Plus](https://element-plus.org/zh-CN/) | Element Plus | 2.2.9 |
| [TypeScript](https://www.typescriptlang.org/docs/) | JavaScript 的超集 | 4.7.4 |
| [pinia](https://pinia.vuejs.org/) | Vue 存储库 替代 vuex5 | 2.0.16 |
| [vueuse](https://vueuse.org//) | 常用工具集 | 8.9.4 |
| [vue-i18n](https://kazupon.github.io/vue-i18n/zh/introduction.html/) | 国际化 | 9.1.10 |
| [vue-router](https://router.vuejs.org/) | vue 路由 | 4.1.2 |
| [windicss](https://cn.windicss.org/) | 下一代工具优先的 CSS 框架 | 3.5.6 |
| [iconify](https://icon-sets.iconify.design/) | 在线图标库 | 2.2.1 |
| [wangeditor](https://www.wangeditor.com/) | 富文本编辑器 | 5.1.11 |

## 用法

### 推荐 VScode 开发，插件如下

- WindiCSS IntelliSense WindiCSS --- 自动完成、语法突出显示、代码折叠和构建等高级功能
- TypeScript Vue Plugin (Volar) --- 用于 TypeScript 的 Vue 插件
- Vue Language Features (Volar) --- Vue3.0 语法支持
- Iconify IntelliSense --- Iconify 预览和搜索
- i18n Ally --- 国际化智能提示
- Stylelint --- css 格式化
- DotENV --- .env 文件高亮
- Prettier --- 代码格式化
- ESLint --- 脚本代码检查

### 安装 pnpm 并启动项目

```
# 查看当前 npm 源

npm config ls

# 如果执行上面命令您并未看到 registry = "https://registry.npmjs.org/"，说明使用的非npm官方源，请执行下面命令

npm config set registry https://registry.npmjs.org

# 如果您还没安装 pnpm，请执行下面命令

npm install -g pnpm

# mac 用户遇到安装报错请在命令前加上 sudo

# 安装依赖

pnpm install

# 运行项目

pnpm run dev

# 打包

pnpm run build:pro

# 安装一个包

pnpm add 包名

# 卸载一个包

pnpm remove 包名

# 其他命令请看package.json
```

## 浏览器支持

本地开发推荐使用 `Chrome 80+` 浏览器

支持现代浏览器, 不支持 IE ,QQ 等

| [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/archive/internet-explorer_9-11/internet-explorer_9-11_48x48.png" alt=" Edge" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>IE | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/edge/edge_48x48.png" alt=" Edge" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Edge | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/firefox/firefox_48x48.png" alt="Firefox" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Firefox | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/chrome/chrome_48x48.png" alt="Chrome" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Chrome | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/safari/safari_48x48.png" alt="Safari" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Safari |
| :-: | :-: | :-: | :-: | :-: |
| not support | last 2 versions | last 2 versions | last 2 versions | last 2 versions |
