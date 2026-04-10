package cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 安灯呼叫记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesProAndonRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "安灯配置编号", example = "1")
    private Long configId;

    @Schema(description = "工作站编号", example = "100")
    private Long workstationId;

    @Schema(description = "工作站编码", example = "WS-001")
    @ExcelProperty("工作站编码")
    private String workstationCode;

    @Schema(description = "工作站名称", example = "工作站 A")
    @ExcelProperty("工作站名称")
    private String workstationName;

    @Schema(description = "发起用户编号", example = "1")
    private Long userId;

    @Schema(description = "发起人昵称", example = "张三")
    @ExcelProperty("发起人")
    private String userNickname;

    @Schema(description = "生产工单编号", example = "200")
    private Long workOrderId;

    @Schema(description = "工单编码", example = "WO-001")
    @ExcelProperty("工单编码")
    private String workOrderCode;

    @Schema(description = "工序编号", example = "300")
    private Long processId;

    @Schema(description = "工序名称", example = "组装工序")
    @ExcelProperty("工序名称")
    private String processName;

    @Schema(description = "呼叫原因", example = "设备故障")
    @ExcelProperty("呼叫原因")
    private String reason;

    @Schema(description = "级别", example = "1")
    @ExcelProperty(value = "级别", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_ANDON_LEVEL)
    private Integer level;

    @Schema(description = "处置状态", example = "0")
    @ExcelProperty(value = "处置状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_ANDON_STATUS)
    private Integer status;

    @Schema(description = "处置时间")
    @ExcelProperty("处置时间")
    private LocalDateTime handleTime;

    @Schema(description = "处置人编号", example = "100")
    private Long handlerUserId;

    @Schema(description = "处置人昵称", example = "李四")
    @ExcelProperty("处置人")
    private String handlerUserNickname;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("发起时间")
    private LocalDateTime createTime;

}
