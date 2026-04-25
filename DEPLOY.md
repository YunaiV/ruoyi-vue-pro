# Deepay 部署手册

> **一页搞定**：从零到上线，包含所有入口、域名配置、SSL、运维命令。  
> 服务器：Linux（CentOS 7/8 或 Ubuntu 20+）+ **宝塔面板**  
> 项目目录：`/www/wwwroot/deepay.srl`

---

## 📐 架构总览

```
用户/手机 App
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│                        Nginx                                │
│                                                             │
│  deepay.srl          →  PWA 前端 dist/    ← Web 入口        │
│  www.deepay.srl      →  同上                                │
│  modaui.com          →  302 → deepay.srl  ← 国际域名        │
│  admin.deepay.srl    →  PWA dist/ + /admin 路由 ← 后台入口  │
│  api.deepay.srl      →  :48080  ← REST API 网关             │
│  ai.deepay.srl       →  302 → deepay.srl/ai-sales           │
└─────────────────────────────────────────────────────────────┘
              │                        │
              ▼                        ▼
     deepay-pwa/dist/        Spring Boot :48080
     （Vue 3 PWA）           （RuoYi-Vue-Pro）
                                       │
                               MySQL 3306 · Redis 6379
```

### 5 个访问入口

| 入口 | 域名 | 说明 |
|------|------|------|
| 🌐 Web 用户端 | `https://deepay.srl` | 主站 PWA，PC / 手机浏览器 |
| 📱 手机 App | 同上安装到桌面 | iOS: Safari→分享→添加到主屏幕<br>Android: Chrome→菜单→安装应用 |
| ⚙️ 管理后台 | `https://admin.deepay.srl`<br>或 `https://deepay.srl/admin` | 同一 PWA，`/admin` 路由全屏独立 |
| 🔌 API 接口 | `https://api.deepay.srl` | Spring Boot REST API |
| 🤖 AI 开店 | `https://ai.deepay.srl` | 302 跳转到 `/ai-sales` |

---

## 🚀 一键部署（推荐）

```bash
# 1. 代码上传到服务器后，进入项目目录
cd /www/wwwroot/deepay.srl

# 2. 执行一键脚本（自动完成：环境检查 → 数据库 → 后端打包 → 启动 → 前端构建 → Nginx → systemd）
bash script/shell/quickstart.sh
```

**脚本自动完成的 10 个步骤：**

1. 自动检测宝塔 JDK17 / 配置 `JAVA_HOME`
2. 检查并安装缺失依赖（Nginx / MySQL / Maven / Node / Redis）
3. 配置 Maven 阿里云加速镜像（仅首次）
4. 创建 MySQL 数据库 + 账号（幂等）
5. 按顺序导入全部 SQL 文件
6. 写后端生产配置 `application-prod.yml`
7. 下载预构建 JAR（失败则本地 Maven 打包）
8. 停止旧进程 → 启动新后端 → 健康检查
9. 构建前端 `deepay-pwa` → 部署到站点根目录
10. 生成 **多域名 Nginx 配置** → reload → 注册 systemd 开机自启

---

## 🔧 分步手动部署

### 前置环境（宝塔软件商店安装）

| 软件 | 版本 | 宝塔搜索关键词 |
|------|------|---------------|
| JDK | 17 | `OpenJDK 17` |
| Maven | 3.9+ | 或命令行安装见下方 |
| Node.js | 18+ | `Node` |
| MySQL | 8.0 | `MySQL 8.0` |
| Redis | 6+ | `Redis` |
| Nginx | 最新稳定版 | `Nginx` |

```bash
# Maven 手动安装（若宝塔未提供）
cd /usr/local
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
ln -sf /usr/local/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn
```

### 步骤 1 — 数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE deepay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'deepay'@'localhost' IDENTIFIED BY 'deepay393163';
GRANT ALL PRIVILEGES ON deepay.* TO 'deepay'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

```bash
# 按顺序导入 SQL（密码：deepay393163）
mysql -u deepay -p deepay < sql/mysql/ruoyi-vue-pro.sql
mysql -u deepay -p deepay < sql/mysql/quartz.sql
mysql -u deepay -p deepay < sql/mysql/deepay.sql
```

### 步骤 2 — 后端打包 & 启动

```bash
cd /www/wwwroot/deepay.srl

# 打包（首次约 5~10 分钟）
mvn clean package -DskipTests -pl yudao-server -am

# 准备运行目录
mkdir -p run/backend/logs run/backend/config run/backend/backup
cp run/backend/config/application-prod.yml run/backend/config/

# 启动
nohup java -server -Xms512m -Xmx512m \
  -jar yudao-server/target/yudao-server.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:./run/backend/config/ \
  >> run/backend/logs/app.log 2>&1 &
echo $! > run/backend/deepay.pid

# 健康检查
curl http://127.0.0.1:48080/actuator/health
# 返回 {"status":"UP"} 表示成功
```

### 步骤 3 — 前端构建 & 部署

```bash
cd /www/wwwroot/deepay.srl/deepay-pwa
npm install
npm run build

# 部署到站点根目录
rm -rf /www/wwwroot/deepay.srl/dist-bak
cp -r /www/wwwroot/deepay.srl/deepay-pwa/dist/. /www/wwwroot/deepay.srl/
```

### 步骤 4 — Nginx 配置

复制以下配置到宝塔面板 → 网站配置文件（或写入 `/www/server/panel/vhost/nginx/deepay.conf`）：

```nginx
# HTTP → HTTPS 全局跳转
server {
    listen 80 default_server;
    server_name _;
    return 301 https://$host$request_uri;
}

# ① deepay.srl — 主站 PWA
server {
    listen 443 ssl http2;
    server_name deepay.srl www.deepay.srl;

    ssl_certificate     /www/server/panel/vhost/cert/deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/deepay.srl/privkey.pem;

    root /www/wwwroot/deepay.srl;
    index index.html;
    client_max_body_size 32m;

    location / {
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://127.0.0.1:48080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
    location /admin-api/ {
        proxy_pass http://127.0.0.1:48080/admin-api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
    location /infra/ws {
        proxy_pass http://127.0.0.1:48080/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 3600s;
    }
    location ~* \.(js|css|png|jpg|svg|ico|woff2?)$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
}

# ② modaui.com → 跳转主站
server {
    listen 443 ssl http2;
    server_name modaui.com www.modaui.com;
    ssl_certificate     /www/server/panel/vhost/cert/modaui.com/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/modaui.com/privkey.pem;
    return 302 https://deepay.srl$request_uri;
}

# ③ admin.deepay.srl — 管理后台（同一 PWA，/admin 路由全屏）
server {
    listen 443 ssl http2;
    server_name admin.deepay.srl;
    ssl_certificate     /www/server/panel/vhost/cert/admin.deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/admin.deepay.srl/privkey.pem;
    root /www/wwwroot/deepay.srl;
    index index.html;
    location = / { return 302 /admin; }
    location / { try_files $uri $uri/ /index.html; }
    location /admin-api/ {
        proxy_pass http://127.0.0.1:48080/admin-api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 300s;
    }
    location /api/ {
        proxy_pass http://127.0.0.1:48080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 300s;
    }
    location ~* \.(js|css|png|jpg|svg|ico|woff2?)$ { expires 30d; }
}

# ④ api.deepay.srl — 纯 API 网关
server {
    listen 443 ssl http2;
    server_name api.deepay.srl;
    ssl_certificate     /www/server/panel/vhost/cert/api.deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/api.deepay.srl/privkey.pem;
    client_max_body_size 32m;
    add_header Access-Control-Allow-Origin  "https://deepay.srl" always;
    add_header Access-Control-Allow-Methods "GET,POST,PUT,DELETE,OPTIONS,PATCH" always;
    add_header Access-Control-Allow-Headers "Authorization,Content-Type,X-Requested-With" always;
    location / {
        if ($request_method = OPTIONS) { return 204; }
        proxy_pass http://127.0.0.1:48080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
    location /infra/ws {
        proxy_pass http://127.0.0.1:48080/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 3600s;
    }
}

# ⑤ ai.deepay.srl → AI 开店页
server {
    listen 443 ssl http2;
    server_name ai.deepay.srl;
    ssl_certificate     /www/server/panel/vhost/cert/ai.deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/ai.deepay.srl/privkey.pem;
    return 302 https://deepay.srl/ai-sales$is_args$args;
}
```

```bash
nginx -t && nginx -s reload
```

### 步骤 5 — SSL 证书

宝塔面板 → **网站** → 每个域名 → **SSL** → **Let's Encrypt** → 申请  
需要申请的域名（DNS 必须已解析到服务器 IP）：

```
deepay.srl
www.deepay.srl
admin.deepay.srl
api.deepay.srl
ai.deepay.srl
modaui.com
www.modaui.com
```

---

## ♻️ 重新部署（日常更新）

```bash
cd /www/wwwroot/deepay.srl
git pull

# 全量重新部署（后端 + 前端）
bash script/shell/quickstart.sh

# 只更新前端
bash script/shell/deepay-deploy.sh frontend

# 只重启后端
bash script/shell/deepay-deploy.sh backend
```

---

## 🛠 常用运维命令

```bash
# ── 后端 ──────────────────────────────────────
# 查看日志（实时）
tail -f /www/wwwroot/deepay.srl/run/backend/logs/app.log

# 健康检查
curl http://127.0.0.1:48080/actuator/health

# 重启后端（systemd）
systemctl restart deepay

# 查看后端状态
systemctl status deepay

# 查看后端进程
ps aux | grep yudao-server.jar | grep -v grep

# ── 前端 ──────────────────────────────────────
# 单独重建前端
cd /www/wwwroot/deepay.srl/deepay-pwa && npm run build
cp -r dist/. /www/wwwroot/deepay.srl/

# ── Nginx ─────────────────────────────────────
nginx -t                    # 检查配置语法
nginx -s reload             # 重载配置

# ── 数据库 ────────────────────────────────────
# 连接数据库
mysql -u deepay -p deepay

# 备份数据库
mysqldump -u deepay -p deepay > /root/deepay-backup-$(date +%Y%m%d).sql

# ── 端口排查 ──────────────────────────────────
ss -tlnp | grep 48080       # 查看后端端口占用
ss -tlnp | grep 80          # 查看 HTTP 端口
ss -tlnp | grep 443         # 查看 HTTPS 端口
```

---

## 🔍 部署检查清单

部署完成后逐项确认：

- [ ] `curl http://127.0.0.1:48080/actuator/health` → `{"status":"UP"}`
- [ ] 浏览器访问 `https://deepay.srl` → 显示 Deepay 主页
- [ ] 浏览器访问 `https://deepay.srl/admin` → 显示管理后台（全屏）
- [ ] 浏览器访问 `https://admin.deepay.srl` → 自动跳转到 `/admin`
- [ ] 浏览器访问 `https://api.deepay.srl/actuator/health` → `{"status":"UP"}`
- [ ] 浏览器访问 `https://ai.deepay.srl` → 302 跳转到 AI 开店页
- [ ] 浏览器访问 `https://modaui.com` → 302 跳转到 `deepay.srl`
- [ ] 侧栏 5 个页面（主页/图库/AI开店/模板库/设置）互相跳转正常
- [ ] 侧栏底部"管理后台"按钮可进入 `/admin`
- [ ] 手机浏览器可安装为桌面 App（PWA standalone 模式）
- [ ] SSL 证书全部有效（宝塔面板查看到期时间）
- [ ] MySQL / Redis 在宝塔面板显示"运行中"

---

## ❓ 常见问题

### Q1：后端启动报 `Unable to acquire JDBC Connection`
```bash
systemctl status mysqld           # 检查 MySQL 是否运行
mysql -u deepay -p deepay -e "SELECT 1"   # 验证账号密码
```

### Q2：后端启动报 `Redis connection refused`
```bash
systemctl start redis
redis-cli ping   # 应返回 PONG
```

### Q3：前端打开空白 / 刷新 404
确认 Nginx 有 `try_files $uri $uri/ /index.html;`（Vue Router history 模式必须）。

### Q4：API 跨域 / 403
检查 `run/backend/config/application-prod.yml` 中的 `yudao.web.admin-ui.url` 是否包含前端域名。

### Q5：端口 48080 被占用
```bash
lsof -i :48080
kill -9 $(lsof -t -i:48080)
```

### Q6：Maven 打包超慢
检查 Maven 阿里云镜像是否配置：
```bash
cat ~/.m2/settings.xml | grep aliyun
```
若无输出，重新运行 `quickstart.sh` 第 2 步会自动配置。

---

## 📂 项目结构

```
deepay.srl/
├── deepay-pwa/              ← Vue 3 PWA 前端（Web + 手机 App + /admin）
│   ├── src/views/
│   │   ├── Home.vue         ← 主页（AI 对话）
│   │   ├── ImageLibrary.vue ← 图库
│   │   ├── AISales.vue      ← AI 开店
│   │   ├── TemplateLibrary.vue ← 模板库
│   │   ├── Settings.vue     ← 设置
│   │   └── Admin.vue        ← 管理后台（/admin，全屏独立）
│   └── dist/                ← vite build 输出（部署到站点根）
├── yudao-server/            ← Spring Boot 后端
├── run/backend/             ← 运行时目录（jar / logs / config / pid）
├── sql/mysql/               ← 数据库初始化 SQL
├── script/shell/
│   ├── quickstart.sh        ← 一键全量部署
│   └── deepay-deploy.sh     ← 分模块部署（backend/frontend/all）
└── DEPLOY.md                ← 本文档
```
