package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

// TODO 芋艿：调整下字段

/**
 * 客户 DO
 *
 * @author Wanwan
 */
@TableName(value = "crm_customer")
@KeySequence("crm_customer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 客户名称
     */
    private String name;
    /**
     * 跟进状态
     */
    private Boolean followUpStatus;
    /**
     * 锁定状态
     */
    private Boolean lockStatus;
    /**
     * 成交状态
     */
    private Boolean dealStatus;
    /**
     * 所属行业
     *
     * 对应字典 {@link DictTypeConstants#CRM_CUSTOMER_INDUSTRY}
     */
    private Integer industryId;
    /**
     * 客户等级
     *
     * 对应字典 {@link DictTypeConstants#CRM_CUSTOMER_LEVEL}
     */
    private Integer level;
    /**
     * 客户来源
     *
     * 对应字典 {@link DictTypeConstants#CRM_CUSTOMER_SOURCE}
     */
    private Integer source;
    /**
     * 手机
     */
    private String mobile;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 网址
     */
    private String website;
    /**
     * QQ
     */
    private String qq;
    /**
     * wechat
     */
    private String wechat;
    /**
     * email
     */
    private String email;
    /**
     * 客户描述
     */
    private String description;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人的用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;
    /**
     * 地区编号
     */
    private Integer areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;

}
