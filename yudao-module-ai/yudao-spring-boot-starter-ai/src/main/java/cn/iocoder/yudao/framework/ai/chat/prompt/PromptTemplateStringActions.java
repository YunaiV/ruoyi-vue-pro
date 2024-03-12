package cn.iocoder.yudao.framework.ai.chat.prompt;

import java.util.Map;

/**
 * 提示次模板字符串操作
 */
public interface PromptTemplateStringActions {

	String render();

	String render(Map<String, Object> model);

}
