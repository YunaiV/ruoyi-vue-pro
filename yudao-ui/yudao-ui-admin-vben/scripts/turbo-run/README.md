# @vben/turbo-run

`turbo-run` 是一个命令行工具，允许你在多个包中并行运行命令。它提供了一个交互式的界面，让你可以选择要运行命令的包。

## 特性

- 🚀 交互式选择要运行的包
- 📦 支持 monorepo 项目结构
- 🔍 自动检测可用的命令
- 🎯 精确过滤目标包

## 安装

```bash
pnpm add -D @vben/turbo-run
```

## 使用方法

基本语法：

```bash
turbo-run [script]
```

例如，如果你想运行 `dev` 命令：

```bash
turbo-run dev
```

工具会自动检测哪些包有 `dev` 命令，并提供一个交互式界面让你选择要运行的包。

## 示例

假设你的项目中有以下包：

- `@vben/app`
- `@vben/admin`
- `@vben/website`

当你运行：

```bash
turbo-run dev
```

工具会：

1. 检测哪些包有 `dev` 命令
2. 显示一个交互式选择界面
3. 让你选择要运行命令的包
4. 使用 `pnpm --filter` 在选定的包中运行命令

## 注意事项

- 确保你的项目使用 pnpm 作为包管理器
- 确保目标包在 `package.json` 中定义了相应的脚本命令
- 该工具需要在 monorepo 项目的根目录下运行
