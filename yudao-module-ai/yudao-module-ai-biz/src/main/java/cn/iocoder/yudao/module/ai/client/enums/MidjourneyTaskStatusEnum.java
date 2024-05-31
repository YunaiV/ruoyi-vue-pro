package cn.iocoder.yudao.module.ai.client.enums;


import lombok.Getter;

/**
 * 来源于 midjourney-proxy
 */
public enum MidjourneyTaskStatusEnum {
	/**
	 * 未启动.
	 */
	NOT_START(0),
	/**
	 * 已提交.
	 */
	SUBMITTED(1),
	/**
	 * 执行中.
	 */
	IN_PROGRESS(3),
	/**
	 * 失败.
	 */
	FAILURE(4),
	/**
	 * 成功.
	 */
	SUCCESS(4);

	@Getter
	private final int order;

	MidjourneyTaskStatusEnum(int order) {
		this.order = order;
	}

}
