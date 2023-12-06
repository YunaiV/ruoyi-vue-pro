package cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @zyna：可以放到 contact 包下
/**
 * CRM 联系人商机关联 DO
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
     * 联系人编号
     *
     * 关联 {@link CrmContactDO#getId()} 字段
     */
    private Long contactId;
    /**
     * 商机编号
     *
     * 关联 {@link CrmBusinessDO#getId()} 字段
     */
    private Long businessId;

}