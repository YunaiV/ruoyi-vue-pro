package cn.iocoder.yudao.module.oa.controller.admin.seal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 印章 Response VO")
@Data
public class OaSealRespVO {

    @Schema(description = "编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "所属部门编号", example = "103", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;

    @Schema(description = "所属部门", example = "研发部门")
    private String deptName;

    @Schema(description = "印章编号", example = "SEAL-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "印章名称", example = "公司公章", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "印章类型", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "印章分类", example = "1")
    private Integer category;

    @Schema(description = "保管人用户编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long keeperUserId;

    @Schema(description = "保管部门编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long keeperDeptId;

    @Schema(description = "保管人", example = "芋道")
    private String keeperName;

    @Schema(description = "保管部门", example = "研发部门")
    private String keeperDeptName;

    @Schema(description = "印章台账状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "购买时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime purchaseTime;

    @Schema(description = "启用时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime enableTime;

    @Schema(description = "停用时间", example = "1789088400000", type = "integer", format = "int64")
    private LocalDateTime disableTime;

    @Schema(description = "印章照片地址", example = "https://example.com/seal.png")
    private String picUrl;

    @Schema(description = "显示顺序", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "备注", example = "合同签署完成后归档")
    private String remark;

    @Schema(description = "创建时间", example = "1789088400000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime createTime;

}
