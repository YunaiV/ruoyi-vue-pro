---
outline: deep
---

# 快速开始 {#quick-start}

## 前置准备

::: info 环境要求

在启动项目前，你需要确保你的环境满足以下要求：

- [Node.js](https://nodejs.org/en) 20.15.0 及以上版本，推荐使用 [fnm](https://github.com/Schniz/fnm) 、 [nvm](https://github.com/nvm-sh/nvm) 或者直接使用[pnpm](https://pnpm.io/cli/env) 进行版本管理。
- [Git](https://git-scm.com/) 任意版本。

验证你的环境是否满足以上要求，你可以通过以下命令查看版本：

```bash
# 出现相应 node LTS版本即可
node -v
# 出现相应 git 版本即可
git -v
```

:::

## 启动项目

### 获取源码

::: code-group

```sh [GitHub]
# clone 代码
git clone https://github.com/vbenjs/vue-vben-admin.git
```

```sh [Gitee]
# clone 代码
# Gitee 的代码可能不是最新的
git clone https://gitee.com/annsion/vue-vben-admin.git
```

:::

::: danger 注意

注意存放代码的目录及所有父级目录不能存在中文、韩文、日文以及空格，否则安装依赖后启动会出错。

:::

### 安装依赖

在你的代码目录内打开终端，并执行以下命令:

```bash
# 进入项目目录
cd vue-vben-admin

# 使用项目指定的pnpm版本进行依赖安装
npm i -g corepack

# 安装依赖
pnpm install
```

::: tip 注意

- 项目只支持使用 `pnpm` 进行依赖安装，默认会使用 `corepack` 来安装指定版本的 `pnpm`。:
- 如果你的网络环境无法访问npm源，你可以设置系统的环境变量`COREPACK_NPM_REGISTRY=https://registry.npmmirror.com`，然后再执行`pnpm install`。
- 如果你不想使用`corepack`，你需要禁用`corepack`，然后使用你自己的`pnpm`进行安装。

:::

### 运行项目

#### 选择项目

执行以下命令运行项目:

```bash
# 启动项目
pnpm dev
```

此时，你会看到类似如下的输出，选择你需要运行的项目：

```bash
│
◆  Select the app you need to run [dev]:
│  ○ @vben/web-antd
│  ○ @vben/web-ele
│  ○ @vben/web-naive
│  ○ @vben/docs
│  ● @vben/playground
└
```

现在，你可以在浏览器访问 `http://localhost:5555` 查看项目。

#### 运行指定项目

如果你不想选择项目，可以直接运行以下命令运行你需要的应用：

```bash
pnpm run dev:antd
pnpm run dev:ele
pnpm run dev:naive
pnpm run dev:docs
pnpm run dev:play
```
