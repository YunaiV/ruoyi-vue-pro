package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * CRM 负责人变更记录 Create Req BO
 *
 * @author 芋道源码
 */
@Data
public class CrmOwnerRecordCreateReqBO {

    /**
     * CRM 业务类型
     */
    @NotNull(message = "CRM 业务类型不能为空")
    @InEnum(CrmBizTypeEnum.class)
    private Integer bizType;

    /**
     * CRM 业务编号
     */
    @NotNull(message = "CRM 业务编号不能为空")
    private Long bizId;

    /**
     * 变更前负责人
     */
    private Long preOwnerUserId;

    /**
     * 变更后负责人
     */
    private Long postOwnerUserId;

}
