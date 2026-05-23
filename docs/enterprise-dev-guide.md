# 企业级开发流程完整操作手册

## 📖 目录

- [1. 环境准备](#1-环境准备)
- [2. 首次启动](#2-首次启动)
- [3. Jenkins 配置](#3-jenkins-配置)
- [4. 日常开发流程](#4-日常开发流程)
- [5. 代码审查](#5-代码审查)
- [6. 版本发布](#6-版本发布)
- [7. 常见问题](#7-常见问题)

---

## 1. 环境准备

### 1.1 前置要求

确保您的 Mac 已安装以下软件：

```bash
# 检查 Docker
docker --version
docker-compose --version

# 检查 Git
git --version

# 检查 Maven（本地开发用）
mvn --version
```

如果未安装，请参考：
- Docker: https://docs.docker.com/desktop/install/mac-install/
- 或使用 OrbStack: https://orbstack.dev/

### 1.2 项目结构

```
ruoyi-vue-pro/
├── docker-compose.full.yml    # Docker 编排文件
├── script/
│   ├── jenkins/
│   │   ├── Jenkinsfile        # 生产环境流水线
│   │   └── Jenkinsfile-local  # 本地环境流水线
│   └── shell/
│       ├── deploy.sh          # 生产环境部署脚本
│       └── deploy-local.sh    # 本地环境一键部署脚本
├── docs/
│   ├── git-branch-strategy.md # Git 分支策略
│   └── enterprise-dev-guide.md # 本文档
└── ...
```

---

## 2. 首次启动

### 2.1 一键部署（推荐）

```bash
# 进入项目目录
cd /Users/zkr/工作/学习/项目/ruoyi-vue-pro

# 执行一键部署脚本
bash script/shell/deploy-local.sh
```

脚本会自动完成：
- ✅ 检查 Docker 环境
- ✅ 启动 MySQL + Redis
- ✅ 构建并启动后端服务
- ✅ 构建并启动前端服务
- ✅ 启动 Jenkins CI/CD

### 2.2 手动启动（可选）

```bash
# 启动所有服务
docker-compose -f docker-compose.full.yml up -d

# 查看服务状态
docker-compose -f docker-compose.full.yml ps

# 查看日志
docker-compose -f docker-compose.full.yml logs -f
```

### 2.3 验证服务

启动完成后，访问以下地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost | 管理后台 |
| 后端 API | http://localhost:48080 | REST API |
| Jenkins | http://localhost:8080 | CI/CD 平台 |
| Druid 监控 | http://localhost:48080/druid | 数据库监控 |

---

## 3. Jenkins 配置

### 3.1 首次登录

```bash
# 获取 Jenkins 初始密码
docker exec ruoyi-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. 访问 http://localhost:8080
2. 粘贴上面的密码
3. 选择"安装推荐插件"
4. 等待插件安装完成（约 5-10 分钟）
5. 创建管理员账号（admin/admin）

### 3.2 配置 Maven 工具

Jenkins 容器需要安装 Maven：

1. 进入 Jenkins 容器：
```bash
docker exec -it ruoyi-jenkins bash
```

2. 在容器内安装 Maven：
```bash
# 安装 Maven
apt-get update
apt-get install -y maven

# 验证
mvn --version
```

3. 或者使用 Jenkins 的 Maven 插件自动安装

### 3.3 创建流水线任务

1. 点击"新建任务"
2. 输入任务名称：`ruoyi-vue-pro-build`
3. 选择"流水线"（Pipeline）
4. 点击"确定"

#### 配置 Git

1. 在"流水线"配置区域
2. 选择"Pipeline script from SCM"
3. SCM 选择"Git"
4. 配置仓库地址：
   - 本地：`file:///Users/zkr/工作/学习/项目/ruoyi-vue-pro`
   - 或远程：`https://gitee.com/your-repo/ruoyi-vue-pro.git`
5. 分支：`*/dev`
6. 脚本路径：`script/jenkins/Jenkinsfile-local`
7. 点击"保存"

### 3.4 配置自动触发（可选）

#### 方式 1：定时轮询

1. 在任务配置中找到"构建触发器"
2. 勾选"轮询 SCM"
3. 输入 cron 表达式：`H/5 * * * *`（每 5 分钟检查一次）

#### 方式 2：Webhook（需要内网穿透）

1. 安装 ngrok：`brew install ngrok`
2. 暴露 Jenkins：`ngrok http 8080`
3. 在 Gitee 配置 Webhook URL

### 3.5 手动触发构建

1. 进入任务页面
2. 点击"立即构建"
3. 查看构建进度
4. 点击构建编号查看日志

---

## 4. 日常开发流程

### 4.1 完整流程图

```
需求分析
   ↓
创建功能分支 (feature/*)
   ↓
开发代码 + 本地测试
   ↓
提交代码 (git commit)
   ↓
推送到远程 (git push)
   ↓
创建 Merge Request
   ↓
代码审查 (Code Review)
   ↓
Jenkins 自动构建
   ↓
合并到 dev 分支
   ↓
自动部署到开发环境
   ↓
测试验证
```

### 4.2 实际操作示例

#### 步骤 1：创建功能分支

```bash
# 切换到 dev 分支并更新
git checkout dev
git pull origin dev

# 创建功能分支
git checkout -b feature/crm-product-category
```

#### 步骤 2：开发代码

```bash
# 使用 IDE 开发代码
# ... 编写 Controller、Service、Mapper 等 ...

# 本地测试
mvn clean test
```

#### 步骤 3：提交代码

```bash
# 查看修改
git status
git diff

# 添加文件
git add .

# 提交（遵循提交规范）
git commit -m "feat(crm): 新增商品分类CRUD功能

- 实现商品分类列表查询
- 实现商品分类创建/编辑/删除
- 添加参数校验
- 编写单元测试"
```

#### 步骤 4：推送代码

```bash
# 推送到远程仓库
git push origin feature/crm-product-category
```

#### 步骤 5：创建 Merge Request

在 Gitee/GitHub 上：

1. 访问仓库页面
2. 点击"Pull Requests" → "新建 Pull Request"
3. 配置：
   - 源分支：`feature/crm-product-category`
   - 目标分支：`dev`
   - 标题：`[Feature] 商品分类管理模块`
   - 描述：
     ```
     ## 功能说明
     - 实现商品分类的增删改查
     - 支持分类层级管理
     
     ## 测试情况
     - ✅ 单元测试通过
     - ✅ 本地测试通过
     
     ## 截图
     （可选）
     ```
4. 点击"创建"

#### 步骤 6：Jenkins 自动构建

- Jenkins 检测到新 PR
- 自动触发构建
- 执行 Maven 编译
- 运行测试用例
- 生成构建报告

#### 步骤 7：代码审查

团队成员审查代码：
- ✅ 代码逻辑正确
- ✅ 遵循编码规范
- ✅ 测试用例完整
- ✅ 无安全隐患

#### 步骤 8：合并代码

审查通过后：
1. 点击"合并"
2. 代码合并到 `dev` 分支
3. Jenkins 自动部署到开发环境

#### 步骤 9：验证部署

```bash
# 查看 Jenkins 构建状态
# 访问开发环境验证功能
curl http://localhost:48080/admin-api/crm/product-category/list
```

---

## 5. 代码审查

### 5.1 审查清单

#### 代码质量
- [ ] 代码逻辑清晰
- [ ] 无重复代码
- [ ] 命名规范
- [ ] 注释完整

#### 功能正确性
- [ ] 实现需求
- [ ] 边界条件处理
- [ ] 异常处理
- [ ] 单元测试覆盖

#### 安全性
- [ ] SQL 注入防护
- [ ] XSS 防护
- [ ] 权限校验
- [ ] 敏感信息加密

#### 性能
- [ ] 数据库索引
- [ ] 查询优化
- [ ] 缓存使用
- [ ] 无内存泄漏

### 5.2 审查示例

```
✅ 好的提交：
- 清晰的 commit message
- 单元测试覆盖率高
- 代码复用性好
- 异常处理完善

❌ 需要改进：
- 硬编码配置
- 缺少参数校验
- 未处理并发场景
- SQL 查询未优化
```

---

## 6. 版本发布

### 6.1 发布流程

```bash
# 1. 从 dev 创建发布分支
git checkout dev
git checkout -b release/v1.1.0

# 2. 更新版本号、文档
# ...

# 3. 提交
git commit -am "chore: 准备发布 v1.1.0"

# 4. 创建 MR 到 main
# 在 Gitee 上创建 release/v1.1.0 → main 的 MR

# 5. 审查通过后合并

# 6. 打标签
git checkout main
git tag -a v1.1.0 -m "Release v1.1.0"
git push origin v1.1.0

# 7. 同步回 dev
git checkout dev
git merge main
git push origin dev
```

### 6.2 Jenkins 生产构建

1. 手动触发 Jenkins 任务
2. 选择 `main` 分支
3. 使用 `Jenkinsfile`（生产版本）
4. 部署到生产服务器

---

## 7. 常见问题

### Q1: Jenkins 启动很慢？

**A**: 首次启动需要初始化，约 1-2 分钟。如果持续很慢：

```bash
# 查看日志
docker logs -f ruoyi-jenkins

# 重启
docker-compose -f docker-compose.full.yml restart jenkins
```

### Q2: 端口被占用？

**A**: 修改 `docker-compose.full.yml` 中的端口映射：

```yaml
ports:
  - "18080:8080"  # 将 8080 改为 18080
```

### Q3: Maven 构建失败？

**A**: 检查网络连接和依赖：

```bash
# 进入容器
docker exec -it ruoyi-jenkins bash

# 测试 Maven
mvn clean compile

# 检查网络
ping repo.maven.apache.org
```

### Q4: 如何重置 Jenkins？

```bash
# 停止并删除 Jenkins 容器和数据
docker-compose -f docker-compose.full.yml down
docker volume rm ruoyi-vue-pro_jenkins_data

# 重新启动
docker-compose -f docker-compose.full.yml up -d jenkins
```

### Q5: 本地开发如何联调？

```bash
# 方式 1: 直接使用 Docker 服务
# 前端配置 API 地址为: http://localhost:48080

# 方式 2: 本地运行后端
cd yudao-server
mvn spring-boot:run -Dspring.profiles.active=local

# 方式 3: IDE 远程调试
# 配置 Docker 端口映射，添加调试端口
```

### Q6: 数据库初始化失败？

```bash
# 检查 SQL 文件
ls -la sql/mysql/

# 手动导入
docker exec -i ruoyi-mysql mysql -uroot -proot123456 ruoyi_vue_pro < sql/mysql/ruoyi-vue-pro.sql

# 重新初始化（会丢失数据！）
docker-compose down -v
docker-compose up -d mysql
```

---

## 🎯 学习计划

### 第 1 天：环境搭建
- [ ] 启动所有服务
- [ ] 访问前端页面
- [ ] 登录 Jenkins

### 第 2-3 天：熟悉 Git Flow
- [ ] 创建功能分支
- [ ] 开发小功能
- [ ] 提交并推送

### 第 4-5 天：Jenkins 入门
- [ ] 配置流水线
- [ ] 手动触发构建
- [ ] 查看构建日志

### 第 6-7 天：完整流程
- [ ] 创建 PR
- [ ] 代码审查
- [ ] 自动部署

### 第 2 周：深入实践
- [ ] 配置 Webhook
- [ ] 多环境管理
- [ ] 版本发布

---

## 📚 进阶学习

- [Jenkins 官方文档](https://www.jenkins.io/doc/)
- [Git Flow 详解](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
- [Docker 最佳实践](https://docs.docker.com/develop/dev-best-practices/)
- [CI/CD 流程设计](https://www.redhat.com/en/topics/devops/what-is-ci-cd)

---

## 💡 提示

1. **定期备份**：重要数据定期备份
2. **日志管理**：定期清理日志文件
3. **镜像清理**：`docker system prune`
4. **版本升级**：关注项目更新

---

**祝您学习愉快！** 🎉
