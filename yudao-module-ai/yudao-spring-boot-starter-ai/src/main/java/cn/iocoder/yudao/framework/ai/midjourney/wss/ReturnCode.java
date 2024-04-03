package cn.iocoder.yudao.framework.ai.midjourney.wss;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReturnCode {
	/**
	 * 成功.
	 */
	public static final int SUCCESS = 1;
	/**
	 * 数据未找到.
	 */
	public static final int NOT_FOUND = 3;
	/**
	 * 校验错误.
	 */
	public static final int VALIDATION_ERROR = 4;
	/**
	 * 系统异常.
	 */
	public static final int FAILURE = 9;

	/**
	 * 已存在.
	 */
	public static final int EXISTED = 21;
	/**
	 * 排队中.
	 */
	public static final int IN_QUEUE = 22;
	/**
	 * 队列已满.
	 */
	public static final int QUEUE_REJECTED = 23;
	/**
	 * prompt包含敏感词.
	 */
	public static final int BANNED_PROMPT = 24;


}