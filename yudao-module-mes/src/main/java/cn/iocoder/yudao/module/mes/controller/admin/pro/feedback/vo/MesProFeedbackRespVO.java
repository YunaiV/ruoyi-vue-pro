package cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产报工 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesProFeedbackRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "报工单编号", example = "FB202503160001")
    @ExcelProperty("报工单编号")
    private String code;

    @Schema(description = "报工类型", example = "1")
    @ExcelProperty(value = "报工类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_FEEDBACK_TYPE)
    private Integer type;

    @Schema(description = "报工途径", example = "PC")
    @ExcelProperty("报工途径")
    private String channel;

    @Schema(description = "报工时间")
    @ExcelProperty("报工时间")
    private LocalDateTime feedbackTime;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "工作站编码", example = "WS-001")
    @ExcelProperty("工作站编码")
    private String workstationCode;

    @Schema(description = "工作站名称", example = "注塑工作站")
    @ExcelProperty("工作站名称")
    private String workstationName;

    @Schema(description = "工艺路线编号", example = "1")
    private Long routeId;

    @Schema(description = "工艺路线编码", example = "RT-001")
    private String routeCode;

    @Schema(description = "工序编号", example = "1")
    private Long processId;

    @Schema(description = "工序编码", example = "PR-001")
    @ExcelProperty("工序编码")
    private String processCode;

    @Schema(description = "工序名称", example = "注塑")
    @ExcelProperty("工序名称")
    private String processName;

    @Schema(description = "是否需要检验")
    private Boolean checkFlag;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工单编码", example = "MO202503120008")
    @ExcelProperty("工单编码")
    private String workOrderCode;

    @Schema(description = "工单名称", example = "博世螺丝刀")
    @ExcelProperty("工单名称")
    private String workOrderName;

    @Schema(description = "生产任务编号", example = "1")
    private Long taskId;

    @Schema(description = "任务编码", example = "PT202503150001")
    private String taskCode;

    @Schema(description = "产品物料编号", example = "75")
    private Long itemId;

    @Schema(description = "物料编码", example = "I-075")
    @ExcelProperty("物料编码")
    private String itemCode;

    @Schema(description = "物料名称", example = "博世螺丝刀")
    @ExcelProperty("物料名称")
    private String itemName;

    @Schema(description = "规格型号", example = "一字型")
    @ExcelProperty("规格型号")
    private String itemSpecification;

    @Schema(description = "单位编号", example = "202")
    private Long unitMeasureId;

    @Schema(description = "单位名称", example = "个")
    @ExcelProperty("单位")
    private String unitMeasureName;

    @Schema(description = "过期日期")
    private LocalDateTime expireDate;

    @Schema(description = "生产批号", example = "LOT20260321")
    private String lotNumber;

    @Schema(description = "排产数量", example = "5000.00")
    @ExcelProperty("排产数量")
    private BigDecimal scheduledQuantity;

    @Schema(description = "本次报工数量", example = "500.00")
    @ExcelProperty("报工数量")
    private BigDecimal feedbackQuantity;

    @Schema(description = "合格品数量", example = "490.00")
    @ExcelProperty("合格品数量")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "不良品数量", example = "10.00")
    @ExcelProperty("不良品数量")
    private BigDecimal unqualifiedQuantity;

    @Schema(description = "待检测数量", example = "0")
    @ExcelProperty("待检测数量")
    private BigDecimal uncheckQuantity;

    @Schema(description = "工废数量", example = "6.00")
    private BigDecimal laborScrapQuantity;

    @Schema(description = "料废数量", example = "4.00")
    private BigDecimal materialScrapQuantity;

    @Schema(description = "其他废品数量", example = "0")
    private BigDecimal otherScrapQuantity;

    @Schema(description = "报工用户编号", example = "1")
    private Long feedbackUserId;

    @Schema(description = "报工人昵称", example = "张三")
    @ExcelProperty("报工人")
    private String feedbackUserNickname;

    @Schema(description = "审核用户编号", example = "1")
    private Long approveUserId;

    @Schema(description = "审核人昵称", example = "李四")
    @ExcelProperty("审核人")
    private String approveUserNickname;

    @Schema(description = "状态", example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_FEEDBACK_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
