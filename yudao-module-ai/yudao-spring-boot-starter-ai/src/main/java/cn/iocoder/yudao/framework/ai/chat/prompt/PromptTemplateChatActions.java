package cn.iocoder.yudao.framework.ai.chat.prompt;

import cn.iocoder.yudao.framework.ai.chat.messages.Message;

import java.util.List;
import java.util.Map;

/**
 * 聊天操作
 *
 */
public interface PromptTemplateChatActions {

	List<Message> createMessages();

	List<Message> createMessages(Map<String, Object> model);

}
