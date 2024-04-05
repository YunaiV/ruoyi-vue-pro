package cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener;


import cn.hutool.core.text.CharSequenceUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjConstants;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

@Slf4j
public class MjMessageListener {

	private MidjourneyConfig midjourneyConfig;

	public MjMessageListener(MidjourneyConfig midjourneyConfig) {
		this.midjourneyConfig = midjourneyConfig;
	}

	public void onMessage(DataObject raw) {
		MjMessageTypeEnum messageType = MjMessageTypeEnum.of(raw.getString("t"));
		if (messageType == null || MjMessageTypeEnum.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		System.err.println(data);
//		if (data.getBoolean(Constants.MJ_MESSAGE_HANDLED, false)) {
//			return;
//		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MjMessageTypeEnum messageType) {
		String channelId = data.getString(MjConstants.CHANNEL_ID);
		if (!CharSequenceUtil.equals(channelId, midjourneyConfig.getChannelId())) {
			return true;
		}
		String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
		log.debug("{} - {} - {}: {}", midjourneyConfig.getChannelId(), messageType.name(), authorName, data.opt("content").orElse(""));
		return false;
	}
}
