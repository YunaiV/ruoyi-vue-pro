# 规范

::: tip 贡献代码

- 如果你想向项目贡献代码，请确保你的代码符合项目的代码规范。
- 如果你使用的是 `vscode`，需要安装以下插件：
  - [ESLint](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) - 脚本代码检查
  - [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode) - 代码格式化
  - [Code Spell Checker](https://marketplace.visualstudio.com/items?itemName=streetsidesoftware.code-spell-checker) - 单词语法检查
  - [Stylelint](https://marketplace.visualstudio.com/items?itemName=stylelint.vscode-stylelint) - css 格式化

:::

## 作用

具备基本工程素养的同学都会注重编码规范，而代码风格检查（Code Linting，简称 Lint）是保障代码规范一致性的重要手段。

遵循相应的代码规范有以下好处：

- 较少 bug 错误率
- 高效的开发效率
- 更高的可读性

## 工具

项目的配置文件位于 `internal/lint-configs` 下，你可以在这里修改各种lint的配置。

项目内集成了以下几种代码校验工具：

- [ESLint](https://eslint.org/) 用于 JavaScript 代码检查
- [Stylelint](https://stylelint.io/) 用于 CSS 样式检查
- [Prettier](https://prettier.io/) 用于代码格式化
- [Commitlint](https://commitlint.js.org/) 用于检查 git 提交信息的规范
- [Publint](https://publint.dev/) 用于检查 npm 包的规范
- [Cspell](https://cspell.org/) 用于检查拼写错误
- [lefthook](https://github.com/evilmartians/lefthook) 用于管理 Git hooks，在提交前自动运行代码校验和格式化

## ESLint

ESLint 是一个代码规范和错误检查工具，用于识别和报告 TypeScript 代码中的语法错误。

### 命令

```bash
pnpm eslint .
```

### 配置

eslint 配置文件为 `eslint.config.mjs`，其核心配置放在`internal/lint-configs/eslint-config`目录下，可以根据项目需求进行修改。

## Stylelint

Stylelint 用于校验项目内部 css 的风格,加上编辑器的自动修复，可以很好的统一项目内部 css 风格

### 命令

```bash
pnpm stylelint "**/*.{vue,css,less.scss}"
```

### 配置

Stylelint 配置文件为 `stylelint.config.mjs`，其核心配置放在`internal/lint-configs/stylelint-config`目录下，可以根据项目需求进行修改。

## Prettier

Prettier 可以用于统一项目代码风格，统一的缩进，单双引号，尾逗号等等风格

### 命令

```bash
pnpm prettier .
```

### 配置

Prettier 配置文件为 `.prettier.mjs`，其核心配置放在`internal/lint-configs/prettier-config`目录下，可以根据项目需求进行修改。

## CommitLint

在一个团队中，每个人的 git 的 commit 信息都不一样，五花八门，没有一个机制很难保证规范化，如何才能规范化呢？可能你想到的是 git 的 hook 机制，去写 shell 脚本去实现。这当然可以，其实 JavaScript 有一个很好的工具可以实现这个模板，它就是 commitlint（用于校验 git 提交信息规范）。

### 配置

CommitLint 配置文件为 `.commitlintrc.mjs`，其核心配置放在`internal/lint-configs/commitlint-config`目录下，可以根据项目需求进行修改。

### Git 提交规范

参考 [Angular](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular)

- `feat` 增加新功能
- `fix` 修复问题/BUG
- `style` 代码风格相关无影响运行结果的
- `perf` 优化/性能提升
- `refactor` 重构
- `revert` 撤销修改
- `test` 测试相关
- `docs` 文档/注释
- `chore` 依赖更新/脚手架配置修改等
- `workflow` 工作流改进
- `ci` 持续集成
- `types` 类型修改

### 关闭Git提交规范检查

如果你想关闭 Git 提交规范检查，有两种方式：

::: code-group

```bash [临时关闭]
git commit -m 'feat: add home page' --no-verify
```

```bash [永久关闭]
# 在 .husky/commit-msg 内注释以下代码即可
pnpm exec commitlint --edit "$1" # [!code --]
```

:::

## Publint

Publint 是一个用于检查 npm 包的规范的工具，可以检查包的版本号是否符合规范，是否符合标准的 ESM 规范包等等。

### 命令

```bash
pnpm vsh publint
```

## Cspell

Cspell 是一个用于检查拼写错误的工具，可以检查代码中的拼写错误，避免因为拼写错误导致的 bug。

### 命令

```bash
pnpm cspell lint \"**/*.ts\"  \"**/README.md\" \".changeset/*.md\" --no-progress
```

### 配置

cspell 配置文件为 `cspell.json`，可以根据项目需求进行修改。

## Git Hook

git hook 一般结合各种 lint，在 git 提交代码的时候进行代码风格校验，如果校验没通过，则不会进行提交。需要开发者自行修改后再次进行提交

### lefthook

有一个问题就是校验会校验全部代码，但是我们只想校验我们自己提交的代码，这个时候就可以使用 lefthook。

最有效的解决方案就是将 Lint 校验放到本地，常见做法是使用 lefthook 在本地提交之前先做一次 Lint 校验。

项目在 `lefthook.yml` 内部定义了相应的 hooks：

- `pre-commit`: 在提交前运行，用于代码格式化和检查
  - `code-workspace`: 更新 VSCode 工作区配置
  - `lint-md`: 格式化 Markdown 文件
  - `lint-vue`: 格式化并检查 Vue 文件
  - `lint-js`: 格式化并检查 JavaScript/TypeScript 文件
  - `lint-style`: 格式化并检查样式文件
  - `lint-package`: 格式化 package.json
  - `lint-json`: 格式化其他 JSON 文件

- `post-merge`: 在合并后运行，用于自动安装依赖
  - `install`: 运行 `pnpm install` 安装新依赖

- `commit-msg`: 在提交时运行，用于检查提交信息格式
  - `commitlint`: 使用 commitlint 检查提交信息

#### 如何关闭 lefthook

如果你想关闭 lefthook，有两种方式：

::: code-group

```bash [临时关闭]
git commit -m 'feat: add home page' --no-verify
```

```bash [永久关闭]
# 删除 lefthook.yml 文件即可
rm lefthook.yml
```

:::

#### 如何修改 lefthook 配置

如果你想修改 lefthook 的配置，可以编辑 `lefthook.yml` 文件。例如：

```yaml
pre-commit:
  parallel: true # 并行执行任务
  jobs:
    - name: lint-js
      run: pnpm prettier --cache --ignore-unknown --write {staged_files}
      glob: '*.{js,jsx,ts,tsx}'
```

其中：

- `parallel`: 是否并行执行任务
- `jobs`: 定义要执行的任务列表
- `name`: 任务名称
- `run`: 要执行的命令
- `glob`: 匹配的文件模式
- `{staged_files}`: 表示暂存的文件列表
