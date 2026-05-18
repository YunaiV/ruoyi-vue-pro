# Sprint 0 实际进度记录

> 与 [Sprint0实施清单.md](Sprint0实施清单.md) 配对：清单是 D1-D10 的**计划**，本文件是**实际进度 + 偏离决策 + 下次 session 接力点**。
> Claude 每完成一个关键节点就更新这里，便于换账号/换 session 接力。

---

## 当前状态：2026-05-18 15:05 ｜ Sprint 0 主体收工，转 Sprint 1

**Sprint 0 决策**：D5/D6 SecurityConfig + 微信登录链路**推迟**到外部资源齐备（小程序 appId/secret + 小程序前端 D7）才做。
**理由**：D5 唯一可独立做的事是"开 member 模块 + 手动转 PostgreSQL 建表 SQL 为 MySQL + 启动验证"，工作量 30% 但只解锁 5% 的真实进度，且 member 强制 phoneCode 限制；不如先做 Sprint 1 业务，登录链路靠后批量 unblock。
**转向**：进入 Sprint 1 第 1 周——活动接龙业务（建表 + CRUD API + 报名/取消报名）。所有 user 上下文先用 query string `?userId=1` mock，D5 完成后改为 SecurityContext 注入。

后续进度记录见 [Sprint1进度.md](Sprint1进度.md)。

1. ✅ D1-D4 全部跑通：芋道启动 → 双前缀路由 → ymq-module-activity 接入并被 `/app-api/activity/hello` 命中
2. ✅ `curl -H "tenant-id: 1" http://localhost:48080/app-api/activity/hello` 返回 `hello from ymq-module-activity`
3. ✅ `curl http://localhost:48080/admin-api/activity/hello` 返回 401（路由按 `controller.app.**` 包路径正确隔离）
4. ⏳ 用户在自己终端跑 baseline commit（pinentry-mac 弹窗输 GPG 密码）—— 完成后我把 D2/D3/D4 改动 + 进度 md 一并 commit
5. ⏳ 启动进程在后台跑（任务 id bzdaa1d49），D5 期间保持运行

**已满足 Sprint0 退出门禁的项**：
- ✅ 芋道本地启动无 ERROR
- ✅ 砍模块后能编译启动
- ✅ /admin-api/* 和 /app-api/* 能分别访问
- ⏳ 小程序微信登录拿到 token（D5）
- ⏳ t_user vs system_users 隔离（D6）
- ⏳ git 干净（D5 前需要把 wxa secret 从 application-local.yaml 抽出来）
- ⏳ 测试环境部署（D8）
- ⏳ 默认密码已改（D9）

---

## 已完成

### D1（环境）✅
- 装：OpenJDK 17.0.19、Maven 3.9.12（已有）、MySQL 8.0.46、Redis 8.6.3、gh 2.92.0、pinentry-mac 1.3.1.1
- 改 `~/.zshrc`：JAVA_HOME 切到 openjdk@17，PATH 加 mysql@8.0/bin
- 加 `~/.zshenv`：写同样配置，让**非交互式 shell**（含 Claude Code Bash）也能拿到——但本 session 因 shell snapshot 已锁定，仍需每次 Bash 显式 export
- 改 `~/.gnupg/gpg-agent.conf`：指定 pinentry-program 为 pinentry-mac，避免 GPG 签名时拿不到密码
- 改 `~/.m2/settings.xml`：禁用旧的 epoint-nexus mirror（mirrorOf 改成 epoint-private-disabled）、加阿里云 public + spring + spring-plugin 三个镜像
- MySQL/Redis 已用 `brew services` 启动，开机自启
- MySQL root **无密码**（brew 默认）

### D2（fork + clone + DB）✅
- gh CLI 登录：账号 `carljings`
- `gh repo fork YunaiV/ruoyi-vue-pro --clone --fork-name=ymq` → https://github.com/carljings/ymq
- 本地 clone 到 `/Users/guchuan/IdeaProjects/ymq`（**单仓库**，docs 和后端代码同仓）
- 检出 tag `v2026.04(jdk17/21)`，建分支 `ymq-base`（这是开发主分支）
- 创建 MySQL 数据库 `ymq_dev`（utf8mb4），灌入：
  - `sql/mysql/ruoyi-vue-pro.sql`（1.3MB）
  - `sql/mysql/quartz.sql`（41KB）
  - 共 **59 张表**
- 改 `yudao-server/src/main/resources/application-local.yaml`：
  - master/slave 数据库 URL：`ruoyi-vue-pro` → `ymq_dev`
  - master/slave password：`123456` → `""`（root 无密）
- 改 `.gitignore`：加 `application-local.yaml`、`*.secret.yaml`、`.claude/`、`.omx/`
- 把原 `README.md` 改名为 `README.upstream.md`（保留芋道原文），用我们的 YMQ README 覆盖
- 备份目录：`/tmp/ymq-prep-backup/`（含 docs/README/.claude/.omx，可清理）

### D3（砍模块）✅
- 物理删除 10 个目录：`yudao-module-{mall,erp,crm,bpm,iot,ai,mes,pay,mp,report}`
- 保留：`yudao-module-system`、`yudao-module-infra`、`yudao-module-member`（member 暂留）
- 验证：root pom.xml 和 yudao-server/pom.xml 对这 10 个模块的引用本来就是注释状态（芋道默认行为），无 dangling refs
- ⚠️ **新发现**：`yudao-module-member` 在 root pom.xml 也被默认注释了，没参与编译。D5 要用其微信登录接口前需要**取消注释 root pom + yudao-server/pom 对应行**，否则 `/app-api/member/auth/weixin-mini-app-login` 不存在

### 验证（D2/D3 退出门禁）✅
- `mvn clean install -DskipTests -T 4` 全 21 个模块 BUILD SUCCESS，编译时间 16 分钟
- `java -jar yudao-server/target/yudao-server.jar` 启动 13.5 秒，端口 48080
- 双前缀路由验证：admin-api 401、app-api 401（路由命中、SecurityConfig 生效）

### D4（创建 ymq-module-activity + ymq-module-poster）✅
- **关键决策**：放弃 Sprint0 清单的 `api+biz` 双子模块拆分，改成**扁平**结构（理由：v2026.04 芋道自身已经放弃这种拆分，跟它一致便于后续 merge upstream）
- `ymq-module-activity/`
  - `pom.xml` 依赖：`yudao-module-infra` + `yudao-spring-boot-starter-security/mybatis/redis` + `spring-boot-starter-validation` + test
  - 包结构：`cn.iocoder.yudao.module.activity.{controller/admin,controller/app,service,dal/dataobject,dal/mysql,enums}`
  - 一个 Hello Controller：`AppActivityController#hello` → 路径 `/app-api/activity/hello`，`@PermitAll` 免登录
- `ymq-module-poster/`：先占位，`pom.xml` + 空包目录，等 Sprint 1 海报开始做
- root `pom.xml` 增加 `<module>ymq-module-activity</module>` 和 `<module>ymq-module-poster</module>`
- `yudao-server/pom.xml` 增加两个新模块的 `<dependency>`
- 重新编译用 `mvn install -pl ymq-module-activity,ymq-module-poster,yudao-server -am`，5 秒搞定（依赖已缓存）
- 启动验证：`curl -H "tenant-id: 1" /app-api/activity/hello` → 200，返回 `hello from ymq-module-activity`

### D6 第一阶段（t_user + Service 数据层）✅
- 建 `sql/migration/V001__init_t_user.sql`（来自 docs/5-数据库设计.md §2.1），灌进 ymq_dev 库
- **关键偏离 Sprint0 清单**：清单写"放 ymq-module-activity-biz 建 UserDO"，但项目记忆明确 t_user 是全局基础设施（信用/举报/实名要被 activity/match/rating/training/content 共同复用），所以单独建 `ymq-module-social` 模块放
- 包结构：`cn.iocoder.yudao.module.social.{dal/dataobject/user, dal/mysql/user, service/user, controller/app/user}`
- 文件：`UserDO`、`AppUserMapper`（带 App 前缀避免 bean 名冲突，见踩坑）、`UserService`/`UserServiceImpl`、`AppUserController`（探针端点 `/app-api/social/user/get-or-create`）
- DO 不继承 `BaseDO`/`TenantBaseDO`：YMQ 是 C 端单租户，不需要 tenantId 也不需要 creator/updater；created_at/updated_at 走 MySQL 默认值
- 端到端验证：openid 幂等 + 默认值 (creditScore=100/status=1) + MyBatis-Plus 驼峰自动映射全部 OK
- **D6 剩余**（SecurityConfig 改造 + token 隔离验证）合并到 D5 做，因为没有真实 JWT 没法验证

### 学到的关键事（D4 期间踩坑）
- **芋道默认开启多租户**：所有请求需带 `tenant-id` 头，否则返回 `400 请求的租户标识未传递`。免租户的端点要加 `@TenantIgnore`；YMQ 是 C 端应用不分租户，Sprint 0 末/Sprint 1 初要把多租户整体禁掉（改 `application.yaml` 的 `yudao.tenant.enable=false`，或全局加 `@TenantIgnore`，或保留但默认 tenant=1）
- **`@RequestMapping` 路径不要带 `/app-api`**：前缀由全局路由配置（`WebProperties.appApi = new Api("/app-api", "**.controller.app.**")`）自动加，Controller 内只写业务路径
- **包名包含 `controller.app` 决定走 app-api 前缀**；包名包含 `controller.admin` 决定走 admin-api 前缀；不在这两个之内的 Controller 不走任何前缀（会按原 `@RequestMapping` 路径暴露，但被 Security 默认拦截）

### 学到的关键事（D6 期间踩坑）
- **MyBatis Mapper bean 名冲突**：芋道 `AdminUserServiceImpl` 用 `@Resource private AdminUserMapper userMapper`，按变量名 `userMapper` 注入。如果我们也叫 `UserMapper`，MyBatis-Plus 生成的 bean name 也是 `userMapper`，Spring 注入歧义就报错（`The bean 'userMapper' could not be injected because it is a JDK dynamic proxy`）。**解法**：业务模块的 Mapper 类名加前缀（如 `AppUserMapper`），避开芋道按字段名注入的坑
- **多租户全局拦截器**：芋道 `yudao.tenant.enable=true` 时所有 SQL 自动加 `WHERE tenant_id = ?`，业务表没有 tenant_id 字段会报 `Unknown column 'tenant_id' in 'where clause'`。**解法**：把表名加到 `yudao.tenant.ignore-tables`（已加 t_user，未来 t_activity/t_club/t_match/... 都要加；Sprint 1 起考虑直接整体禁多租户）
- **CLI 客户端字符集**：mysql 命令行查询出来中文显示 `????`，但 API 返回的 JSON 是对的。底层数据没问题，只是 `mysql -u root` 默认非 utf8mb4，可以加 `--default-character-set=utf8mb4` 或者忽略

### 顺手做的
- 改 `docs/Sprint0实施清单.md` 第 28 行：`v2.4.0` → `v2026.04(jdk17/21)`
- 装 `pinentry-mac` 解决 GPG 签名 ioctl 问题，下次 commit 自动走 macOS Keychain 缓存

---

## 偏离 Sprint0 清单的决策（已经定的）

| 项 | 原清单 | 实际 | 原因 |
|---|---|---|---|
| 仓库布局 | 双仓库（docs 在 ymq/，后端 clone 到 ymq-backend） | 单仓库（fork 进 ymq/ 本体） | 独立开发者，CI/部署/演进一套就够 |
| 芋道 tag | `v2.4.0` | `v2026.04(jdk17/21)` | 芋道命名规则改了，已不存在 v2.4.0；最新稳定按月发版 |
| 砍模块清单 | mall/erp/crm/bpm/ai（清单 D3） | 上面 5 个 + iot/report/mes/pay/mp | 芋道默认 pom 已注释掉这些，物理删只是清理目录 |
| Java JDK | Sprint0 没指定具体发行版（"JDK 17 推荐 SDKMAN temurin"） | brew `openjdk@17`（Homebrew OpenJDK） | brew cask temurin 要 sudo 密码非交互模式拿不到，formula 版无 sudo |
| MySQL | 写"MySQL 8.0" | brew `mysql@8.0` | 已知 deprecated 警告（2027-04-30 才禁用），保留 8.0 跟项目记忆一致 |
| .gitignore | 清单写"加 application-local.yaml" | 已加，**且**当前文件改动暂未 commit | 改动里没有 wxa 真 secret（D5 才换），但仍按 ignore 处理避免误传 |

---

## 待决定（已挖坑、未填）

1. `yudao-module-member` 到底保留还是替换：
   - Sprint0 D5 要用 `/app-api/member/auth/weixin-mini-app-login` 走芋道现成的微信登录
   - 但项目记忆要"sys_user vs t_user 隔离"——意思是 C 端用户不走芋道 member 表
   - **方案 A**：member 模块改为读 t_user（重写 MemberUserService）
   - **方案 B**：保留 member 独立，**只用**它的 auth 接口，建独立的 t_user
   - **方案 C**：完全砍掉 member，从头写微信登录链路
   - D5/D6 时再拍板

2. `ymq-module-*` 包名是否带 ymq 一层：
   - Sprint0 清单写 `cn.iocoder.yudao.module.ymq.activity`
   - 芋道原生模块是 `cn.iocoder.yudao.module.{moduleName}`
   - 带 ymq 隔离 vs 不带 ymq 跟芋道命名一致——D4 建骨架时再确定

3. 微信小程序 appId/secret：
   - 当前 `application-local.yaml` 里还是芋道测试号（Kongdy 提供的）
   - D5 必须替换为我们自己的（用户没注册微信小程序之前进行不下去）

---

## 下一步（按优先级）

1. **等 mvn install 跑完**（监控关键事件中：BUILD SUCCESS/FAILURE/ERROR）
2. 启动 `YudaoServerApplication`：
   ```bash
   cd /Users/guchuan/IdeaProjects/ymq/yudao-server
   mvn spring-boot:run
   # 或者直接 java -jar target/yudao-server-*.jar
   ```
   验证：`curl http://localhost:48080/admin-api/system/captcha/check` 返回 JSON
3. 用户登录运营后台 `http://localhost:48080`（用户名 admin，密码 admin123，D9 步骤再改）
4. **D4**：建 `ymq-module-activity` 和 `ymq-module-poster` 骨架
5. **D5**：注册微信小程序 → 拿 appId/secret → 替换 `application-local.yaml` 里的占位 → 联调登录链路

---

## 下个 session 接力清单（换账号必读）

如果你（另一个 Claude session）刚接手这个项目：

1. 先读这个文件（Sprint0进度.md）了解当前状态
2. 读 [docs/README.md](README.md)、[1-产品愿景.md](1-产品愿景.md)、[Sprint0实施清单.md](Sprint0实施清单.md) 了解全局
3. 检查环境（应该都就绪）：
   ```bash
   java -version              # 17.0.19
   mvn -v                     # 3.9.12 + Java 17
   mysql --version            # 8.0.46
   redis-cli ping             # PONG
   brew services list         # mysql@8.0 + redis 都 started
   gh auth status             # carljings 已登录
   git -C /Users/guchuan/IdeaProjects/ymq log --oneline -3  # 看 baseline commit 是否已落
   ```
4. 如果 `java -version` 显示损坏的 Zulu 8："非交互式 shell 没 source .zshrc"。**每个 Bash 命令前显式 export**：
   ```bash
   export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
   export PATH="$JAVA_HOME/bin:/opt/homebrew/Cellar/maven/3.9.12/libexec/bin:/opt/homebrew/opt/mysql@8.0/bin:$PATH"
   ```
   或者：让用户重启 Claude（让 shell snapshot 重新生成，自动读 .zshenv）
5. 当前进度卡在哪→看本文件「当前状态」节

---

## 关键文件 / 路径速查

- 项目根：`/Users/guchuan/IdeaProjects/ymq/`
- 芋道启动类：`yudao-server/src/main/java/cn/iocoder/yudao/server/YudaoServerApplication.java`（启动端口 48080）
- 本地配置：`yudao-server/src/main/resources/application-local.yaml`（**被 .gitignore，但目前未删 cached**，所以芋道历史里有，我们的改动会进 working dir）
- 数据库脚本目录：`sql/mysql/`（芋道初始化用 ruoyi-vue-pro.sql + quartz.sql）
- 待建模块：`ymq-module-activity/`、`ymq-module-poster/`（D4）
- 备份：`/tmp/ymq-prep-backup/`（含 fork 前的 docs/README/.claude/.omx）
- Maven 镜像：`~/.m2/settings.xml` 已配阿里云 + spring + spring-plugin
- GPG：`~/.gnupg/gpg-agent.conf` 已配 pinentry-mac
