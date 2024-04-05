package cn.iocoder.yudao.framework.ai.midjourney.constants;


public enum MjMessageTypeEnum {
	/**
	 * 创建.
	 */
	CREATE,
	/**
	 * 修改.
	 */
	UPDATE,
	/**
	 * 删除.
	 */
	DELETE;

	public static MjMessageTypeEnum of(String type) {
		return switch (type) {
			case "MESSAGE_CREATE" -> CREATE;
			case "MESSAGE_UPDATE" -> UPDATE;
			case "MESSAGE_DELETE" -> DELETE;
			default -> null;
		};
	}
}
