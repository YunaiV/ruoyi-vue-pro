package cn.iocoder.yudao.module.crm.controller.admin.backlog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 今日需联系客户 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmTodayCustomerPageReqVO extends PageParam {

    /**
     * 联系状态 - 今日需联系
     */
    public static final int CONTACT_TODAY = 1;
    /**
     * 联系状态 - 已逾期
     */
    public static final int CONTACT_EXPIRED = 2;
    /**
     * 联系状态 - 已联系
     */
    public static final int CONTACT_ALREADY = 3;

    @Schema(description = "联系状态", example = "1")
    private Integer contactStatus;

    @Schema(description = "场景类型", example = "1")
    @InEnum(CrmSceneTypeEnum.class)
    private Integer sceneType;

}