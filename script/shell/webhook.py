#!/usr/bin/env python3
"""
Deepay GitHub Webhook 自动部署服务器  v2.0

push 代码到 GitHub → 服务器自动拉代码 + 修依赖 + 全栈部署
不再需要每天手动开代理 pull 30 次！

用法:
  python3 webhook.py run          前台运行（systemd/supervisor 使用这个）
  python3 webhook.py start        后台守护进程启动
  python3 webhook.py stop         停止
  python3 webhook.py status       查看运行状态
  python3 webhook.py logs [n]     查看最近 n 行日志（默认 80）
  python3 webhook.py test         测试本机服务连通性

配置（环境变量，也可以直接改下方默认值）:
  WEBHOOK_SECRET    GitHub Webhook 密钥（必填！不设置任何人都能触发部署）
  WEBHOOK_PORT      监听端口，默认 9000
  WEBHOOK_BRANCH    触发部署的分支，默认 main
  WEBHOOK_MODE      传给部署脚本的模式，默认 all
  PROJECT_ROOT      项目根目录，默认 /www/wwwroot/deepay.srl
  WEBHOOK_DEPLOY    部署脚本路径（不填则自动根据 PROJECT_ROOT 推断）
  WEBHOOK_LOG_DIR   日志目录（不填则 PROJECT_ROOT/.build-logs）

GitHub Webhook 配置步骤:
  1. 启动此服务:
       export WEBHOOK_SECRET=你的密钥
       python3 webhook.py start

  2. 开放防火墙端口（宝塔面板 → 安全 → 放行 9000 端口）

  3. GitHub 仓库 → Settings → Webhooks → Add webhook:
       Payload URL   : http://你的服务器IP:9000/webhook
       Content type  : application/json
       Secret        : 与 WEBHOOK_SECRET 一致
       Events        : Just the push event
       Active        : ✔

  4. 保存后 GitHub 会发一个 ping，日志里出现
     "收到 GitHub ping！Webhook 配置成功" 即表示正常

  5. 之后每次 git push，服务器自动部署 ✔
"""

import os
import sys
import hmac
import hashlib
import json
import subprocess
import threading
import logging
import time
import signal
from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
from collections import deque

# ── 配置（优先读环境变量）────────────────────────────────────
PROJECT_ROOT  = os.getenv("PROJECT_ROOT",   "/www/wwwroot/deepay.srl")
SECRET        = os.getenv("WEBHOOK_SECRET", "").encode("utf-8")
PORT          = int(os.getenv("WEBHOOK_PORT",   "9000"))
BRANCH        = os.getenv("WEBHOOK_BRANCH",  "main")
DEPLOY_MODE   = os.getenv("WEBHOOK_MODE",    "all")
DEPLOY_SCRIPT = os.getenv(
    "WEBHOOK_DEPLOY",
    os.path.join(PROJECT_ROOT, "script/shell/deepay-deploy.sh"),
)
LOG_DIR = os.getenv(
    "WEBHOOK_LOG_DIR",
    os.path.join(PROJECT_ROOT, ".build-logs"),
)
PID_FILE = "/tmp/deepay-webhook.pid"

os.makedirs(LOG_DIR, exist_ok=True)

# ── 日志 ─────────────────────────────────────────────────────
_fmt = "%(asctime)s [%(levelname)s] %(message)s"
logging.basicConfig(
    level=logging.INFO,
    format=_fmt,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler(
            os.path.join(LOG_DIR, "webhook.log"), encoding="utf-8"
        ),
    ],
)
log = logging.getLogger("deepay-webhook")


# ── 部署队列（防并发，最多排队 1 个）────────────────────────
_deploy_lock  = threading.Lock()
_deploy_queue: deque = deque(maxlen=1)  # 新任务替换旧排队任务
_is_deploying = False


def _run_deploy(pusher: str, commit: str, commits: int) -> None:
    """后台线程：执行部署脚本"""
    global _is_deploying
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    log_file = os.path.join(LOG_DIR, f"deploy-{ts}.log")
    t0 = time.time()

    log.info(
        "🚀 开始部署 | 推送人: %s | commit: %s | commits数: %d",
        pusher, commit, commits,
    )
    log.info("   日志文件: %s", log_file)

    try:
        with open(log_file, "w", encoding="utf-8") as f:
            f.write(f"# Deepay Auto Deploy  {datetime.now()}\n")
            f.write(f"# Pusher={pusher}  Commit={commit}  Commits={commits}\n\n")
            proc = subprocess.run(
                ["bash", DEPLOY_SCRIPT, DEPLOY_MODE],
                stdout=f,
                stderr=subprocess.STDOUT,
                timeout=1800,  # 最长 30 分钟
                env={**os.environ, "SKIP_PULL": "false"},
            )

        elapsed = int(time.time() - t0)
        if proc.returncode == 0:
            log.info("✔  部署成功  耗时 %ds | %s", elapsed, log_file)
        else:
            log.error(
                "✘  部署失败  returncode=%d  耗时 %ds | %s",
                proc.returncode, elapsed, log_file,
            )
            _tail_log(log_file, 30)

    except subprocess.TimeoutExpired:
        log.error("✘  部署超时（30min），已强制终止")
    except Exception as exc:
        log.exception("✘  部署异常: %s", exc)
    finally:
        with _deploy_lock:
            _is_deploying = False
        _drain_queue()


def _drain_queue() -> None:
    """如果队列里有等待任务，启动它"""
    global _is_deploying
    with _deploy_lock:
        if _deploy_queue:
            task = _deploy_queue.popleft()
            _is_deploying = True
            threading.Thread(
                target=_run_deploy, args=task, daemon=True
            ).start()


def _trigger(pusher: str, commit: str, commits: int) -> bool:
    """触发部署，如果当前正在部署则入队"""
    global _is_deploying
    with _deploy_lock:
        if _is_deploying:
            _deploy_queue.append((pusher, commit, commits))
            log.info("⏳ 当前有部署在执行，已加入等待队列 (pusher=%s)", pusher)
            return False
        _is_deploying = True
    threading.Thread(
        target=_run_deploy, args=(pusher, commit, commits), daemon=True
    ).start()
    return True


def _tail_log(path: str, n: int = 30) -> None:
    try:
        lines = open(path, encoding="utf-8").readlines()
        for line in lines[-n:]:
            log.error("    %s", line.rstrip())
    except Exception:
        pass


# ── HTTP 请求处理 ─────────────────────────────────────────────
class WebhookHandler(BaseHTTPRequestHandler):

    def do_POST(self) -> None:  # noqa: N802
        if self.path.rstrip("/") not in ("/webhook",):
            self._send(404, "Not Found")
            return

        try:
            length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(length)
        except Exception:
            self._send(400, "Bad Request")
            return

        # ── GitHub HMAC-SHA256 签名验证 ──────────────────────
        if SECRET:
            sig = self.headers.get("X-Hub-Signature-256", "")
            if not self._verify(body, sig):
                log.warning(
                    "⚠  签名验证失败，拒绝请求 (IP=%s)", self.client_address[0]
                )
                self._send(403, "Invalid signature")
                return
        else:
            log.warning(
                "⚠  WEBHOOK_SECRET 未配置，跳过签名验证（请立即设置！）"
            )

        event = self.headers.get("X-GitHub-Event", "")

        # GitHub 首次配置时发送 ping 验证
        if event == "ping":
            log.info("✓  收到 GitHub ping！Webhook 配置成功 🎉")
            self._send(200, "pong")
            return

        if event != "push":
            self._send(200, f"event '{event}' ignored")
            return

        try:
            payload = json.loads(body)
        except json.JSONDecodeError:
            self._send(400, "Invalid JSON")
            return

        ref    = payload.get("ref", "")
        branch = ref.replace("refs/heads/", "")

        if branch != BRANCH:
            log.info("🔕 分支 %s 不是目标 %s，跳过", branch, BRANCH)
            self._send(200, f"branch '{branch}' ignored")
            return

        pusher  = payload.get("pusher", {}).get("name", "unknown")
        commit  = payload.get("after", "")[:7]
        commits = len(payload.get("commits", []))

        # 立即返回 200，再后台部署（防止 GitHub 超时重发）
        self._send(200, f"Deploy triggered commit={commit}")
        _trigger(pusher, commit, commits)

    def do_GET(self) -> None:  # noqa: N802
        if self.path in ("/", "/health"):
            status = "deploying" if _is_deploying else "idle"
            queued = len(_deploy_queue)
            self._send(
                200,
                f"Deepay Webhook OK | port={PORT} branch={BRANCH} "
                f"status={status} queued={queued}",
            )
        else:
            self._send(404, "Not Found")

    def _verify(self, body: bytes, sig: str) -> bool:
        if not sig.startswith("sha256="):
            return False
        expected = "sha256=" + hmac.new(SECRET, body, hashlib.sha256).hexdigest()
        return hmac.compare_digest(expected, sig)

    def _send(self, code: int, msg: str) -> None:
        data = msg.encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, fmt, *args) -> None:  # noqa: ANN001
        pass  # 静音默认 HTTP 访问日志，使用自己的 logger


# ── 服务器启动 ───────────────────────────────────────────────
def _banner() -> None:
    log.info("━" * 55)
    log.info("  Deepay Webhook Server  v2.0")
    log.info("  监听端口 : 0.0.0.0:%d", PORT)
    log.info("  目标分支 : %s", BRANCH)
    log.info("  部署脚本 : %s %s", DEPLOY_SCRIPT, DEPLOY_MODE)
    log.info("  密钥校验 : %s", "✔ 已配置（安全）" if SECRET else "✘ 未配置（危险！）")
    log.info("━" * 55)
    log.info("  GitHub Webhook URL : http://YOUR_SERVER_IP:%d/webhook", PORT)
    log.info("  健康检查 URL       : curl http://localhost:%d/health", PORT)
    log.info("━" * 55)


def run_server() -> None:
    _banner()
    server = HTTPServer(("0.0.0.0", PORT), WebhookHandler)

    def _stop(sig, frame) -> None:  # noqa: ANN001
        log.info("收到停止信号，正在关闭...")
        threading.Thread(target=server.shutdown, daemon=True).start()

    signal.signal(signal.SIGTERM, _stop)
    signal.signal(signal.SIGINT, _stop)
    server.serve_forever()
    log.info("服务器已停止")


# ── CLI 管理命令 ─────────────────────────────────────────────
def cmd_start() -> None:
    if os.path.exists(PID_FILE):
        try:
            pid = int(open(PID_FILE).read().strip())
            os.kill(pid, 0)
            print(f"✔  服务器已在运行 (PID={pid})")
            print(f"   健康检查: curl http://localhost:{PORT}/health")
            return
        except (ProcessLookupError, OSError, ValueError):
            os.remove(PID_FILE)

    pid = os.fork()
    if pid > 0:
        open(PID_FILE, "w").write(str(pid))
        print(f"✔  Webhook 服务器已后台启动 (PID={pid})")
        print(f"   端口    : {PORT}")
        print(f"   健康检  : curl http://localhost:{PORT}/health")
        print(f"   日志    : {LOG_DIR}/webhook.log")
        print(f"   停止    : python3 {__file__} stop")
        return

    # 子进程
    os.setsid()
    run_server()
    sys.exit(0)


def cmd_stop() -> None:
    if not os.path.exists(PID_FILE):
        print("❌  未运行（无 PID 文件）")
        return
    try:
        pid = int(open(PID_FILE).read().strip())
        os.kill(pid, signal.SIGTERM)
        for _ in range(20):
            time.sleep(0.3)
            try:
                os.kill(pid, 0)
            except ProcessLookupError:
                break
        try:
            os.remove(PID_FILE)
        except FileNotFoundError:
            pass
        print(f"✔  已停止 (PID={pid})")
    except (ValueError, ProcessLookupError):
        try:
            os.remove(PID_FILE)
        except FileNotFoundError:
            pass
        print("已清理 PID 文件（进程已不存在）")
    except OSError as exc:
        print(f"❌  停止失败: {exc}")


def cmd_status() -> None:
    print(f"\n  Deepay Webhook Server 状态")
    print("  " + "─" * 42)
    if os.path.exists(PID_FILE):
        try:
            pid = int(open(PID_FILE).read().strip())
            os.kill(pid, 0)
            print(f"  状态     : ✔  运行中 (PID={pid})")
            print(f"  监听端口 : {PORT}")
            print(f"  目标分支 : {BRANCH}")
            print(f"  密钥校验 : {'已配置 ✔' if SECRET else '未配置 ✘（危险！）'}")
            print(f"  Webhook  : http://YOUR_IP:{PORT}/webhook")
            print(f"  健康检查 : curl http://localhost:{PORT}/health")
        except (ProcessLookupError, ValueError, OSError):
            print("  状态     : ❌  PID 文件存在但进程已退出")
            print(f"  重启     : python3 {__file__} start")
    else:
        print("  状态     : ❌  未运行")
        print(f"  启动     : python3 {__file__} start")
    print()


def cmd_logs(n: int = 80) -> None:
    log_file = os.path.join(LOG_DIR, "webhook.log")
    if os.path.exists(log_file):
        os.system(f"tail -n {n} '{log_file}'")
    else:
        print("暂无日志")


def cmd_test() -> None:
    import urllib.request
    import urllib.error
    url = f"http://127.0.0.1:{PORT}/health"
    try:
        r = urllib.request.urlopen(url, timeout=5)
        print(f"✔  服务正常  HTTP {r.status}")
        print(f"   响应: {r.read().decode()}")
    except urllib.error.URLError as exc:
        print(f"❌  无法连接 {url}: {exc.reason}")
        print(f"   请先启动: python3 {__file__} start")


if __name__ == "__main__":
    _cmds = {
        "run":    run_server,
        "start":  cmd_start,
        "stop":   cmd_stop,
        "status": cmd_status,
        "logs":   cmd_logs,
        "test":   cmd_test,
    }
    _cmd = sys.argv[1] if len(sys.argv) > 1 else "help"
    _fn = _cmds.get(_cmd)
    if _fn:
        _fn()
    else:
        print(__doc__)
