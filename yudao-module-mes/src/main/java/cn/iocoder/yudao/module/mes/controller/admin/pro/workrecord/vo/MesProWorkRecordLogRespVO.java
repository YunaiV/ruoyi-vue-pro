package cn.iocoder.yudao.module.mes.controller.admin.pro.workrecord.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 上下工记录流水 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class MesProWorkRecordLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    @ExcelProperty("用户昵称")
    private String userNickname;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "工作站编码", example = "WS-001")
    @ExcelProperty("工作站编码")
    private String workstationCode;

    @Schema(description = "工作站名称", example = "注塑工作站")
    @ExcelProperty("工作站名称")
    private String workstationName;

    @Schema(description = "操作类型（1=上工 2=下工）", example = "1")
    @ExcelProperty(value = "操作类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_PRO_WORK_RECORD_TYPE)
    private Integer type;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
