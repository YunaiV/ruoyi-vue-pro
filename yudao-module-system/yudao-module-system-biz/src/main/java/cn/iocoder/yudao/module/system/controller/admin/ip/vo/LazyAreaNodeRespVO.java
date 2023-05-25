package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @jason：1）VO 不添加注释作者哈。2）是不是改成 AreaNodeSimpleRespVO，它其实是返回一个简洁的地区，懒加载只是它的使用场景；
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

    // TODO @jason：1）不设置默认值，交给业务逻辑那写入；2）这个字段必须返回哇？
    @Schema(description = "是否叶子节点", required = true, example = "false")
    private Boolean leaf = Boolean.FALSE;

}
