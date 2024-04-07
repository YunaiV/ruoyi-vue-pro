package cn.iocoder.yudao.framework.ai.midjourney.constants;

public final class MjConstants {

	/**
	 * 消息 - 编号
	 */
	public static final String MSG_ID = "id";
	/**
	 * 消息 - 类型
	 * 现在已知：
	 * 0：我们发送的消息，和指令
	 * 20: mj生成图片发送过程中
	 * 19: 选择了某一张图片后的通知
	 */
	public static final String MSG_TYPE = "type";
	/**
	 * 平道id
	 */
	public static final String MSG_CHANNEL_ID = "channel_id";
	/**
	 * 内容
	 *
	 * "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (32%) (fast, stealth)",
	 */
	public static final String MSG_CONTENT = "content";
	/**
	 * 组件(图片生成好之后才有)
	 */
	public static final String MSG_COMPONENTS = "components";
	/**
	 * 附件(生成中比较模糊的图片)
	 */
	public static final String MSG_ATTACHMENTS = "attachments";


}
