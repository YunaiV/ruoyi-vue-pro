# 小程序 RTMP ↔ LiveKit 网关（试验）

> **试验功能，默认关闭，勿用于生产。** 小程序音视频经 RTMP 转码中转，端到端延迟预计为**秒级**（通常 1~3 秒，弱网更高），明显差于 PC / H5 / App 的 WebRTC 直连；回声消除、资源成本、真机兼容性均未经验收。

## 1. 为什么需要网关

微信小程序不能运行 `livekit-client`（无 WebRTC），原生 `live-pusher` 只能推 RTMP，`live-player` 只能拉 RTMP / FLV。网关在不更换 LiveKit 的前提下把小程序接入同一个房间：

```
小程序 live-pusher ──RTMP──▶ LiveKit Ingress ──WebRTC──▶ LiveKit 房间 ◀──WebRTC── PC / H5 / App
                                                              │
                         每个远端参与者 1 路 Track Composite Egress
                                                              ▼
小程序 live-player ◀──RTMP── SRS（推流、播放均回调网关鉴权）◀──RTMP──┘
```

- 小程序自己的 Ingress 参与者 identity 即后端 Token 内的 userId（不加后缀），与 PC / App 同账号进房时沿用 LiveKit 同 identity 互踢；后端 webhook 的 joined / left 照常生效，**无需修改 Java 代码**。
- 每个远端单独一路流，**从不混入小程序自己的声音**；视频屏幕共享优先、摄像头回退；Egress、Agent、SIP 等非业务参与者不转出。
- 群通话一个远端一路 live-player，单会话默认最多 4 路，超出部分如实返回 `overflow`。

## 2. 目录

| 文件 | 说明 |
| --- | --- |
| `src/server.mjs` | 入口：小程序会话 API（8090）+ SRS 回调（8091，仅回环） |
| `src/session-manager.mjs` | 会话、租约、Ingress / Egress 生命周期、SRS 鉴权、残留回收 |
| `src/jwt.mjs` | 校验后端签发的房间 Token（HS256）、签发管理 Token |
| `src/livekit-api.mjs` | LiveKit Twirp API 最小客户端（无第三方依赖） |
| `src/config.mjs` | 环境变量配置，`MP_GATEWAY_ENABLED=true` 才允许启动 |
| `docker-compose.yml` | 独立试验环境：Redis + LiveKit + Ingress + Egress + SRS + 网关 |
| `srs.conf` | SRS 配置，HTTP 回调鉴权 |
| `.env.example` | 凭据与地址占位，复制为 `.env` 使用 |

网关进程只使用 Node.js 22 内置模块（`http`、`crypto`、`fetch`），无需安装 npm 依赖；运行时依赖 LiveKit Server、Ingress、Egress 与 SRS，由同目录 `docker-compose.yml` 一并启动。

## 3. 本机回环联调

与上级目录 `../docker-compose.yml`（LiveKit-only PoC）**二选一**，两者都占用 7880 等端口。

```bash
cd script/livekit-poc/mp-gateway
docker compose -f ../docker-compose.yml down   # 停掉 LiveKit-only PoC
cp .env.example .env                           # 填写与后端 yudao.im.rtc.api-key / api-secret 一致的凭据，MP_GATEWAY_ENABLED=true
docker compose up -d
docker compose logs -f mp-gateway              # 看到「小程序网关已启动（试验功能）」
curl -s http://127.0.0.1:8090/healthz          # {"status":"ok"}
```

- 全部服务使用 `network_mode: host`。macOS 需 Docker Desktop 4.34+，并在 Settings → Resources → Network 勾选「Enable host networking」，否则容器能启动但端口不通。
- 后端 `yudao.im.rtc.livekit-url` 仍为 `ws://127.0.0.1:7880`；webhook 默认回调 `http://127.0.0.1:48080/admin-api/im/livekit/webhook`。
- 本机端口：LiveKit 7880 / 7881 / 7882udp，Ingress RTMP 1935、WHIP 18080、relay 19090、UDP 7885，Egress 健康检查 18082，SRS 1936，Redis 16379，网关 8090 / 8091。LiveKit HTTP、SRS、Redis、网关默认只监听回环；**Ingress RTMP 与 LiveKit 媒体端口会监听所有网卡**（推流靠一次性 stream key 鉴权），在不可信局域网中请配合防火墙。

### 3.1 用 FFmpeg 模拟小程序

1. 用 H5 或 PC 账号 A 发起通话，账号 B（模拟小程序端）取得同一房间的 Token（例如在 H5 发起 / 接听接口响应中取得 `token`，**只在本机终端使用，勿写日志或提交**）。
2. 创建会话：

   ```bash
   curl -s -X POST -H "Authorization: Bearer $ROOM_TOKEN" http://127.0.0.1:8090/mp-rtc/sessions
   ```

3. 用返回的 `pushUrl` 推测试流（模拟 live-pusher）：

   ```bash
   ffmpeg -re -f lavfi -i testsrc=size=640x360:rate=15 -f lavfi -i sine=frequency=440 \
     -c:v libx264 -preset veryfast -tune zerolatency -g 15 -c:a aac -ar 48000 -f flv "$PUSH_URL"
   ```

   账号 A 的页面应出现 B 的测试画面与提示音；会话查询 `publishing` 变为 `true`。
4. 每 2 秒查询会话（同时续租），待远端 A 的 `state` 为 `ready` 后，用 `playUrl` 拉流（模拟 live-player）：

   ```bash
   curl -s -H "Authorization: Bearer $SESSION_TOKEN" http://127.0.0.1:8090/mp-rtc/sessions/$SESSION_ID
   ffplay -fflags nobuffer -flags low_delay "$PLAY_URL"
   ```

5. 反向校验：去掉 `playUrl` 的 `token` 参数或改错一位，SRS 应拒绝播放；停止查询超过 `leaseTtlMs`，或 `DELETE` 会话后，Ingress、Egress 应被释放（`docker compose logs mp-gateway` 有「会话已关闭」）。

## 4. 配置

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MP_GATEWAY_ENABLED` | 无 | 必须为 `true` 才启动 |
| `MP_GATEWAY_API_HOST` / `_PORT` | `127.0.0.1` / `8090` | 小程序 API；公网部署放在 HTTPS 反向代理之后 |
| `MP_GATEWAY_HOOK_HOST` / `_PORT` | `127.0.0.1` / `8091` | SRS 回调，只允许 SRS 访问 |
| `MP_GATEWAY_LIVEKIT_HTTP_URL` | `http://127.0.0.1:7880` | LiveKit API 地址 |
| `MP_GATEWAY_LIVEKIT_API_KEY` / `_SECRET` | 必填 | 与后端一致；secret 至少 32 位 |
| `MP_GATEWAY_SRS_PUBLISH_BASE_URL` | `rtmp://127.0.0.1:1936/yudao-mp-gw` | Egress 推到 SRS 的内部地址；app 必须为网关专用的 `yudao-mp-gw` 或 `yudao-mp-gw-<后缀>`，否则拒绝启动 |
| `MP_GATEWAY_SRS_PLAY_BASE_URL` | 同上 | 小程序拉流的外部地址，app 需与推流一致 |
| `MP_GATEWAY_POLL_INTERVAL_MS` | `2000` | 建议轮询间隔，也是网关同步房间的间隔 |
| `MP_GATEWAY_LEASE_TTL_MS` | `20000` | 租约，至少 3 倍轮询间隔；超时未查询即释放 |
| `MP_GATEWAY_MAX_SESSION_MS` | 2 小时 | 单会话最长存活，另受房间 Token `exp` 限制 |
| `MP_GATEWAY_MAX_SESSIONS` | `4` | 全局会话数（每个 1 路 Ingress 转码） |
| `MP_GATEWAY_MAX_REMOTES_PER_SESSION` | `4` | 单会话最多拉取的远端 |
| `MP_GATEWAY_MAX_EGRESS` | `8` | 全局 Egress 路数 |
| `MP_GATEWAY_EGRESS_RETRY_MS` | `10000` | 单路 Egress 失败后的冷却 |
| `MP_GATEWAY_EGRESS_PUBLISH_TIMEOUT_MS` | `20000` | Egress 启动后等待 SRS `on_publish` 的最长时间，超时停止并按 `failed` 冷却重试 |
| `MP_GATEWAY_CLEANUP_RETRY_MS` | `5000` | 启动回收失败后的重试间隔 |
| `MP_GATEWAY_RELEASE_RETRY_MS` | `5000` | Ingress / Egress 释放失败后的重试间隔；确认释放前继续占用配额 |
| `MP_GATEWAY_REQUEST_TIMEOUT_MS` | `10000` | 调用 LiveKit API 的超时 |
| `MP_GATEWAY_CLOCK_SKEW_SEC` | `30` | 校验房间 Token `nbf` / `exp` 允许的时钟偏差 |
| `MP_GATEWAY_EGRESS_WIDTH` / `HEIGHT` / `FRAMERATE` / `VIDEO_KBPS` / `AUDIO_KBPS` / `KEYFRAME_SEC` | 640 / 360 / 15 / 600 / 64 / 1 | Egress 编码（H264 Baseline + AAC） |

上表是网关进程直接读取的环境变量，自行运行 `node src/server.mjs` 时全部按此配置。使用本目录 `docker-compose.yml` 时，网关容器的环境由 compose 组装，`.env` 中的变量并非全部原样转发：

- **compose 固定、`.env` 不生效**：`MP_GATEWAY_API_PORT=8090`、`MP_GATEWAY_HOOK_HOST=127.0.0.1`、`MP_GATEWAY_HOOK_PORT=8091`、`MP_GATEWAY_LIVEKIT_HTTP_URL=http://127.0.0.1:7880`，与 `srs.conf` 及 compose 内各服务端口一一对应。
- **改名转发**：LiveKit 凭据读取 `.env` 的 `LIVEKIT_API_KEY` / `LIVEKIT_API_SECRET`（同时供 LiveKit、Ingress、Egress 使用），写 `MP_GATEWAY_LIVEKIT_API_KEY` / `_SECRET` 不生效。
- **由 compose 变量拼出**：`MP_GATEWAY_SRS_PUBLISH_BASE_URL` 固定为 `rtmp://127.0.0.1:1936/<app>`，`MP_GATEWAY_SRS_PLAY_BASE_URL` 为 `rtmp://<MP_SRS_PLAY_PUBLIC_HOST>:1936/<app>`；`<app>` 取 `MP_GATEWAY_SRS_APP`（默认 `yudao-mp-gw`），`MP_SRS_PLAY_PUBLIC_HOST` 默认 `127.0.0.1`。
- **原样转发**：`MP_GATEWAY_ENABLED`（默认 `false`）、`MP_GATEWAY_API_HOST`（默认 `127.0.0.1`），以及从 `MP_GATEWAY_POLL_INTERVAL_MS` 到 Egress 编码的其余可调参数，未配置即用上表默认值（见 `.env.example`）。

## 5. 会话 API

所有响应 `Cache-Control: no-store`，错误体为 `{ "code": "...", "message": "..." }`。不支持 CORS（小程序 `uni.request` 不受 CORS 限制）。

### 5.1 创建会话 `POST /mp-rtc/sessions`

- 请求头 `Authorization: Bearer <后端签发的房间 Token>`；请求体可为空（上限 4KB）。**不得**携带后端登录 accessToken。
- 网关严格校验：HS256 签名、`iss` = apiKey、`exp` / `nbf`（允许 30 秒时钟偏差）、`sub` 为数字 userId、`video.roomJoin` / `canPublish` / `canSubscribe` 为 `true`、非 hidden / recorder。房间与身份**只取自 Token**。
- 同一用户在同一房间已有未释放的会话或推流资源（含释放中、创建中）时返回 `409 session_busy`，不会替换正在推流的会话：替换已进房的 Ingress 会触发 `participant_left`，后端会据此结束私聊通话。原会话 `DELETE` 或租约到期、资源确认释放后才能再次创建；若该房间 + 用户存在创建结果未知的占位，则需重启网关才能再次创建（见第 7 节）。网关不会返回已有会话的凭证。

`201` 响应：

```json
{
  "sessionId": "32 位十六进制",
  "sessionToken": "会话凭证，仅本会话后续请求使用",
  "userId": 1,
  "pushUrl": "rtmp://host:1935/x/<stream key>",
  "pollIntervalMs": 2000,
  "leaseTtlMs": 20000,
  "expiresAt": 1760000000000
}
```

错误：`401` Token 无效 / 过期，`403` 授权不足，`429` 会话数已满，`502` LiveKit / Ingress 不可用或未配置 `ingress.rtmp_base_url`，`409` 同一用户已有会话（`session_busy`）或会话在创建过程中被关闭（`session_closed`），`503` 网关正在退出（`shutting_down`）或启动回收尚未完成（`cleanup_pending`）。创建过程中客户端断开，网关会立即释放刚创建的资源。

### 5.2 查询并续租 `GET /mp-rtc/sessions/{sessionId}`

请求头 `Authorization: Bearer <sessionToken>`；会话不存在、已结束或凭证错误统一返回 `404`。

```json
{
  "sessionId": "...",
  "status": "active",
  "publishing": true,
  "remotes": [
    { "userId": 2, "state": "ready", "ready": true, "playUrl": "rtmp://host:1936/yudao-mp-gw/<stream>?token=<拉流凭证>", "hasAudio": true, "hasVideo": true, "videoVisible": true }
  ],
  "error": null,
  "leaseTtlMs": 20000,
  "pollIntervalMs": 2000,
  "expiresAt": 1760000000000
}
```

- `publishing`：小程序推流已作为 LiveKit 参与者进房（Ingress 参与者已有轨道）。
- `remotes[].state`：
  - `ready`：SRS 已收到该远端的流，`playUrl` 可播放；只有此状态下 `playUrl` 非空。
  - `starting`：Egress 启动中。
  - `waiting`：远端已进房，暂无可转出的音视频轨道。
  - `overflow`：超出单会话路数上限，不会转出。
  - `capacity`：全局 Egress 已满，空出后自动重试。
  - `failed`：Egress 启动失败、超过 `MP_GATEWAY_EGRESS_PUBLISH_TIMEOUT_MS` 仍未推到 SRS，或中途结束时冷却后自动重试；创建结果未知（见「7. 生命周期与资源」）时不再为该远端新建转流，直到网关重启。具体原因（`egress_failed` / `egress_no_media` / `egress_ended` / 创建结果未知）只记录在网关日志。
- 远端媒体组合变化（如开始 / 结束屏幕共享）时会换新的流名与 `playUrl`，客户端需按最新值重新播放。
- `error`：最近一次同步房间失败为 `livekit_unavailable`，否则为 `null`。

### 5.3 结束会话 `DELETE /mp-rtc/sessions/{sessionId}`

请求头同上，成功 `204`，释放 Ingress 与全部 Egress。

### 5.4 健康检查 `GET /healthz`

启动回收完成后 `200 {"status":"ok"}`；完成前 `503 {"status":"cleanup_pending"}`。

## 6. SRS 鉴权与凭证

- 每路 Egress 使用新的随机流名、推流密钥（`secret`）、拉流凭证（`token`）。推流密钥与会话凭证只保存摘要；拉流凭证需要在会话查询中重新下发 `playUrl`，因此明文保存在网关进程内存中，会话关闭或远端流移除时随之删除。所有凭证都不落盘、不写日志。
- `on_publish`：只允许当前存活会话中对应流名 + 正确 `secret` 推流；`on_play`：只允许正确 `token` 播放；网关不可用时 SRS 拒绝。
- 日志只记录会话编号、房间、userId、资源编号，**不记录**房间 Token、会话凭证、stream key、推拉流 URL；LiveKit API 出错时只记录方法、HTTP 状态与 Twirp 错误码，不记录上游返回的错误原文。

## 7. 生命周期与资源

- 会话超过 `leaseTtlMs` 未查询、到达 Token `exp` 或 `MP_GATEWAY_MAX_SESSION_MS`、房间被销毁（曾进房之后）时自动释放。
- 会话关闭或远端流移除时立即撤销推拉流授权与会话查询；对应的 Ingress / Egress 删除失败时保留在内存待释放列表中，按 `MP_GATEWAY_RELEASE_RETRY_MS` 重试（同时最多 4 个重试请求），**确认释放前继续计入会话数与 Egress 路数上限**，同一用户也不能新建会话。
- 异步创建 Ingress / Egress 期间会话被关闭或远端已变化时，结果返回后立即转入待释放；创建中的资源同样计入上限。
- `SIGINT` / `SIGTERM` 时拒绝新会话，关闭全部会话，等待进行中的创建返回并确认全部资源释放后以 0 退出；15 秒内未能全部释放则以非零状态退出，剩余资源由下次启动回收或按 7.2 手动处理。
- **创建结果未知**：创建 Ingress / Egress 时超时、网络错误、HTTP 5xx（含反向代理 502 / 504），或成功响应缺少资源编号，LiveKit 侧都可能已建好资源而网关拿不到编号。网关按 fail-closed 处理：该次创建作为占位**永久计入**会话数 / Egress 路数上限，同一房间 + 用户不能再新建会话（`409 session_busy`），发现结果未知后不再为对应远端新建转流，避免不断叠加未知资源：若该远端换轨后的新转流在旧请求结果未知前已经启动，则继续按其真实状态显示和运行，否则显示 `failed`；旧的未知占位仍计入配额直到重启。占位只能**重启网关**后由启动回收（Ingress 按名称前缀、Egress 按专用 app）清除；存在占位时网关退出以非零状态结束。出现日志「创建结果未知」后请择机重启网关。注意 LiveKit 在 Egress 容量不足时也可能返回 5xx，同样会被保守占位、需要重启恢复，这是试验阶段的取舍；4xx 等明确拒绝的失败按未创建处理，沿用冷却重试。
- **单实例限制**：会话保存在内存，多实例共用同一 LiveKit 会互相清理，暂不支持。

### 7.1 启动回收与专用 SRS app

- LiveKit 返回的 EgressInfo 会把 RTMP 地址最后一段（流名 + 查询参数）脱敏为 `{首 3 位...末 3 位}`，例如 `rtmp://127.0.0.1:1936/yudao-mp-gw/{yud...xyz}`，只有 scheme / host / port / app 可信。因此**网关用专用 app 标识归属**：`yudao-mp-gw`（或 `yudao-mp-gw-<后缀>`）只能给网关使用。
- 网关启动后先回收残留：删除名称前缀为 `yudao-mp-gw-` 的 Ingress；停止输出地址为「`MP_GATEWAY_SRS_PUBLISH_BASE_URL`/单段流名」且流名为完整网关流名或 `{yud...xxx}` 脱敏形式的活跃 Egress。其它 app（如 `live`）下的 Egress 不受影响。
- 回收任一步失败都会按 `MP_GATEWAY_CLEANUP_RETRY_MS` 后台重试；完成前 `/healthz` 与创建会话返回 `503 cleanup_pending`，避免旧残留与新会话并存。
- `srs.conf` 的 HTTP 回调对整个 vhost 生效，网关会拒绝非专用 app 与未知流名的推流 / 播放，所以 SRS 需**独占一个实例或 vhost**，不要与其它直播业务共用。
- 已经建立的 RTMP 连接不会因网关重启而重新鉴权；残留 Egress 停止后其推流断开，播放端随之断流。

### 7.2 无法自动回收时的手动恢复

LiveKit 长时间不可用、回收一直失败时，网关会持续返回 `cleanup_pending`，不会自行放行。恢复步骤：

1. `docker compose stop mp-gateway`；
2. 通过 LiveKit API（`ListIngress`）删除名称前缀为 `yudao-mp-gw-` 的 Ingress；
3. 通过 `ListEgress`（active）停止输出地址以 `MP_GATEWAY_SRS_PUBLISH_BASE_URL/` 开头的 Egress；
4. `docker compose restart srs`，断开 SRS 上残留的推流与播放连接；
5. `docker compose start mp-gateway`，确认 `/healthz` 返回 `ok`。
- 成本估算（LiveKit 官方 CPU 估值）：每个小程序会话 1 路 RTMP Ingress 转码约 2 核，每个远端 1 路 Track Composite Egress 约 2 核。1 对 1 小程序通话约 4 核，Egress 超出 CPU 余量会被 LiveKit 拒绝并如实返回 `failed`。

## 8. 已知限制与待验证点

- **延迟**：RTMP 推流 + Ingress 转码 + Egress 转码 + RTMP 拉流，秒级延迟，不适合对实时性要求高的场景。
- **回声**：小程序 live-pusher 与 live-player 是两个独立组件，系统回声消除效果未验证，试验阶段建议戴耳机。
- **静音状态**：小程序关闭摄像头或麦克风时，Ingress 推流仍在，PC / App 看到的是黑屏或静音画面，而不是 LiveKit 的 muted 状态。
- **后台 / 断网**：小程序进入后台或 RTMP 网络断开（live-pusher 内部重连耗尽，状态码 -1307）时推流中断，Ingress 参与者离房，1 对 1 通话会由后端 webhook 结束；小程序端也会在推流丢失超过时限后结束本次通话。不支持无缝恢复，需要重新发起。
- **屏幕共享转出**：本地真实媒体验证中出现过屏幕共享的 Track Composite Egress 处于 `EGRESS_ACTIVE` 但始终未推到 SRS 的情况（摄像头转出正常），根因待排查；网关只做启动超时与 `failed` 如实返回，不静默回退摄像头。
- 本地真实媒体验证（FFmpeg 模拟推流 + H5，非微信原生组件）已观察到 Egress 地址中的 `?secret=` 原样传给 SRS 并通过回调校验。
- **待真实运行验证**：纯音频 Track Composite 推 RTMP；live-player 拉带 `?token=` 的 RTMP 地址；Ingress 对 live-pusher RTC 模式码流的兼容性。

## 9. 真机要求

- 小程序主体为企业，类目在 live-pusher / live-player 支持范围内，并在小程序后台「开发 → 开发管理 → 接口设置」开通「实时播放音视频流」「实时录制音视频流」。
- 网关 API 部署在 HTTPS 域名后，并加入小程序「request 合法域名」。
- 手机可达的地址：Ingress RTMP（`MP_INGRESS_RTMP_PUBLIC_HOST:1935`）、SRS（`MP_SRS_PLAY_PUBLIC_HOST:1936`，需把 `srs.conf` 的 `listen` 改为对外地址）；PC / H5 / App 照常需要访问 LiveKit 信令与媒体端口（`MP_LIVEKIT_BIND_ADDRESS`、`MP_LIVEKIT_NODE_IP`）。
- 开发者工具不支持 live-pusher 推流，必须真机调试。
