# LiveKit Server PoC

最小可用的 LiveKit Server 自部署验证环境，用于零期 PoC。

## 启动

```bash
cd tools/livekit-poc
docker compose up -d
bash verify.sh
```

## 端口

- 7880：HTTP / WebSocket 信令；
- 7881：WebRTC TCP fallback；
- 7882/UDP：WebRTC 媒体；
- macOS / Windows：当前 `docker-compose.yml` 走端口映射模式，webhook URL 用 `host.docker.internal:48080` 让容器访问到宿主机 yudao 后端；
- macOS 上 host network（`network_mode: host`）需要 Docker Desktop 4.34+ 并在 Settings → Resources → Network 勾选「Enable host networking」，老版本静默失败（容器跑得起来但端口完全不通）；
- Linux：可以把 `docker-compose.yml` 改成 `network_mode: host` + 删 `ports:` 段，并把 `livekit.yaml` 的 webhook URL 改为 `http://127.0.0.1:48080/admin-api/im/livekit/webhook`。

## 凭据 (仅 PoC，勿用于生产)

- `LIVEKIT_KEYS=devkey: secret-poc-key-min-32-chars-required-here`
- API Key：`devkey`
- API Secret：`secret-poc-key-min-32-chars-required-here`

生产环境必须改用强随机 secret，并通过 `--config /etc/livekit.yaml` 加载。

## 浏览器联调

`verify.sh` 跑完会输出一个 `meet.livekit.io` 链接，用两个浏览器（或两台机器）打开同一链接即可看到对方画面。
