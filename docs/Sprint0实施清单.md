# Sprint 0 实施清单（第 1-2 周）

> 目标：芋道骨架跑通、小程序登录链路打通、ymq-module-* 骨架建立、可重复部署。
> 完成标志：见文末"Sprint 0 退出门禁"。
> 配套文档：[2-MVP与验收.md](2-MVP与验收.md) · [4-技术架构.md](4-技术架构.md) · [3-路线图与待决策.md](3-路线图与待决策.md)

---

## D-1：环境准备（半天）

- [ ] 安装 JDK 17（推荐 SDKMAN：`sdk install java 17.0.10-tem`）
- [ ] 安装 Maven 3.9+
- [ ] 安装 MySQL 8.0（本地或 Docker）
- [ ] 安装 Redis 7（本地或 Docker）
- [ ] 安装 Node.js 20+（给芋道前端用）
- [ ] 安装 IntelliJ IDEA Ultimate（社区版也行，但插件支持差）
- [ ] 安装微信开发者工具（最新版）
- [ ] 注册腾讯云账号、申请域名（`ymq.example.com`）+ 启动 ICP 备案

**验证**：`java -version` 显示 17.x；`mvn -v` 正常；MySQL/Redis 能连通。

---

## D-2：Fork 芋道 + 跑通本地启动（1 天）

- [ ] 在 GitHub fork [YunaiV/ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro) 到自己账号
- [ ] `git clone` 到 `~/IdeaProjects/ymq-backend`（保留 `ymq` 目录给整体工程后续整理）
- [ ] 切到最新稳定 tag（如 `v2026.04(jdk17/21)`），打 `ymq-base` 分支
- [ ] 用 IDEA 打开，等 Maven 下载完依赖
- [ ] 创建本地 MySQL 数据库 `ymq_dev`
- [ ] 执行芋道自带 SQL 脚本（`sql/mysql/ruoyi-vue-pro.sql`）
- [ ] 修改 `application-local.yaml` 中数据库/Redis 连接
- [ ] 启动 `YudaoServerApplication`
- [ ] 访问 `http://localhost:48080/admin-api/system/captcha/check` 看返回 JSON

**验证**：芋道后端启动无 ERROR，运营后台 API 可访问。

---

## D-3：砍掉芋道无用模块（半天）

按 [4-技术架构.md T-01](4-技术架构.md#技术决策记录tdr) 决策砍模块：

- [ ] 删除 `yudao-module-mall/`（商城）
- [ ] 删除 `yudao-module-erp/`（进销存）
- [ ] 删除 `yudao-module-crm/`（CRM）
- [ ] 删除 `yudao-module-bpm/`（工作流）
- [ ] 删除 `yudao-module-ai/`（芋道自带 AI，与我们方案冲突）
- [ ] 删除 `yudao-module-iot/`（IoT，如果有）
- [ ] 删除 `yudao-module-report/`（如不需要复杂报表）
- [ ] 删除 `yudao-server/pom.xml` 中对上述模块的 `<dependency>`
- [ ] 删除 `yudao-server/src/main/resources/application*.yaml` 中相关配置
- [ ] 同步删除前端 `yudao-ui-admin-vue3/` 对应菜单（保留先不删，后期一起处理）
- [ ] `mvn clean install -DskipTests` 确保能编译

**验证**：编译成功；启动成功；运营后台菜单清爽。

→ 满足验收 [N-08](2-MVP与验收.md)

---

## D-4：创建 ymq-module-* 骨架（半天）

- [ ] 在 root pom.xml 的 `<modules>` 加入：
  ```xml
  <module>ymq-module-activity</module>
  <module>ymq-module-poster</module>
  ```
  （其他模块先不建，按需创建）
- [ ] 复制 `yudao-module-infra/` 作为模板，创建 `ymq-module-activity/`
- [ ] 内部结构：
  ```
  ymq-module-activity/
  ├── ymq-module-activity-api/      ← 接口定义（给其他模块依赖）
  └── ymq-module-activity-biz/      ← 业务实现
      └── src/main/java/
          └── cn/iocoder/yudao/module/ymq/activity/
              ├── controller/
              │   ├── admin/        ← /admin-api/ymq/activity/**
              │   └── app/          ← /app-api/ymq/activity/**
              ├── service/
              ├── dal/
              │   ├── dataobject/
              │   └── mysql/
              └── enums/
  ```
- [ ] 同样建 `ymq-module-poster/`（先空着，预留）
- [ ] 在 `yudao-server/pom.xml` 引用新模块
- [ ] 写一个 hello world Controller 在 `ymq-module-activity-biz` 的 `app/` 下，返回 `{"hello": "ymq"}`
- [ ] 启动后访问 `/app-api/ymq/activity/hello` 返回正常

**验证**：新模块编译通过，启动后能命中。

→ 满足验收 [A-09](2-MVP与验收.md) 的一半（接口前缀路由通）

---

## D-5：小程序 appId 配置 + 微信登录（1 天）

- [ ] 微信公众平台注册小程序（个人 / 企业主体）
- [ ] 拿到 `appId` 和 `secret`
- [ ] **不要**写到 `application-prod.yaml` 提交 git；写到本地 `application-local.yaml` 或环境变量
- [ ] 配 git 忽略：`.gitignore` 加 `application-local.yaml`、`*.secret.yaml`
- [ ] 芋道配置项：
  ```yaml
  yudao:
    wechat:
      miniapp:
        appid: ${WECHAT_MINIAPP_APPID}
        secret: ${WECHAT_MINIAPP_SECRET}
  ```
- [ ] 用芋道自带的小程序登录接口 `POST /app-api/member/auth/weixin-mini-app-login`，参数：`code` + `state`
- [ ] 验证返回 token + 用户信息

**验证**：从微信开发者工具（或 Postman 模拟）调用登录，返回 JWT token。

→ 满足验收 [U-01](2-MVP与验收.md) + [N-07](2-MVP与验收.md)

---

## D-6：建 C 端用户表 t_user + 隔离运营用户（半天）

- [ ] 写 SQL `sql/migration/V001__init_t_user.sql`（参考 [data-model.md §2.1](../data-model.md)）
- [ ] 执行迁移：`mysql ymq_dev < sql/migration/V001__init_t_user.sql`
- [ ] 在 `ymq-module-activity-biz` 建 `UserDO` + `UserMapper` + `UserService`
- [ ] 小程序登录成功后，若 `t_user` 没有此 openid 记录则插入
- [ ] 改造 `/app-api/*` SecurityConfig，使其只信任 `t_user` 的 token
- [ ] 验证：用 `system_users` token 访问 `/app-api/*` 返回 401
- [ ] 验证：用 `t_user` token 访问 `/admin-api/*` 返回 401

**验证**：U-05 验收通过——两套用户体系完全隔离。

→ 满足验收 [U-05](2-MVP与验收.md)

---

## D-7：小程序前端骨架（1 天）

- [ ] 用微信开发者工具新建小程序工程到 `ymq/ymq-miniprogram/`
- [ ] 配 appId
- [ ] 接入 [Vant Weapp](https://vant-contrib.gitee.io/vant-weapp/)：`npm i @vant/weapp -S`
- [ ] 配 `app.json` 全局组件
- [ ] 抄 [yudaocode/yudao-mall-uniapp](https://github.com/yudaocode/yudao-mall-uniapp) 的登录态/请求拦截器代码到我们工程
- [ ] 实现 `utils/request.js`（带 token 自动续期 + 401 自动跳登录）
- [ ] 实现 `pages/index/index`：调 `/app-api/ymq/activity/hello`，显示返回
- [ ] 实现 `pages/login/login`：调微信登录拿 code → 请求后端登录接口 → 存 token

**验证**：小程序启动 → 自动登录 → 首页显示 hello world。

---

## D-8：测试环境部署 + CI/CD（1 天）

- [ ] 购买腾讯云轻量服务器（2 核 4G，约 ¥60/月）
- [ ] 装 JDK 17、MySQL 8、Redis 7（或腾讯云数据库 + Redis）
- [ ] 域名解析 + Nginx 反向代理 + HTTPS 证书（腾讯云免费 SSL）
- [ ] 小程序后台白名单加入测试域名
- [ ] 写 `Dockerfile`（可选）或 `deploy.sh` 部署脚本
- [ ] 写 GitHub Actions workflow `.github/workflows/deploy.yml`：
  - push 到 `develop` 分支 → 构建 jar → SSH 上传 → 重启服务
- [ ] 把 `WECHAT_MINIAPP_APPID` 等敏感配置加到 GitHub Secrets
- [ ] 触发一次部署，验证测试环境可访问

**验证**：能从外网访问 `https://ymq.example.com/admin-api/system/captcha/check`。

---

## D-9：基础工具与监控（半天）

- [ ] 接入 Sentry（免费版）后端 + 小程序
- [ ] 接入腾讯云 CLS 日志收集
- [ ] 改密码：芋道默认 `admin/admin123` 改成自己的强密码
- [ ] 删除芋道默认演示账号（test/test123 等）
- [ ] 配置 MySQL 自动备份（腾讯云自带每日备份）

→ 满足验收 [O-05](2-MVP与验收.md) + [N-06](2-MVP与验收.md)

---

## D-10：演示与文档（半天）

- [ ] 录一段 30 秒的演示视频：
  - 小程序启动 → 登录 → 调 hello world
  - 运营后台登录 → 看用户列表
- [ ] 更新 README.md 加"如何本地启动"段落
- [ ] 把 `application-local.yaml.example` 提交（不含真实 secret），便于新成员上手

---

## Sprint 0 退出门禁

满足以下全部条件 = Sprint 0 完成，可进入 Sprint 1：

| 项 | 验证方式 |
|---|---|
| ✅ 芋道工程能本地启动，无 ERROR | `mvn spring-boot:run` |
| ✅ 砍掉 mall/erp/crm/bpm/ai 模块后能编译启动 | `mvn clean install` |
| ✅ `/admin-api/*` 和 `/app-api/*` 能分别访问 | curl 两个前缀的 hello 接口 |
| ✅ 小程序能微信登录拿到 token | 开发者工具走一遍流程 |
| ✅ `t_user` 和 `system_users` 完全隔离 | 用 A 端 token 访问 B 端返回 401 |
| ✅ WxJava 配置走环境变量，git 干净 | `git log -p -- '*.yaml' | grep -i secret` 无输出 |
| ✅ 测试环境部署成功 | 浏览器访问 https 域名 |
| ✅ 默认密码已改 | 登录运营后台用新密码 |

---

## 常见坑（提前避雷）

| 坑 | 应对 |
|---|---|
| 芋道默认配置 schema 跟你本地数据库版本不匹配 | 用芋道指定版本的 SQL，别用最新的 |
| `application-local.yaml` 被误提交 | 立即用 `git filter-repo` 清掉历史，密钥 revoke 重新生成 |
| 小程序后台域名白名单忘记加 | 体现在 wx.request 报"不在以下 request 合法域名列表" |
| 砍模块时漏删 ApplicationRunner | 启动报 NoSuchBean，删 `@Component`/`@Service` 引用 |
| WxJava 版本和芋道不一致 | 用芋道 pom 锁定的版本，别自己升 |
| SecurityConfig 改坏导致全部接口 401 | 改之前先 `git stash` 一下，方便回滚 |

---

## 时间估算

| 任务 | 工时 |
|---|---|
| D1 环境 | 0.5 天 |
| D2 跑通芋道 | 1 天 |
| D3 砍模块 | 0.5 天 |
| D4 模块骨架 | 0.5 天 |
| D5 微信登录 | 1 天 |
| D6 t_user 隔离 | 0.5 天 |
| D7 小程序前端 | 1 天 |
| D8 部署 + CI | 1 天 |
| D9 监控 | 0.5 天 |
| D10 演示 | 0.5 天 |
| **合计** | **7 个工作日** |

留 3 天 buffer 给踩坑和环境问题，2 周完成。
