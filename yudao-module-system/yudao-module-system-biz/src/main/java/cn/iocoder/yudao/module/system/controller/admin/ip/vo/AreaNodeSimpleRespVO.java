package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 简洁的地区节点 Response VO")
@Data
public class AreaNodeSimpleRespVO {

    @Schema(description = "编号", required = true, example = "110000")
    private Integer id;

    @Schema(description = "名字", required = true, example = "北京")
    private String name;

    @Schema(description = "是否叶子节点", required = false, example = "false")
    private Boolean leaf;

}
