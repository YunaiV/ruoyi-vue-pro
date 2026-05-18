# Sprint 1 实际进度记录（活动接龙 + 对阵表）

> 配套：[Sprint0进度.md](Sprint0进度.md)（已收工 baseline）、[2-MVP与验收.md](2-MVP与验收.md)（第一版做什么）、[6-核心算法.md](6-核心算法.md)（对阵表算法）。
> Sprint 1 范围：第 3-6 周（4 周）→ 活动接龙基础链路（创建/接龙/取消/列表）+ 对阵表算法。

---

## 当前状态：2026-05-18 15:40 ｜ Sprint 1 第 1 周接龙业务全套跑通

刚从 Sprint 0 收尾切过来。当前能继续推进的是：
- 建活动相关数据表（t_activity / t_activity_member）
- 活动 CRUD API
- 接龙报名/取消报名
- 列表查询（我创建的活动、我参加的活动）

**用户上下文 mock 策略**：D5 登录链路未通之前，所有需要 `currentUserId` 的接口暂以 `?userId=1` query 参数注入。D5 接通后改成读 SecurityContext。本规则在每个新 Controller 顶部用注释提醒。

---

## 本 Sprint 计划（按 [2-MVP与验收.md] 推导）

### 第 3-4 周：活动接龙
- [ ] V002 建表：t_activity + t_activity_member
- [ ] ActivityDO/Mapper/Service/Controller
- [ ] POST /app-api/activity/create 创建活动
- [ ] GET /app-api/activity/{id} 活动详情
- [ ] POST /app-api/activity/{id}/signup 接龙报名
- [ ] DELETE /app-api/activity/{id}/signup 取消报名
- [ ] GET /app-api/activity/list-mine 我创建的活动
- [ ] GET /app-api/activity/list-joined 我参加的活动
- [ ] 活动短码（6 位）唯一生成
- [ ] 活动状态机：RECRUITING/MATCHED/PLAYING/FINISHED/CANCELLED

### 第 5-6 周：对阵表算法
- [ ] 按性别/水平/上场次数生成均衡对阵表（[6-核心算法.md] §1）
- [ ] t_match 表 + 对阵生成/编辑/重排接口
- [ ] 算法单测覆盖 5 个典型场景

---

## 已完成

### 第 1 周 / 活动接龙基础 ✅
- V002 建表：`t_activity`（30 字段全套）+ `t_activity_member`（10 字段）；空间索引推迟到第 4 周
- `application.yaml` 把 t_activity / t_activity_member 加入 `yudao.tenant.ignore-tables`
- ymq-module-activity 完整业务层：
  - `ActivityDO` / `ActivityMemberDO`
  - `ActivityMapper` / `ActivityMemberMapper`（用芋道 `BaseMapperX` + `LambdaQueryWrapperX`）
  - `ActivityStatusEnum`、`ActivityMemberStatusEnum`（String 常量类）
  - `ActivityService` / `ActivityServiceImpl`（含短码生成 + 状态机校验 + 召集人保护 + QUIT 复活）
  - `AppActivityCreateReqVO` / `AppActivityRespVO`
  - `AppActivityController`（6 个 endpoint：create / get / signup / cancel-signup / list-mine / list-joined）
- 端到端联调（mock userId via query string）：
  - 创建（短码 `PSZ9F3` 自动生成，召集人自动加入 members）
  - 报名 user 1/2 → current_count 2→3
  - 重复报名 → 400 已报名
  - 召集人取消 → 400 召集人不能取消
  - user 2 取消 → status=QUIT，current_count 3→2
  - 我创建的列表 / 我参与的列表 → 都返回 PSZ9F3 一条

### 第 1 周 / 待补
- [ ] 候补（人数满了自动 WAITING + 有人退时自动转 JOINED）
- [ ] 时间格式：考虑改全局 ISO 字符串（vs 当前 unix ms），让小程序前端写起来更顺手
- [ ] 短码生成的乐观锁保护（极端并发可能冲突）
- [ ] 单元测试覆盖几个关键 case

### 第 5-6 周 / 对阵表算法（未启动）
按 [6-核心算法.md] §1 实现：性别/水平/上场次数均衡，详见该文档。

---

## 偏离原计划的决策

- **时间字段用 unix timestamp 而不是 ISO 字符串**：芋道全局 Jackson 配 `write-dates-as-timestamps: true`，前端必须传毫秒数（`Date.now()`）。如果未来想换 ISO，要全局改 yaml，会影响芋道现有接口的时间序列化，未必划算
- **空间索引推迟**：MySQL 8 SPATIAL KEY 要 POINT/GEOMETRY 字段，跟我们 DECIMAL(10,6) 不兼容；Sprint 1 第 4 周做"附近发现"时再决定是改 POINT 还是用 `(lat - ?)² + (lng - ?)²` 简单距离

---

## 下个 session 接力清单

接力时第一件事：
1. 读这个文件 + [Sprint0进度.md](Sprint0进度.md)
2. 当前状态在最上面
3. mock user 注入规则：所有 `controller/app` 下需要 currentUserId 的接口先用 `@RequestParam Long userId`
