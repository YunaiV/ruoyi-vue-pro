# Git 分支策略 - Git Flow 工作流

## 📋 分支说明

本项目采用 **Git Flow** 分支管理策略，模拟真实企业开发流程。

### 主要分支

| 分支 | 说明 | 保护规则 | 部署环境 |
|------|------|---------|---------|
| `main` | 生产环境代码 | 禁止直接推送，仅接受 MR | 生产环境 |
| `dev` | 开发环境代码 | 禁止直接推送，仅接受 MR | 开发环境 |

### 临时分支

| 分支类型 | 命名规则 | 来源 | 合并目标 | 说明 |
|---------|---------|------|---------|------|
| 功能分支 | `feature/*` | `dev` | `dev` | 开发新功能 |
| 修复分支 | `fix/*` | `dev` | `dev` | 修复 Bug |
| 发布分支 | `release/*` | `dev` | `main` + `dev` | 准备发布 |
| 热修复分支 | `hotfix/*` | `main` | `main` + `dev` | 紧急修复 |

---

## 🎯 标准开发流程

### 场景 1：开发新功能

```bash
# 1. 从 dev 分支创建功能分支
git checkout dev
git pull origin dev
git checkout -b feature/crm-product-category

# 2. 开发代码
# ... 编写代码、测试 ...

# 3. 提交代码
git add .
git commit -m "feat: 新增商品分类CRUD功能"

# 4. 推送到远程
git push origin feature/crm-product-category

# 5. 在 Gitee/GitHub 创建 Merge Request
# 目标分支: dev
# 标题: [Feature] 商品分类管理模块
# 描述: 详细说明功能、测试情况

# 6. 代码审查通过后合并到 dev

# 7. 删除功能分支
git branch -d feature/crm-product-category
git push origin --delete feature/crm-product-category
```

### 场景 2：发布新版本

```bash
# 1. 从 dev 创建发布分支
git checkout dev
git pull origin dev
git checkout -b release/v1.1.0

# 2. 修改版本号、更新文档
# ... 

# 3. 提交
git commit -am "chore: 准备发布 v1.1.0"

# 4. 合并到 main（通过 MR）
# 创建 MR: release/v1.1.0 → main

# 5. 同时也要合并回 dev（保持同步）
git checkout dev
git merge release/v1.1.0
git push origin dev

# 6. 打标签
git checkout main
git tag -a v1.1.0 -m "Release v1.1.0"
git push origin v1.1.0

# 7. 删除发布分支
git branch -d release/v1.1.0
git push origin --delete release/v1.1.0
```

### 场景 3：紧急修复（Hotfix）

```bash
# 1. 从 main 创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/fix-login-bug

# 2. 修复 Bug
# ...

# 3. 提交
git commit -am "fix: 修复登录页面超时问题"

# 4. 合并到 main（通过 MR）
# 创建 MR: hotfix/fix-login-bug → main

# 5. 合并到 dev（保持同步）
git checkout dev
git merge hotfix/fix-login-bug
git push origin dev

# 6. 打标签
git checkout main
git tag -a v1.0.1 -m "Hotfix v1.0.1"
git push origin v1.0.1
```

---

## 🔧 Git 配置建议

### 全局配置

```bash
# 设置用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 设置默认分支
git config --global init.defaultBranch main

# 设置编辑器
git config --global core.editor "vim"

# 设置合并策略
git config --global merge.ff false
```

### 分支保护（Gitee/GitHub 设置）

#### main 分支保护规则
- ✅ 禁止推送到 main
- ✅ 需要至少 1 人审查
- ✅ 必须通过 Pull Request 合并
- ✅ 必须通过 CI 检查
- ✅ 禁止强制推送
- ✅ 禁止删除

#### dev 分支保护规则
- ✅ 禁止推送到 dev
- ✅ 需要至少 1 人审查
- ✅ 必须通过 Pull Request 合并
- ✅ 必须通过 CI 检查

---

## 📝 提交信息规范

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(crm): 新增商品分类管理` |
| `fix` | Bug 修复 | `fix(auth): 修复登录超时问题` |
| `docs` | 文档更新 | `docs: 更新部署文档` |
| `style` | 代码格式 | `style: 格式化代码` |
| `refactor` | 重构 | `refactor(crm): 重构商品模块` |
| `perf` | 性能优化 | `perf: 优化数据库查询` |
| `test` | 测试 | `test(crm): 添加单元测试` |
| `build` | 构建系统 | `build: 升级 Maven 依赖` |
| `ci` | CI/CD | `ci: 配置 Jenkins 流水线` |
| `chore` | 其他修改 | `chore: 更新 .gitignore` |

### 示例

```bash
# 新功能
git commit -m "feat(crm): 新增商品分类CRUD功能"

# Bug 修复
git commit -m "fix(auth): 修复 Token 刷新失败问题"

# 文档
git commit -m "docs: 更新 API 文档"

# 重构
git commit -m "refactor(crm): 优化商品查询逻辑"
```

---

## 🚀 Jenkins CI/CD 集成

### 触发规则

| 分支 | 触发动作 | 部署环境 |
|------|---------|---------|
| `feature/*` | 提交代码时 | 不部署，仅构建检查 |
| `dev` | 合并到 dev 时 | 自动部署到开发环境 |
| `main` | 合并到 main 时 | 自动部署到生产环境 |
| `release/*` | 创建时 | 部署到测试环境 |

### Jenkins 流水线配置

1. 创建新任务 → 选择"流水线"
2. 配置 Git 仓库地址
3. 分支规范器：选择对应的分支
4. 脚本路径：`script/jenkins/Jenkinsfile-local`
5. 触发器：勾选"轮询 SCM"或配置 Webhook

---

## 📊 分支管理最佳实践

### ✅ 推荐做法

1. **小步提交**：每个功能分支不超过 2-3 天
2. **及时同步**：每天从 dev 同步最新代码
3. **代码审查**：所有代码必须经过审查
4. **测试先行**：提交前必须本地测试通过
5. **及时清理**：合并后删除临时分支

### ❌ 避免做法

1. **不要在 main 上直接开发**
2. **不要长期不合并**（超过 1 周）
3. **不要提交编译失败的代码**
4. **不要跳过代码审查**
5. **不要在功能分支合并其他功能**

---

## 🎓 学习路径

### 第 1 周：熟悉流程
- 创建功能分支
- 开发并提交代码
- 创建 Merge Request
- 合并到 dev

### 第 2 周：加入 CI/CD
- 配置 Jenkins 流水线
- 自动化构建和部署
- 查看构建日志
- 处理构建失败

### 第 3 周：完整流程
- 从需求到发布
- 代码审查实践
- 版本发布打标签
- 热修复流程

---

## 📚 参考资料

- [Git Flow 工作流](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)
