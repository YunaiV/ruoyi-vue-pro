package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("管理后台 - 地区节点 Response VO")
@Data
public class AreaNodeRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "110000")
    private Integer id;

    @ApiModelProperty(value = "名字", required = true, example = "北京")
    private String name;

    /**
     * 子节点
     */
    private List<AreaNodeRespVO> children;

}
