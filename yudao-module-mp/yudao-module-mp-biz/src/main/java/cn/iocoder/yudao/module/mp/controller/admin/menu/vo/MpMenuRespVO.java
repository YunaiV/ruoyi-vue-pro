package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

// TODO swagger 文档
@ApiModel("管理后台 - 微信菜单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuRespVO extends MpMenuBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    /**
     * 微信公众号 appid
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
