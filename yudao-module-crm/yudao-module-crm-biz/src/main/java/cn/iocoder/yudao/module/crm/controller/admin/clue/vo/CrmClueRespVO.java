package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.infra.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 线索 Response VO")
@Data
@ToString(callSuper = true)
@ExcelIgnoreUnannotated
public class CrmClueRespVO {

    @Schema(description = "编号，主键自增", requiredMode = Schema.RequiredMode.REQUIRED, example = "10969")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "转化状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "转化状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean transformStatus;

    @Schema(description = "跟进状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "跟进状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean followUpStatus;

    @Schema(description = "线索名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "线索xxx")
    @ExcelProperty("线索名称")
    private String name;

    @Schema(description = "客户 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "520")
    // TODO 这里需要导出成客户名称
    @ExcelProperty("客户id")
    private Long customerId;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("下次联系时间")
    private LocalDateTime contactNextTime;

    @Schema(description = "电话", example = "18000000000")
    @ExcelProperty("电话")
    private String telephone;

    @Schema(description = "手机号", example = "18000000000")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "地址", example = "北京市海淀区")
    @ExcelProperty("地址")
    private String address;

    @Schema(description = "负责人编号")
    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("最后跟进时间")
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
