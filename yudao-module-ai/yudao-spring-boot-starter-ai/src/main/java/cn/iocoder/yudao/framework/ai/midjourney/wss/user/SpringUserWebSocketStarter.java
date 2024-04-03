package cn.iocoder.yudao.framework.ai.midjourney.wss.user;


import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.framework.ai.midjourney.jad.DiscordAccount;
import cn.iocoder.yudao.framework.ai.midjourney.wss.AsyncLockUtils;
import cn.iocoder.yudao.framework.ai.midjourney.wss.ReturnCode;
import cn.iocoder.yudao.framework.ai.midjourney.wss.WebSocketStarter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.Constants;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
public class SpringUserWebSocketStarter implements WebSocketStarter {
	private static final int CONNECT_RETRY_LIMIT = 5;

	private final DiscordAccount account;
	private final UserMessageListener userMessageListener;
	private final String wssServer;
	private final String resumeWss;

	private boolean running = false;

	private WebSocketSession webSocketSession = null;
	private ResumeData resumeData = null;

	public SpringUserWebSocketStarter(String wssServer, String resumeWss, DiscordAccount account, UserMessageListener userMessageListener) {
		this.wssServer = wssServer;
		this.resumeWss = resumeWss;
		this.account = account;
		this.userMessageListener = userMessageListener;
	}

	@Override
	public void start() throws Exception {
		start(false);
	}

	private void start(boolean reconnect) {
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Accept-Encoding", "gzip, deflate, br");
		headers.add("Accept-Language", "zh-CN,zh;q=0.9");
		headers.add("Cache-Control", "no-cache");
		headers.add("Pragma", "no-cache");
		headers.add("Sec-Websocket-Extensions", "permessage-deflate; client_max_window_bits");
		headers.add("User-Agent", this.account.getUserAgent());
		var handler = new SpringWebSocketHandler(this.account, this.userMessageListener, this::onSocketSuccess, this::onSocketFailure);
		String gatewayUrl;
		if (reconnect) {
			gatewayUrl = getGatewayServer(this.resumeData.resumeGatewayUrl()) + "/?encoding=json&v=9&compress=zlib-stream";
			handler.setSessionId(this.resumeData.sessionId());
			handler.setSequence(this.resumeData.sequence());
			handler.setResumeGatewayUrl(this.resumeData.resumeGatewayUrl());
		} else {
			gatewayUrl = getGatewayServer(null) + "/?encoding=json&v=9&compress=zlib-stream";
		}
		var webSocketClient = new StandardWebSocketClient();
		webSocketClient.getUserProperties().put(Constants.IO_TIMEOUT_MS_PROPERTY, "10000");
		var socketSessionFuture = webSocketClient.doHandshake(handler, headers, URI.create(gatewayUrl));
		socketSessionFuture.addCallback(new ListenableFutureCallback<>() {
			@Override
			public void onFailure(@NotNull Throwable e) {
				onSocketFailure(SpringWebSocketHandler.CLOSE_CODE_EXCEPTION, e.getMessage());
			}

			@Override
			public void onSuccess(WebSocketSession session) {
				SpringUserWebSocketStarter.this.webSocketSession = session;
			}
		});
	}

	private void onSocketSuccess(String sessionId, Object sequence, String resumeGatewayUrl) {
		this.resumeData = new ResumeData(sessionId, sequence, resumeGatewayUrl);
		this.running = true;
		notifyWssLock(ReturnCode.SUCCESS, "");
	}

	private void onSocketFailure(int code, String reason) {
		if (code == 1001) {
			return;
		}
		closeSocketSessionWhenIsOpen();
		if (!this.running) {
			notifyWssLock(code, reason);
			return;
		}
		this.running = false;
		if (code >= 4000) {
			log.warn("[wss-{}] Can't reconnect! Account disabled. Closed by {}({}).", this.account.getDisplay(), code, reason);
			disableAccount();
		} else if (code == 2001) {
			log.warn("[wss-{}] Closed by {}({}). Try reconnect...", this.account.getDisplay(), code, reason);
			tryReconnect();
		} else {
			log.warn("[wss-{}] Closed by {}({}). Try new connection...", this.account.getDisplay(), code, reason);
			tryNewConnect();
		}
	}

	private void tryReconnect() {
		try {
			tryStart(true);
		} catch (Exception e) {
			if (e instanceof TimeoutException) {
				closeSocketSessionWhenIsOpen();
			}
			log.warn("[wss-{}] Reconnect fail: {}, Try new connection...", this.account.getDisplay(), e.getMessage());
			ThreadUtil.sleep(1000);
			tryNewConnect();
		}
	}

	private void tryNewConnect() {
		for (int i = 1; i <= CONNECT_RETRY_LIMIT; i++) {
			try {
				tryStart(false);
				return;
			} catch (Exception e) {
				if (e instanceof TimeoutException) {
					closeSocketSessionWhenIsOpen();
				}
				log.warn("[wss-{}] New connect fail ({}): {}", this.account.getDisplay(), i, e.getMessage());
				ThreadUtil.sleep(5000);
			}
		}
		log.error("[wss-{}] Account disabled", this.account.getDisplay());
		disableAccount();
	}

	public void tryStart(boolean reconnect) throws Exception {
		start(reconnect);
		AsyncLockUtils.LockObject lock = AsyncLockUtils.waitForLock("wss:" + this.account.getId(), Duration.ofSeconds(20));
		int code = lock.getProperty("code", Integer.class, 0);
		if (code == ReturnCode.SUCCESS) {
			log.debug("[wss-{}] {} success.", this.account.getDisplay(), reconnect ? "Reconnect" : "New connect");
			return;
		}
		throw new ValidateException(lock.getProperty("description", String.class));
	}

	private void notifyWssLock(int code, String reason) {
		AsyncLockUtils.LockObject lock = AsyncLockUtils.getLock("wss:" + this.account.getId());
		if (lock != null) {
			lock.setProperty("code", code);
			lock.setProperty("description", reason);
			lock.awake();
		}
	}

	private void disableAccount() {
		if (Boolean.FALSE.equals(this.account.isEnable())) {
			return;
		}
		this.account.setEnable(false);
	}

	private void closeSocketSessionWhenIsOpen() {
		try {
			if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
				this.webSocketSession.close(CloseStatus.GOING_AWAY);
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	private String getGatewayServer(String resumeGatewayUrl) {
		if (CharSequenceUtil.isNotBlank(resumeGatewayUrl)) {
			return CharSequenceUtil.isBlank(this.resumeWss) ? resumeGatewayUrl : this.resumeWss;
		}
		return this.wssServer;
	}

	public record ResumeData(String sessionId, Object sequence, String resumeGatewayUrl) {
	}
}