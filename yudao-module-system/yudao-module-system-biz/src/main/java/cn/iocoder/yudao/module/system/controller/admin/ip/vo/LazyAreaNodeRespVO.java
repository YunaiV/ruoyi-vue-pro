package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 懒加载地区节点 Response VO")
@Data
public class LazyAreaNodeRespVO {

    @Schema(description = "编号", required = true, example = "110000")
    private Integer id;

    @Schema(description = "名字", required = true, example = "北京")
    private String name;

    @Schema(description = "是否叶子节点", required = true, example = "false")
    private Boolean leaf = Boolean.FALSE;
}
