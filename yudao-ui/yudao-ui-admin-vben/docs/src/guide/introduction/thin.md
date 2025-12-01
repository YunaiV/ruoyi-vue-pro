---
outline: deep
---

# 精简版本

从 `5.0` 版本开始，我们不再提供精简的仓库或者分支。我们的目标是提供一个更加一致的开发体验，同时减少维护成本。在这里，我们将如何介绍自己的项目，如何去精简以及移除不需要的功能。

## 应用精简

首先，确认你需要的 `UI` 组件库版本，然后删除对应的应用，比如你选择使用 `Ant Design Vue`，那么你可以删除其他应用， 只需要删除下面两个文件夹即可：

```bash
apps/web-ele
apps/web-naive

```

::: tip

如果项目没有内置你需要的 `UI` 组件库应用，你可以直接全部删除其他应用。然后自行新建应用即可。

:::

## 演示代码精简

如果你不需要演示代码，你可以直接删除的`playground`文件夹。

## 文档精简

如果你不需要文档，你可以直接删除`docs`文件夹。

## Mock 服务精简

如果你不需要`Mock`服务，你可以直接删除`apps/backend-mock`文件夹。同时在你的应用下`.env.development`文件中删除`VITE_NITRO_MOCK`变量。

```bash
# 是否开启 Nitro Mock服务，true 为开启，false 为关闭
VITE_NITRO_MOCK=false
```

## 安装依赖

到这里，你已经完成了精简操作，接下来你可以安装依赖，并启动你的项目：

```bash
# 根目录下执行
pnpm install

```

## 命令调整

在精简后，你可能需要根据你的项目调整命令，在根目录下的`package.json`文件中，你可以调整`scripts`字段，移除你不需要的命令。

```json
{
  "scripts": {
    "build:antd": "pnpm run build --filter=@vben/web-antd",
    "build:docs": "pnpm run build --filter=@vben/docs",
    "build:ele": "pnpm run build --filter=@vben/web-ele",
    "build:naive": "pnpm run build --filter=@vben/web-naive",
    "build:tdesign": "pnpm run build --filter=@vben/web-tdesign",
    "build:play": "pnpm run build --filter=@vben/playground",
    "dev:antd": "pnpm -F @vben/web-antd run dev",
    "dev:docs": "pnpm -F @vben/docs run dev",
    "dev:ele": "pnpm -F @vben/web-ele run dev",
    "dev:play": "pnpm -F @vben/playground run dev",
    "dev:naive": "pnpm -F @vben/web-naive run dev"
  }
}
```

## 其他

如果你想更进一步精简，你可以删除参考以下文件或者文件夹的作用，判断自己是否需要，不需要删除即可：

- `.changeset` 文件夹用于管理版本变更
- `.github` 文件夹用于存放 GitHub 的配置文件
- `.vscode` 文件夹用于存放 VSCode 的配置文件，如果你使用其他编辑器，可以删除
- `./scripts/deploy` 文件夹用于存放部署脚本，如果你不需要docker部署，可以删除

## 应用精简

当你确定了某个应用，你还可以进一步精简：

### 删除不需要的路由及页面

- 在应用的 `src/router/routes` 文件中，你可以删除不需要的路由。其中 `core` 文件夹内，如果只需要登录和忘记密码，你可以删除其他路由，如忘记密码、注册等。路由删除后，你可以删除对应的页面文件，在 `src/views/_core` 文件夹中。

- 在应用的 `src/router/routes` 文件中，你可以按需求删除不需要的路由，如`demos`、`vben` 目录等。路由删除后，你可以删除对应的页面文件，在 `src/views` 文件夹中。

### 删除不需要的组件

- 在应用的 `packages/effects/common-ui/src/ui` 文件夹中，你可以删除不需要的组件，如`about`、`dashboard` 目录等。删除之前请先确保你的路由中没有引用到这些组件。
