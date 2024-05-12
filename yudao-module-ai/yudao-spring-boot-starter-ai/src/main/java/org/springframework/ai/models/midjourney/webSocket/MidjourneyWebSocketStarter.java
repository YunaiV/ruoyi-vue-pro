package org.springframework.ai.models.midjourney.webSocket;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.constants.MidjourneyNotifyCode;
import org.springframework.ai.models.midjourney.webSocket.handler.MidjourneyWebSocketHandler;
import org.springframework.ai.models.midjourney.webSocket.listener.MidjourneyMessageListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.Constants;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.net.URI;

// TODO @fansili：mj 这块 websocket 有点小复杂，虽然代码量 400 多行；感觉可以考虑，有没第三方 sdk，通过它透明接入 mj
@Slf4j
public class MidjourneyWebSocketStarter implements WebSocketStarter {
	/**
	 * 链接重试次数
	 */
	private static final int CONNECT_RETRY_LIMIT = 5;
	/**
	 * mj 配置文件
	 */
	private final MidjourneyConfig midjourneyConfig;
	/**
	 * mj 监听(所有message 都会 callback到这里)
	 */
	private final MidjourneyMessageListener userMessageListener;
	/**
	 * wss 服务器
	 */
	private final String wssServer;
	/**
	 *
	 */
	private final String resumeWss;
	/**
	 *
	 */
	private ResumeData resumeData = null;
	/**
	 * 是否运行成功
	 */
	private boolean running = false;
	/**
	 * 链接成功的 session
	 */
	private WebSocketSession webSocketSession = null;
	private WssNotify wssNotify = null;

	public MidjourneyWebSocketStarter(String wssServer,
									  String resumeWss,
									  MidjourneyConfig midjourneyConfig,
									  MidjourneyMessageListener userMessageListener) {
		this.wssServer = wssServer;
		this.resumeWss = resumeWss;
		this.midjourneyConfig = midjourneyConfig;
		this.userMessageListener = userMessageListener;
	}

	@Override
	public void start(WssNotify wssNotify) {
		this.wssNotify = wssNotify;
		start(false);
	}

	private void start(boolean reconnect) {
		// 设置header
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Accept-Encoding", "gzip, deflate, br");
		headers.add("Accept-Language", "zh-CN,zh;q=0.9");
		headers.add("Cache-Control", "no-cache");
		headers.add("Pragma", "no-cache");
		headers.add("Sec-Websocket-Extensions", "permessage-deflate; client_max_window_bits");
		headers.add("User-Agent", this.midjourneyConfig.getUserAage());
		// 创建 mjHeader
		MidjourneyWebSocketHandler mjWebSocketHandler = new MidjourneyWebSocketHandler(
				this.midjourneyConfig, this.userMessageListener, this::onSocketSuccess, this::onSocketFailure);
		//
		String gatewayUrl;
		if (reconnect) {
			gatewayUrl = getGatewayServer(this.resumeData.getResumeGatewayUrl()) + "/?encoding=json&v=9&compress=zlib-stream";
			mjWebSocketHandler.setSessionId(this.resumeData.getSessionId());
			mjWebSocketHandler.setSequence(this.resumeData.getSequence());
			mjWebSocketHandler.setResumeGatewayUrl(this.resumeData.getResumeGatewayUrl());
		} else {
			gatewayUrl = getGatewayServer(null) + "/?encoding=json&v=9&compress=zlib-stream";
		}
		// 创建 StandardWebSocketClient
		StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
		// 设置 io timeout 时间
		webSocketClient.getUserProperties().put(Constants.IO_TIMEOUT_MS_PROPERTY, "10000");
		//
		ListenableFuture<WebSocketSession> socketSessionFuture = webSocketClient.doHandshake(mjWebSocketHandler, headers, URI.create(gatewayUrl));
		// 添加 callback 进行回调
		socketSessionFuture.addCallback(new ListenableFutureCallback<>() {
			@Override
			public void onFailure(@NotNull Throwable e) {
				onSocketFailure(MidjourneyWebSocketHandler.CLOSE_CODE_EXCEPTION, e.getMessage());
			}

			@Override
			public void onSuccess(WebSocketSession session) {
				MidjourneyWebSocketStarter.this.webSocketSession = session;
			}
		});
	}

	private void onSocketSuccess(String sessionId, Object sequence, String resumeGatewayUrl) {
		this.resumeData = new ResumeData(sessionId, sequence, resumeGatewayUrl);
		this.running = true;
		notifyWssLock(MidjourneyNotifyCode.SUCCESS, "");
	}

	private void onSocketFailure(int code, String reason) {
		// 1001异常可以忽略
		if (code == 1001) {
			return;
		}
		// 关闭 socket
		closeSocketSessionWhenIsOpen();
		// 没有运行通知
		if (!this.running) {
			notifyWssLock(code, reason);
			return;
		}
		// 已经运行先设置为false，发起
		this.running = false;
		if (code >= 4000) {
			log.warn("[wss-{}] Can't reconnect! Account disabled. Closed by {}({}).", this.midjourneyConfig.getChannelId(), code, reason);
		} else if (code == 2001) {
			log.warn("[wss-{}] Closed by {}({}). Try reconnect...", this.midjourneyConfig.getChannelId(), code, reason);
			tryReconnect();
		} else {
			log.warn("[wss-{}] Closed by {}({}). Try new connection...", this.midjourneyConfig.getChannelId(), code, reason);
			tryNewConnect();
		}
	}

	/**
	 * 重连
	 */
	private void tryReconnect() {
		try {
			tryStart(true);
		} catch (Exception e) {
            log.warn("[wss-{}] Reconnect fail: {}, Try new connection...", this.midjourneyConfig.getChannelId(), e.getMessage());
			ThreadUtil.sleep(1000);
			tryNewConnect();
		}
	}

	private void tryNewConnect() {
		// 链接重试次数5
		for (int i = 1; i <= CONNECT_RETRY_LIMIT; i++) {
			try {
				tryStart(false);
				return;
			} catch (Exception e) {
                log.warn("[wss-{}] New connect fail ({}): {}", this.midjourneyConfig.getChannelId(), i, e.getMessage());
				ThreadUtil.sleep(5000);
			}
		}
		log.error("[wss-{}] Account disabled", this.midjourneyConfig.getChannelId());
	}

	public void tryStart(boolean reconnect) {
		start(reconnect);
	}

	private void notifyWssLock(int code, String reason) {
		System.err.println("notifyWssLock: " + code + " - " + reason);
		if (wssNotify != null) {
			wssNotify.notify(code, reason);
		}
	}

	/**
	 * 关闭 socket session
	 */
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

	@Getter
	public static class ResumeData {

		public ResumeData(String sessionId, Object sequence, String resumeGatewayUrl) {
			this.sessionId = sessionId;
			this.sequence = sequence;
			this.resumeGatewayUrl = resumeGatewayUrl;
		}

		/**
		 * socket session
		 */
		private final String sessionId;
		private final Object sequence;
		private final String resumeGatewayUrl;
	}
}