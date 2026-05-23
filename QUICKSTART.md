# 🚀 快速开始指南

## 一键启动企业级开发环境

### 第 1 步：启动所有服务

```bash
cd /Users/zkr/工作/学习/项目/ruoyi-vue-pro
bash script/shell/deploy-local.sh
```

⏳ 首次启动约需 5-10 分钟（需要下载镜像和构建）

### 第 2 步：验证服务

启动完成后，访问：

- 🌐 **前端页面**: http://localhost
- 🔧 **后端 API**: http://localhost:48080
- ⚙️ **Jenkins**: http://localhost:8080

### 第 3 步：配置 Jenkins

```bash
# 获取初始密码
docker exec ruoyi-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. 访问 http://localhost:8080
2. 输入上面的密码
3. 选择"安装推荐插件"
4. 创建管理员账号

### 第 4 步：创建第一个流水线

1. Jenkins 首页 → 新建任务
2. 名称：`ruoyi-build`
3. 类型：流水线（Pipeline）
4. 配置：
   - SCM: Git
   - 仓库路径: 您的项目路径
   - 分支: `*/dev`
   - 脚本路径: `script/jenkins/Jenkinsfile-local`
5. 保存 → 立即构建

---

## 📋 日常开发命令

### 查看服务状态

```bash
docker-compose -f docker-compose.full.yml ps
```

### 查看日志

```bash
# 所有服务
docker-compose -f docker-compose.full.yml logs -f

# 特定服务
docker-compose -f docker-compose.full.yml logs -f backend
docker-compose -f docker-compose.full.yml logs -f jenkins
```

### 重启服务

```bash
# 重启所有
docker-compose -f docker-compose.full.yml restart

# 重启特定服务
docker-compose -f docker-compose.full.yml restart backend
```

### 停止服务

```bash
docker-compose -f docker-compose.full.yml down
```

---

## 🎯 快速体验完整流程

### 10 分钟体验 CI/CD

```bash
# 1. 创建功能分支
git checkout -b feature/demo

# 2. 修改任意代码（例如添加注释）

# 3. 提交
git add .
git commit -m "feat: 测试 CI/CD 流程"

# 4. 在 Jenkins 手动触发构建

# 5. 查看构建结果
```

---

## 📚 详细文档

- [企业开发流程完整手册](./enterprise-dev-guide.md)
- [Git 分支策略](./git-branch-strategy.md)

---

## ❓ 遇到问题？

### 端口冲突

```bash
# 查看端口占用
lsof -i :3306
lsof -i :6379
lsof -i :48080
lsof -i :80
lsof -i :8080

# 修改 docker-compose.full.yml 中的端口映射
```

### Docker 未启动

```bash
# Mac 上启动 Docker Desktop
open -a Docker

# 或使用 OrbStack
open -a OrbStack
```

### Jenkins 无法连接 Docker

```bash
# 检查 Docker Socket 权限
ls -l /var/run/docker.sock

# 重启 Jenkins
docker-compose -f docker-compose.full.yml restart jenkins
```

---

**开始您的企业级开发之旅！** 🎉
