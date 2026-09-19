package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 云盘文件 Response VO")
@Data
public class OaFileNodeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "父目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long parentId;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer type;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "办公用品盘点结果.pdf")
    private String name;

    @Schema(description = "临时文件地址，仅详情中具有下载权限时返回", example = "https://example.com/file.pdf")
    private String url;

    @Schema(description = "扩展名", example = "pdf")
    private String extension;

    @Schema(description = "文件分类", example = "1")
    private Integer category;

    @Schema(description = "大小，单位字节", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long size;

    @Schema(description = "节点状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建人", example = "1024")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime updateTime;

    @Schema(description = "当前用户权限级别", example = "1")
    private Integer level;

    @Schema(description = "是否收藏", example = "false")
    private Boolean favorite;

}
