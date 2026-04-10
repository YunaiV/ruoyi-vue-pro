package cn.iocoder.yudao.module.system.controller.admin.socail.vo.client;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 社交客户端分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SocialClientPageReqVO extends PageParam {

    @Schema(description = "应用名", example = "yudao商城")
    private String name;

    @Schema(description = "社交平台的类型", example = "31")
    private Integer socialType;

    @Schema(description = "用户类型", example = "2")
    private Integer userType;

    @Schema(description = "客户端编号", example = "145442115")
    private String clientId;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
