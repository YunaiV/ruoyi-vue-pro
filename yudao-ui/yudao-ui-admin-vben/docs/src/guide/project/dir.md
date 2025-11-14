# 目录说明

目录使用 Monorepo 管理，项目结构如下：

```bash
.
├── Dockerfile # Docker 镜像构建文件
├── README.md # 项目说明文档
├── apps # 项目应用目录
│   ├── backend-mock # 后端模拟服务应用
│   ├── web-antd # 基于 Ant Design Vue 的前端应用
│   ├── web-ele # 基于 Element Plus 的前端应用
│   └── web-naive # 基于 Naive UI 的前端应用
├── build-local-docker-image.sh # 本地构建 Docker 镜像脚本
├── cspell.json # CSpell 配置文件
├── docs # 项目文档目录
├── eslint.config.mjs # ESLint 配置文件
├── internal # 内部工具目录
│   ├── lint-configs # 代码检查配置
│   │   ├── commitlint-config # Commitlint 配置
│   │   ├── eslint-config # ESLint 配置
│   │   ├── prettier-config # Prettier 配置
│   │   └── stylelint-config # Stylelint 配置
│   ├── node-utils # Node.js 工具
│   ├── tailwind-config # Tailwind 配置
│   ├── tsconfig # 通用 tsconfig 配置
│   └── vite-config # 通用Vite 配置
├── package.json # 项目依赖配置
├── packages # 项目包目录
│   ├── @core # 核心包
│   │   ├── base # 基础包
│   │   │   ├── design # 设计相关
│   │   │   ├── icons # 图标
│   │   │   ├── shared # 共享
│   │   │   └── typings # 类型定义
│   │   ├── composables # 组合式 API
│   │   ├── preferences # 偏好设置
│   │   └── ui-kit # UI 组件集合
│   │       ├── layout-ui # 布局 UI
│   │       ├── menu-ui  # 菜单 UI
│   │       ├── shadcn-ui # shadcn UI
│   │       └── tabs-ui # 标签页 UI
│   ├── constants # 常量
│   ├── effects # 副作用相关包
│   │   ├── access # 访问控制
│   │   ├── plugins # 第三方大型依赖插件
│   │   ├── common-ui # 通用 UI
│   │   ├── hooks # 组合式 API
│   │   ├── layouts # 布局
│   │   └── request # 请求
│   ├── icons # 图标
│   ├── locales # 国际化
│   ├── preferences  # 偏好设置
│   ├── stores # 状态管理
│   ├── styles # 样式
│   ├── types # 类型定义
│   └── utils # 工具
├── playground # 演示目录
├── pnpm-lock.yaml # pnpm 锁定文件
├── pnpm-workspace.yaml # pnpm 工作区配置文件
├── scripts # 脚本目录
│   ├── turbo-run # Turbo 运行脚本
│   └── vsh # VSH 脚本
├── stylelint.config.mjs # Stylelint 配置文件
├── turbo.json # Turbo 配置文件
├── vben-admin.code-workspace # VS Code 工作区配置文件
└── vitest.config.ts # Vite 配置文件
```
