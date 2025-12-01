# 本地开发 {#development}

::: tip 代码获取

如果你还没有获取代码，可以先从 [快速开始](../introduction/quick-start.md) 处开始阅读文档。

:::

## 前置准备

为了更好的开发体验，我们提供了一些工具配置、项目说明，以便于您更好的开发。

### 需要掌握的基础知识

本项目需要一定前端基础知识，请确保掌握 Vue 的基础知识，以便能处理一些常见的问题。建议在开发前先学一下以下内容，提前了解和学习这些知识，会对项目理解非常有帮助:

- [Vue3](https://vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [TypeScript](https://www.typescriptlang.org/)
- [Vue Router](https://router.vuejs.org/)
- [Vitejs](https://vitejs.dev/)
- [Pnpm](https://pnpm.io/)
- [Turbo](https://turbo.build/)

### 工具配置

如果您使用的 IDE 是[vscode](https://code.visualstudio.com/)(推荐)的话，可以安装以下工具来提高开发效率及代码格式化:

- [Vue - Official](https://marketplace.visualstudio.com/items?itemName=Vue.volar) - Vue 官方插件（必备）。
- [Tailwind CSS](https://marketplace.visualstudio.com/items?itemName=bradlc.vscode-tailwindcss) - Tailwindcss 提示插件。
- [CSS Variable Autocomplete](https://marketplace.visualstudio.com/items?itemName=bradlc.vunguyentuan.vscode-css-variables) - Css 变量提示插件。
- [Iconify IntelliSense](https://marketplace.visualstudio.com/items?itemName=antfu.iconify) - Iconify 图标插件
- [i18n Ally](https://marketplace.visualstudio.com/items?itemName=Lokalise.i18n-ally) - i18n 插件
- [ESLint](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) - 脚本代码检查
- [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode) - 代码格式化
- [Stylelint](https://marketplace.visualstudio.com/items?itemName=stylelint.vscode-stylelint) - css 格式化
- [Code Spell Checker](https://marketplace.visualstudio.com/items?itemName=streetsidesoftware.code-spell-checker) - 单词语法检查
- [DotENV](https://marketplace.visualstudio.com/items?itemName=mikestead.dotenv) - .env 文件 高亮

## Npm Scripts

npm 脚本是项目常见的配置，用于执行一些常见的任务，比如启动项目、打包项目等。以下的脚本都可以在项目根目录的 `package.json` 文件中找到。

执行方式为：`pnpm run [script]` 或 `npm run [script]`。

```json
{
  "scripts": {
    // 构建项目
    "build": "cross-env NODE_OPTIONS=--max-old-space-size=8192 turbo build",
    // 构建项目并分析
    "build:analyze": "turbo build:analyze",
    // 构建本地 docker 镜像
    "build:docker": "./build-local-docker-image.sh",
    // 单独构建 web-antd 应用
    "build:antd": "pnpm run build --filter=@vben/web-antd",
    // 单独构建文档
    "build:docs": "pnpm run build --filter=@vben/docs",
    // 单独构建 web-ele 应用
    "build:ele": "pnpm run build --filter=@vben/web-ele",
    // 单独构建 web-naive 应用
    "build:naive": "pnpm run build --filter=@vben/naive",
    // 单独构建 web-tdesign 应用
    "build:tdesign": "pnpm run build --filter=@vben/web-tdesign",
    // 单独构建 playground 应用
    "build:play": "pnpm run build --filter=@vben/playground",
    // changeset 版本管理
    "changeset": "pnpm exec changeset",
    // 检查项目各种问题
    "check": "pnpm run check:circular && pnpm run check:dep && pnpm run check:type && pnpm check:cspell",
    // 检查循环引用
    "check:circular": "vsh check-circular",
    // 检查拼写
    "check:cspell": "cspell lint **/*.ts **/README.md .changeset/*.md --no-progress"
    // 检查依赖
    "check:dep": "vsh check-dep",
    // 检查类型
    "check:type": "turbo run typecheck",
    // 清理项目（删除node_modules、dist、.turbo）等目录
    "clean": "node ./scripts/clean.mjs",
    // 提交代码
    "commit": "czg",
    // 启动项目（默认会运行整个仓库所有包的dev脚本）
    "dev": "turbo-run dev",
    // 启动web-antd应用
    "dev:antd": "pnpm -F @vben/web-antd run dev",
    // 启动文档
    "dev:docs": "pnpm -F @vben/docs run dev",
    // 启动web-ele应用
    "dev:ele": "pnpm -F @vben/web-ele run dev",
    // 启动web-naive应用
    "dev:naive": "pnpm -F @vben/web-naive run dev",
    // 启动演示应用
    "dev:play": "pnpm -F @vben/playground run dev",
    // 格式化代码
    "format": "vsh lint --format",
    // lint 代码
    "lint": "vsh lint",
    // 依赖安装完成之后，执行所有包的stub脚本
    "postinstall": "pnpm -r run stub --if-present",
    // 只允许使用pnpm
    "preinstall": "npx only-allow pnpm",
    // lefthook的安装
    "prepare": "is-ci || lefthook install",
    // 预览应用
    "preview": "turbo-run preview",
    // 包规范检查
    "publint": "vsh publint",
    // 删除所有的node_modules、yarn.lock、package.lock.json，重新安装依赖
    "reinstall": "pnpm clean --del-lock && pnpm install",
    // 运行 vitest 单元测试
    "test:unit": "vitest run --dom",
    // 更新项目依赖
    "update:deps": " pnpm update --latest --recursive",
    // changeset生成提交集
    "version": "pnpm exec changeset version && pnpm install --no-frozen-lockfile"
  }
}
```

## 本地运行项目

如需本地运行文档，并进行调整，可以执行以下命令，执行该命令，你可以选择需要的应用进行开发：

```bash
pnpm dev
```

如果你想直接运行某个应用，可以执行以下命令：

运行 `web-antd` 应用：

```bash
pnpm dev:antd
```

运行 `web-naive` 应用：

```bash
pnpm dev:naive
```

运行 `web-ele` 应用：

```bash
pnpm dev:ele
```

运行 `docs` 应用：

```bash
pnpm dev:docs
```

## 区分构建环境

在实际的业务开发中，通常会在构建时区分多种环境，如测试环境`test`、生产环境`build`等。

此时可以修改三个文件，在其中增加对应的脚本配置来达到区分生产环境的效果。

以`@vben/web-antd`添加测试环境`test`为例：

- `apps\web-antd\package.json`

```json
"scripts": {
  "build:prod": "pnpm vite build --mode production",
  "build:test": "pnpm vite build --mode test",
  "build:analyze": "pnpm vite build --mode analyze",
  "dev": "pnpm vite --mode development",
  "preview": "vite preview",
  "typecheck": "vue-tsc --noEmit --skipLibCheck"
},
```

增加命令`"build:test"`, 并将原`"build"`改为`"build:prod"`以避免同时构建两个环境的包。

- `package.json`

```json
"scripts": {
    "build": "cross-env NODE_OPTIONS=--max-old-space-size=8192 turbo build",
    "build:analyze": "turbo build:analyze",
    "build:antd": "pnpm run build --filter=@vben/web-antd",
    "build-test:antd": "pnpm run build --filter=@vben/web-antd build:test",

    ······
}
```

在根目录`package.json`中加入构建测试环境的命令

- `turbo.json`

```json
"tasks": {
    "build": {
      "dependsOn": ["^build"],
      "outputs": [
        "dist/**",
        "dist.zip",
        ".vitepress/dist.zip",
        ".vitepress/dist/**"
      ]
    },

    "build-test:antd": {
      "dependsOn": ["@vben/web-antd#build:test"],
      "outputs": ["dist/**"]
    },

    "@vben/web-antd#build:test": {
      "dependsOn": ["^build"],
      "outputs": ["dist/**"]
    },

    ······
```

在`turbo.json`中加入相关依赖的命令

## 公共静态资源

项目中需要使用到的公共静态资源，如：图片、静态HTML等，需要在开发中通过 `src="/xxx.png"` 直接引入的。

需要将资源放在对应项目的 `public/static` 目录下。引入的路径为：`src="/static/xxx.png"`。

## DevTools

项目内置了 [Vue DevTools](https://github.com/vuejs/devtools-next) 插件，可以在开发过程中使用。默认关闭，可在`.env.development` 内开启，并重新运行项目即可：

```bash
VITE_DEVTOOLS=true
```

开启后，项目运行会在页面底部显示一个 Vue DevTools 的图标，点击即可打开 DevTools。

![Vue DevTools](/guide/devtools.png)

## 本地运行文档

如需本地运行文档，并进行调整，可以执行以下命令：

```bash
pnpm dev:docs
```

## 问题解决

如果你在使用过程中遇到依赖相关的问题，可以尝试以下重新安装依赖：

```bash
# 请在项目根目录下执行
# 该命令会删除整个仓库所有的 node_modules、yarn.lock、package.lock.json后
# 再进行依赖重新安装（安装速度会明显变慢）。
pnpm reinstall
```
