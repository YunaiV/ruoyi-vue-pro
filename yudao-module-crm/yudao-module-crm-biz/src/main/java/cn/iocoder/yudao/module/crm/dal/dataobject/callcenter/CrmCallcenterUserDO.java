package cn.iocoder.yudao.module.crm.dal.dataobject.callcenter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CRM 外呼系统与用户绑定关系
 *
 * @author fhqsuhpv
 */
@TableName("crm_callcenter_user")
@KeySequence("crm_callcenter_user") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCallcenterUserDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 云客外呼系统的用户ID
     */
    private String yunkeCallcenterUserId;
    /**
     * 云客外呼系统的电话号码
     */
    private String yunkeCallcenterPhone;

    /**
     * 连连外呼系统的用户ID
     */
    private String lianlianCallcenterUserId;
    /**
     * 连连外呼系统的用户电话号码
     */
    private String lianlianCallcenterPhone;

}
