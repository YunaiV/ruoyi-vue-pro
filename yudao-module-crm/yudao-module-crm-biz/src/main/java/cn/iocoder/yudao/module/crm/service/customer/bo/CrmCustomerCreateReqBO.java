package cn.iocoder.yudao.module.crm.service.customer.bo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.framework.common.validation.Telephone;
import cn.iocoder.yudao.module.crm.enums.CrmDictTypeConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户创建 Create Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmCustomerCreateReqBO {

    /**
     * 客户名称
     */
    @NotEmpty(message = "客户名称不能为空")
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
     * 手机
     */
    @Mobile
    private String mobile;
    /**
     * 电话
     */
    @Telephone
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
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 客户描述
     */
    @Size(max = 4096, message = "客户描述长度不能超过 4096 个字符")
    private String description;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人的用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;
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
     * 存储客户标签的 ID 列表，标签来自于字典，用于标记客户特征 crm_client_tag
     */
    private List<Long> labelCodes;

    /**
     * 国家列表
     * <p>
     * 存储客户所在国家的 ID 列表，国家信息来自于字典 country_code
     */
    private List<Long> countryCodes;
}
