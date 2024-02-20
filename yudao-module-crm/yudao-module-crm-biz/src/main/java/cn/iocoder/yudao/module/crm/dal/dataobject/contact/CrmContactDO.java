package cn.iocoder.yudao.module.crm.dal.dataobject.contact;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 联系人 DO
 *
 * @author 芋道源码
 */
@TableName("crm_contact")
@KeySequence("crm_contact_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContactDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 联系人姓名
     */
    private String name;
    /**
     * 客户编号
     *
     * 关联 {@link CrmCustomerDO#getId()}
     */
    private Long customerId;

    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 最后跟进内容
     */
    private String contactLastContent;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;

    /**
     * 负责人用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * QQ
     */
    private Long qq;
    /**
     * 微信
     */
    private String wechat;
    /**
     * 所在地
     *
     * 关联 {@link cn.iocoder.yudao.framework.ip.core.Area#getId()} 字段
     */
    private Integer areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 性别
     *
     * 枚举 {@link cn.iocoder.yudao.module.system.enums.common.SexEnum}
     */
    private Integer sex;
    /**
     * 是否关键决策人
     */
    private Boolean master;
    /**
     * 职位
     */
    private String post;
    /**
     * 直属上级
     *
     * 关联 {@link CrmContactDO#id}
     */
    private Long parentId;
    /**
     * 备注
     */
    private String remark;

}
