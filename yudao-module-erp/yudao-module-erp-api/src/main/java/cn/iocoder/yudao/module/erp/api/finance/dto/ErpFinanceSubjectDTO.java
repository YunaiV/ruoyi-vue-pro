package cn.iocoder.yudao.module.erp.api.finance.dto;

import lombok.Data;

@Data
public class ErpFinanceSubjectDTO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 乐观锁
     */
    private Integer revision;
    /**
     * 主体名称
     */
    private String name;
    /**
     * 联系人
     */
    private String contact;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 传真
     */
    private String fax;
    /**
     * 送达地址
     */
    private String deliveryAddress;
    /**
     * 公司地址
     */
    private String companyAddress;
    /**
     * 备注
     */
    private String remark;
    /**
     * 开启状态 (0-关闭, 1-开启)
     * <p>
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum common_boolean_status 对应的类}
     */
    private Boolean status;
    /**
     * 纳税人识别号
     */
    private String taxNo;
    /**
     * 开户行
     */
    private String bankName;
    /**
     * 开户账号
     */
    private String bankAccount;
    /**
     * 开户地址
     */
    private String bankAddress;

}
