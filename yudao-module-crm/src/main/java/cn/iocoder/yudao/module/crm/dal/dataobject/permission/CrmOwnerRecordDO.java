package cn.iocoder.yudao.module.crm.dal.dataobject.permission;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 负责人变更记录 DO
 *
 * @author 芋道源码
 */
@TableName("crm_owner_record")
@KeySequence("crm_owner_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOwnerRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * CRM 业务类型
     *
     * 枚举 {@link CrmBizTypeEnum}
     */
    private Integer bizType;
    /**
     * CRM 业务编号
     */
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
