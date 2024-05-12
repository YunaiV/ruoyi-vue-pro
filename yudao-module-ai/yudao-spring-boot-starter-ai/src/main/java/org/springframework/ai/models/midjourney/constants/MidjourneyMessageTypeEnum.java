package org.springframework.ai.models.midjourney.constants;


public enum MidjourneyMessageTypeEnum {
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

	public static MidjourneyMessageTypeEnum of(String type) {
		return switch (type) {
			case "MESSAGE_CREATE" -> CREATE;
			case "MESSAGE_UPDATE" -> UPDATE;
			case "MESSAGE_DELETE" -> DELETE;
			default -> null;
		};
	}
}
