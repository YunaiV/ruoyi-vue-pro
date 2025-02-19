package cn.iocoder.yudao.module.iot.service.ota.bo.upgrade.record;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class IotOtaUpgradeRecordUpdateReqBO {

    /**
     * 升级记录编号
     */
    @NotNull(message = "升级记录编号不能为空")
    private Long id;
    /**
     * 升级状态
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum}
     */
    @InEnum(IotOtaUpgradeRecordStatusEnum.class)
    private Integer status;
    /**
     * 升级进度，百分比
     */
    @Range(min = 0, max = 100, message = "升级进度必须介于 0-100 之间")
    private Integer progress;
    /**
     * 升级开始时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
