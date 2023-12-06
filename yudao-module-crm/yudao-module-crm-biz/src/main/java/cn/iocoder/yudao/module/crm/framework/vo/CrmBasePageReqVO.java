package cn.iocoder.yudao.module.crm.framework.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - CRM 分页 Base Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CrmBasePageReqVO extends PageParam {

    /**
     * 场景类型，为 null 时则表示全部
     */
    @Schema(description = "场景类型", example = "1")
    @InEnum(CrmSceneEnum.class)
    private Integer sceneType;

    @Schema(description = "是否为公海数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean pool; // null 则表示为不是公海数据

}
