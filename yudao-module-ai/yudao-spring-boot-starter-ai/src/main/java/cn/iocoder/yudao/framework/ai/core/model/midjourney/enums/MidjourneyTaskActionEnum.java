package cn.iocoder.yudao.framework.ai.core.model.midjourney.enums;

import lombok.Getter;

/**
 * 来源于 midjourney-proxy
 */
@Getter
public enum MidjourneyTaskActionEnum {
	/**
	 * 生成图片.
	 */
	IMAGINE,
	/**
	 * 选中放大.
	 */
	UPSCALE,
	/**
	 * 选中其中的一张图，生成四张相似的.
	 */
	VARIATION,
	/**
	 * 重新执行.
	 */
	REROLL,
	/**
	 * 图转prompt.
	 */
	DESCRIBE,
	/**
	 * 多图混合.
	 */
	BLEND

}
