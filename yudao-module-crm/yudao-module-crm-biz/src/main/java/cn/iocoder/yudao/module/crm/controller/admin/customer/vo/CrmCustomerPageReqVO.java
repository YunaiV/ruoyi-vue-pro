package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - CRM 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPageReqVO extends PageParam {

    @Schema(description = "客户名称", example = "赵六")
    private String name;

    @Schema(description = "手机", example = "18000000000")
    private String mobile;

    @Schema(description = "所属行业", example = "1")
    private Integer industryId;

    @Schema(description = "客户等级", example = "1")
    private Integer level;

    @Schema(description = "客户来源", example = "1")
    private Integer source;

    /**
     * 场景类型，关联 {@link CrmCustomerSceneEnum}
     */
    @Schema(description = "场景类型", example = "1")
    private Integer sceneType;

}
