package org.springframework.ai.chat.prompt;

import org.springframework.ai.chat.messages.Message;

import java.util.Map;

/**
 * 用户输入的提示内容 模板信息操作
 */
public interface PromptTemplateMessageActions {

	/**
	 * 创建一个 message
	 * @return
	 */
	Message createMessage();

	/**
	 * 创建一个 message
	 * @return
	 */
	Message createMessage(Map<String, Object> model);

}
