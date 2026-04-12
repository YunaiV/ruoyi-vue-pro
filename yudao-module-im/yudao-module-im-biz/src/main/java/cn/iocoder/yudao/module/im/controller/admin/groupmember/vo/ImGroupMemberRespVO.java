package cn.iocoder.yudao.module.im.controller.admin.groupmember.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @AI：去掉部分导出字段
@Schema(description = "管理后台 - 群成员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ImGroupMemberRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17071")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "群编号", example = "13279")
    @ExcelProperty("群编号")
    private Long groupId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21730")
    @ExcelProperty("用户编号")
    private Long userId;

    @Schema(description = "组内显示名", example = "芋艿")
    @ExcelProperty("组内显示名")
    private String displayUserName;

    @Schema(description = "群显示备注", example = "核心群")
    @ExcelProperty("群显示备注")
    private String displayGroupName;

    @Schema(description = "是否免打扰")
    @ExcelProperty("是否免打扰")
    private Boolean muted;

    @Schema(description = "成员状态", example = "0")
    @ExcelProperty("成员状态")
    private Integer status;

    @Schema(description = "入群时间")
    @ExcelProperty("入群时间")
    private LocalDateTime joinTime;

    @Schema(description = "退群时间")
    @ExcelProperty("退群时间")
    private LocalDateTime quitTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}