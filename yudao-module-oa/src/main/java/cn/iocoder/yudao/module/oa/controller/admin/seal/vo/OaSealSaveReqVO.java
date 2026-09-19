package cn.iocoder.yudao.module.oa.controller.admin.seal.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 印章新增/修改 Request VO")
@Data
public class OaSealSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "所属部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "103")
    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @Schema(description = "印章名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "公司公章")
    @NotBlank(message = "印章名称不能为空")
    @Size(max = 128, message = "印章名称长度不能超过 128 个字符")
    private String name;

    @Schema(description = "印章类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "印章类型不能为空")
    private Integer type;

    @Schema(description = "印章分类", example = "1")
    private Integer category;

    @Schema(description = "保管人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "保管人用户编号不能为空")
    private Long keeperUserId;

    @Schema(description = "保管部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "保管部门编号不能为空")
    private Long keeperDeptId;

    @Schema(description = "印章状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "印章状态不能为空")
    @InEnum(value = OaSealStatusEnum.class, message = "印章状态必须是 {value}")
    private Integer status;

    @Schema(description = "购买时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime purchaseTime;

    @Schema(description = "启用时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime enableTime;

    @Schema(description = "停用时间", example = "1789174800000", type = "integer", format = "int64")
    private LocalDateTime disableTime;

    @Schema(description = "印章照片地址", example = "https://example.com/seal.png")
    @Size(max = 2048, message = "印章照片地址长度不能超过 2048 个字符")
    private String picUrl;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "合同签署完成后归档")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
