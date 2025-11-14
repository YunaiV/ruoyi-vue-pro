---
outline: deep
---

# CLI

项目中，提供了一些命令行工具，用于一些常用的操作，代码位于 `scrips` 内。

## vsh

用于一些项目操作，如清理项目、检查项目等。

### 用法

```bash
pnpm vsh [command] [options]
```

### vsh check-circular

检查整个项目循环引用，如果有循环引用，会在控制台输出循环引用的模块。

#### 用法

```bash
pnpm vsh check-circular
```

#### 选项

| 选项       | 说明                                |
| ---------- | ----------------------------------- |
| `--staged` | 只检查git暂存区内的文件,默认`false` |

### vsh check-dep

检查整个项目依赖情况，并在控制台输出`未使用的依赖`、`未安装的依赖`信息

#### 用法

```bash
pnpm vsh check-dep
```

#### 选项

| 选项             | 说明                                    |
| ---------------- | --------------------------------------- |
| `-r,--recursive` | 递归删除整个项目,默认`true`             |
| `--del-lock`     | 是否删除`pnpm-lock.yaml`文件,默认`true` |

### vsh lint

对项目进行lint检查，检查项目中的代码是否符合规范。

#### 用法

```bash
pnpm vsh lint
```

#### 选项

| 选项       | 说明                           |
| ---------- | ------------------------------ |
| `--format` | 检查并尝试修复错误,默认`false` |

### vsh publint

对 `Monorepo` 项目进行包规范检查，检查项目中的包是否符合规范。

#### 用法

```bash
pnpm vsh publint
```

#### 选项

| 选项      | 说明                   |
| --------- | ---------------------- |
| `--check` | 仅执行检查,默认`false` |

### vsh code-workspace

生成 `vben-admin.code-workspace` 文件，目前不需要手动执行，会在代码提交时自动执行。

#### 用法

```bash
pnpm vsh code-workspace
```

#### 选项

| 选项            | 说明                                   |
| --------------- | -------------------------------------- |
| `--auto-commit` | `git commit`时候，自动提交,默认`false` |
| `--spaces`      | 缩进格式,默认 `2`个缩进                |

## turbo-run

用于快速执行大仓中脚本，并提供选项式交互选择。

### 用法

```bash
pnpm turbo-run [command]
```

### turbo-run dev

快速执行`dev`命令，并提供选项式交互选择。
