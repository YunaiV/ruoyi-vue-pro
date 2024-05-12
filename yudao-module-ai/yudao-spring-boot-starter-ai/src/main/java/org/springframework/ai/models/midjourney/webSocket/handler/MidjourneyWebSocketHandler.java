package org.springframework.ai.models.midjourney.webSocket.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.webSocket.FailureCallback;
import org.springframework.ai.models.midjourney.webSocket.SuccessCallback;
import org.springframework.ai.models.midjourney.webSocket.listener.MidjourneyMessageListener;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.requests.WebSocketCode;
import net.dv8tion.jda.internal.utils.compress.Decompressor;
import net.dv8tion.jda.internal.utils.compress.ZlibDecompressor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MidjourneyWebSocketHandler implements WebSocketHandler {
	/**
	 * close 错误码：重连
	 */
	public static final int CLOSE_CODE_RECONNECT = 2001;
	/**
	 * close 错误码：无效、作废
	 */
	public static final int CLOSE_CODE_INVALIDATE = 1009;
	/**
	 * close 错误码：异常
	 */
	public static final int CLOSE_CODE_EXCEPTION = 1011;
	/**
	 * mj配置文件
	 */
	private final MidjourneyConfig midjourneyConfig;
	/**
	 * mj 消息监听
	 */
	private final MidjourneyMessageListener userMessageListener;
	/**
	 * 成功回调
	 */
	private final SuccessCallback successCallback;
	/**
	 * 失败回调
	 */
	private final FailureCallback failureCallback;
	/**
	 * 心跳执行器
	 */
	private final ScheduledExecutorService heartExecutor;
	/**
	 * auth数据
	 */
	private final DataObject authData;

	@Setter
	private String sessionId = null;
	@Setter
	private Object sequence = null;
	@Setter
	private String resumeGatewayUrl = null;

	private long interval = 41250;
	private boolean heartbeatAck = false;

	private Future<?> heartbeatInterval;
	private Future<?> heartbeatTimeout;

	/**
	 * 处理 message 消息的 Decompressor
	 */
	private final Decompressor decompressor = new ZlibDecompressor(2048);

	public MidjourneyWebSocketHandler(MidjourneyConfig account,
									  MidjourneyMessageListener userMessageListener,
									  SuccessCallback successCallback,
									  FailureCallback failureCallback) {
		this.midjourneyConfig = account;
		this.userMessageListener = userMessageListener;
		this.successCallback = successCallback;
		this.failureCallback = failureCallback;
		this.heartExecutor = Executors.newSingleThreadScheduledExecutor();
		this.authData = createAuthData();
	}

	@Override
	public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
		// do nothing
	}

	@Override
	public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable e) throws Exception {
		log.error("[wss-{}] Transport error", this.midjourneyConfig.getChannelId(), e);
		// 通知链接异常
		onFailure(CLOSE_CODE_EXCEPTION, "transport error");
	}

	@Override
	public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
		// 链接关闭
		onFailure(closeStatus.getCode(), closeStatus.getReason());
	}

	@Override
	public boolean supportsPartialMessages() {
		return true;
	}

	@Override
	public void handleMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// 获取 message 消息
		ByteBuffer buffer = (ByteBuffer) message.getPayload();
		// 解析 message
		byte[] decompressed = decompressor.decompress(buffer.array());
		if (decompressed == null) {
			return;
		}
		// 转换 json
		String json = new String(decompressed, StandardCharsets.UTF_8);
		// 转换 jda 自带的 dataObject(和json object 差不多)
		DataObject data = DataObject.fromJson(json);
		// 获取消息类型
		int opCode = data.getInt("op");
		switch (opCode) {
			case WebSocketCode.HEARTBEAT -> handleHeartbeat(session);
			case WebSocketCode.HEARTBEAT_ACK -> {
				this.heartbeatAck = true;
				clearHeartbeatTimeout();
			}
			case WebSocketCode.HELLO -> {
				handleHello(session, data);
				doResumeOrIdentify(session);
			}
			case WebSocketCode.RESUME -> onSuccess();
			case WebSocketCode.RECONNECT -> onFailure(CLOSE_CODE_RECONNECT, "receive server reconnect");
			case WebSocketCode.INVALIDATE_SESSION -> onFailure(CLOSE_CODE_INVALIDATE, "receive session invalid");
			case WebSocketCode.DISPATCH -> handleDispatch(data);
			default -> log.debug("[wss-{}] Receive unknown code: {}.", midjourneyConfig.getChannelId(), data);
		}
	}

	private void handleDispatch(DataObject raw) {
		this.sequence = raw.opt("s").orElse(null);
		if (!raw.isType("d", DataType.OBJECT)) {
			return;
		}
		DataObject content = raw.getObject("d");
		String t = raw.getString("t", null);
		if ("READY".equals(t)) {
			this.sessionId = content.getString("session_id");
			this.resumeGatewayUrl = content.getString("resume_gateway_url");
			onSuccess();
		} else if ("RESUMED".equals(t)) {
			onSuccess();
		} else {
			try {
				this.userMessageListener.onMessage(raw);
			} catch (Exception e) {
				log.error("[wss-{}] Handle message error", this.midjourneyConfig.getChannelId(), e);
			}
		}
	}

	private void handleHeartbeat(WebSocketSession session) {
		sendMessage(session, WebSocketCode.HEARTBEAT, this.sequence);
		this.heartbeatTimeout = ThreadUtil.execAsync(() -> {
			ThreadUtil.sleep(this.interval);
			onFailure(CLOSE_CODE_RECONNECT, "heartbeat has not ack");
		});
	}

	private void handleHello(WebSocketSession session, DataObject data) {
		clearHeartbeatInterval();
		this.interval = data.getObject("d").getLong("heartbeat_interval");
		this.heartbeatAck = true;
		this.heartbeatInterval = this.heartExecutor.scheduleAtFixedRate(() -> {
			if (this.heartbeatAck) {
				this.heartbeatAck = false;
				sendMessage(session, WebSocketCode.HEARTBEAT, this.sequence);
			} else {
				onFailure(CLOSE_CODE_RECONNECT, "heartbeat has not ack interval");
			}
		}, (long) Math.floor(RandomUtil.randomDouble(0, 1) * this.interval), this.interval, TimeUnit.MILLISECONDS);
	}

	private void doResumeOrIdentify(WebSocketSession session) {
		if (CharSequenceUtil.isBlank(this.sessionId)) {
			sendMessage(session, WebSocketCode.IDENTIFY, this.authData);
		} else {
			var data = DataObject.empty().put("token", this.midjourneyConfig.getToken())
					.put("session_id", this.sessionId).put("seq", this.sequence);
			sendMessage(session, WebSocketCode.RESUME, data);
		}
	}

	private void sendMessage(WebSocketSession session, int op, Object d) {
		var data = DataObject.empty().put("op", op).put("d", d);
		try {
			session.sendMessage(new TextMessage(data.toString()));
		} catch (IOException e) {
			log.error("[wss-{}] Send message error", this.midjourneyConfig.getChannelId(), e);
			onFailure(CLOSE_CODE_EXCEPTION, "send message error");
		}
	}

	private void onSuccess() {
		ThreadUtil.execute(() -> this.successCallback.onSuccess(this.sessionId, this.sequence, this.resumeGatewayUrl));
	}

	private void onFailure(int code, String reason) {
		clearHeartbeatTimeout();
		clearHeartbeatInterval();
		ThreadUtil.execute(() -> this.failureCallback.onFailure(code, reason));
	}

	private void clearHeartbeatTimeout() {
		if (this.heartbeatTimeout != null) {
			this.heartbeatTimeout.cancel(true);
			this.heartbeatTimeout = null;
		}
	}

	private void clearHeartbeatInterval() {
		if (this.heartbeatInterval != null) {
			this.heartbeatInterval.cancel(true);
			this.heartbeatInterval = null;
		}
	}

	private DataObject createAuthData() {
		UserAgent userAgent = UserAgentUtil.parse(this.midjourneyConfig.getUserAage());
		DataObject connectionProperties = DataObject.empty()
				.put("browser", userAgent.getBrowser().getName())
				.put("browser_user_agent", this.midjourneyConfig.getUserAage())
				.put("browser_version", userAgent.getVersion())
				.put("client_build_number", 222963)
				.put("client_event_source", null)
				.put("device", "")
				.put("os", userAgent.getOs().getName())
				.put("referer", "https://www.midjourney.com")
				.put("referrer_current", "")
				.put("referring_domain", "www.midjourney.com")
				.put("referring_domain_current", "")
				.put("release_channel", "stable")
				.put("system_locale", "zh-CN");
		DataObject presence = DataObject.empty()
				.put("activities", DataArray.empty())
				.put("afk", false)
				.put("since", 0)
				.put("status", "online");
		DataObject clientState = DataObject.empty()
				.put("api_code_version", 0)
				.put("guild_versions", DataObject.empty())
				.put("highest_last_message_id", "0")
				.put("private_channels_version", "0")
				.put("read_state_version", 0)
				.put("user_guild_settings_version", -1)
				.put("user_settings_version", -1);
		return DataObject.empty()
				.put("capabilities", 16381)
				.put("client_state", clientState)
				.put("compress", false)
				.put("presence", presence)
				.put("properties", connectionProperties)
				.put("token", this.midjourneyConfig.getToken());
	}
}
