package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * FMS 账套 DO
 *
 * @author 芋道源码
 */
@TableName("fms_account_set")
@KeySequence("fms_account_set_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsAccountSetDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 公司编码
     */
    private String companyCode;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 公司简介
     */
    private String companyProfile;
    /**
     * 所在行业
     */
    private String industry;
    /**
     * 所在地
     */
    private String location;
    /**
     * 法人代表
     */
    private String legalRepresentative;
    /**
     * 身份证号
     */
    private String legalRepresentativeIdNumber;
    /**
     * 营业执照号
     */
    private String businessLicenseNumber;
    /**
     * 组织机构代码
     */
    private String organizationCode;
    /**
     * 备注
     */
    private String remark;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 办公电话
     */
    private String officeTelephone;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 传真号码
     */
    private String faxNumber;
    /**
     * QQ 号码
     */
    private String qqNumber;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 其他
     */
    private String otherContact;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 币种编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO#getId()}
     */
    private Long currencyId;
    /**
     * 启用期间
     */
    private LocalDateTime startTime;
    /**
     * 会计制度
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.config.FmsAccountingStandardEnum}
     */
    private Integer standard;
    /**
     * 是否已初始化
     */
    private Boolean initialized;
}
