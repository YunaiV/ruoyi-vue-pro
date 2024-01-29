package cn.iocoder.yudao.module.promotion.util;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;

import java.time.LocalDateTime;

/**
 * 活动工具类
 *
 * @author 芋道源码
 */
public class PromotionUtils {

    /**
     * 根据时间，计算活动状态
     *
     * @param endTime 结束时间
     * @return 活动状态
     */
    public static Integer calculateActivityStatus(LocalDateTime endTime) {
        return LocalDateTimeUtils.beforeNow(endTime) ? CommonStatusEnum.DISABLE.getStatus() : CommonStatusEnum.ENABLE.getStatus();
    }

}
