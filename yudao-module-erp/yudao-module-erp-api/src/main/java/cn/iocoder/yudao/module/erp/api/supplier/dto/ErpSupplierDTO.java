package cn.iocoder.yudao.module.erp.api.supplier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @className: ErpSupplierDTO
 * @author: Wqh
 * @date: 2024/11/6 10:18
 * @Version: 1.0
 * @description:
 */
@Data
public class ErpSupplierDTO {
    /**
     * 供应商编号
     */
    private Long id;
    /**
     * 供应商名称
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
     * 备注
     */
    private String remark;
    /**
     * 开启状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 纳税人识别号
     */
    private String taxNo;
    /**
     * 税率
     */
    private BigDecimal taxPercent;
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
