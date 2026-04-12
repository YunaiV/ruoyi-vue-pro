package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.excel.annotation.*;

import java.time.LocalDateTime;

// TODO @AI：不需要导出的注解；
@Schema(description = "管理后台 - 群 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ImGroupRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "群名称", example = "芋艿")
    @ExcelProperty("群名称")
    private String name;

    @Schema(description = "群主用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31460")
    @ExcelProperty("群主用户编号")
    private Long ownerUserId;

    @Schema(description = "群头像")
    @ExcelProperty("群头像")
    private String avatar;

    @Schema(description = "群公告")
    @ExcelProperty("群公告")
    private String notice;

    @Schema(description = "是否封禁")
    @ExcelProperty("是否封禁")
    private Boolean banned;

    @Schema(description = "是否解散")
    @ExcelProperty("是否解散")
    private Boolean dissolved;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}