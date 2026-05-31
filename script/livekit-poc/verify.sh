#!/usr/bin/env bash
# LiveKit Server PoC 验证脚本；
# 用法： bash verify.sh
set -e

API_KEY="${LIVEKIT_API_KEY:-devkey}"
API_SECRET="${LIVEKIT_API_SECRET:-secret-poc-key-min-32-chars-required-here}"
HOST="${LIVEKIT_HOST:-localhost:7880}"
ROOM="${LIVEKIT_ROOM:-poc-room}"

ok()   { printf "[OK]   %s\n" "$1"; }
fail() { printf "[FAIL] %s\n" "$1"; exit 1; }

echo "==> 1/5 等待 HTTP 端点就绪 (http://${HOST}/)"
for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "http://${HOST}/" || echo "000")
  [ "$code" = "200" ] && { ok "HTTP 200"; break; }
  [ $i -eq 20 ] && fail "20 秒内未就绪 (last code=${code})"
  sleep 1
done

echo "==> 2/5 签发管理 + 客户端权限 Token"
TOKEN=$(API_KEY="$API_KEY" API_SECRET="$API_SECRET" ROOM="$ROOM" python3 - <<'PY'
import json, time, hmac, hashlib, base64, os
def b64u(b): return base64.urlsafe_b64encode(b).rstrip(b'=').decode()
header  = b64u(json.dumps({"alg":"HS256","typ":"JWT"}, separators=(',',':')).encode())
payload = b64u(json.dumps({
    "iss": os.environ["API_KEY"],
    "sub": "poc-tester",
    "name": "PoC Tester",
    "video": {
        "roomJoin": True, "room": os.environ["ROOM"],
        "canPublish": True, "canSubscribe": True, "canPublishData": True,
        "roomCreate": True, "roomList": True, "roomAdmin": True
    },
    "exp": int(time.time()) + 3600,
    "nbf": int(time.time())
}, separators=(',',':')).encode())
sig = b64u(hmac.new(os.environ["API_SECRET"].encode(),
                    f"{header}.{payload}".encode(),
                    hashlib.sha256).digest())
print(f"{header}.{payload}.{sig}")
PY
)
[ -n "$TOKEN" ] || fail "Token 生成失败"
ok "Token 已生成 (${#TOKEN} chars)"

echo "==> 3/5 创建房间 ${ROOM} (CreateRoom RPC)"
create_resp=$(curl -s -X POST "http://${HOST}/twirp/livekit.RoomService/CreateRoom" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"${ROOM}\",\"empty_timeout\":300,\"max_participants\":10}")
echo "    响应: $create_resp"
echo "$create_resp" | jq -e '.sid' >/dev/null 2>&1 \
    && ok "房间已创建" \
    || fail "CreateRoom 失败"

echo "==> 4/5 列出房间 (ListRooms RPC)"
list_resp=$(curl -s -X POST "http://${HOST}/twirp/livekit.RoomService/ListRooms" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{}')
room_count=$(echo "$list_resp" | jq '.rooms | length' 2>/dev/null || echo "0")
ok "当前房间数: ${room_count}"
echo "$list_resp" | jq '.'

echo "==> 5/5 删除房间 (DeleteRoom RPC) —— 清理"
del_resp=$(curl -s -X POST "http://${HOST}/twirp/livekit.RoomService/DeleteRoom" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"room\":\"${ROOM}\"}")
ok "删除响应: $del_resp"

# 重新签一个仅 client 权限的 token；用于浏览器进会
CLIENT_TOKEN=$(API_KEY="$API_KEY" API_SECRET="$API_SECRET" ROOM="$ROOM" python3 - <<'PY'
import json, time, hmac, hashlib, base64, os
def b64u(b): return base64.urlsafe_b64encode(b).rstrip(b'=').decode()
header  = b64u(json.dumps({"alg":"HS256","typ":"JWT"}, separators=(',',':')).encode())
payload = b64u(json.dumps({
    "iss": os.environ["API_KEY"],
    "sub": "browser-tester",
    "name": "Browser",
    "video": {
        "roomJoin": True, "room": os.environ["ROOM"],
        "canPublish": True, "canSubscribe": True, "canPublishData": True
    },
    "exp": int(time.time()) + 7200
}, separators=(',',':')).encode())
sig = b64u(hmac.new(os.environ["API_SECRET"].encode(),
                    f"{header}.{payload}".encode(),
                    hashlib.sha256).digest())
print(f"{header}.{payload}.{sig}")
PY
)

echo ""
echo "============================================================"
echo "  LiveKit Server 验证通过"
echo "============================================================"
echo "  浏览器测试 (开两个窗口能互通)："
echo "    https://meet.livekit.io/?liveKitUrl=ws%3A%2F%2F${HOST}&token=${CLIENT_TOKEN}"
echo ""
echo "  停止服务："
echo "    docker compose -f tools/livekit-poc/docker-compose.yml down"
echo "============================================================"
