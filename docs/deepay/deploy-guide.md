# Deepay 宝塔部署全流程指南（最终版）

> 服务器环境：Linux（CentOS 7/8 或 Ubuntu 20+）+ 宝塔面板  
> 项目根目录：`/www/wwwroot/deepay.srl`  
> 后端 JAR 目录：`/www/wwwroot/deepay.srl/yudao-server/target`  
> 管理前端目录：`/www/wwwroot/admin`（对应域名 `admin.deepay.srl`）  
> 后端 API 域名：`api.deepay.srl` → 反代 `127.0.0.1:48080`

---

## 目录

1. [前置环境安装（仅首次）](#1-前置环境安装)
2. [创建数据库](#2-创建数据库)
3. [上传 / 克隆代码](#3-上传--克隆代码)
4. [修改配置文件](#4-修改配置文件)
5. [一键部署脚本](#5-一键部署脚本)
6. [手动分步部署](#6-手动分步部署)
7. [宝塔 Nginx 配置](#7-宝塔-nginx-配置)
8. [开机自启（Systemd）](#8-开机自启-systemd)
9. [常用运维命令](#9-常用运维命令)
10. [常见问题](#10-常见问题)

---

## 1. 前置环境安装

> 在宝塔面板 → **软件商店** 中安装，或用以下命令。

### 1.1 JDK 17

```bash
# 宝塔面板 → 软件商店 → 搜索 "OpenJDK 17" 安装
# 或手动：
yum install -y java-17-openjdk-devel    # CentOS
apt install -y openjdk-17-jdk           # Ubuntu

java -version   # 确认输出 17.x
```

### 1.2 Maven 3.9

```bash
cd /usr/local
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
ln -s /usr/local/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn

# 配置阿里云镜像（国内加速）
cat > /usr/local/apache-maven-3.9.6/conf/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>aliyun maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

mvn -version    # 确认输出 3.9.x
```

### 1.3 Node.js 18 + npm

```bash
# 宝塔面板 → 软件商店 → 搜索 "Node" 安装 18.x
# 或用 nvm：
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

node -v && npm -v   # 确认 v18.x
```

### 1.4 Redis 6+

```bash
# 宝塔面板 → 软件商店 → 搜索 "Redis" 安装
# 确认运行状态：
systemctl status redis
redis-cli ping   # 应返回 PONG
```

### 1.5 MySQL 8

```bash
# 宝塔面板 → 软件商店 → 搜索 "MySQL 8.0" 安装
# 确认运行状态：
systemctl status mysqld
```

---

## 2. 创建数据库

在宝塔面板 → **数据库** → **添加数据库**，或用命令行：

```bash
mysql -u root -p
```

```sql
-- 创建数据库（字符集必须 utf8mb4）
CREATE DATABASE sdsdsdas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建账号并授权
CREATE USER 'sdsdsdas'@'localhost' IDENTIFIED BY 'sdsdsdas';
GRANT ALL PRIVILEGES ON sdsdsdas.* TO 'sdsdsdas'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

导入初始化 SQL（选其中一个，根据项目 sql 目录选最新文件）：

```bash
# 查看 sql 目录
ls /www/wwwroot/deepay.srl/sql/mysql/

# 导入（文件名以实际为准）
mysql -u sdsdsdas -p sdsdsdas sdsdsdas < /www/wwwroot/deepay.srl/sql/mysql/sdsdsdas.sql
# 如果文件名还是旧的：
# mysql -u sdsdsdas -p sdsdsdas sdsdsdas < /www/wwwroot/deepay.srl/sql/mysql/ruoyi-vue-pro.sql
```

---

## 3. 上传 / 克隆代码

### 方式 A：Git 克隆（推荐）

```bash
cd /www/wwwroot
git clone https://github.com/deepveloce-dot/ruoyi-vue-pro.git deepay.srl
cd deepay.srl
```

### 方式 B：宝塔文件管理器上传压缩包

1. 宝塔面板 → **文件** → 进入 `/www/wwwroot/`
2. 上传 zip 压缩包
3. 右键解压到 `deepay.srl` 目录

---

## 4. 修改配置文件

编辑 `yudao-server/src/main/resources/application-local.yaml`（已在代码里改好，确认一下）：

```bash
grep -A2 "master:" /www/wwwroot/deepay.srl/yudao-server/src/main/resources/application-local.yaml | grep "url\|username\|password"
```

应该看到：

```
url: jdbc:mysql://127.0.0.1:3306/sdsdsdas?...
username: sdsdsdas
password: sdsdsdas
```

如果没有，手动编辑：

```bash
nano /www/wwwroot/deepay.srl/yudao-server/src/main/resources/application-local.yaml
```

找到 `datasource.master` 和 `slave` 两段，改为：

```yaml
master:
  url: jdbc:mysql://127.0.0.1:3306/sdsdsdas?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
  username: sdsdsdas
  password: sdsdsdas
slave:
  lazy: true
  url: jdbc:mysql://127.0.0.1:3306/sdsdsdas?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&nullCatalogMeansCurrent=true
  username: sdsdsdas
  password: sdsdsdas
```

---

## 5. 一键部署脚本

> **推荐用法**：代码上传后执行一次搞定所有步骤。

```bash
cd /www/wwwroot/deepay.srl
chmod +x script/shell/deepay-deploy.sh
bash script/shell/deepay-deploy.sh
```

脚本会依次完成：

| 步骤 | 内容 |
|------|------|
| ① 环境检查 | 确认 java / mvn / node / npm 可用 |
| ② Maven 打包 | `mvn clean package -DskipTests` |
| ③ 备份旧 jar | 备份到 `run/backup/` |
| ④ 停止旧进程 | 优雅 kill，超时强制 kill -9 |
| ⑤ 启动新进程 | `nohup java -jar ... &` |
| ⑥ 健康检查 | 轮询 `/actuator/health` 最长 120s |
| ⑦ npm 构建前端 | `npm install && npm run build` |
| ⑧ 部署前端 | 复制 `dist/` 到 `/www/wwwroot/admin/` |

---

## 6. 手动分步部署

如果想分开操作，按以下顺序执行：

### 6.1 打包后端

```bash
cd /www/wwwroot/deepay.srl

# 全量打包（第一次，较慢）
mvn clean package -DskipTests

# 增量打包（只打 server 模块，较快）
mvn clean package -DskipTests -pl yudao-server -am
```

打包成功后 JAR 在：

```
/www/wwwroot/deepay.srl/yudao-server/target/yudao-server.jar
```

### 6.2 启动后端

```bash
mkdir -p /www/wwwroot/deepay.srl/run

# 先停掉旧进程（如果有）
PID=$(cat /www/wwwroot/deepay.srl/run/deepay-server.pid 2>/dev/null)
[ -n "$PID" ] && kill -15 "$PID" && sleep 3

# 启动新进程
nohup java -server -Xms512m -Xmx512m \
  -jar /www/wwwroot/deepay.srl/yudao-server/target/yudao-server.jar \
  --spring.profiles.active=local \
  >> /www/wwwroot/deepay.srl/run/deepay-server.log 2>&1 &

echo $! > /www/wwwroot/deepay.srl/run/deepay-server.pid
echo "后端已启动，PID=$!"
```

### 6.3 查看后端日志

```bash
tail -f /www/wwwroot/deepay.srl/run/deepay-server.log
```

看到 `Started YudaoServerApplication` 表示启动成功。

### 6.4 构建前端

```bash
cd /www/wwwroot/deepay.srl/yudao-ui-deepay

npm install
npm run build

# 构建完成后文件在 dist/ 目录
ls dist/
```

### 6.5 部署前端

```bash
# 清空旧文件并复制新文件
rm -rf /www/wwwroot/admin/*
cp -r /www/wwwroot/deepay.srl/yudao-ui-deepay/dist/. /www/wwwroot/admin/

echo "前端部署完成"
```

---

## 7. 宝塔 Nginx 配置

### 7.1 创建两个网站

在宝塔面板 → **网站** → **添加站点**：

| 域名 | 根目录 | 备注 |
|------|--------|------|
| `admin.deepay.srl` | `/www/wwwroot/admin` | 管理后台前端 |
| `api.deepay.srl`   | `/www/wwwroot/deepay.srl` | 后端反代（根目录随便填） |

### 7.2 管理前端 Nginx 配置

宝塔面板 → 网站 → `admin.deepay.srl` → **设置** → **配置文件**，替换为：

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name admin.deepay.srl;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name admin.deepay.srl;

    # SSL 证书（宝塔申请 Let's Encrypt 后自动填入）
    ssl_certificate     /www/server/panel/vhost/cert/admin.deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/admin.deepay.srl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    root /www/wwwroot/admin;
    index index.html;

    # Vue Router history 模式支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }

    # ── Deepay 前端 API（/api/**）→ 后端 48080 ──────────────────────────
    # 前端所有业务接口均以 /api/ 开头（design / quota / pay / shop / user …）
    # 必须在 location / 之前声明，否则 try_files 会把 API 请求当静态文件处理。
    location /api/ {
        proxy_pass http://127.0.0.1:48080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # 代理到后端管理 API（/admin-api/）
    location /admin-api/ {
        proxy_pass http://127.0.0.1:48080/admin-api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # WebSocket 支持
    location /infra/ws {
        proxy_pass http://127.0.0.1:48080/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }
}
```

### 7.3 后端 API Nginx 配置

宝塔面板 → 网站 → `api.deepay.srl` → **设置** → **配置文件**，替换为：

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name api.deepay.srl;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name api.deepay.srl;

    ssl_certificate     /www/server/panel/vhost/cert/api.deepay.srl/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/api.deepay.srl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 上传文件大小限制
    client_max_body_size 32m;

    location / {
        proxy_pass http://127.0.0.1:48080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # WebSocket 支持
    location /infra/ws {
        proxy_pass http://127.0.0.1:48080/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }
}
```

### 7.4 申请 SSL 证书

宝塔面板 → 网站 → 对应域名 → **SSL** → **Let's Encrypt** → 申请免费证书  
（需要域名 DNS 已解析到本服务器 IP）

---

## 8. 开机自启（Systemd）

创建 systemd 服务，保证服务器重启后自动启动后端：

```bash
cat > /etc/systemd/system/deepay-server.service << 'EOF'
[Unit]
Description=Deepay Server (Spring Boot)
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/www/wwwroot/deepay.srl/run
ExecStart=/usr/bin/java -server -Xms512m -Xmx512m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/www/wwwroot/deepay.srl/run/heapDump \
    -jar /www/wwwroot/deepay.srl/run/yudao-server.jar \
    --spring.profiles.active=local
ExecStop=/bin/kill -15 $MAINPID
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=append:/www/wwwroot/deepay.srl/run/deepay-server.log
StandardError=append:/www/wwwroot/deepay.srl/run/deepay-server.log

[Install]
WantedBy=multi-user.target
EOF

# 注意：先用部署脚本跑一次，把 jar 复制到 run/ 目录后再启用 systemd
systemctl daemon-reload
systemctl enable deepay-server
systemctl start deepay-server
systemctl status deepay-server
```

> 启用 systemd 后，请**停用**原来 nohup 启动的进程，避免端口冲突：
> ```bash
> kill $(cat /www/wwwroot/deepay.srl/run/deepay-server.pid)
> ```

---

## 9. 常用运维命令

```bash
# ---- 后端相关 ----

# 查看后端日志（实时）
tail -f /www/wwwroot/deepay.srl/run/deepay-server.log

# 查看后端日志（最后 100 行）
tail -100 /www/wwwroot/deepay.srl/run/deepay-server.log

# 查看进程是否运行
ps aux | grep yudao-server.jar | grep -v grep

# 健康检查
curl http://127.0.0.1:48080/actuator/health

# 重启后端（systemd 方式）
systemctl restart deepay-server

# 重启后端（nohup 方式）
kill $(cat /www/wwwroot/deepay.srl/run/deepay-server.pid) && sleep 3
bash /www/wwwroot/deepay.srl/script/shell/deepay-deploy.sh

# ---- 数据库相关 ----

# 连接数据库
mysql -u sdsdsdas -p sdsdsdas

# 备份数据库
mysqldump -u sdsdsdas -p sdsdsdas sdsdsdas > /www/backup/sdsdsdas-$(date +%Y%m%d).sql

# ---- 重新部署 ----

# 拉取最新代码后一键重新部署
cd /www/wwwroot/deepay.srl
git pull
bash script/shell/deepay-deploy.sh
```

---

## 10. 常见问题

### Q1：启动报错 `Unable to acquire JDBC Connection`

**原因**：数据库连接失败  
**解决**：
```bash
# 检查 MySQL 是否运行
systemctl status mysqld

# 检查账号密码
mysql -u sdsdsdas -p sdsdsdas -e "SELECT 1"

# 检查配置文件
grep -A5 "master:" /www/wwwroot/deepay.srl/yudao-server/src/main/resources/application-local.yaml
```

### Q2：启动报错 `Redis connection refused`

**原因**：Redis 未运行  
**解决**：
```bash
systemctl start redis
redis-cli ping  # 应返回 PONG
```

### Q3：前端打开空白 / 404

**原因**：Vue Router history 模式需要 Nginx 配置 `try_files`  
**解决**：确认 Nginx 配置中有：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### Q4：API 请求 403 / CORS 跨域

**原因**：前端请求的 API 地址和后端配置不一致  
**解决**：检查前端 `yudao-ui-deepay/vite.config.js` 中的 `proxy` 设置，以及 `application.yaml` 中 `yudao.web.admin-ui.url` 是否为 `https://admin.deepay.srl`。

### Q5：Maven 打包很慢 / 超时

**解决**：确认使用了阿里云镜像，或在公司内网走代理：
```bash
cat ~/.m2/settings.xml  # 或 /usr/local/apache-maven-*/conf/settings.xml
```

### Q6：端口 48080 被占用

```bash
# 查看谁占用了端口
ss -tlnp | grep 48080
# 或
lsof -i :48080

# 强制释放
kill -9 $(lsof -t -i:48080)
```

---

## 部署检查清单

部署完成后按以下项目逐一确认：

- [ ] `curl http://127.0.0.1:48080/actuator/health` 返回 `{"status":"UP"}`
- [ ] `curl https://api.deepay.srl/actuator/health` 返回 `{"status":"UP"}`
- [ ] 浏览器访问 `https://admin.deepay.srl` 可以看到登录页
- [ ] 能用管理员账号 `admin / admin123` 登录（初始密码以 SQL 文件为准）
- [ ] Swagger 文档 `https://api.deepay.srl/swagger-ui` 可访问
- [ ] MySQL / Redis 宝塔面板显示运行中
- [ ] Nginx 两个站点 SSL 证书有效

---

*本文档最后更新于部署时生成，如有疑问查看 `script/shell/deepay-deploy.sh` 源码。*
