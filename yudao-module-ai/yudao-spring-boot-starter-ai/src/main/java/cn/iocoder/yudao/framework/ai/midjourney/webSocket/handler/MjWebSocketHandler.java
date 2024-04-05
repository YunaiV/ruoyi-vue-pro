package cn.iocoder.yudao.framework.ai.midjourney.webSocket.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.FailureCallback;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.SuccessCallback;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener.MjMessageListener;
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
public class MjWebSocketHandler implements WebSocketHandler {
	public static final int CLOSE_CODE_RECONNECT = 2001;
	public static final int CLOSE_CODE_INVALIDATE = 1009;
	public static final int CLOSE_CODE_EXCEPTION = 1011;

	private final MidjourneyConfig midjourneyConfig;
	private final MjMessageListener userMessageListener;
	private final SuccessCallback successCallback;
	private final FailureCallback failureCallback;

	private final ScheduledExecutorService heartExecutor;
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

	private final Decompressor decompressor = new ZlibDecompressor(2048);

	public MjWebSocketHandler(MidjourneyConfig account,
							  MjMessageListener userMessageListener,
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
		onFailure(CLOSE_CODE_EXCEPTION, "transport error");
	}

	@Override
	public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
		onFailure(closeStatus.getCode(), closeStatus.getReason());
	}

	@Override
	public boolean supportsPartialMessages() {
		return true;
	}

	@Override
	public void handleMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		ByteBuffer buffer = (ByteBuffer) message.getPayload();
		byte[] decompressed = decompressor.decompress(buffer.array());
		if (decompressed == null) {
			return;
		}
		String json = new String(decompressed, StandardCharsets.UTF_8);
		DataObject data = DataObject.fromJson(json);
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
