package cn.iocoder.yudao.framework.ai.midjourney.demo.wss.user;


import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.framework.ai.midjourney.demo.wss.MessageType;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

@Slf4j
public class UserMessageListener {

	public void onMessage(DataObject raw) {
		MessageType messageType = MessageType.of(raw.getString("t"));
		if (messageType == null || MessageType.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		System.err.println(data);
		ThreadUtil.sleep(50);
//		for (MessageHandler messageHandler : this.messageHandlers) {
//			if (data.getBoolean(Constants.MJ_MESSAGE_HANDLED, false)) {
//				return;
//			}
//			messageHandler.handle(this.instance, messageType, data);
//		}
	}
}
