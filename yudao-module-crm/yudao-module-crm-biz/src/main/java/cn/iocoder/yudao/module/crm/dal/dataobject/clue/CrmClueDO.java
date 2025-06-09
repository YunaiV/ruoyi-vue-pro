package cn.iocoder.yudao.module.crm.dal.dataobject.clue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.CrmDictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CRM 线索 DO
 *
 * @author Wanwan
 */
@TableName("crm_clue")
@KeySequence("crm_clue_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClueDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 线索名称
     */
    private String name;

    /**
     * 跟进状态
     */
    private Boolean followUpStatus;
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
     * 负责人的用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 转化状态
     * <p>
     * true 表示已转换，会更新 {@link #customerId} 字段
     */
    private Boolean transformStatus;
    /**
     * 客户编号
     * <p>
     * 关联 {@link CrmCustomerDO#getId()}
     */
    private Long customerId;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 电话
     */
    private String telephone;
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
     * 所在地
     * <p>
     * 关联 {@link cn.iocoder.yudao.framework.ip.core.Area#getId()} 字段
     */
    private Integer areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 所属行业
     * <p>
     * 对应字典 {@link CrmDictTypeConstants#CRM_CUSTOMER_INDUSTRY}
     */
    private Integer industryId;
    /**
     * 客户等级
     * <p>
     * 对应字典 {@link CrmDictTypeConstants#CRM_CUSTOMER_LEVEL}
     */
    private Integer level;
    /**
     * 客户来源
     * <p>
     * 对应字典 {@link CrmDictTypeConstants#CRM_CUSTOMER_SOURCE}
     */
    private Integer source;
    /**
     * 备注
     */
    private String remark;

    /**
     * 公司介绍
     * <p>
     * 存储公司简介，通常用于展示公司背景信息
     */
    private String companyIntroduction;

    /**
     * 官网
     * <p>
     * 存储公司官方网站的 URL 地址
     */
    private String companyWebsite;

    /**
     * 客户标签列表
     * <p>
     * 对应字典 {@link CrmDictTypeConstants#CRM_CLIENT_TAG}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> labelCodes;

    /**
     * 国家列表
     * <p>
     * 对应字典
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> countryCodes;
}
