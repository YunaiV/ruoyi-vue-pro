package cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 联系人商机关联 DO
 *
 * @author 芋道源码
 */
@TableName("crm_contact_business_link")
@KeySequence("crm_contact_business_link_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContactBusinessLinkDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 联系人id
     */
    private Long contactId;
    /**
     * 商机id
     */
    private Long businessId;

}