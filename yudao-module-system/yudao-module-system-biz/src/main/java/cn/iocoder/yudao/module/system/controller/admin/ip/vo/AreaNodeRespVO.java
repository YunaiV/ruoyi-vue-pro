package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 地区节点 Response VO")
@Data
public class AreaNodeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110000")
    private Integer id;

    @Schema(description = "名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京")
    private String name;

    /**
     * 子节点
     */
    private List<AreaNodeRespVO> children;

}
