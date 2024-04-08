package cn.iocoder.yudao.module.crm.dal.dataobject.callcenter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 外呼系统配制表
 *
 * @author fhqsuhpv
 */
@TableName("crm_callcenter_config")
@KeySequence("crm_callcenter_config") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCallcenterConfigDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * apiKey由外呼厂商分配
     */
    private String apiKey;

    /**
     * 公司码
     */
    private String companyCode;

    /**
     * 管理员ID（有些厂商把这个当做密钥使用，如云客）
     */
    private String admincode;

    /**
     * secreKey 密钥
     */
    private String secreKey;
    /**
     * 厂商ID
     */
    private Long manufacturerId;
    /**
     * 厂商是否启动
     */
    private Boolean enabled;


}
