package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 线索 Excel 导出 Request VO，参数和 CrmCluePageReqVO 是一致的")
@Data
public class CrmClueExportReqVO {

    @Schema(description = "转化状态", example = "true")
    private Boolean transformStatus;

    @Schema(description = "跟进状态", example = "true")
    private Boolean followUpStatus;

    @Schema(description = "线索名称", example = "线索xxx")
    private String name;

    @Schema(description = "客户id", example = "520")
    private Long customerId;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactNextTime;

    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    @Schema(description = "手机号", example = "18000000000")
    private String mobile;

    @Schema(description = "地址", example = "北京市海淀区")
    private String address;

    @Schema(description = "负责人的用户编号", example = "27199")
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactLastTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
