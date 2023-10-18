package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 线索 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmClueBaseVO {

    @Schema(description = "转化状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "转化状态不能为空")
    private Boolean transformStatus;

    @Schema(description = "跟进状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "跟进状态不能为空")
    private Boolean followUpStatus;

    @Schema(description = "线索名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "线索xxx")
    @NotNull(message = "线索名称不能为空")
    private String name;

    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "520")
    @NotNull(message = "客户id不能为空")
    private Long customerId;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    @Mobile(message = "电话格式不正确")
    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    @Mobile(message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18000000000")
    private String mobile;

    @Schema(description = "地址", example = "北京市海淀区")
    private String address;

    @Schema(description = "负责人的用户编号", example = "27199")
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
